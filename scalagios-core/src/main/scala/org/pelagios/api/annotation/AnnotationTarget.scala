package org.pelagios.api.annotation

import org.pelagios.api.annotation.selector.Selector

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

/** SpecificResource provides a generic implementation of a selector-based target **/  
case class SpecificResource(private val annotatedThing: AnnotatedThing, private val selector: Selector) extends AnnotationTarget {
  
  override val hasSource: Option[AnnotatedThing] = Some(annotatedThing)
  
  override val hasSelector: Option[Selector] = Some(selector)
  
}