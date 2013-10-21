package org.pelagios.api

/** Pelagios 'Label' model primitive.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Label {
  
  /** String label **/
  def label: String
  
  /** ISO language code **/
  def lang: Option[String]
  
}

/** A default POJO-style implementation of 'Label' **/
private[api] class DefaultLabel(val label: String, val lang: Option[String]) extends Label

/** Companion object for generating DefaultLabel instances **/
object Label {
  
  def apply(label: String, lang: Option[String] = None) = new DefaultLabel(label, lang)
  
}
