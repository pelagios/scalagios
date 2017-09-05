package org.pelagios.rdf.parser.annotation

import java.util.Date
import org.pelagios.api._
import org.pelagios.rdf.vocab._
import org.openrdf.model.URI
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.BNode
import java.text.SimpleDateFormat
import org.pelagios.api.annotation.Annotation
import org.pelagios.api.annotation.AnnotatedThing
import org.pelagios.api.annotation.AnnotationTarget
import org.pelagios.api.annotation.Relation
import org.pelagios.api.annotation.TranscriptionType
import org.pelagios.api.annotation.Transcription
import org.pelagios.api.annotation.Tag
import org.pelagios.api.PeriodOfTime
import org.pelagios.rdf.parser.Resource
import org.pelagios.rdf.parser.Resource
import org.pelagios.rdf.parser.ResourceCollector
import org.slf4j.LoggerFactory

/** An implementation of [[org.pelagios.rdf.parser.ResourceCollector]] to handle Pelagios data dump files.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class PelagiosDataParser extends ResourceCollector {   
  
  def data: Iterable[AnnotatedThing] = {
    val allAnnotations = resourcesOfType(OA.Annotation).map(new AnnotationResource(_))  
    
    // Resolve foaf:Agents
    val agents = resourcesOfType(FOAF.Organization).map(new AgentResource(_)).map(agent => (agent.uri.get, agent)).toMap

    // Resolve depiction resources
    val depictions = resourcesOfType(FOAF.Image, Seq(_.hasAnyPredicate(Seq(DCTerms.isReferencedBy))))
      // TODO include isReferencedBy as possible field in Image
      .map(resource => (resource.uri -> Image(resource.uri))).toMap
    
    // Resolve transcriptions
    val allTranscriptions = resourcesOfType(Pelagios.Transcription, Seq(_.hasAnyType(Seq(Pelagios.Toponym, Pelagios.Metonym, Pelagios.Ethnonym))))
    allAnnotations.foreach(annotation => {
      // Transcriptions per Annotation
      val rdfTranscriptions = annotation.resource.get(OA.hasBody).filter(_.isInstanceOf[BNode])
                             .map(bnode => allTranscriptions.find(_.uri.equals(bnode.stringValue)))
                             .filter(_.isDefined).map(_.get)
        
      
      if (rdfTranscriptions.size > 0) {
        // We only allow one transcription per annotation - so we'll discard additional ones, if any
        val transcription = rdfTranscriptions(0)
        val chars = transcription.getFirst(Content.chars).map(_.stringValue).getOrElse("[NONE]")
        val transcriptionType = transcription.getFirst(RDF.TYPE).map(uri => { 
            uri match {
              case Pelagios.Metonym => TranscriptionType.Metonym
              case Pelagios.Ethnonym => TranscriptionType.Ethnonym
              case _ => TranscriptionType.Toponym
            }
          }).getOrElse(TranscriptionType.Toponym)
        annotation.transcription = Some(Transcription(chars, transcriptionType))
      }
    })
    
    // Construct Work/Expression hierarchy
    val allAnnotatedThings = resourcesOfType(Pelagios.AnnotatedThing).map(new AnnotatedThingResource(_))
    allAnnotatedThings.foreach(thing => {
      val realizationOf = thing.resource.getFirst(DCTerms.isPartOf).map(_.stringValue)
      if (realizationOf.isDefined) {        
        val work = allAnnotatedThings.find(t => { t.uri.equals(realizationOf.get) })
        thing.isPartOf = work
        if (work.isDefined) {
          work.get.parts = thing +: work.get.parts
        }
      }
    })  
    
    // Link annotations and annotated things
    val annotationsPerThing = allAnnotations.toSeq.groupBy(_.resource.getFirst(OA.hasTarget).map(_.stringValue).getOrElse("_:empty"))
    allAnnotatedThings.foreach(thing => {
      val annotations = annotationsPerThing.get(thing.uri).getOrElse(Seq.empty[AnnotationResource])
      annotations.foreach(_.hasTarget = thing)
      thing.annotations = annotations.toSeq.sortWith((a, b) => { // Sort by index number if any
        if (a.index.isDefined && b.index.isDefined)
          a.index.get < b.index.get
        else if (a.index.isDefined)
          true
        else if (b.index.isDefined)
          false
        else
          false
      })
    })
    
    // Link annotations and authors
    allAnnotatedThings.foreach(thing => {
      thing.annotations.foreach(annotation => {
        val agentURI = annotation.resource.getFirst(OA.annotatedBy)
        annotation.creator = agentURI.map(uri => agents.get(uri.stringValue)).flatten
      })
    })
    
    // Filter out top-level things, i.e. those that are not expressions of something else
    allAnnotatedThings.filter(thing => thing.isPartOf.isEmpty)
  }
      
}

private[parser] class AgentResource(val resource: Resource) extends Agent {
    
  val name = resource.getFirst(FOAF.name).map(_.stringValue).get
  
  val uri = Some(resource.uri)
  
}

/** Wraps an oa:Annotation RDF resource as an Annotation domain model primitive.
  *  
  * @constructor create a new AnnotationResource
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotationResource(val resource: Resource) extends Annotation {
  
  private val DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd")
  
  val uri = resource.uri
  
  var hasTarget: AnnotationTarget = null // resource.getFirst(OA.hasTarget).map(_.stringValue).getOrElse("_:empty")
  
  val places = resource.get(OA.hasBody).filter(_.isInstanceOf[URI]).map(_.stringValue)
  
  var transcription: Option[Transcription] = None
  
  var tags: Seq[Tag] = Seq.empty[Tag]
  
  val relation: Option[Relation.Type] = 
    resource.getFirst(Pelagios.relation).map(uri => PelagiosRelations.fromURI(uri.stringValue))
    
  // TODO 
  def annotatedBy: Option[Agent] = None

  val annotatedAt: Option[Date] = resource.getFirst(OA.annotatedAt)
    .map(literal => DATE_FORMAT.parse(literal.stringValue))
    
  val serializedBy: Option[Agent] = None
  
  var creator: Option[Agent] = None
  
  // TODO
  val created: Option[Date] = None
  
  val index: Option[Int] = resource.getFirst(PelagiosSequence.index).map(_.stringValue.toInt)
  
}

/** Wraps a pelagios:AnnotatedThing RDF resource as an AnnotatedThing domain model primitive, with
 *  Annotations in-lined.
  *  
  * @constructor create a new AnnotatedThing
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotatedThingResource(val resource: Resource) extends AnnotatedThing {
    
  val uri = resource.uri
  
  val title = resource.getFirst(DCTerms.title).map(_.stringValue).getOrElse("[NO TITLE]") // 'NO TITLE' should never happen!
  
  var isPartOf: Option[AnnotatedThing] = None
  
  val identifier = resource.getFirst(DCTerms.identifier).map(_.stringValue)

  val description = resource.getFirst(DCTerms.description).map(_.stringValue)
  
  val homepage = resource.getFirst(FOAF.homepage).map(_.stringValue)
  
  val sources = resource.get(DCTerms.source).map(_.stringValue)
  
  val primaryTopicOf = resource.get(FOAF.primaryTopicOf).map(_.stringValue)
  
  val temporal: Option[PeriodOfTime] =
    try {
      resource.getFirst(DCTerms.temporal).map(literal => PeriodOfTime.fromString(literal.stringValue))
    } catch {
      case t: Throwable => {
        // Vast majority of resources will be fine, so no need to create a logger, unless needed
        val logger = LoggerFactory.getLogger(classOf[AnnotatedThingResource])
        logger.warn("Error parsing dcterms:temporal on " + uri)
        logger.warn(t.getMessage)
        None
      }
    }

  var creator: Option[Agent] = None

  // TODO    
  def contributors: Seq[Agent] = Seq.empty[Agent]
  
  def languages = resource.get(DCTerms.language).map(_.stringValue)
  
  def thumbnails = resource.get(FOAF.thumbnail).map(_.stringValue)
  
  def depictions = resource.get(FOAF.depiction).map(_.stringValue)
  
  def bibliographicCitations = resource.get(DCTerms.bibliographicCitation).map(_.stringValue)
  
  def subjects = resource.get(DCTerms.subject).map(_.stringValue)
  
  var annotations = Seq.empty[AnnotationResource]
  
  var parts = Seq.empty[AnnotatedThingResource]
  
}



