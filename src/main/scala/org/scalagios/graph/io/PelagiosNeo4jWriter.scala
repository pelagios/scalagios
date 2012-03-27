package org.scalagios.graph.io

import scala.collection.JavaConverters._
import com.weiglewilczek.slf4s.Logging
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import org.scalagios.graph.Constants._
import org.neo4j.graphdb.DynamicRelationshipType
import org.neo4j.graphdb.index.IndexManager

/**
 * Provides Pelagios-specific Graph DB I/O features that are only supported
 * by Neo4j, but not the default Tinkerpop graph DB abstraction.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class PelagiosNeo4jWriter(graph: Neo4jGraph) extends PelagiosGraphWriter(graph) with Logging {
  
  private val hasTarget = DynamicRelationshipType.withName(RELATION_HASTARGET)
  
  def dropPlaces(): Int = {
    val neo4j = graph.getRawGraph()
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
  
  /**
   * Drops all GeoAnnotations with a URI starting with the specified 
   * base URI.
   * @return the number of annotations successfully dropped
   */
  def dropDataset(baseURI: String): Int = {
    val neo4j = graph.getRawGraph()
    val index = neo4j.index().forNodes(INDEX_FOR_ANNOTATIONS)
    
    // A prefix-query with ':' escaped, since this has special meaning in Lucene queries
    // (see http://docs.neo4j.org/chunked/snapshot/indexing-lucene-extras.html#indexing-lucene-query-objects)
    val query = baseURI.replace(":", "\\:") + "*"
    
    val transaction = neo4j.beginTx()
    var ctr = 0
    try {
      index.query(ANNOTATION_URI, query).iterator().asScala.foreach(node => {
        // 1. remove annotation target node
        node.getRelationships(hasTarget).asScala.foreach(_.getEndNode().delete())
        
        // 2. remove all relationships
        node.getRelationships().asScala.foreach(_.delete())
        
        // 3. remove the node
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