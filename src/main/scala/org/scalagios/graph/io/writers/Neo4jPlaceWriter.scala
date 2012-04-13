package org.scalagios.graph.io.writers

import scala.collection.JavaConverters._
import com.weiglewilczek.slf4s.Logging
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import org.scalagios.graph.Constants._

trait Neo4jPlaceWriter extends GraphPlaceWriter with Logging {
  
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