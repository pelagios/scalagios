package org.scalagios.rdf.parser

object Severity extends Enumeration {
  
  type Severity = Value
  
  val INFO, WARNING, ERROR = Value
  
}

import Severity._

case class ValidationIssue(val severity: Severity, val message: String) {

}