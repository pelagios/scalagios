package org.pelagios.api

/** 'Tag' model primitive **/
trait Tag {
  
  def chars: String
  
}

/** A default POJO-style implementation of 'Tag' **/
private[api] class DefaultTag(val chars: String) extends Tag

/** Companion object for generating DefaultTag instances **/
object Tag {
  
  def apply(chars: String) = new DefaultTag(chars)
  
}
