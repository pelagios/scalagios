package org.pelagios.api

/** 'AnnotationTarget' model primitive.
  *  
  * AnnotationTarget is just a minimal interface with a single 
  * property - a URI.
  *   
  * @author Rainer Simon <rainer.simon@ait.ac.at> 
  */
trait AnnotationTarget {
  
  def uri: String
  
}