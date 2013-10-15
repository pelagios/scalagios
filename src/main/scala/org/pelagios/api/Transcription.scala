package org.pelagios.api

/** 'Transcription' model primitive.
  * 
  * @constructor create a new transcription
  * @param name the transcribed name
  * @param nameType the type of the name (Toponym, Metonym, etc.)
  * @param lang the language the name is in (optional)
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
case class Transcription(val name: String, val nameType: Transcription.Type, val lang: Option[String] = None)

/** Name types **/
object Transcription extends Enumeration {
  
  type Type = Value
    
  val Toponym = Value("Toponym")
  
  val Metonym = Value("Metonym")
  
  val Ethnonym = Value("Ethnonym")
    
}