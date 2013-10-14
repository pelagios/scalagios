package org.pelagios.api

import java.util.Date
import scala.collection.mutable.ListBuffer

/** 'AnnotatedThing' model entity.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait AnnotatedThing {
  
  /** A URI for the annotated thing.
    *  
    * The URI should be in the namespace of the institution or project providing the
    * data, and should resolve to RDF. The easiest way to achieve this is to use a
    * hash URI, i.e. by appending a fragment identifier to the HTTP URI of the dump 
    * file. Examples:
    * 
    * http://example.com/pelagios.rdf#item  
    * http://example.com/pelagios.rdf#items/01
    */
  def uri: String
  
  /** dcterms:title **/
  def title: String
  
  /** dcterms:identifier
    *  
    * An unambiguous reference for the annotated thing. We recommend
    * the use of a Wikidata URI!
    */ 
  def identifier: Option[String]

  /** dcterms:description **/
  def description: Option[String]
  
  /** dcterms:source
    *
    * According to the DCMI definition: "a related resource from which the described 
    * resource is derived". In the Pelagios context, use this field to provide
    * references to URIs from where digital source material was obtained (e.g.
    * a Web page with toponym lists, etc.)   
    */
  def sources: Seq[String]
  
  /** dcterms:temporal
    * 
    * According to the DCMI definition the "temporal coverage" or "temporal
    * characteristics of the resource". We recommend using the DCMI
    * Period Encoding Scheme: http://dublincore.org/documents/dcmi-period/
    */
  def temporal: Option[PeriodOfTime]
  
  /** dcterms:creator
    *
    * According to the DCMI definition "an entity primarily responsible for making the 
    * resource".
    */
  def creator: Option[Agent]
  
  /** dcterms:contributor
    * 
    * According to the DCMI definition "an entity responsible for making contributions to the
    * resource".
    */
  def contributors: Seq[Agent]
  
  /** dcterms:language 
    *
    * Use ISO 639-2 language codes - see http://en.wikipedia.org/wiki/List_of_ISO_639-2_codes
    */
  def languages: Seq[String]
  
  /** foaf:homepage **/
  def homepage: Option[String]
  
  /** foaf:thumbnails **/
  def thumbnails: Seq[String]
  
  /** dcterms:biblographicCitation
    *  
    * A (list of) bibliographic citation(s) in free text format.    
    */
  def bibliographicCitations: Seq[String]
  
  /** dcterms:subjects 
    * 
    * According to the DCMI definition: "the topic of the resource"
    */
  def subjects: Seq[String]  
    
  /** rdfs:seeAlso pointing to additional, related information.
    *  
    * According to the original definition, rdfs:seeAlso is "used to
    * indicate a resource that might provide additional information
    * about the subject resource". Although usually frowned upon as
    * a "vague" relationship, I think it makes sense to use it to
    * point to online content that is related, but not necessarily
    * scholarly in nature (e.g. Wikipedia pages)   
    */
  def seeAlso: Seq[String]
  
  /** frbr:realizationOf
    *  
    * An annotated thing may be a "Work" (such as "The Vicarello Beakers") or
    * an "Expression" (such as "the fourth Vicarello Beaker, discovered in 1863"). 
    * To quote the FRBR definition, a Work is "an abstract notion of an 
    * artistic or intellectual creation", whereas an Expression is "a realization
    * of a single work usually in a physical form."
    *  
    * http://en.wikipedia.org/wiki/Functional_Requirements_for_Bibliographic_Records
    * http://vocab.org/frbr/core.html
    * 
    * If the annotated thing represents a Expression, frbr:realizationOf links
    * to the work it realizes.
    */
  def realizationOf: Option[AnnotatedThing]  
 
  /** Expressions (as defined by through frbr:realizationOf)
    *   
    * If the annotated thing represents a Work, this method returns the expressions
    * linked to it.
    */ 
  def expressions: Seq[AnnotatedThing]
  
  /** The annotations on the annotated thing (if any). **/
  def annotations: Seq[Annotation]
  
}

/** A default POJO-style implementation of AnnotatedThing. **/
class DefaultAnnotatedThing(val uri: String, val title: String) extends AnnotatedThing {
  
  var identifier: Option[String] = None
  
  var description: Option[String] = None
  
  var sources: Seq[String] = Seq.empty[String]
  
  var temporal: Option[PeriodOfTime] = None

  var creator: Option[Agent] = None
  
  var contributors: Seq[Agent] = Seq.empty[Agent]
  
  var languages: Seq[String] = Seq.empty[String]
  
  var homepage: Option[String] = None
  
  var thumbnails: Seq[String] = Seq.empty[String]
  
  var bibliographicCitations: Seq[String] = Seq.empty[String]
  
  var subjects: Seq[String] = Seq.empty[String]
  
  var seeAlso: Seq[String] = Seq.empty[String]

  var realizationOf: Option[AnnotatedThing] = None
  
  val expressions: ListBuffer[AnnotatedThing] =  ListBuffer.empty[AnnotatedThing]
  
  val annotations: ListBuffer[Annotation] = ListBuffer.empty[Annotation]
  
  def addExpression(thing: DefaultAnnotatedThing) = {
    thing.realizationOf = Some(this)
    expressions.append(thing)
  }
  
  def addAnnotation(annotation: DefaultAnnotation) = {
    annotation.hasTarget = this.uri
    annotations.append(annotation)
  }
  
}
