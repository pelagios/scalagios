package org.pelagios.api

trait AnnotatedThing {
  
  def uri: String
  
  def title: Option[String]
  
  def description: Option[String]

}

case class DefaultAnnotatedThing(var uri: String) extends AnnotatedThing {
  
  var title: Option[String] = None
  
  var description: Option[String] = None
  
}