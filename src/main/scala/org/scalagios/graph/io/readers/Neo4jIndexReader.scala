package org.scalagios.graph.io.readers

import scala.collection.JavaConverters._
import com.tinkerpop.blueprints.pgm.impls.neo4j.{Neo4jGraph, Neo4jVertex}
import org.neo4j.index.lucene.QueryContext
import org.scalagios.api.{Dataset, Place}
import org.scalagios.graph.Constants._
import org.scalagios.graph.{DatasetVertex, PlaceVertex}
import org.scalagios.graph.io.PelagiosGraphIOBase

trait Neo4jIndexReader extends PelagiosGraphIOBase {

  val neo4jGraph = graph.asInstanceOf[Neo4jGraph]
  
  val placeNodeIndex = neo4jGraph.getRawGraph.index().forNodes(INDEX_FOR_PLACES)
  val datasetNodeIndex = neo4jGraph.getRawGraph.index().forNodes(INDEX_FOR_DATASETS)
  
  def queryPlaces(query: String): List[Place] = {
    val q = new QueryContext(
      PLACE_LABEL + ":" + query + "* " +
      PLACE_COMMENT + ":" + query + "* " +
      PLACE_ALTLABELS + ":" + query + "* " + 
      PLACE_COVERAGE + ":" + query + "*").sortByScore
      
    placeNodeIndex.query(q).iterator.asScala.map(node => 
      new PlaceVertex(new Neo4jVertex(node, neo4jGraph))).toList 
  }
  
  def queryDatasets(query: String): List[Dataset] = {
    datasetNodeIndex.query(DATASET_TITLE, query).iterator().asScala.map(node => 
      new DatasetVertex(new Neo4jVertex(node, neo4jGraph))).toList
  }

}