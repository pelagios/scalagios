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

@RunWith(classOf[JUnitRunner])
class GraphImportTest extends FunSuite with BeforeAndAfterAll {

  private val NEO4J_DIR = "neo4j-test"
  private val PLEIADES_DUMP = "src/test/resources/places-20120401.ttl.gz"
   
  private val SAMPLE_VOID = "src/test/resources/gap-void-sample.ttl"
  private val SAMPLE_ANNOTATIONS = "src/test/resources/gap-triples-sample.n3" 
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
    print("  Importing Places to graph. ")
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
    val datasetCollector = new DatasetCollector(ANNOTATION_BASEURI)
    ttlParser.setRDFHandler(datasetCollector)
    ttlParser.parse(new FileInputStream(new File(SAMPLE_VOID)), ANNOTATION_BASEURI)
    assert(datasetCollector.datasetsTotal == 410)
    println("Took " + (System.currentTimeMillis() - startTime) + " milliseconds.")
    
    // Parse annotation RDF
    print("  Parsing GeoAnnotation dump. ")
    startTime = System.currentTimeMillis()
    val n3Parser = new N3ParserFactory().getParser()
    val annotationCollector = new AnnotationCollector()
    n3Parser.setRDFHandler(annotationCollector)
    n3Parser.parse(new FileInputStream(new File(SAMPLE_ANNOTATIONS)), ANNOTATION_BASEURI)
    assert(annotationCollector.annotationsTotal == 1849)
    println("Took " + (System.currentTimeMillis() - startTime) + " milliseconds.")
    
    // Import data to Graph
    print("  Importing GeoAnnotations to Graph. ")
    val writer = new PelagiosGraphWriter(graph)
    datasetCollector.getRootDatasets.foreach(writer.insertDataset(_))
    writer.insertAnnotations(annotationCollector.getAnnotations, ANNOTATION_BASEURI)
    graph.shutdown()
    println("Took " + (System.currentTimeMillis - startTime) + " milliseconds.")    
    println("  " + annotationCollector.getAnnotations.size + " GeoAnnnotations imported to Graph.")    
  }
    
  test("Verify graph structure") {
    println("Verifying Graph Structure")
    
    var startTime = System.currentTimeMillis
    val graph = new Neo4jGraph(NEO4J_DIR)
    val reader = new PelagiosGraphReader(graph)
    
    val places = reader.getPlaces()
    assert(places.size == 36129)
    
    // TODO finish graph verification
    /*
    places.foreach(p => println(p.uri))
    
    val datasets = reader.listAllDatasets().size
    println("  " + datasets + " Datasets")
    assert(datasets == 410)
    */
    
    // TODO dataset hierarchy
    
    // TODO check a few sample places and datasets via URI
    
    // TODO check annotations inside datasets
    
    graph.shutdown()
  }

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