package org.pelagios.rdf.parser

import scala.collection.mutable.HashMap
import org.openrdf.model.{ Statement, URI, Value }
import org.openrdf.model.vocabulary.RDF
import org.openrdf.rio.helpers.RDFHandlerBase
import scala.collection.mutable.ArrayBuffer
import org.pelagios.api.Label
import org.openrdf.model.Literal

/**
 * A mutable data structure to collect the triples for a specific subject
 * into a single bag.
 */
private[parser] class Resource(val uri:String) {
  
  val properties = new ArrayBuffer[(URI, Value)]
   
  def get(property: URI): Seq[Value] = properties.filter(_._1 == property).map(_._2).toSeq
  
  def getFirst(property: URI): Option[Value] = {
    val properties = get(property)
    if (properties.size > 0) 
      Some(properties(0))
    else
      None
  }
  
  def hasType(typ: Value): Boolean = get(RDF.TYPE).contains(typ)
  
  def hasProperty(property: URI): Boolean = properties.filter(_._1 == property).size > 0
    
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

object ResourceCollector {
  
  def toLabel(value: Value): Label = {
    value match {
      case v if v.isInstanceOf[Literal] => { 
        val literal = v.asInstanceOf[Literal]
        val lang = literal.getLanguage
        if (lang != null)
          Label(literal.stringValue, Some(lang))
        else
          Label(literal.stringValue)
      }
      
      case v => new Label(v.stringValue)
    }
  }
  
  def toLabel(values: Seq[Value]): Seq[Label] = values.map(toLabel(_))
  
}