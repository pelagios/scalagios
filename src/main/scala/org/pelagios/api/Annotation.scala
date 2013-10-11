package org.pelagios.api

import java.util.Date

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
    * Note: we currently restrict to exactly one target, and require that it
    * points to an [[AnnotatedItem]].
    * 
    * TODO parts of [[AnnotatedItem]]s
    */
  def hasTarget: String

  /** Place references expressed through oa:hasBody
    *
    * Pelagios supports multiple annotation bodies (as defined in the OA spec), but
    * requires that annotation bodies either point to URIs that represent places
    * in a gazetteer, or that they are textual bodies representing toponyms. This 
    * method exposes annotation bodies of the former type (place references).
    */
  def places: Seq[String]
  
  /** Toponym expressed through oa:hasBody
    *  
    * Pelagios supports multiple annotation bodies (as defined in the OA spec), but
    * requires that annotation bodies either point to URIs that represent places
    * in a gazetteer, or that they are textual bodies representing toponyms, (i.e.
    * transcriptions of placenames as written in the source document). This
    * method exposes annotation bodies of the latter type (toponyms).
    */
  def toponym: Option[String]
  
  /** oa:motivatedBy  **/
  def motivatedBy: Option[String]
  
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
  
  /** pelagios:hasNeighbour and pelagios:hasNext
    * 
    * This Pelagios-specific property is used to record sequence (e.g. in 
    * an itinerary or book) and topology (e.g. on a map) of annotations.
    * See definition of the [[Neighbour]] model entity for additional 
    * information
    */
  def hasNeighbour: Seq[Neighbour]
  
}

/** A default POJO-style implementation of Annotation. **/
class DefaultAnnotation(val uri: String) extends Annotation {
 
  var hasTarget: String = ""
  
  var places: Seq[String] = Seq.empty[String]
  
  var toponym: Option[String] = None
    
  var motivatedBy: Option[String] = Some("geotagging")
  
  var annotatedBy: Option[Agent] = None

  var annotatedAt: Option[Date] = None
  
  var creator: Option[Agent] = None
  
  var created: Option[Date] = None
  
  var hasNeighbour: Seq[Neighbour] = Seq.empty[Neighbour]
  
}
