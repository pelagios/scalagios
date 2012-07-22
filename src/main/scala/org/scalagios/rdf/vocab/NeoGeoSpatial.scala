package org.scalagios.rdf.vocab

object NeoGeoSpatial extends BaseVocab {
  
  val NAMESPACE = "http://geovocab.org/spatial#"
    
  val connectsWith = factory.createURI(NAMESPACE, "C")

}