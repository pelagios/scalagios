package org.pelagios.legacy.rdf.vocab

import org.openrdf.model.impl.ValueFactoryImpl

object SKOS {
  
  private val factory = ValueFactoryImpl.getInstance()
  
  val NAMESPACE = "http://www.w3.org/2004/02/skos/core#"
  
  val altLabel = factory.createURI(NAMESPACE, "altLabel")

}