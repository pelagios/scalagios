package org.scalagios.graph.io.readers

import scala.collection.JavaConverters._
import com.tinkerpop.blueprints.pgm.impls.neo4j.{Neo4jGraph, Neo4jVertex}
import org.neo4j.index.lucene.QueryContext
import org.scalagios.api.{Dataset, Place}
import org.scalagios.graph.Constants._
import org.scalagios.graph.{DatasetVertex, PlaceVertex}
import org.scalagios.graph.io.PelagiosGraphIOBase
import org.apache.lucene.util.Version
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.analysis.KeywordAnalyzer
import org.apache.lucene.search.NumericRangeQuery
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.BooleanClause

trait Neo4jIndexReader extends PelagiosGraphIOBase {

  val neo4jGraph = graph.asInstanceOf[Neo4jGraph]
  
  val placeNodeIndex = neo4jGraph.getRawGraph.index().forNodes(INDEX_FOR_PLACES)
  val datasetNodeIndex = neo4jGraph.getRawGraph.index().forNodes(INDEX_FOR_DATASETS)
  
  def queryPlaces(q: String, fuzzy: Boolean = false): List[Place] = {
    val query = if (fuzzy)
      new QueryParser(Version.LUCENE_35, PLACE_LABEL, new KeywordAnalyzer)
        .parse(q + "* " + q + "~ " + 
          PLACE_ALTLABELS + ":" + q + "* " + q + "~ " +
          PLACE_COVERAGE + ":" + q + "* " + q + "~ " +
          PLACE_COMMENT + ":" + q + "*")
      else
        new QueryParser(Version.LUCENE_35, PLACE_LABEL, new KeywordAnalyzer)
        .parse(q + "* " +
          PLACE_ALTLABELS + ":" + q + "* " + 
          PLACE_COVERAGE + ":" + q + "* " +
          PLACE_COMMENT + ":" + q + "*")
    
    placeNodeIndex.query(new QueryContext(query).sortByScore).iterator.asScala 
      .map(node =>  new PlaceVertex(new Neo4jVertex(node, neo4jGraph))).toList 
  }
  
  def queryPlaces(minLon: Double, minLat: Double, maxLon: Double, maxLat: Double): List[Place] = {
    val lon = NumericRangeQuery.newDoubleRange(PLACE_LON, minLon, maxLon, true, true)
    val lat = NumericRangeQuery.newDoubleRange(PLACE_LAT, minLat, maxLat, true, true)
    
    val compound = new BooleanQuery()
    compound.add(lon, BooleanClause.Occur.MUST)
    compound.add(lat, BooleanClause.Occur.MUST)
    
    placeNodeIndex.query(compound).iterator.asScala
      .map(node => new PlaceVertex(new Neo4jVertex(node, neo4jGraph))).toList
  }
  
  def queryDatasets(query: String): List[Dataset] = {
    datasetNodeIndex.query(DATASET_TITLE, query).iterator().asScala.map(node => 
      new DatasetVertex(new Neo4jVertex(node, neo4jGraph))).toList
  }

}
