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
    * http://example.com/pelagios.rdf#annotaions/01
    */
  def uri: String

  /** oa:hasBody
    *
    * Pelagios supports multiple annotation bodies (as defined in the OA spec), but
    * requires that bodies point to URIs from (a) gazetteer(s) participating in the
    * Pelagios network.
    * 
    * It is valid for annotations to have no body (or no body that points to a known
    * gazetteer URI). In this case, the annotation should provide a pelagios:toponym
    * property, and will be treated as a toponym transcription without explict gazetteer
    * mapping.
    */
  def hasBody: Seq[String]
  
  /** oa:hasTarget 
    *
    * Note: we currently restrict to exactly one target, and require that it
    * points to an [[AnnotatedItem]].
    * 
    * TODO parts of [[AnnotatedItem]]s
    */
  def hasTarget: String
  
  /** oa:motivatedBy  **/
  def motivatedBy: Option[String]
  
  /** oa:annotatedBy
    *  
    * In Pelagios, we explicitly use this field to record who has produced
    * the original (possibly "physical"/manual) annotations on the source document.
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

  /** pelagios:toponym
    *
    * If applicable, this Pelagios-specific property records the
    * original toponym used in the annotated source document.   
    */
  def toponym: Option[String]
  
  /**
   * pelagios:hasNext
   * 
   * This Pelagios-specific property is used to record sequence (e.g. in 
   * an itinerary or book) and topology (e.g. on a map) of annotations.
   * See definition of the [[Neighbour]] model entity for additional 
   * information
   */
  def hasNext: Option[Neighbour]
  
}

/** A default POJO-style implementation of Annotation. **/
class DefaultAnnotation(val uri: String) extends Annotation {
  
  var hasBody: Seq[String] = Seq.empty[String]
    
  var hasTarget: String = ""
    
  var motivatedBy: Option[String] = Some("geotagging")
  
  var annotatedBy: Option[Agent] = None

  var annotatedAt: Option[Date] = None
  
  var creator: Option[Agent] = None
  
  var created: Option[Date] = None
  
  var toponym: Option[String] = None
  
  var hasNext: Option[Neighbour] = None
  
}
