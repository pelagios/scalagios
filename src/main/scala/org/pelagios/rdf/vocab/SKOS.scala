package org.pelagios.rdf.vocab

object SKOS extends BaseVocab {
  
  val NAMESPACE = "http://www.w3.org/2004/02/skos/core#"
    
  val label = factory.createURI(NAMESPACE, "label")
    
  val altLabel = factory.createURI(NAMESPACE, "altLabel")
  
  val closeMatch = factory.createURI(NAMESPACE, "closeMatch")

}