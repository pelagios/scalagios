package org.scalagios.api

/**
 * Pelagios <em>GeoAnnotationTarget</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait GeoAnnotationTarget {
  
  /**
   * The GeoAnnotationTarget URI (mandatory)
   */
  def uri: String
  
  /**
   * The GeoAnnotationTarget title 
   */
  def title: Option[String]
  
  /**
   * The thumbnail image
   */
  def thumbnail: Option[String]
  
  /**
   * Utility method that checks if all mandatory properties are set
   */
  def isValid: Boolean = (uri != null)
  
}

/**
 * A default (POJO-style) implementation of the Pelagios
 * <em>GeoAnnotationTarget</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class DefaultGeoAnnotationTarget(var uri: String) extends GeoAnnotationTarget {
  
  var title: Option[String] = None
  
  var thumbnail: Option[String] = None
  
}
