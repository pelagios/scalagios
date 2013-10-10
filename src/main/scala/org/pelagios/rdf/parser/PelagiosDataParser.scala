package org.pelagios.rdf.parser

import java.util.Date
import org.pelagios.api._
import org.pelagios.rdf.vocab._
import org.openrdf.model.vocabulary.RDFS
import org.openrdf.model.URI
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.Literal

/** An implementation of [[org.pelagios.rdf.parser.ResourceCollector]] to handle Pelagios data dump files.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class PelagiosDataParser extends ResourceCollector {   
  
  def data: Seq[AnnotatedThing] = {
    val typedResources = groupByType(
      // We're looking for AnnotatedThings, Annotations, and Neighbours
      Seq(Pelagios.AnnotatedThing, OA.Annotation, Pelagios.Neighbour),
        
      // Identify resources that have a neighbourURI as Neighbours
      Seq(
        (resource => if (resource.hasPredicate(Pelagios.hasNeighbour)) Some(Pelagios.Neighbour) else None)
    ))
    
    // Step 1: wrap annotations
    val allAnnotations = typedResources.get(OA.Annotation).getOrElse(Map.empty[String, Resource]).mapValues(new AnnotationResource(_))
    
    // Step 2: construct the neighbourhood network
    val allNeighbours = typedResources.get(Pelagios.Neighbour).getOrElse(Map.empty[String, Resource])
    
    def toNeighbours(uris: Seq[String], directional: Boolean): Seq[NeighbourResource] = {
      uris.foldLeft(List.empty[NeighbourResource])((resultList, currentURI) => {
        val n = allNeighbours.get(currentURI)
        if (n.isDefined) {
          val neighbourAnnotationURI = n.get.getFirst(Pelagios.neighbourURI)
          if (neighbourAnnotationURI.isDefined) {
            val neighbourAnnotation = allAnnotations.get(neighbourAnnotationURI.get.stringValue)
            if (neighbourAnnotation.isDefined)
              new NeighbourResource(n.get, directional,neighbourAnnotation.get) :: resultList
            else
              resultList
          } else {
            resultList
          }
        } else {
          resultList
        }
      })
    }
    
    // Step 3: attach annotations to their target things
    
    allAnnotations.foreach{ case (uri, annotation) => {
      val neighbourURIs = annotation.resource.get(Pelagios.hasNeighbour).map(_.stringValue)
      val nextURIs = annotation.resource.get(Pelagios.hasNext).map(_.stringValue)
      val neighbours = toNeighbours(neighbourURIs, false) ++ toNeighbours(nextURIs, true)
      annotation.setNeighbours(neighbours)
    }}
    
    null
  }
      
}

/** Wraps an oa:Annotation RDF resource as an Annotation domain model primitive.
  *  
  * @constructor create a new AnnotationResource
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotationResource(val resource: Resource) extends Annotation {
  
  private var _neighbours: Seq[NeighbourResource] = Seq.empty[NeighbourResource]
  
  def setNeighbours(n: Seq[NeighbourResource]) = { _neighbours = n } 
  
  val uri = resource.uri
  
  val hasBody = resource.get(OA.hasBody).map(_.stringValue)
  
  val hasTarget = resource.getFirst(OA.hasTarget).map(_.stringValue).getOrElse("_:empty") // '_:empty' should never happen!
  
  val motivatedBy: Option[String] = Some(resource.getFirst(OA.motivatedBy).map(_.stringValue).getOrElse("geotagging")) // Default to geotagging
  
  // TODO 
  def annotatedBy: Option[Agent] = None

  // TODO
  def annotatedAt: Option[Date] = None
  
  // TODO
  def creator: Option[Agent] = None
  
  // TODO
  def created: Option[Date] = None
  
  val toponym: Option[String] = resource.getFirst(Pelagios.toponym).map(_.stringValue)
  
  def hasNeighbour: Seq[Neighbour] = _neighbours
  
}

/** Wraps an RDF resource representing a Neighbour node into the corresponding domain model primitive.
  * 
  * @constructor
  * @param resource the RDF resource
  * @param annotation a reference to the annotation referenced by pelagios:neighbourURI
  * @param directional flag to distinguish between pelagios:hasNeighbour and pelagios:hasNext
  */
private[parser] class NeighbourResource(val resource: Resource, val directional: Boolean, val annotation: AnnotationResource) extends Neighbour {
    
  val distance: Option[Double] = resource.getFirst(Pelagios.neighbourDistance).map(_.asInstanceOf[Literal].doubleValue)
   
  val unit: Option[String] = resource.getFirst(Pelagios.distanceUnit).map(_.stringValue)
    
}

/** Wraps a pelagios:AnnotatedThing RDF resource as an AnnotatedThing domain model primitive, with
 *  Annotations in-lined.
  *  
  * @constructor create a new AnnotatedThing
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotatedThingResource(resource: Resource, val expressions: Seq[AnnotatedThingResource], val annotations: Seq[AnnotationResource]) extends AnnotatedThing {

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
  
  // TODO
  def realizationOf = None
  
}



