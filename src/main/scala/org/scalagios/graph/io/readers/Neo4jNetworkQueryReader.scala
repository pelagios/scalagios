package org.scalagios.graph.io.readers

import scala.collection.JavaConverters._
import org.scalagios.api.Place
import org.scalagios.graph.{Path, PlaceVertex}
import org.scalagios.graph.Constants._
import org.scalagios.graph.io.PelagiosGraphIOBase
import org.scalagios.graph.exception.GraphIOException
import org.neo4j.kernel.Traversal
import org.neo4j.graphalgo.GraphAlgoFactory
import org.neo4j.graphdb.{Direction, DynamicRelationshipType, Node}
import org.neo4j.graphdb.{Path => Neo4jPath}
import com.tinkerpop.blueprints.pgm.impls.neo4j.{Neo4jGraph, Neo4jVertex}
import org.scalagios.graph.PlaceVertex
import com.tinkerpop.blueprints.pgm.Vertex

trait Neo4jNetworkQueryReader extends GraphPlaceReader with GraphDatasetReader {

  val MSG_PLACE_NOT_FOUND = "Place not found: "
    
  val pathExpander = Traversal.expanderForTypes(
      DynamicRelationshipType.withName(RELATION_WITHIN), Direction.INCOMING,
      DynamicRelationshipType.withName(RELATION_WITHIN), Direction.OUTGOING,
      
      DynamicRelationshipType.withName(RELATION_REFERENCES), Direction.INCOMING,
      DynamicRelationshipType.withName(RELATION_REFERENCES), Direction.OUTGOING,
      
      DynamicRelationshipType.withName(RELATION_SUBSET), Direction.INCOMING,
      DynamicRelationshipType.withName(RELATION_SUBSET), Direction.OUTGOING)
  
  private def _getPlaceNodeOrThrowException(uri: String): Neo4jVertex = {
    val idxHits = placeIndex.get(PLACE_URI, uri)
    if (!idxHits.hasNext)
      throw new GraphIOException(MSG_PLACE_NOT_FOUND + uri)
    
    idxHits.next.asInstanceOf[Neo4jVertex]
  }
    
  def findShortestPaths(fromUri: String, toUri: String): Iterable[Path] = {
    val from = _getPlaceNodeOrThrowException(fromUri).getRawVertex
    val to = _getPlaceNodeOrThrowException(toUri).getRawVertex
 
    val finder = GraphAlgoFactory.shortestPath(pathExpander, 5);
    finder.findAllPaths(from, to).asScala.map(new Path(_, graph.asInstanceOf[Neo4jGraph]))
  }
  
  def networkNeighbourHood(placeUri: String): List[(Place, Double)] = {
    // Compile a flat list of all place-dataset-place hops in the graph
    val neighbourhood = this.getReferencingDatasets(placeUri)
      .map { case (dataset, sourceReferenceCount) =>
        val referencesTotal = dataset.countAnnotations(true)
        val sourceRefRatio = sourceReferenceCount.toDouble / referencesTotal
      
        // For ranking, we compute a weight for each path
        this.getReferencedPlaces(dataset).map { case (place, destinationReferenceCount) =>
          val destRefRatio = destinationReferenceCount.toDouble / referencesTotal
          (place, sourceRefRatio * destRefRatio)
        }
      }.flatten.toSeq
    
    // Group by place...
    val grouped = neighbourhood.groupBy{ case (place, weight) => place }
    
    // ...sum up the weights for all paths to one place
    val aggregated = grouped.map { case (place, paths) =>
      (place, paths.map(_._2).foldLeft(0.0)(_ + _))
    } toList
    
    aggregated.filter(_._1.uri != placeUri).sortBy { case (place, weight) => -weight }
  }
  
}