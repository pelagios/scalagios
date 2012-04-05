package org.scalagios.graph.io

import scala.collection.JavaConverters._
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import org.scalagios.api.Place
import org.scalagios.graph.Constants._
import org.scalagios.graph.PlaceVertex
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jVertex

class PelagiosNeo4jReader(graph: Neo4jGraph) extends PelagiosGraphReader(graph) {
  
  def findPlaceByName(query: String): List[Place] = {
    val neo4j = graph.getRawGraph()
    val index = neo4j.index().forNodes(INDEX_FOR_PLACES)
    
    val q = "*" + query + "*"
    val hits =
      index.query(PLACE_URI, q).iterator.asScala ++
      index.query(PLACE_LABEL, q).iterator.asScala ++ 
      index.query(PLACE_COMMENT, q).iterator.asScala ++
      index.query(PLACE_ALTLABELS, q).iterator.asScala ++
      index.query(PLACE_COVERAGE, q).iterator.asScala
    
    val duplicatesRemoved = hits.toList.groupBy(node => node).keys
    
    duplicatesRemoved.map(node => new PlaceVertex(new Neo4jVertex(node, graph))).toList 
  }

}