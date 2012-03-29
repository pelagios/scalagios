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
class GraphIOTest extends FunSuite with BeforeAndAfterAll {
  
  private val NEO4J_DIR = "neo4j-test"
  
  test("Place Search") {
    val startTime = System.currentTimeMillis   
    val graph = new Neo4jGraph(NEO4J_DIR)
    val reader = new PelagiosNeo4jReader(graph)
    
    reader.findPlaceByName("athen").foreach(place => println(place.label))
    
    graph.shutdown()
  }

}