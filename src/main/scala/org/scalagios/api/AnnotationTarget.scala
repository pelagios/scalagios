package org.scalagios.api

/**
 * Pelagios <em>AnnotationTarget</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait AnnotationTarget {
  
  def uri: String
  
  def title: String

}

/**
 * A default (POJO-style) implementation of the Pelagios
 * <em>AnnotationTarget</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class DefaultAnnotationTarget(var uri: String) extends AnnotationTarget {
  
  var title: String = _
  
}
