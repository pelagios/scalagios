package org.pelagios.rdf.parser

import org.openrdf.model.{ Statement, URI }
import org.openrdf.model.vocabulary.RDF
import org.openrdf.rio.helpers.RDFHandlerBase
import org.slf4j.LoggerFactory
import scala.collection.mutable.HashMap

/** An alternative ResourceCollector that allows 'stream' processing of RDF resources.
  *
  * RDF has no notion of sequence and ordering. It is perfectly valid - and possible - that
  * the first triple in a large data dump is part of the same RDF resource as the last triple.
  * In practice, however, many RDF dump files are sequential, i.e. triples for a specific 
  * resource are close together in the file.
  * 
  * This resource collector implementation leverages this fact by buffering only as many 
  * resources as necessary in memory, and handing 'completed' resources back via a callback
  * function. This way, huge datasets can, for example, be streamed into an index or a datbase
  * without the need to assemble the whole RDF graph of the file first.
  * 
  * WARNING: Only use this collector if you KNOW that your dump file contains resources in 
  * sequential order. 
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
private[parser] abstract class ResourceStreamHandler(triggerType: URI) extends RDFHandlerBase {
  
  /** This class writes progress log messages - this value determines after how many triples **/ 
  private val PROGRESS_LOG_STEP = 100000
  
  protected val logger = LoggerFactory.getLogger(classOf[ResourceStreamHandler])
  
  protected val cache = new HashMap[String, Resource]
  
  private var tripleCounter = 0
  
  private val startTime = System.currentTimeMillis
  
  /** Subclasses need to implement this method.
    *  
    * This is where the the target resources should be assembled from the
    * cache and forwarded to the result stream.
    */ 
  protected def pushToStream(): Unit
  
  override def handleStatement(s: Statement): Unit = {
    tripleCounter += 1

    val subj = s.getSubject.stringValue
    val pred = s.getPredicate
    val obj = s.getObject
    
    // In case of subject -> rdf:type -> pelagios:PlaceRecord purge the cache
    if ((pred == RDF.TYPE) && (obj == triggerType))
      pushToStream()
    
    // Keep parsing...
    val resource = cache.getOrElse(subj, new Resource(subj))
    resource.properties.append((pred, obj))
    cache.put(subj, resource)
    
    if (tripleCounter % PROGRESS_LOG_STEP == 0)
      logger.info("Parsed " + tripleCounter + " triples from dump file")
  }
  
  override def endRDF(): Unit = {
    pushToStream()
    
    logger.info("Parsing complete - took " + (System.currentTimeMillis - startTime) + "ms")
    logger.info(tripleCounter + " triples")
    
    if (cache.size > 0) {
      logger.warn("There are " + cache.values.size + " unassigned triples left in the streaming cache:")
      cache.values.foreach(resource => logger.debug(resource.uri))
    }
    
	  cache.clear()
  }

}