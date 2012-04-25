package org.scalagios.graph.io

import scala.collection.JavaConverters._
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jVertex
import org.neo4j.index.lucene.QueryContext
import org.scalagios.api.{Place, Dataset}
import org.scalagios.graph.Constants._
import org.scalagios.graph.{PlaceVertex, DatasetVertex}

class PelagiosNeo4jReader(graph: Neo4jGraph) extends PelagiosGraphReader(graph) {
  
  val neo4j = graph.getRawGraph()
  
  def queryPlaces(query: String): List[Place] = {
    val index = neo4j.index().forNodes(INDEX_FOR_PLACES)
    
    val q = new QueryContext(
      PLACE_LABEL + ":" + query + "* " +
      PLACE_COMMENT + ":" + query + "* " +
      PLACE_ALTLABELS + ":" + query + "* " + 
      PLACE_COVERAGE + ":" + query + "*").sortByScore
      
    index.query(q).iterator.asScala.map(node => 
      new PlaceVertex(new Neo4jVertex(node, graph))).toList 
  }
  
  def queryDatasets(query: String): List[Dataset] = {
    val index = neo4j.index().forNodes(INDEX_FOR_DATASETS)
        
    index.query(query).iterator().asScala.map(node => 
      new DatasetVertex(new Neo4jVertex(node, graph))).toList
  }

}