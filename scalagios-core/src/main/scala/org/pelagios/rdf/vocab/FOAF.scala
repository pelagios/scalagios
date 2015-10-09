package org.pelagios.rdf.vocab

/** Fried-of-a-Friend vocabulary **/
object FOAF extends BaseVocab("http://xmlns.com/foaf/0.1/") {
  
  val Agent = createURI("Agent")
  
  val Image = createURI("Image")
  
  val Organization = createURI("Organization")
  
  val depiction = createURI("depiction")
  
  val name = createURI("name")
  
  val homepage = createURI("homepage")
  
  val thumbnail = createURI("thumbnail")
  
  val primaryTopicOf = createURI("primaryTopicOf")

}