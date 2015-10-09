package org.pelagios.api

trait Image {
  
  def uri: String
  
  def title: Option[String]
  
  def license: Option[String]
  
}

private[api] class DefaultImage(val uri: String, val title: Option[String], val license: Option[String]) extends Image

object Image {
  
  def apply(uri: String, title: Option[String] = None, license: Option[String] = None)
    = new DefaultImage(uri, None, None)
  
}