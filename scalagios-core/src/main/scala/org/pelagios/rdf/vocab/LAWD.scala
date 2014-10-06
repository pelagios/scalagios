package org.pelagios.rdf.vocab

object LAWD extends BaseVocab("http://lawd.info/ontology/") {
  
  val Place = createURI("Place")
  
  val hasAttestation = createURI("hasAttestation")
  
  val hasName = createURI("hasName")
  
  val primaryForm = createURI("primaryForm")

}