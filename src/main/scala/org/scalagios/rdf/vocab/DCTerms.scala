package org.scalagios.rdf.vocab

object DCTerms extends BaseVocab {
  
  val NAMESPACE = "http://purl.org/dc/terms/"
    
  val title = factory.createURI(NAMESPACE, "title")

  val description = factory.createURI(NAMESPACE, "description")
  
  val license = factory.createURI(NAMESPACE, "license")
  
  val coverage = factory.createURI(NAMESPACE, "coverage")

}