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

import org.scalagios.rdf.parser.{PlaceCollector, AnnotationCollector}

@RunWith(classOf[JUnitRunner])
class GraphImportTest extends FunSuite with BeforeAndAfterAll {

  private val NEO4J_DIR = "neo4j-test"
  private val PLEIADES_DUMP = "src/test/resources/places-20120212.ttl.gz"
  private val SAMPLE_ANNOTATIONS = "src/test/resources/gap-triples-sample.n3" 
  private val ANNOTATION_BASEURI = "http://gap.alexandriaarchive.org/bookdata/GAPtriples"
  
  override def beforeAll(configMap: Map[String, Any]) = deleteNeo4j
  override def afterAll(configMap: Map[String, Any]) = deleteNeo4j
  
  test("Transactional Place import with Neo4j") {
    println("Importing Pleiades RDF dump")
    importPlaces(new Neo4jGraph(NEO4J_DIR))
  }
  
  test("Drop all Places from Neo4j") {
    val startTime = System.currentTimeMillis()
    val graph = new Neo4jGraph(NEO4J_DIR)
    val writer = new PelagiosNeo4jWriter(graph)
    val placesDropped = writer.dropPlaces()
    graph.shutdown()
    println("Dropped " + placesDropped + " Places from graph. Took " + (System.currentTimeMillis() - startTime) + " milliseconds")    
  }
  
  test("Batch Place import with Neo4j") {
    println("Batch-importing Pleiades RDF dump")
    importPlaces(new Neo4jBatchGraph(NEO4J_DIR))        
  }

  test("Transactional Annotation import with Neo4j") {
    println("Importing OAC Annotations")
    importAnnotations(new Neo4jGraph(NEO4J_DIR))
  }
  
  test("Drop Annotations from Neo4j") {
    val graph = new Neo4jGraph(NEO4J_DIR)
    val writer = new PelagiosNeo4jWriter(graph)
    val annotationsDropped = writer.dropDataset(ANNOTATION_BASEURI)
    graph.shutdown()
    println("Dropped " + annotationsDropped + " from graph")
  }
  
  test("Batch Annotation import with Neo4j") {
    println("Batch-importing OAC Annotations")
    importAnnotations(new Neo4jBatchGraph(NEO4J_DIR))    
  }
    
  test("Verify graph structure") {
    // TODO Implement test to verify graph structure
  }
  
  def importPlaces(graph: IndexableGraph) = {
    val startTime = System.currentTimeMillis   
      
    val inputStream = new GZIPInputStream(new FileInputStream(PLEIADES_DUMP))
    val parser = new TurtleParserFactory().getParser()
    val placeCollector = new PlaceCollector
    parser.setRDFHandler(placeCollector);
    parser.parse(inputStream, "http://pleiades.stoa.org")
    println("Pleiades RDF parsed. Took " + (System.currentTimeMillis() - startTime)/1000 + " seconds")
    println("Importing to graph")
    
    // Add to graph
    val writer = new PelagiosGraphWriter(graph)
    writer.insertPlaces(placeCollector.getPlaces)
    graph.shutdown();    
    
    println("Imported " + placeCollector.getPlaces.size + " places. Took " + (System.currentTimeMillis - startTime)/1000 + " seconds")
  }
  
  def importAnnotations(graph: IndexableGraph) = {
    val startTime = System.currentTimeMillis
    
    val parser = new N3ParserFactory().getParser()
    val annotationCollector = new AnnotationCollector()
    parser.setRDFHandler(annotationCollector)
    parser.parse(new FileInputStream(new File(SAMPLE_ANNOTATIONS)), ANNOTATION_BASEURI)
    
    val writer = new PelagiosGraphWriter(graph) 
    writer.insertAnnotations(annotationCollector.getAnnotations)
    graph.shutdown();
    
    println("Imported " + annotationCollector.getAnnotations.size + " annotations. Took " + (System.currentTimeMillis - startTime) + " milliseconds")    
  }

  private def deleteNeo4j = {
    val neo4j = new File(NEO4J_DIR)
    if (neo4j.exists()) {
      try {
        println("Removing Neo4j test DB")
        FileUtils.deleteRecursively(new File(NEO4J_DIR))
        println("Done.")
      } catch {
        case t: Throwable => { println("WARNING: Could not delete Neo4j test DB") }
      }
    }
  }
  
}