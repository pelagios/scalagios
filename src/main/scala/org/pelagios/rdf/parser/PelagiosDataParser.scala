package org.pelagios.rdf.parser

import org.pelagios.rdf.vocab.{ OA, Pelagios }
import org.pelagios.api.{ AnnotatedThing, Annotation }
import org.pelagios.rdf.vocab.DCTerms
import org.pelagios.api.Neighbour
import org.pelagios.rdf.vocab.FOAF
import org.pelagios.api.PeriodOfTime
import org.pelagios.api.Agent
import org.pelagios.rdf.vocab.DCTerms
import org.openrdf.model.vocabulary.RDFS
import java.util.Date

/** An implementation of [[org.pelagios.rdf.parser.ResourceCollector]] to handle Pelagios data dump files.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class PelagiosDataParser extends ResourceCollector {

  def annotations = 
    resources.values.filter(_.hasType(OA.Annotation)).map(new AnnotationResource(_))
  
  def annotatedThings = {
    val annotationsPerThing = annotations.groupBy(_.hasTarget)
      
    resources.values
      .filter(_.hasType(Pelagios.AnnotatedThing)).map(resource => 
        new AnnotatedThingResource(
            resource, 
            Seq.empty[AnnotatedThingResource],
            annotationsPerThing.get(resource.uri).getOrElse(Seq.empty[AnnotationResource]).toSeq))
  }
      
}

/** Wraps a pelagios:AnnotatedThing RDF resource as an AnnotatedThing domain model primitive, with
 *  Annotations in-lined.
  *  
  * @constructor create a new AnnotatedThing
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotatedThingResource(resource: Resource, val variants: Seq[AnnotatedThingResource], val annotations: Seq[AnnotationResource]) extends AnnotatedThing {

  def uri = resource.uri
  
  def title = resource.getFirst(DCTerms.title).map(_.stringValue).getOrElse("[NO TITLE]") // 'NO TITLE' should never happen!
  
  def identifier = resource.getFirst(DCTerms.identifier).map(_.stringValue)

  def description = resource.getFirst(DCTerms.description).map(_.stringValue)
  
  def sources = resource.get(DCTerms.source).map(_.stringValue)
  
  // TODO
  def temporal: Option[PeriodOfTime] = None

  // TODO 
  def creator: Option[Agent] = None

  // TODO    
  def contributors: Seq[Agent] = Seq.empty[Agent]
  
  def languages = resource.get(DCTerms.language).map(_.stringValue)
  
  def homepage = resource.getFirst(FOAF.homepage).map(_.stringValue)
  
  def thumbnails = resource.get(FOAF.thumbnail).map(_.stringValue)
  
  def bibliographicCitations = resource.get(DCTerms.bibliographicCitation).map(_.stringValue)
  
  def subjects = resource.get(DCTerms.subject).map(_.stringValue)
  
  def seeAlso = resource.get(RDFS.SEEALSO).map(_.stringValue)  
  
}

/** Wraps an oa:Annotation RDF resource as an Annotation domain model primitive.
  *  
  * @constructor create a new AnnotationResource
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotationResource(resource: Resource) extends Annotation {

  def uri = resource.uri
  
  def hasBody = resource.get(OA.hasBody).map(_.stringValue)
  
  def hasTarget = resource.getFirst(OA.hasTarget).map(_.stringValue).getOrElse("_:empty") // '_:empty' should never happen!
  
  def motivatedBy: Option[String] = Some(resource.getFirst(OA.motivatedBy).map(_.stringValue).getOrElse("geotagging")) // Default to geotagging
  
  // TODO 
  def annotatedBy: Option[Agent] = None

  // TODO
  def annotatedAt: Option[Date] = None
  
  // TODO
  def creator: Option[Agent] = None
  
  // TODO
  def created: Option[Date] = None
  
  def toponym: Option[String] = resource.getFirst(Pelagios.toponym).map(_.stringValue)
  
  // TODO
  def hasNext: Option[Neighbour] = None // TODO implement!   
  
}

