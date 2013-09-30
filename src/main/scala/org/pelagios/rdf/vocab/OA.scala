package org.pelagios.rdf.vocab

object OA extends BaseVocab {
  
  val NAMESPACE = "http://www.w3.org/ns/oa#"

  val Annotation = factory.createURI(NAMESPACE, "Annotation")
  
  val hasBody = factory.createURI(NAMESPACE, "hasBody")
  
  val hasTarget = factory.createURI(NAMESPACE, "hasTarget")
    
  val motivatedBy = factory.createURI(NAMESPACE, "motivatedBy")
  
}