package org.pelagios.api

import org.pelagios.api.selectors.Selector

/** 'AnnotationTarget' model primitive.
  *  
  * AnnotationTarget is just a minimal interface with a single 
  * property - a URI.
  *   
  * @author Rainer Simon <rainer.simon@ait.ac.at> 
  */
trait AnnotationTarget {
    
  def hasSource: Option[AnnotatedThing] = None
  
  def hasSelector: Option[Selector] = None 
  
}