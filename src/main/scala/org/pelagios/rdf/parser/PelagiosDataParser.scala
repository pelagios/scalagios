package org.pelagios.rdf.parser

import org.pelagios.rdf.vocab.{ OA, Pelagios }
import org.pelagios.api.{ AnnotatedThing, Annotation }
import org.pelagios.rdf.vocab.DCTerms
import org.pelagios.api.Neighbour

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
            annotationsPerThing.get(resource.uri).getOrElse(Seq.empty[AnnotationResource]).toSeq))
  }
      
}

/** Wraps a pelagios:AnnotatedThing RDF resource as an AnnotatedThing domain model primitive, with
 *  Annotations in-lined.
  *  
  * @constructor create a new AnnotatedThing
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotatedThingResource(resource: Resource, val annotations: Seq[AnnotationResource]) extends AnnotatedThing {

  def uri = resource.uri
  
  def title = resource.getFirst(DCTerms.title).map(_.stringValue)

  def description = resource.getFirst(DCTerms.description).map(_.stringValue)
  
}

/** Wraps an oa:Annotation RDF resource as an Annotation domain model primitive.
  *  
  * @constructor create a new AnnotationResource
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotationResource(resource: Resource) extends Annotation {

  def uri = resource.uri
  
  def hasBody: String = resource.getFirst(OA.hasBody).get.stringValue
  
  def hasTarget: String = resource.getFirst(OA.hasTarget).get.stringValue
  
  def motivatedBy: Option[String] = resource.getFirst(OA.motivatedBy).map(_.stringValue)
  
  def toponym: Option[String] = resource.getFirst(Pelagios.toponym).map(_.stringValue)
  
  def hasNext: Option[Neighbour] = None // TODO implement! 
  
}

