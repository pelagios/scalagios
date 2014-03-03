package org.pelagios.api.targets

import org.pelagios.api.{ AnnotationTarget, AnnotatedThing }

case class TextQuoteTarget(annotatedThing: AnnotatedThing, exact: String, prefix: String, suffix: String) extends AnnotationTarget