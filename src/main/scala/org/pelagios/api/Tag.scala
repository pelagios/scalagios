package org.pelagios.api

/** 'Tag' model primitive **/
trait Tag {
  
  def tag: String
  
}

/** A default POJO-style implementation of 'Tag' **/
private[api] class DefaultTag(val tag: String) extends Tag

/** Companion object for generating DefaultTag instances **/
object Tag {
  
  def apply(tag: String) = new DefaultTag(tag)
  
}
