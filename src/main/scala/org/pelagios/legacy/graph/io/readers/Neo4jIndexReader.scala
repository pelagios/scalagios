package org.pelagios.legacy.graph.io.readers


import scala.collection.JavaConverters._
import com.tinkerpop.blueprints.pgm.impls.neo4j.{Neo4jGraph, Neo4jVertex}
import org.neo4j.index.lucene.QueryContext
import org.pelagios.legacy.api.{Dataset, Place}
import org.pelagios.legacy.graph.Constants._
import org.pelagios.legacy.graph.{DatasetVertex, PlaceVertex}
import org.pelagios.legacy.graph.io.PelagiosGraphIOBase
import org.apache.lucene.util.Version
import org.apache.lucene.search.NumericRangeQuery
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.BooleanClause
import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.Node
import org.apache.lucene.queryParser.QueryParser
import org.apache.lucene.analysis.KeywordAnalyzer

trait Neo4jIndexReader extends PelagiosGraphIOBase {

  val neo4jGraph = graph.asInstanceOf[Neo4jGraph]
  
  val placeNodeIndex = neo4jGraph.getRawGraph.index().forNodes(INDEX_FOR_PLACES)
  val datasetNodeIndex = neo4jGraph.getRawGraph.index().forNodes(INDEX_FOR_DATASETS)
  
  def queryPlaces(q: String, fuzzy: Boolean = false): List[Place] = {
    val rewritten = 
      if (q.contains("\"")) { 
        q
      } else {
        if (fuzzy)
          q + "~ "
        else
          q + "* "
      }
      
    val query = new QueryParser(Version.LUCENE_35, PLACE_LABEL, new KeywordAnalyzer)
        .parse(
          rewritten +
          PLACE_ALTLABELS + ":" + rewritten +
          PLACE_COVERAGE + ":" + rewritten +
          PLACE_COMMENT + ":" + q)
    
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
  
  def getRawIndex: Index[Node] = placeNodeIndex
  
  def queryDatasets(query: String): List[Dataset] = {
    datasetNodeIndex.query(DATASET_TITLE, query).iterator().asScala.map(node => 
      new DatasetVertex(new Neo4jVertex(node, neo4jGraph))).toList
  }

}
