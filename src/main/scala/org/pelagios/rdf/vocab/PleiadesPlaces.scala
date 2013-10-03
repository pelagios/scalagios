package org.pelagios.rdf.vocab

/** Pleiades RDF Vocabulary - http://pleiades.stoa.org/places/vocab **/
object PleiadesPlaces extends BaseVocab("http://pleiades.stoa.org/places/vocab#") {
    
  val hasFeatureType = createURI("hasFeatureType") 
  
  val Name = createURI("Name")
  
  val Location = createURI("Location")
  
  val hasName = createURI("hasName")
  
  val hasLocation = createURI("hasLocation")  

}