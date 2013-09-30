package org.pelagios.rdf.vocab

object PleiadesPlaces extends BaseVocab {
  
  val NAMESPACE = "http://pleiades.stoa.org/places/vocab#"
    
  val hasFeatureType = factory.createURI(NAMESPACE, "hasFeatureType") 
  
  val Name = factory.createURI(NAMESPACE, "Name")
  
  val Location = factory.createURI(NAMESPACE, "Location")
  
  val hasName = factory.createURI(NAMESPACE, "hasName")
  
  val hasLocation = factory.createURI(NAMESPACE, "hasLocation")  

}