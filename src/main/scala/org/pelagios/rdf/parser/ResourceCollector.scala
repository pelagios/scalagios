package org.pelagios.rdf.parser

import scala.collection.mutable.{ArrayBuffer, HashMap}
import org.openrdf.model.{ Literal, Statement, URI, Value }
import org.openrdf.model.vocabulary.RDF
import org.openrdf.rio.helpers.RDFHandlerBase
import org.pelagios.api.Label

/** An RDFHandler that collects triples into a more convenient map. 
  *   
  * An implementation of [[org.openrdf.rio.RDFHandler]] that collects
  * individual triples into a map. The map has RDF resource subject URIs
  * as keys, and [[org.pelagios.rdf.parser.Resource]] objects as values. 
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
private[parser] abstract class ResourceCollector extends RDFHandlerBase {
  
  protected val resources = new HashMap[String, Resource]
  
  override def handleStatement(statement: Statement): Unit = {
    val subj = statement.getSubject.stringValue
    val resource = resources.getOrElse(subj, new Resource(subj))
    resource.properties.append((statement.getPredicate, statement.getObject))
    resources.put(subj, resource)
  }

}

/** A helper structure to collect the triples for a specific RDF resource.
  *  
  * This helper structure aggregates all property/object pairs for one
  * particular RDF resource. Since this is only a helper, we restrict
  * visibility to this package.
  * 
  * @constructor create a new resource with a URI
  * @param uri the URI
  */
private[parser] class Resource(val uri:String) {
  
  val properties = new ArrayBuffer[(URI, Value)]
  
  /** Returns the values (objects) for a particular RDF property.
    *
    * @param property the property URI
    * @return the object values for this property
    */
  def get(property: URI): Seq[Value] = properties.filter(_._1 == property).map(_._2).toSeq
  
  /** Returns the first value (object) for a particular RDF property or 'None'
    *  
    * @param property the property URI
    * @return the first value found for that property, or 'None', if the resource
    *         does not have this property 
    */ 
  def getFirst(property: URI): Option[Value] = {
    val properties = get(property)
    if (properties.size > 0) 
      Some(properties(0))
    else
      None
  }
  
  /** Checks if the resource has a specific RDF type.
    *  
    * @param typ the RDF type value  
    * @return true if the resource has the type
    */
  def hasType(typ: Value): Boolean = get(RDF.TYPE).contains(typ)

  /** Checks if the resource has a specific RDF property.
    *  
    * @param property the property URI
    * @return true if the resource has the property
    */
  def hasProperty(property: URI): Boolean = properties.filter(_._1 == property).size > 0
    
}

/** A companion to [[ResourceCollector]] providing helper methods **/
private[parser] object ResourceCollector {
  
  /** Converts an RDF value to a [[org.pelagios.api.Label]] domain object.
    * 
    * @param value the RDF value
    * @return the label
    */
  def toLabel(value: Value): Label = {
    value match {
      case v if v.isInstanceOf[Literal] => { 
        val literal = v.asInstanceOf[Literal]
        val lang = literal.getLanguage
        if (lang != null)
          new Label(literal.stringValue, Some(lang))
        else
          new Label(literal.stringValue)
      }
      
      case v => new Label(v.stringValue)
    }
  }

  /** Converts a sequence of RDF values to [[org.pelagios.api.Label]] domain objects.
    * 
    * @param values the sequence of RDF values
    * @return the labels
    */
  def toLabel(values: Seq[Value]): Seq[Label] = values.map(toLabel(_))
  
}