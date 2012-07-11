package org.scalagios.graph.io.readers

import scala.collection.JavaConverters._
import org.scalagios.graph.PlaceVertex
import org.scalagios.graph.Constants._
import org.scalagios.graph.io.PelagiosGraphIOBase
import org.scalagios.graph.exception.GraphIOException
import org.neo4j.kernel.Traversal
import org.neo4j.graphalgo.GraphAlgoFactory
import org.neo4j.graphdb.{Direction, DynamicRelationshipType, Node}
import org.neo4j.graphdb.{Path => Neo4jPath}
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jVertex
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import org.scalagios.graph.PlaceVertex
import org.scalagios.graph.DatasetVertex
import org.scalagios.graph.exception.GraphIntegrityException

trait Neo4jNetworkQueryReader extends PelagiosGraphIOBase {

  val MSG_PLACE_NOT_FOUND = "Place not found: "
    
  val pathExpander = Traversal.expanderForTypes(
      DynamicRelationshipType.withName(RELATION_WITHIN), Direction.INCOMING,
      DynamicRelationshipType.withName(RELATION_WITHIN), Direction.OUTGOING,
      
      DynamicRelationshipType.withName(RELATION_REFERENCES), Direction.INCOMING,
      DynamicRelationshipType.withName(RELATION_REFERENCES), Direction.OUTGOING,
      
      DynamicRelationshipType.withName(RELATION_SUBSET), Direction.INCOMING,
      DynamicRelationshipType.withName(RELATION_SUBSET), Direction.OUTGOING)
  
  private def _getPlaceNodeOrThrowException(uri: String): Node = {
    val idxHits = placeIndex.get(PLACE_URI, uri)
    if (!idxHits.hasNext)
      throw new GraphIOException(MSG_PLACE_NOT_FOUND + uri)
    
    idxHits.next.asInstanceOf[Neo4jVertex].getRawVertex
  }
    
  def findShortestPaths(fromUri: String, toUri: String): Iterable[Path] = {
    val from = _getPlaceNodeOrThrowException(fromUri)
    val to = _getPlaceNodeOrThrowException(toUri)
 
    val finder = GraphAlgoFactory.shortestPath(pathExpander, 5);
    finder.findAllPaths(from, to).asScala.map(new Path(_, graph.asInstanceOf[Neo4jGraph]))
  }
  
}

class Path(neo4jPath: Neo4jPath, graph: Neo4jGraph) {
  
  val MSG_INTEGRITY_EXCEPTION = "Something else than place or dataset found during path search: "
  
  lazy val startPlace = new PlaceVertex(new Neo4jVertex(neo4jPath.startNode, graph))
  
  lazy val endPlace = new PlaceVertex(new Neo4jVertex(neo4jPath.endNode, graph))
  
  lazy val length = neo4jPath.length
  
  lazy val nodes = {
    neo4jPath.nodes.asScala.map(node => {
      new Neo4jVertex(node, graph) match {
        case v: Neo4jVertex if v.getProperty(VERTEX_TYPE).equals(PLACE_VERTEX) => new PlaceVertex(v)
        case v: Neo4jVertex if v.getProperty(VERTEX_TYPE).equals(DATASET_VERTEX) => new DatasetVertex(v)
        // Should never happen
        case v: Neo4jVertex => throw GraphIntegrityException(MSG_INTEGRITY_EXCEPTION + v.getProperty(VERTEX_TYPE))
      }
    })
  }
  
}