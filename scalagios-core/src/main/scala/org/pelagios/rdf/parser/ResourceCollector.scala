package org.pelagios.rdf.parser

import org.openrdf.rio.helpers.RDFHandlerBase
import org.openrdf.model.{ Literal, URI, Statement, Value }
import org.openrdf.model.vocabulary.RDF
import org.pelagios.api.PlainLiteral
import scala.collection.mutable.HashMap

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
  
  protected def resourcesOfType(rdfType: URI): Iterator[Resource] =
    resources.valuesIterator.filter(resource => {
      val types = resource.get(RDF.TYPE)
      types.contains(rdfType)
    }).toIterator
  
  /** A helper method that filters the collected resources by type.
    * 
    * The method takes two arguments: (i) the type to filter by, (ii) an optional list
    * of 'rules' for determining the type of resources that are not explicitly typed.
    * 
    * In the first step, the method retrieves all explicit rdf:type properties from the 
    * resources and checks whether any matches the filter type. If no explicit match was
    * found, the method goes through the type rules and determines the type by the 
    * first matching rule.
    * 
    * @param rdfType the RDF type to filter by
    * @param typeRules the list of type checking rules
    * @return the list of resources that are of the specified RDF type or satisfy any of the rules
    */  
  protected def resourcesOfType(rdfType: URI, typeRules: Seq[Resource => Boolean]): Iterator[Resource] =
    resources.valuesIterator.filter(resource => {
      val types = resource.get(RDF.TYPE)
      if (types.contains(rdfType))
        true
      else
        typeRules.exists(rule => rule(resource))        
    }).toIterator
  
  protected def getResource(uri: Value): Option[Resource] =
    resources.get(uri.stringValue)

}

/** A companion to [[ResourceCollector]] providing helper methods **/
private[parser] object ResourceCollector {
  
  /** Converts an RDF value to a [[org.pelagios.api.Label]] domain object.
    * 
    * @param value the RDF value
    * @return the label
    */
  def toPlainLiteral(value: Value): PlainLiteral = {
    value match {
      case v if v.isInstanceOf[Literal] => { 
        val literal = v.asInstanceOf[Literal]
        val lang = literal.getLanguage
        if (lang != null)
          PlainLiteral(literal.stringValue, Some(lang))
        else
          PlainLiteral(literal.stringValue)
      }
      
      case v => PlainLiteral(v.stringValue)
    }
  }

  /** Converts a sequence of RDF values to [[org.pelagios.api.Label]] domain objects.
    * 
    * @param values the sequence of RDF values
    * @return the labels
    */
  def toPlainLiteral(values: Seq[Value]): Seq[PlainLiteral] = values.map(toPlainLiteral(_))
  
}