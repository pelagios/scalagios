package org.scalagios.api

/**
 * Pelagios <em>GeoAnnotation</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait GeoAnnotation {
  
  /**
   * The GeoAnnotation's original source URI (mandatory)
   */
  def uri: String
  
  /**
   * The body URI (mandatory)
   */
  def body: String
  
  /**
   * The target (mandatory)
   */
  def target: GeoAnnotationTarget
  
  /**
   * The title
   */
  def title: String
  
  def isValid: Boolean = (!uri.isEmpty() && !body.isEmpty() && target != null && target.isValid) 

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