package org.pelagios.api

import org.pelagios.rdf.vocab.PelagiosRelations

object Relation extends Enumeration {
  
  type Type = Value
  
  val attestsTo = Value(PelagiosRelations.attestsTo.stringValue)
  
  val foundAt = Value(PelagiosRelations.foundAt.stringValue)
  
  val locatedAt = Value(PelagiosRelations.locatedAt.stringValue)

}