package org.pelagios.rdf.vocab

/** Open Annotation Data Model - http://www.openannotation.org/spec/core/ **/
object OA extends BaseVocab("http://www.w3.org/ns/oa#") {

  val Annotation = createURI("Annotation")
  
  val hasBody = createURI("hasBody")
  
  val hasTarget = createURI("hasTarget")
    
  val motivatedBy = createURI("motivatedBy")
  
}