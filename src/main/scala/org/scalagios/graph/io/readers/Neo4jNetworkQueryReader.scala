package org.scalagios.graph.io.readers

import scala.collection.JavaConverters._
import org.scalagios.graph.PlaceVertex
import org.scalagios.graph.Constants._
import org.scalagios.graph.io.PelagiosGraphIOBase
import org.scalagios.graph.exception.GraphIOException
import org.neo4j.kernel.Traversal
import org.neo4j.graphalgo.GraphAlgoFactory
import org.neo4j.graphdb.{Direction, DynamicRelationshipType, Node, Path}
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jVertex

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
    finder.findAllPaths(from, to).asScala
  }
  
}