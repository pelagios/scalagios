package org.pelagios.rdf.parser

import org.openrdf.model.Statement
import org.openrdf.rio.helpers.RDFHandlerBase
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.vocabulary.RDFS
import scala.collection.mutable.HashMap
import org.openrdf.model.URI
import org.openrdf.model.Value
import org.pelagios.rdf.vocab.Pelagios
import org.pelagios.api.DefaultAnnotatedThing
import org.pelagios.rdf.vocab.DCTerms

class CachedResource(val uri: String) {
  
  private val properties = new HashMap[URI, Value]
  
  def setProperty(uri: URI, value: Value) = properties.put(uri, value)
    
  def getProperty(uri: URI) = properties.get(uri)
  
  def hasType(typ: Value) = 
    properties.contains(RDF.TYPE) && (properties.get(RDF.TYPE).get == typ)

}

class PelagiosDumpCollector extends RDFHandlerBase {
    
  val cachedResources = new HashMap[String, CachedResource]
  
  override def handleStatement(statement: Statement): Unit = {
    val subj = statement.getSubject.stringValue
    val resource = cachedResources.getOrElse(subj, new CachedResource(subj))
    resource.setProperty(statement.getPredicate, statement.getObject)
    cachedResources.put(subj, resource)
  }
  
  def annotatedThings = cachedResources.values
    .filter(_.hasType(Pelagios.AnnotatedThing))
    .map(toAnnotatedThing(_))
  
  private def toAnnotatedThing(resource: CachedResource) = {
    val annotatedThing = DefaultAnnotatedThing(resource.uri)
    annotatedThing.title = resource.getProperty(DCTerms.title).map(_.stringValue)
    annotatedThing.description = resource.getProperty(DCTerms.description).map(_.stringValue)
    annotatedThing
  }
    
}