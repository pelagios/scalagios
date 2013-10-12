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
  
  def data: Iterable[AnnotatedThing] = {
    val allAnnotations = resourcesOfType(OA.Annotation).map(new AnnotationResource(_))    
    val allNeighbours = resourcesOfType(Pelagios.Neighbour, Seq(_.hasPredicate(Pelagios.neighbourURI))).map(new NeighbourResource(_))
    
    /** Converts neighbour resources to neighbours, filtering out invalid URIs **/
    def toNeighbours(uris: Seq[String], directional: Boolean): Seq[NeighbourResource] = {
      uris.foldLeft(List.empty[NeighbourResource])((resultList, currentURI) => {
        val n = allNeighbours.find(_.resource.uri.equals(currentURI))
        if (n.isDefined) {
          val neighbourAnnotationURI = n.get.resource.getFirst(Pelagios.neighbourURI)
          if (neighbourAnnotationURI.isDefined) {
            val neighbourAnnotation = allAnnotations.find(annotation => annotation.uri.equals(neighbourAnnotationURI.get.stringValue))
            if (neighbourAnnotation.isDefined) {
              n.get.annotation = neighbourAnnotation.get
              n.get.directional = directional
              n.get :: resultList
            } else {
              resultList
            }
          } else {
            resultList
          }
        } else {
          resultList
        }
      })
    }
    
    /** Creates neighbourhood links **/
    allAnnotations.foreach(annotation => {
      val neighbourURIs = annotation.resource.get(Pelagios.hasNeighbour).map(_.stringValue)
      val nextURIs = annotation.resource.get(Pelagios.hasNext).map(_.stringValue)
      val neighbours = toNeighbours(neighbourURIs, false) ++ toNeighbours(nextURIs, true)
      annotation.hasNeighbour = neighbours
    })
    
    /** Constructs Work/Expression hierarchy **/
    val allAnnotatedThings = resourcesOfType(Pelagios.AnnotatedThing).map(new AnnotatedThingResource(_))
    allAnnotatedThings.foreach(thing => {
      val realizationOf = thing.resource.getFirst(FRBR.realizationOf).map(_.stringValue)
      if (realizationOf.isDefined) {        
        val work = allAnnotatedThings.find(t => { t.uri.equals(realizationOf.get) })
        thing.realizationOf = work
        if (work.isDefined) {
          work.get.expressions = thing +: work.get.expressions
        }
      }
    })
    
    /** Link annotations and annotated things **/
    val annotationsPerThing = allAnnotations.groupBy(_.hasTarget)
    allAnnotatedThings.foreach(thing => {
      val annotations = annotationsPerThing.get(thing.uri).getOrElse(Seq.empty[AnnotationResource])
      thing.annotations = annotations.toSeq
    })
    
    /** Filters out top-level things, i.e. those that are not expressions of something else **/
    allAnnotatedThings.filter(thing => thing.realizationOf.isEmpty)
  }
      
}

/** Wraps an oa:Annotation RDF resource as an Annotation domain model primitive.
  *  
  * @constructor create a new AnnotationResource
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotationResource(val resource: Resource) extends Annotation {
  
  val uri = resource.uri
  
  val hasTarget = resource.getFirst(OA.hasTarget).map(_.stringValue).getOrElse("_:empty") // '_:empty' should never happen!
  
  val place = resource.get(OA.hasBody).map(_.stringValue)
  
  // TODO
  val transcription: Option[Transcription] = resource.getFirst(Pelagios.toponym)
    .map(value => new Transcription(value.stringValue, Transcription.Toponym))
  
  // TODO
  def relation: Option[Relation.Type] = None
    
  // TODO 
  def annotatedBy: Option[Agent] = None

  // TODO
  def annotatedAt: Option[Date] = None
  
  // TODO
  def creator: Option[Agent] = None
  
  // TODO
  def created: Option[Date] = None
  
  var hasNeighbour: Seq[Neighbour] = Seq.empty[NeighbourResource]
  
}

/** Wraps an RDF resource representing a Neighbour node into the corresponding domain model primitive.
  * 
  * @constructor
  * @param resource the RDF resource
  * @param annotation a reference to the annotation referenced by pelagios:neighbourURI
  * @param directional flag to distinguish between pelagios:hasNeighbour and pelagios:hasNext
  */
private[parser] class NeighbourResource(val resource: Resource) extends Neighbour {
    
  var annotation: AnnotationResource = null
  
  var directional: Boolean = false
  
  val distance: Option[Double] = resource.getFirst(Pelagios.neighbourDistance).map(_.asInstanceOf[Literal].doubleValue)
   
  val unit: Option[String] = resource.getFirst(Pelagios.distanceUnit).map(_.stringValue)
    
}

/** Wraps a pelagios:AnnotatedThing RDF resource as an AnnotatedThing domain model primitive, with
 *  Annotations in-lined.
  *  
  * @constructor create a new AnnotatedThing
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotatedThingResource(val resource: Resource) extends AnnotatedThing {
    
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
  
  var realizationOf: Option[AnnotatedThing] = None
  
  var annotations = Seq.empty[AnnotationResource]
  
  var expressions = Seq.empty[AnnotatedThingResource]
  
}



