package org.scalagios.rdf.parser

import org.scalagios.rdf.validator.ValidationIssue

private[parser] trait HasValidation {

  var issues = List[ValidationIssue]()
  
}