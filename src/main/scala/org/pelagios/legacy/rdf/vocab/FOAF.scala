package org.pelagios.legacy.rdf.vocab

object FOAF extends BaseVocab {
  
  val NAMESPACE = "http://xmlns.com/foaf/0.1/"
    
  val homepage = factory.createURI(NAMESPACE, "homepage")
  
  val thumbnail = factory.createURI(NAMESPACE, "thumbnail")

}