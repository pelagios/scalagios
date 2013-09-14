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

private[parser] class CachedAnnotatedThing(resource: Resource, annotations: Seq[CachedAnnotation]) extends AnnotatedThing {

  def uri = resource.uri
  
  def title = None //resource.getAsString(DCTerms.title)

  def description = None // resource.getAsString(DCTerms.description)
  
}

private[parser] class CachedAnnotation(resource: Resource) extends Annotation {

  def uri = resource.uri
  
}

class PelagiosDataParser extends ResourceCollector {

  lazy val annotations = 
    resources.values.filter(_.hasType(OA.Annotation)).map(new CachedAnnotation(_))
  
  lazy val annotatedThings = {
    val annotationsPerThing = annotations.groupBy(_.uri)
      
    resources.values
      .filter(_.hasType(Pelagios.AnnotatedThing)).map(resource => 
        new CachedAnnotatedThing(
            resource, 
            annotationsPerThing.get(resource.uri).getOrElse(Seq.empty[CachedAnnotation]).toSeq))
  }
      
}