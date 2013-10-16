package org.pelagios.rdf.parser

import java.util.Date
import org.pelagios.api._
import org.pelagios.rdf.vocab._
import org.openrdf.model.vocabulary.RDFS
import org.openrdf.model.URI
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.Literal
import org.openrdf.model.BNode
import org.pelagios.api.layout.{ Layout, Link }

/** An implementation of [[org.pelagios.rdf.parser.ResourceCollector]] to handle Pelagios data dump files.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class PelagiosDataParser extends ResourceCollector {   
  
  def data: Iterable[AnnotatedThing] = {
    val allAnnotations = resourcesOfType(OA.Annotation).map(new AnnotationResource(_))  
    
    /** Resolve transcriptions **/
    // TODO a bit hacky - clean up!
    val allTranscriptions = resourcesOfType(Pelagios.Transcription, Seq(_.hasAnyType(Seq(Pelagios.Toponym, Pelagios.Metonym, Pelagios.Ethnonym))))
    allAnnotations.foreach(annotation => {
      val rdfTranscriptions = annotation.resource.get(OA.hasBody).filter(_.isInstanceOf[BNode])
                             .map(bnode => allTranscriptions.find(_.uri.equals(bnode.stringValue)))
                             .filter(_.isDefined).map(_.get)
                             
      if (rdfTranscriptions.size > 0)
        annotation.transcription = Some(new Transcription(rdfTranscriptions(0).getFirst(RDFS.LABEL).map(_.stringValue).getOrElse("[NONE]"), Transcription.Toponym))
    })
    
    /** Construct Work/Expression hierarchy **/
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
    
    /** Convert link resources to Links, filtering out invalid URIs **/
    val allLinks = resourcesOfType(PelagiosLayout.Link, Seq(_.hasPredicate(PelagiosLayout.next))).map(new LinkResource(_))
    def toLinks(annotation: AnnotationResource, neighbourUris: Seq[String], directional: Boolean): Seq[LinkResource] = {
      neighbourUris.foldLeft(List.empty[LinkResource])((resultList, currentURI) => {
        val n = allLinks.find(_.resource.uri.equals(currentURI))
        if (n.isDefined) {
          val neighbourAnnotationURI = n.get.resource.getFirst(PelagiosLayout.next)
          if (neighbourAnnotationURI.isDefined) {
            val neighbourAnnotation = allAnnotations.find(annotation => annotation.uri.equals(neighbourAnnotationURI.get.stringValue))
            if (neighbourAnnotation.isDefined) {
              n.get.from = annotation
              n.get.to = neighbourAnnotation.get
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
    
    /** Create layout **/
    val links = allAnnotations.map(annotation => {
      val neighbourURIs = annotation.resource.get(PelagiosLayout.hasLink).map(_.stringValue)
      val nextURIs = annotation.resource.get(PelagiosLayout.hasNext).map(_.stringValue)
      toLinks(annotation, neighbourURIs, false) ++ toLinks(annotation, nextURIs, true)
    }).flatten
    
    allAnnotatedThings.foreach(thing => {
      val linksForThing = thing.annotations.map(annotation => links.filter(_.from.uri.equals(annotation.uri))).flatten
      if (linksForThing.size > 0)
        thing.layout = Some(Layout(linksForThing, thing))
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
  
  val place = resource.get(OA.hasBody).filter(_.isInstanceOf[URI]).map(_.stringValue)
  
  var transcription: Option[Transcription] = None
  
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
  
}

/** Wraps an RDF resource representing a Neighbour node into the corresponding domain model primitive.
  * 
  * @constructor
  * @param resource the RDF resource
  * @param annotation a reference to the annotation referenced by pelagios:neighbourURI
  * @param directional flag to distinguish between pelagios:hasNeighbour and pelagios:hasNext
  */
private[parser] class LinkResource(val resource: Resource) extends Link {
    
  var from: AnnotationResource = null
  
  var to: AnnotationResource = null
  
  var directional: Boolean = false
  
  val distance: Option[Double] = resource.getFirst(PelagiosLayout.distance).map(_.asInstanceOf[Literal].doubleValue)
   
  val unit: Option[String] = resource.getFirst(PelagiosLayout.unit).map(_.stringValue)
    
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
  
  var realizationOf: Option[AnnotatedThing] = None
  
  def identifier = resource.getFirst(DCTerms.identifier).map(_.stringValue)

  def description = resource.getFirst(DCTerms.description).map(_.stringValue)
  
  def homepage = resource.getFirst(FOAF.homepage).map(_.stringValue)
  
  def sources = resource.get(DCTerms.source).map(_.stringValue)
  
  def primaryTopicOf = resource.get(FOAF.primaryTopicOf).map(_.stringValue)
  
  // TODO
  def temporal: Option[PeriodOfTime] = None

  // TODO 
  def creator: Option[Agent] = None

  // TODO    
  def contributors: Seq[Agent] = Seq.empty[Agent]
  
  def languages = resource.get(DCTerms.language).map(_.stringValue)
  
  def thumbnails = resource.get(FOAF.thumbnail).map(_.stringValue)
  
  def depictions = resource.get(FOAF.depiction).map(_.stringValue)
  
  def bibliographicCitations = resource.get(DCTerms.bibliographicCitation).map(_.stringValue)
  
  def subjects = resource.get(DCTerms.subject).map(_.stringValue)
  
  var annotations = Seq.empty[AnnotationResource]
  
  var expressions = Seq.empty[AnnotatedThingResource]
  
  var layout: Option[Layout] = None
  
}



