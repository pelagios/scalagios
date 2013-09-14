package org.pelagios.rdf.parser

import scala.collection.mutable.HashMap
import org.openrdf.model.{ Statement, URI, Value }
import org.openrdf.model.vocabulary.RDF
import org.openrdf.rio.helpers.RDFHandlerBase
import scala.collection.mutable.ArrayBuffer

/**
 * A mutable data structure to collect the triples for a specific subject
 * into a single bag.
 */
private[parser] class Resource(val uri:String) {
  
  val properties = new ArrayBuffer[(URI, Value)]
   
  def get(property: URI) = properties.filter(_._1 == property).map(_._2)
  
  def hasType(typ: Value) = get(RDF.TYPE).contains(typ)
    
}

class ResourceCollector extends RDFHandlerBase {
  
  protected val resources = new HashMap[String, Resource]
  
  override def handleStatement(statement: Statement): Unit = {
    val subj = statement.getSubject.stringValue
    val resource = resources.getOrElse(subj, new Resource(subj))
    resource.properties.append((statement.getPredicate, statement.getObject))
    resources.put(subj, resource)
  }

}