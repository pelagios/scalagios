package org.scalagios.rdf.validator

object Severity extends Enumeration {
  
  type Severity = Value
  
  val INFO, WARNING, ERROR = Value
  
}

import Severity._

class ValidationIssue(val severity: Severity, val message: String) {

}