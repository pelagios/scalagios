package org.pelagios.rdf.parser

import scala.collection.JavaConversions._
import org.openrdf.rio.helpers.RDFHandlerBase
import org.openrdf.model.URI
import org.slf4j.LoggerFactory
import org.openrdf.model.Statement
import org.mapdb.DBMaker
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.Value

private[parser] abstract class OffHeapResourceCollector extends RDFHandlerBase {
  
  private var ctr = 0
  
  protected val logger = LoggerFactory.getLogger(classOf[GazetteerParser])

  protected val resources = DBMaker.newTempHashMap[String, Resource]()
  
  override def handleStatement(statement: Statement): Unit = {
    ctr += 1
    if (ctr % 50000 == 0)
      logger.info("Parsed " + ctr + " triples")
      
    val subj = statement.getSubject.stringValue
    
    val resource = Option(resources.get(subj)).getOrElse(new Resource(subj))
    resource.properties.append((statement.getPredicate, statement.getObject))
    resources.put(subj, resource)
  }
  
  protected def resourcesOfType(rdfType: URI): Iterator[Resource] =
    resources.valuesIterator.filter(resource => {
      val types = resource.get(RDF.TYPE) 
      types.contains(rdfType)
    })
  
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
        true // Resource was explicity typed and matches
      else
        typeRules.exists(rule => rule(resource)) // No type match - try the rules
    })
  
  protected def getResource(uri: Value): Option[Resource] =
    Option(resources.get(uri.stringValue))

}
