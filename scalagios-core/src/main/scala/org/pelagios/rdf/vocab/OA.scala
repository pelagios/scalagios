package org.pelagios.rdf.vocab

/** Open Annotation Data Model - http://www.openannotation.org/spec/core/ **/
object OA extends BaseVocab("http://www.w3.org/ns/openannotation/core/") {

  val Annotation = createURI("Annotation")
  
  val SpecificResource = createURI("SpecificResource")
  
  val annotatedBy = createURI("annotatedBy")
  
  val annotatedAt = createURI("annotatedAt")
  
  val hasBody = createURI("hasBody")
  
  val hasSelector = createURI("hasSelector")
  
  val hasSource = createURI("hasSource")
  
  val hasTarget = createURI("hasTarget")
    
  val motivatedBy = createURI("motivatedBy")
  
}