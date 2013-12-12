package org.pelagios.legacy.graph

import scala.collection.JavaConverters._
import org.neo4j.graphdb.{Path => Neo4jPath}
import com.tinkerpop.blueprints.pgm.impls.neo4j.{Neo4jGraph, Neo4jVertex}
import org.pelagios.legacy.graph.Constants._
import org.pelagios.legacy.graph.exception.GraphIntegrityException

class Path(neo4jPath: Neo4jPath, graph: Neo4jGraph) {
   
  lazy val startPlace = new PlaceVertex(new Neo4jVertex(neo4jPath.startNode, graph))
  
  lazy val endPlace = new PlaceVertex(new Neo4jVertex(neo4jPath.endNode, graph))
  
  lazy val length = neo4jPath.length
  
  lazy val via = {
    val nodes = neo4jPath.nodes.asScala
    nodes.take(nodes.size - 1).drop(1).map(node => {
      new Neo4jVertex(node, graph) match {
        case v: Neo4jVertex if v.getProperty(VERTEX_TYPE).equals(PLACE_VERTEX) => new PlaceVertex(v)
        case v: Neo4jVertex if v.getProperty(VERTEX_TYPE).equals(DATASET_VERTEX) => new DatasetVertex(v)
        // Should never happen
        case v: Neo4jVertex => throw GraphIntegrityException(Path.MSG_INTEGRITY_EXCEPTION + v.getProperty(VERTEX_TYPE))
      }
    })
  }
  
}

object Path {

  val MSG_INTEGRITY_EXCEPTION = "Something else than place or dataset found during path search: "
  
}