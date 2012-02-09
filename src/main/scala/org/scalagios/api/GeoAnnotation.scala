package org.scalagios.api

/**
 * Pelagios <em>GeoAnnotation</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait GeoAnnotation {
  
  def uri: String
  
  def body: String
  
  def target: String
  
  def isValid: Boolean = (uri != null && target != null && body != null) 

}

/**
 * A default (POJO-style) implementation of the Pelagios
 * <em>GeoAnnotation</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class DefaultGeoAnnotation(var uri: String) extends GeoAnnotation {

  var body: String = _
  
  var target: String = _
  
}