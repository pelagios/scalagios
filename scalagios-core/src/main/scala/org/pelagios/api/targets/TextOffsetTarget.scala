package org.pelagios.api.targets

import org.pelagios.api.{ AnnotatedThing, AnnotationTarget }

case class TextOffsetSelector(annotatedThing: AnnotatedThing, offset: Int, range: Int) extends AnnotationTarget