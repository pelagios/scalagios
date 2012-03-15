package org.scalagios.api

/**
 * Pelagios <em>GeoAnnotation</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait GeoAnnotation {
  
  def uri: String
  
  def title: String
  
  def body: String
  
  def target: GeoAnnotationTarget
  
  def isValid: Boolean = (!uri.isEmpty() && target != null && !body.isEmpty()) 

}

/**
 * A default (POJO-style) implementation of the Pelagios
 * <em>GeoAnnotation</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class DefaultGeoAnnotation(var uri: String) extends GeoAnnotation {

  var title: String = _
  
  var body: String = _
  
  var target: GeoAnnotationTarget = _
  
}