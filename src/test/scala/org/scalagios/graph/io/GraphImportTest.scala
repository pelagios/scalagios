package org.scalagios.graph.io

import java.io.{File, FileInputStream}
import java.net.URL
import java.util.zip.GZIPInputStream
import scala.collection.JavaConverters._
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfterAll
import org.junit.runner.RunWith
import org.openrdf.rio.n3.N3ParserFactory
import org.openrdf.rio.turtle.TurtleParserFactory
import org.neo4j.kernel.impl.util.FileUtils
import info.aduna.io.FileUtil
import com.tinkerpop.blueprints.pgm.IndexableGraph
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.Neo4jBatchGraph
import org.scalagios.rdf.parser._
import org.scalagios.graph.Constants._
import org.scalagios.graph.DatasetVertex
import com.tinkerpop.blueprints.pgm.Graph

@RunWith(classOf[JUnitRunner])
class GraphImportTest extends FunSuite with BeforeAndAfterAll {

  private val NEO4J_DIR = "neo4j-test"
  private val PLEIADES_DUMP = "src/test/resources/places-20120401.ttl.gz"
   
  private val VOID = "src/test/resources/gap-void-sample.ttl"
  private val ANNOTATIONS_DUMP = "src/test/resources/gap-triples-sample.n3.gz" 
  private val ANNOTATION_BASEURI = "http://gap.alexandriaarchive.org/bookdata/GAPtriples/"
  
  override def beforeAll(configMap: Map[String, Any]) = deleteNeo4j
  override def afterAll(configMap: Map[String, Any]) = deleteNeo4j
  
  test("Place import with Neo4j") {
    println("Importing Pleiades Gazetteer")
    
    val startTime = System.currentTimeMillis   
    val graph = new Neo4jGraph(NEO4J_DIR)
    
    // Parse RDF
    print("  Parsing RDF dump. ")
    val inputStream = new GZIPInputStream(new FileInputStream(PLEIADES_DUMP))
    val parser = new TurtleParserFactory().getParser()
    val placeCollector = new PlaceCollector
    parser.setRDFHandler(placeCollector);
    parser.parse(inputStream, "http://pleiades.stoa.org")
    assert(placeCollector.placesTotal == 36129)
    println("Took " + (System.currentTimeMillis() - startTime) + " milliseconds.")
    
    // Import data to Graph
    print("  Importing Places to Graph. ")
    val writer = new PelagiosGraphWriter(graph)
    writer.insertPlaces(placeCollector.getPlaces)
    graph.shutdown()
    println("Took " + (System.currentTimeMillis - startTime)/1000 + " seconds.")
    println("  " + placeCollector.placesTotal + " Places imported to Graph.")
  }
 
  test("Annotation import with Neo4j") {
    println("Importing GeoAnnotations")
    
    var startTime = System.currentTimeMillis
    val graph = new Neo4jGraph(NEO4J_DIR)
    
    // Parse VoID RDF
    print("  Parsing VoID. ")
    val ttlParser = new TurtleParserFactory().getParser()
    val datasetCollector = new DatasetCollector()
    ttlParser.setRDFHandler(datasetCollector)
    ttlParser.parse(new FileInputStream(new File(VOID)), ANNOTATION_BASEURI)
    assert(datasetCollector.datasetsTotal == 806)
    println("Took " + (System.currentTimeMillis() - startTime) + " milliseconds.")
    
    // Parse annotation RDF
    print("  Parsing GeoAnnotation dump. ")
    startTime = System.currentTimeMillis()
    val n3Parser = new N3ParserFactory().getParser()
    val annotationCollector = new AnnotationCollector()
    n3Parser.setRDFHandler(annotationCollector)
    n3Parser.parse(new GZIPInputStream(new FileInputStream(new File(ANNOTATIONS_DUMP))), ANNOTATION_BASEURI)
    assert(annotationCollector.annotationsTotal == 2116)
    println("Took " + (System.currentTimeMillis() - startTime) + " milliseconds.")
    
    // Import data to Graph
    print("  Importing GeoAnnotations to Graph. ")
    val writer = new PelagiosGraphWriter(graph)
    datasetCollector.getRootDatasets.foreach(writer.insertDataset(_))
    writer.insertAnnotations(annotationCollector.getAnnotations, datasetCollector.getRootDatasets.head.uri)
    graph.shutdown()
    println("Took " + (System.currentTimeMillis - startTime) + " milliseconds.")    
    println("  " + annotationCollector.getAnnotations.size + " GeoAnnnotations imported to Graph.")    
  }
    
  test("Verify Graph integrity") {
    println("Verifying Graph integrity")
    val graph = new Neo4jGraph(NEO4J_DIR)
    val reader = new PelagiosNeo4jReader(graph)
    val writer = new PelagiosGraphWriter(graph)
    
    print("  Counting Places. ")
    val places = reader.getPlaces()
    assert(places.size == 36129)
    println(places.size + ". OK")

    print("  Counting Datasets. ")
    val topLevelDatasets = reader.getDatasets()
    assert(topLevelDatasets.size == 1)
    topLevelDatasets.foreach(dataset => {
      assert(dataset.isValid)
      
      // For top-level sets, root URI must equal dataset URI
      assert(dataset.rootUri.equals(dataset.uri))
    })
    
    val datasetsTotal = getVertices(graph, DATASET_VERTEX).map(new DatasetVertex(_))
    assert(datasetsTotal.size == 806)
     
    var ctRoot = 0
    datasetsTotal.foreach(dataset => {
      assert(dataset.isValid)
      if (dataset.rootUri.equals(dataset.uri))
        ctRoot += 1
    })
    assert(ctRoot == topLevelDatasets.size)
    println(topLevelDatasets.size + " at top level, " + datasetsTotal.size + " total. OK")

    val sampleDataset = datasetsTotal.drop(3).head
    val hierarchy = reader.getDatasetHierarchy(sampleDataset)
    println("  Hierarchy example: " + sampleDataset.title + " > " + 
        hierarchy.map(_.title).mkString(" > "))
    
    val root = hierarchy.reverse.head
    for (i <- 0 to (hierarchy.size - 2)) {
      val child = hierarchy.drop(i).head
      val parent = hierarchy.drop(i + 1).head
      println("parent: " + parent)
      assert(child.isChildOf(parent.uri))
      assert(child.isChildOf(root.uri))
    }
        
    println(topLevelDatasets.head.countAnnotations(true))
    assert(topLevelDatasets.head.countAnnotations(true) == 2116)    
    graph.shutdown()
  }
  
  test("Test index queries") {
    println("  Testing index queries.")
    val graph = new Neo4jGraph(NEO4J_DIR)
    val reader = new PelagiosNeo4jReader(graph)
    
    reader.queryPlaces("leptis magna").foreach(place => println("    " + place.label.get))
    reader.queryDatasets("herodot").foreach(dataset => println("    " + dataset.title))
        
    val viennaRegion = reader.queryPlaces(16.3, 48.2, 16.4, 48.3)
    assert(viennaRegion.size > 0)
    assert(viennaRegion.mapConserve(_.label).contains(Some("Vindobona")))
    
    graph.shutdown()
  }
  
  test("Test network queries") {
    println("  Testing network queries.")
    val graph = new Neo4jGraph(NEO4J_DIR)
    val reader = new PelagiosNeo4jReader(graph)
    
    // Vindobona
    println("    Network neighbourhood for Vindobona:")
    reader.networkNeighbourHood("http://pleiades.stoa.org/places/128537").foreach { case (place, weight) =>
      println("      " + place.label.getOrElse("untitled") + " (" + weight + ")")
    }
    
    // Athens
    println("    Network neighbourhood for Athenae:")
    reader.networkNeighbourHood("http://pleiades.stoa.org/places/579885").foreach { case (place, weight) =>
      println("      " + place.label.getOrElse("untitled") + " (" + weight + ")")
    }    
    
    graph.shutdown()
  }
  
  test("Verify delete functionality") {
    println("Verifying delete functionality")
    val graph = new Neo4jGraph(NEO4J_DIR)
    val reader = new PelagiosGraphReader(graph)
    val writer = new PelagiosGraphWriter(graph)

    print("  Deleting datasets. ")
    reader.getDatasets.foreach(dataset => writer.dropDataset(dataset.uri))
    assert(reader.getDatasets().size == 0)
    assert(getVertices(graph, DATASET_VERTEX).size == 0)
    println("OK")

    graph.shutdown()
  }
  
  private def getVertices(graph: Graph, vertexType: String) = 
    graph.getVertices().asScala.filter(_.getProperty(VERTEX_TYPE).equals(vertexType))

  private def deleteNeo4j = {
    val neo4j = new File(NEO4J_DIR)
    if (neo4j.exists()) {
      try {
        println("Cleanup")
        print("  Removing Neo4j test DB. ")
        FileUtils.deleteRecursively(new File(NEO4J_DIR))
        println("Done.")
      } catch {
        case t: Throwable => { println("WARNING: Could not delete Neo4j test DB") }
      }
    }
  }
  
}