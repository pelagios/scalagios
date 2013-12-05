package org.pelagios.api

import org.pelagios.rdf.vocab.Pelagios

/** 'Transcription' model primitive.
  * 
  * @constructor create a new transcription
  * @param name the transcribed name
  * @param nameType the type of the name (Toponym, Metonym, etc.)
  * @param lang the language the name is in (optional)
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Transcription{
  
  def chars: String
  
  def nameType: TranscriptionType.Value
  
  def lang: Option[String]
  
  // TODO temporary hack! Offset needs to go into the target!
  def offset: Option[Int]
  
}

/** A default POJO-style implementation of 'Transcription' **/
private[api] class DefaultTranscription(val chars: String, val nameType: TranscriptionType.Value, val lang: Option[String], val offset: Option[Int]) 
  extends Transcription

/** Companion object with a pimped apply method for generating DefaultTranscription instances **/
object Transcription extends AbstractApiCompanion {
 
  def apply(chars: String, nameType: TranscriptionType.Value, lang: String = null, offset: Option[Int] = None) =
    new DefaultTranscription(chars, nameType, lang, offset)
    
}

/** Name types **/
object TranscriptionType extends Enumeration {
    
  val Toponym = Value("Toponym")
  
  val Metonym = Value("Metonym")
  
  val Ethnonym = Value("Ethnonym")
  
}