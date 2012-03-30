package org.scalagios.api

/**
 * Pelagios <em>GeoAnnotationTarget</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait GeoAnnotationTarget {
  
  def uri: String
  
  def title: String

  def isValid: Boolean = (uri != null)
  
}

/**
 * A default (POJO-style) implementation of the Pelagios
 * <em>GeoAnnotationTarget</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class DefaultGeoAnnotationTarget(var uri: String) extends GeoAnnotationTarget {
  
  var title: String = _
  
}
