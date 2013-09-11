package org.pelagios.rdf.parser

import org.openrdf.model.Statement
import org.openrdf.rio.helpers.RDFHandlerBase

class PelagiosDumpCollector extends RDFHandlerBase {
  
  override def handleStatement(statement: Statement): Unit = {
    println(statement)
  }

}