package org.pelagios.api

trait Image {
  
  def uri: String
  
  def title: Option[String]
  
  def license: Option[String]
  
  def iiifEndpoint: Option[String]
  
}

private[api] class DefaultImage(val uri: String, val title: Option[String], val license: Option[String], val iiifEndpoint: Option[String]) extends Image

object Image {
  
  def apply(uri: String, title: Option[String] = None, license: Option[String] = None, iiifUrl: Option[String] = None) = {
    val validIiifUrl = iiifUrl match {
      case Some(url) if url.endsWith("info.json") => Some(url)
      case _ => None
    }
      
    new DefaultImage(uri, None, None, validIiifUrl)
  }
  
}