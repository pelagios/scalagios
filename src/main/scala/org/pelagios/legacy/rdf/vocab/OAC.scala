package org.pelagios.legacy.rdf.vocab

import org.openrdf.model.impl.ValueFactoryImpl

object OAC {
  
  private val factory = ValueFactoryImpl.getInstance()
  
  val NAMESPACE = "http://www.openannotation.org/ns/"
  
  val Annotation = factory.createURI(NAMESPACE, "Annotation")
  
  val Target = factory.createURI(NAMESPACE, "Target")
  
  val hasBody = factory.createURI(NAMESPACE, "hasBody")
  
  val hasTarget = factory.createURI(NAMESPACE, "hasTarget")
  
}