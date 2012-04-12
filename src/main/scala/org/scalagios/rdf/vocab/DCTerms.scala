package org.scalagios.rdf.vocab

object DCTerms extends BaseVocab {
  
  val NAMESPACE = "http://purl.org/dc/terms/"

  val coverage = factory.createURI(NAMESPACE, "coverage")

  val description = factory.createURI(NAMESPACE, "description")
  
  val license = factory.createURI(NAMESPACE, "license")
  
  val source = factory.createURI(NAMESPACE, "source")
  
  val title = factory.createURI(NAMESPACE, "title")

}