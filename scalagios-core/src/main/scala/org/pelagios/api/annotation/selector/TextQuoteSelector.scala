package org.pelagios.api.annotation.selector

import org.pelagios.api.annotation.SpecificResource

case class TextQuoteSelector(exact: String, prefix: String, suffix: String) extends Selector