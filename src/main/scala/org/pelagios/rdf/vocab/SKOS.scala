package org.pelagios.rdf.vocab

/** Simple Knowledge Organization System - http://www.w3.org/2004/02/skos/ **/
object SKOS extends BaseVocab("http://www.w3.org/2004/02/skos/core#") {
    
  val altLabel = createURI("altLabel")
  
  val closeMatch = createURI("closeMatch")
  
  val label = createURI("label")

}