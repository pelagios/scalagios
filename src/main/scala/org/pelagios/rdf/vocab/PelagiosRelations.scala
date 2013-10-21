package org.pelagios.rdf.vocab

object PelagiosRelations extends BaseVocab("http://pelagios.github.io/vocab/relations#") {

  val attestsTo = createURI("attestsTo")
  
  val foundAt = createURI("foundAt")
  
  val locatedAt = createURI("locatedAt")
  
}