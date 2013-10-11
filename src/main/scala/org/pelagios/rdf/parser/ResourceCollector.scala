package org.pelagios.rdf.parser

import scala.collection.mutable.{ArrayBuffer, HashMap}
import org.openrdf.model.{ Literal, Statement, URI, Value }
import org.openrdf.model.vocabulary.RDF
import org.openrdf.rio.helpers.RDFHandlerBase
import org.pelagios.api.Label
import org.pelagios.rdf.vocab.PleiadesPlaces
import org.pelagios.rdf.vocab.Pelagios

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
  protected def resourcesOfType(rdfType: URI, 
                                 typeRules: Seq[Resource => Boolean] = Seq.empty[Resource => Boolean])
                                 :Seq[Resource] = {
    
    resources.foldLeft(Seq.empty[Resource])((resultList, currentResource) => {
      val (uri, resource) = (currentResource._1, currentResource._2)
      val types = resource.get(RDF.TYPE)
      
      if (types.contains(rdfType)) {
        // Resource was explicity typed and matches
        resource +: resultList
      } else {
        // No match - try the rules
        if (typeRules.exists(rule => rule(resource))) 
          resource +: resultList
        else
          resultList        
      }      
    })
  }

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