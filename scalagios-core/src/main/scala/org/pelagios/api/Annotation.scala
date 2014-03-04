package org.pelagios.api

import java.util.Date
import scala.collection.mutable.ListBuffer

/** 'Annotation' model entity.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Annotation {
  
  /** A URI for the annotation.
    *  
    * The URI should be in the namespace of the institution or project providing the
    * data, and should resolve to RDF. The easiest way to achieve this is to use a
    * hash URI, i.e. by appending a fragment identifier to the HTTP URI of the dump 
    * file. Examples:
    * 
    * http://example.com/pelagios.rdf#annotation
    * http://example.com/pelagios.rdf#annotations/01
    */
  def uri: String
  
  /** oa:hasTarget 
    *
    * Note: we currently restrict to exactly one target.
    */
  def hasTarget: AnnotationTarget

  /** Place reference expressed through oa:hasBody
    *
    * A Pelagios annotation relates (part of) an annotated item with a 
    * place. By convention, Pelagios requires that annotation bodies either
    * point to a URI that represents this place in a gazetteer; or that they
    * are textual bodies containing either the place name transcription (see 
    * method 'transcription'), or tags (see method 'tags'). The 'place' 
    * method exposes annotation bodies representing gazetteer references.
    * 
    * Note: It is possible to add multiple bodies that point to gazetteer 
    * URIs - but only in order to refer to the same place in different
    * gazetteers.
    */
  def place: Seq[String]
  
  /** Transcription expressed through oa:hasBody
    *  
    * A Pelagios annotation relates (part of) an annotated item with a 
    * place. By convention, Pelagios requires that annotation bodies either
    * point to a URI that represents this place in a gazetteer; or that 
    * they are textual bodies containing a place name transcription or a tag.
    * This method exposes annotation bodies representing the transcription.
    */
  def transcription: Option[Transcription]
  
  /** Tag expressed through oa:hasBody
    *  
    * A Pelagios annotation relates (part of) an annotated item with a 
    * place. By convention, Pelagios requires that annotation bodies either
    * point to a URI that represents this place in a gazetteer; or that
    * they are textual bodies containing a place name transcription or a tag.
    * This method exposes annotation bodies representing tags.
    */
  def tags: Seq[Tag]
    
  /** pelagios:relation
    * 
    * This method returns the <em>type</em> of the relation that this annotation 
    * establishes between (part of) an annotated item and the place (e.g. found at,
    * located at, attests to, etc.)
    */
  def relation: Option[Relation.Type]
  
  /** oa:annotatedBy
    *  
    * In Pelagios, we use this field to record who has produced the original 
    * (possibly physical/written/manual) annotations on the source document.
    * E.g. the historian that has transcribed a map into a toponym table, etc.     
    */
  def annotatedBy: Option[Agent]

  /** oa:annotatedAt 
    *
    * In Pelagios, this field captures the time when the original annotations on 
    * the source document were created (cf. oa:annotatedBy)
    */
  def annotatedAt: Option[Date]
  
  /** oa:serializedBy **/
  def serializedBy: Option[Agent]

  /** dcterms:creator
    *  
    * In contrast to oa:annotatedBy, this property captures who produced the digital
    * annotations (e.g. in the case when an existing toponym list was translated to
    * Pelagios annotations and/or mapped to gazetteer entries).
    *    
    * If the creator of the annotation is also responsible for the original annotations,
    * we recommend omitting oa:annotatedBy. 
    */
  def creator: Option[Agent]
  
  /** dcterms:created
    * 
    * The time when the digital annotations were produced.
    */
  def created: Option[Date]
    
  /** sequence: index
    *
    * The sequence index number, in case the ordering of the annotations
    * on the annotated thing is relevant.  
    */
  def index: Option[Int]
  
  /** sequence:distance
    *
    * Experimental feature  that records 'distance' information between
    * this annotation and the next in the sequence.
    */
  def distanceToNext: Option[Distance]
  
}

/** A default POJO-style implementation of Annotation. **/
private[api] class DefaultAnnotation(
    
  val uri: String,
   
  val hasTarget: AnnotationTarget,
  
  val place: Seq[String] = Seq.empty[String],
  
  val transcription: Option[Transcription] = None,
  
  val tags: Seq[Tag] = Seq.empty[Tag],
      
  val relation: Option[Relation.Type] = None,
  
  val annotatedBy: Option[Agent] = None,

  val annotatedAt: Option[Date] = None,
  
  val serializedBy: Option[Agent] = None,
  
  val creator: Option[Agent] = None,
  
  val created: Option[Date] = None,
  
  val index: Option[Int] = None,
  
  val distanceToNext: Option[Distance] = None
  
) extends Annotation {
  
  if (hasTarget.isInstanceOf[DefaultAnnotatedThing])
      hasTarget.asInstanceOf[DefaultAnnotatedThing].annotations.asInstanceOf[ListBuffer[Annotation]].append(this)
    else
      throw new RuntimeException("cannot mix different model impelementation types - requires instance of DefaultAnnotatedThing")

}

/** Companion object with a pimped apply method for generating DefaultAnnotation instances **/
object Annotation extends AbstractApiCompanion {
  
  def apply(uri: String,
   
            target: AnnotatedThing,
  
            place: ObjOrSeq[String] = new ObjOrSeq(Seq.empty[String]),
  
            transcription: ObjOrOption[Transcription] = new ObjOrOption(None),
            
            tags: ObjOrSeq[Tag] = new ObjOrSeq(Seq.empty[Tag]),
      
            relation: Relation.Type = null,
  
            annotatedBy: Agent = null,

            annotatedAt: Date = null,
            
            serializedBy: Agent = null,
  
            creator: Agent = null,
  
            created: Date = null,
            
            index: ObjOrOption[Int] = new ObjOrOption(None),
            
            distanceToNext: Distance = null): Annotation = {
      
    new DefaultAnnotation(uri, target, place.seq, transcription.option, tags.seq, relation, annotatedBy, annotatedAt,
                          serializedBy, creator, created, index.option, distanceToNext)
  }
 
}
