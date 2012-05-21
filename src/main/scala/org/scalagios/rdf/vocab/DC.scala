package org.scalagios.rdf.vocab

object DC extends BaseVocab {
  
  val NAMESPACE = "http://purl.org/dc/elements/1.1/"

  val title = factory.createURI(NAMESPACE, "title")

}