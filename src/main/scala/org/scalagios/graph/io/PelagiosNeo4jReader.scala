package org.scalagios.graph.io.read

import scala.collection.JavaConverters._
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import org.scalagios.api.{Place, Dataset}
import org.scalagios.graph.Constants._
import org.scalagios.graph.{PlaceVertex, DatasetVertex}
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jVertex
import org.apache.lucene.search.WildcardQuery
import org.apache.lucene.index.Term
import org.apache.lucene.search.spans.SpanTermQuery
import org.apache.lucene.queryParser.MultiFieldQueryParser
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.standard.StandardAnalyzer

class PelagiosNeo4jReader(graph: Neo4jGraph) extends PelagiosGraphReader(graph) {
  
  val neo4j = graph.getRawGraph()
  
  val placeQueryParser = new MultiFieldQueryParser(
      Version.LUCENE_35,
      Array(PLACE_LABEL, PLACE_COMMENT, PLACE_ALTLABELS, PLACE_COVERAGE),
      new StandardAnalyzer(Version.LUCENE_35))
  
  val datasetQueryParser = new MultiFieldQueryParser(
      Version.LUCENE_35,
      Array(DATASET_TITLE, DATASET_DESCRIPTION),
      new StandardAnalyzer(Version.LUCENE_35))
  
  def queryPlaces(query: String): List[Place] = {
    val index = neo4j.index().forNodes(INDEX_FOR_PLACES)
    val q = if (query.contains("\""))
          placeQueryParser.parse(query)
        else
          placeQueryParser.parse(query + "*")
      
    index.query(q).iterator.asScala.map(node => 
      new PlaceVertex(new Neo4jVertex(node, graph))).toList 
  }
  
  def queryDatasets(query: String): List[Dataset] = {
    val index = neo4j.index().forNodes(INDEX_FOR_DATASETS)
    val q = if (query.contains("\""))
        datasetQueryParser.parse(query)
      else
        datasetQueryParser.parse(query + "*")
        
    index.query(q).iterator().asScala.map(node => 
      new DatasetVertex(new Neo4jVertex(node, graph))).toList
  }

}