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
   * The body (mandatory)
   */
  def body: String
  
  /**
   * The target (mandatory)
   */
  def target: GeoAnnotationTarget
  
  /**
   * The title
   */
  def title: Option[String]
  
  /**
   * Utility method that checks if all mandatory properties are set
   */
  def isValid: Boolean = (!uri.isEmpty() && body!=null && target != null && target.isValid) 

}

/**
 * A default (POJO-style) implementation of the Pelagios
 * <em>GeoAnnotation</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class DefaultGeoAnnotation(var uri: String) extends GeoAnnotation {
  
  var body: String = _
  
  var target: GeoAnnotationTarget = _
  
  var title: Option[String] = None
  
}