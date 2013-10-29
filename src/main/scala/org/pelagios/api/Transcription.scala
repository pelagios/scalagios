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
  
}

/** A default POJO-style implementation of 'Transcription' **/
private[api] class DefaultTranscription(val chars: String, val nameType: TranscriptionType.Value, val lang: Option[String]) 
  extends Transcription

/** Companion object with a pimped apply method for generating DefaultTranscription instances **/
object Transcription extends AbstractApiCompanion {
 
  def apply(chars: String, nameType: TranscriptionType.Value, lang: String = null) =
    new DefaultTranscription(chars, nameType, lang)
    
}

/** Name types **/
object TranscriptionType extends Enumeration {
    
  val Toponym = Value("Toponym")
  
  val Metonym = Value("Metonym")
  
  val Ethnonym = Value("Ethnonym")
  
}