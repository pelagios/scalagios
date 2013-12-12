package org.pelagios.legacy.graph.io.writers

import scala.collection.JavaConverters._
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import org.pelagios.legacy.graph.Constants._
import grizzled.slf4j.Logger

trait Neo4jPlaceWriter extends GraphPlaceWriter {
  
  val logger = Logger(classOf[Neo4jPlaceWriter])
  
  val graph: Neo4jGraph
  
  private val neo4j = graph.getRawGraph()
  
  def dropPlaces(): Int = {
    val index = neo4j.index().forNodes(INDEX_FOR_PLACES)

    val transaction = neo4j.beginTx()
    var ctr = 0
    try {
      index.query(PLACE_URI, "*").iterator().asScala.foreach(node => {
        node.getRelationships().asScala.foreach(_.delete())
        node.delete()
        ctr += 1
      })
      transaction.success()
    } catch {
      case t: Throwable => {
        transaction.failure()
        ctr = 0
        logger.error(t.getMessage())
      }
    } finally {
      transaction.finish()
    }
    
    ctr
  }

}