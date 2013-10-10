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
  
  /** A helper method that determines explicit type information to the collected resources.
    * 
    * The method takes three arguments: (i) the resources, (ii) a list of supported RDF 
    * types,(iii) a list of rules for "guessing" the type of resources that are not 
    * explicitly typed.
    * 
    * In the first step, the method retrieves all explicit rdf:type properties from the 
    * resource and checks whether any match the list of supported types. The first one
    * that does is used as the type.
    * 
    * If no explict matching type was found, the method goes through the type rules.
    * The type of the resource will be determined by the first rule that returns matches.
    * If no rule matches, the resource will be discared. 
    * 
    * @param resources the RDF resources
    * @param supportedTypes the list of supported RDF types
    * @param typeRules the list of type checking rules
    * @return a map of the resources, grouped by RDF types (untyped resources are discarded)
    */
  protected def groupByType(resources: Map[String, Resource], supportedTypes: Seq[Value], typeRules: Seq[Resource => Option[URI]]): Map[URI, Map[String, Resource]] = {
    val typedResources = resources.foldLeft(List.empty[(URI, String, Resource)])((result, current) => {
      val (uri, resource) = (current._1, current._2)
      val types = resource.get(RDF.TYPE).toSeq      
      
      // Find the resource's first explicit rdf:type that's in the supported types list
      val firstType = types.find(t => supportedTypes.contains(t))
      
      if (firstType.isDefined) {
        // Found one -> add to result list & done.
        (firstType.get.asInstanceOf[URI], uri, resource) :: result
      } else {
        // No luck - try the type rules & take the first match
        val firstRuleHit = typeRules.collectFirst { case rule if { rule(resource).isDefined } => rule(resource).get }
        if (firstRuleHit.isDefined) {
          (firstRuleHit.get.asInstanceOf[URI], uri, resource) :: result          
        } else {
          result
        }
      }
    })
    
    typedResources.groupBy(_._1).mapValues(_.map { case (typeURI, subjURI, resource) => subjURI -> resource }.toMap)
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