package org.pelagios.api

/** Pelagios 'Label' model primitive.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait PlainLiteral {
  
  /** String label **/
  def chars: String
  
  /** ISO language code **/
  def lang: Option[String]
  
}

/** A default POJO-style implementation of 'Label' **/
private[api] class DefaultLabel(val chars: String, val lang: Option[String]) extends PlainLiteral

/** Companion object for generating DefaultLabel instances **/
object PlainLiteral {
  
  def apply(label: String, lang: Option[String] = None) = new DefaultLabel(label, lang)
  
}
