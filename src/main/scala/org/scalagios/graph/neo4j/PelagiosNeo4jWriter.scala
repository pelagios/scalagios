package org.scalagios.graph.neo4j

import scala.collection.JavaConverters._
import com.weiglewilczek.slf4s.Logging
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import org.scalagios.graph.Constants.{ANNOTATION_URI, INDEX_FOR_ANNOTATIONS}

/**
 * Provides Pelagios-specific write functionality for modifying
 * a <em>Neo4jGraph<em>, including
 * 
 * <ul>
 * <li>dropping a dataset from the graph</li>
 * </ul>
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class PelagiosNeo4jWriter(graph: Neo4jGraph) extends Logging {
      
  /**
   * Drops all GeoAnnotations with a URI starting with the specified 
   * base URI.
   * @return the number of annotations successfully dropped
   */
  def dropDataset(baseURI: String): Int = {
    // Note: we need to use the native Neo4j index, since the Tinkerpop
    // abstraction does not support wildcard searches (d'oh!)
    val neo4j = graph.getRawGraph()
    val index = neo4j.index().forNodes(INDEX_FOR_ANNOTATIONS)
    
    // A prefix-query with ':' escaped, since this has special meaning in Lucene queries
    // (see http://docs.neo4j.org/chunked/snapshot/indexing-lucene-extras.html#indexing-lucene-query-objects)
    val query = baseURI.replace(":", "\\:") + "*"
    
    val transaction = neo4j.beginTx()
    var ctr = 0
    try {
      index.query(ANNOTATION_URI, query).iterator().asScala.foreach(node => {
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