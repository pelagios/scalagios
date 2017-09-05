package org.pelagios.rdf.vocab

/** DCMI metadata terms **/
object DCTerms extends BaseVocab("http://purl.org/dc/terms/") {

  val bibliographicCitation = createURI("bibliographicCitation")
  
  val creator = createURI("creator")
  
  val created = createURI("created")
  
  val contributor = createURI("contributor")
  
  val coverage = createURI("coverage")

  val description = createURI("description")
  
  val identifier = createURI("identifier")
  
  val isPartOf = createURI("isPartOf")
  
  val isReferencedBy = createURI("isReferencedBy")
  
  val language = createURI("language")
  
  val license = createURI("license")
  
  val publisher = createURI("publisher")
  
  val source = createURI("source")
  
  val subject = createURI("subject")
  
  val temporal = createURI("temporal")
  
  val title = createURI("title")
  
  val typ = createURI("type")

}