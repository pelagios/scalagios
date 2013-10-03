package org.pelagios.rdf.vocab

/** DCMI metadata terms **/
object DCTerms extends BaseVocab("http://purl.org/dc/terms/") {

  val coverage = createURI("coverage")

  val description = createURI("description")
  
  val license = createURI("license")
  
  val title = createURI("title")

}