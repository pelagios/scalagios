package org.pelagios.rdf.parser

import org.openrdf.model.Statement
import org.openrdf.rio.helpers.RDFHandlerBase
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.vocabulary.RDFS
import scala.collection.mutable.HashMap
import org.openrdf.model.URI
import org.openrdf.model.Value
import org.pelagios.rdf.vocab.Pelagios
import org.pelagios.api.AnnotatedThing
import org.pelagios.rdf.vocab.DCTerms
import org.pelagios.api.Annotation
import org.pelagios.rdf.vocab.OA

private[parser] class CachedResource(val uri: String) {
  
  val properties = new HashMap[URI, Value]
   
  def getAsString(property: URI) = properties.get(property).map(_.stringValue)
  
  def hasType(typ: Value) = 
    properties.contains(RDF.TYPE) && (properties.get(RDF.TYPE).get == typ)

}

private[parser] class CachedAnnotatedThing(resource: CachedResource, annotations: Seq[CachedAnnotation]) extends AnnotatedThing {

  def uri = resource.uri
  
  def title = resource.getAsString(DCTerms.title)

  def description = resource.getAsString(DCTerms.description)
  
}

private[parser] class CachedAnnotation(resource: CachedResource) extends Annotation {

  def uri = resource.uri
  
}

private[parser] class CachedAnnotationTarget(resource: CachedResource) /** extends AnnotationTarget **/ {
  
}

class PelagiosDumpCollector extends RDFHandlerBase {
    
  val cachedResources = new HashMap[String, CachedResource]
  
  override def handleStatement(statement: Statement): Unit = {
    val subjUri = statement.getSubject.stringValue
    val resource = cachedResources.getOrElse(subjUri, new CachedResource(subjUri))
    resource.properties.put(statement.getPredicate, statement.getObject)
    cachedResources.put(subjUri, resource)
  }
  
  lazy val annotations = 
    cachedResources.values.filter(_.hasType(OA.Annotation)).map(new CachedAnnotation(_))
  
  lazy val annotatedThings = {
    val annotationsPerThing = annotations.groupBy(_.uri)
      
    cachedResources.values
      .filter(_.hasType(Pelagios.AnnotatedThing)).map(resource => 
        new CachedAnnotatedThing(
            resource, 
            annotationsPerThing.get(resource.uri).getOrElse(Seq.empty[CachedAnnotation]).toSeq))
  }
      
}