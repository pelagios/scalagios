package org.scalagios.graph.neo4j

import java.io.File
import java.io.FileInputStream
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
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.Neo4jBatchGraph

import org.scalagios.rdf.parser.AnnotationCollector
import org.scalagios.rdf.parser.PlaceCollector

@RunWith(classOf[JUnitRunner])
class GraphImportTest extends FunSuite with BeforeAndAfterAll {

  private val NEO4J_DIR = "neo4j-test"
  private val SAMPLE_ANNOTATIONS = "src/test/resources/gap-triples-sample.n3" 
  private val ANNOTATION_BASEURI = "http://googleancientplaces.wordpress.com/"
  
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
  
  override def beforeAll(configMap: Map[String, Any]) = deleteNeo4j
  override def afterAll(configMap: Map[String, Any]) = deleteNeo4j
    
  test("Batch Place Import with Neo4j") {
    println("Importing Pleiades RDF dump from the Web")
    val startTime = System.currentTimeMillis   

    // Get GZIPped Turtle stream directly from Pleiades site
    val connection = 
      new URL("http://atlantides.org/downloads/pleiades/rdf/places-latest.ttl.gz")
      .openConnection()
      
    val inputStream = new GZIPInputStream(connection.getInputStream())

    // Download and parse 
    val parser = new TurtleParserFactory().getParser()
    val placeCollector = new PlaceCollector
    parser.setRDFHandler(placeCollector);
    parser.parse(inputStream, "http://pleiades.stoa.org")
    println("File download complete. Took " + (System.currentTimeMillis() - startTime)/1000 + " seconds")
    println("Importing to graph")
    
    // Add to graph
    val graph = new Neo4jBatchGraph(NEO4J_DIR)
    val writer = new PelagiosNeo4jBatchWriter(graph)
    writer.insertPlaces(placeCollector.getPlaces)
    graph.shutdown();
    
    println("Imported " + placeCollector.getPlaces.size + " places. Took " + (System.currentTimeMillis - startTime)/1000 + " seconds")
  }
  
  test("Batch Annotation Import with Neo4j") {
    println("Importing OAC Annotations to Neo4j at " + NEO4J_DIR)
    val startTime = System.currentTimeMillis
    
    val parser = new N3ParserFactory().getParser()
    val annotationCollector = new AnnotationCollector()
    parser.setRDFHandler(annotationCollector)
    parser.parse(new FileInputStream(new File(SAMPLE_ANNOTATIONS)), ANNOTATION_BASEURI)
    
    val graph = new Neo4jBatchGraph(NEO4J_DIR)
    val writer = new PelagiosNeo4jBatchWriter(graph) 
    writer.insertAnnotations(annotationCollector.getAnnotations)
    graph.shutdown();
    
    println("Imported " + annotationCollector.getAnnotations.size + " annotations. Took " + (System.currentTimeMillis - startTime) + " milliseconds")
  }
  
  test("Dropping Datasets from Neo4j") {
    // TODO
  }
  
  test("Verify Graph Setup") {
    // TODO
  }
  
}