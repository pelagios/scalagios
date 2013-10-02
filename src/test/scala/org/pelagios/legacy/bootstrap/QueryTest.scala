package org.pelagios.legacy.bootstrap

import com.tinkerpop.blueprints.pgm.impls.neo4j._
import org.pelagios.legacy.graph.io._

object QueryTest {
  
  def main(args: Array[String]): Unit = {
    val neo4j = new Neo4jGraph("neo4j")
    val reader = new PelagiosNeo4jReader(neo4j)
    
    /*
    reader.queryPlaces("attica").foreach(place => {
      println(place.label.get)
    })
    *
    */
    
    neo4j.shutdown
  }
  
}
