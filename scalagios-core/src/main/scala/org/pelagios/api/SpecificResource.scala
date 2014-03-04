package org.pelagios.api

import org.pelagios.api.selectors.Selector

case class SpecificResource(private val annotatedThing: AnnotatedThing, private val selector: Selector) extends AnnotationTarget {
  
  override val hasSource: Option[AnnotatedThing] = Some(annotatedThing)
  
  override val hasSelector: Option[Selector] = Some(selector)
  
}