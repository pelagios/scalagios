package org.scalagios.rdf.vocab

object OAC extends BaseVocab {
  
  val NAMESPACE = "http://www.openannotation.org/ns/"
  
  val Annotation = factory.createURI(NAMESPACE, "Annotation")
  
  val Target = factory.createURI(NAMESPACE, "Target")
  
  val hasBody = factory.createURI(NAMESPACE, "hasBody")
  
  val hasTarget = factory.createURI(NAMESPACE, "hasTarget")
  
}