package org.pelagios.rdf.parser

import org.openrdf.model.Statement
import org.openrdf.rio.helpers.RDFHandlerBase
import org.openrdf.model.vocabulary.RDF
import org.pelagios.rdf.vocab.LAWD

class TripleCounter extends RDFHandlerBase {

  var ctr: Long = 0
  
  override def handleStatement(s: Statement): Unit = {
    ctr += 1
  }

  def getTotal(): Long = ctr 
  
}

class PlaceCounter extends RDFHandlerBase {
  
  var ctr: Long = 0
  
  override def handleStatement(s: Statement): Unit = {
    if (s.getPredicate == RDF.TYPE && s.getObject == LAWD.Place)
      ctr += 1
  }
  
  def getTotal(): Long = ctr
  
}