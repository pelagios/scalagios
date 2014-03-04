package org.pelagios.api.selectors

import org.pelagios.api.SpecificResource

case class TextQuoteSelector(exact: String, prefix: String, suffix: String) extends Selector