package org.pelagios.rdf.vocab

import org.pelagios.api.annotation.Relation

object PelagiosRelations extends BaseVocab("http://pelagios.github.io/vocab/relations#") {

  val attestsTo = createURI("attestsTo")
  
  val foundAt = createURI("foundAt")
  
  val locatedAt = createURI("locatedAt")
  
  def fromURI(uri: String): Relation.Value = uri match {
    case s if s.equals(attestsTo.stringValue) => Relation.attestsTo
    case s if s.equals(foundAt.stringValue) => Relation.foundAt
    case s if s.equals(locatedAt.stringValue) => Relation.locatedAt
  }
  
}