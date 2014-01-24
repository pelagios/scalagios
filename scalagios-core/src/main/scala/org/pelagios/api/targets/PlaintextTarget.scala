package org.pelagios.api.targets

import org.pelagios.api.{AnnotatedThing, AnnotationTarget }

case class PlaintextTarget(annotatedThing: AnnotatedThing, characterOffset: Option[Int]) extends AnnotationTarget {
  
  val uri = annotatedThing.uri + characterOffset.map(offset => "#offset=" + offset).getOrElse("")
  
}