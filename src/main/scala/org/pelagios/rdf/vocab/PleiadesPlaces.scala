package org.pelagios.rdf.vocab

/** Pleiades RDF Vocabulary - http://pleiades.stoa.org/places/vocab **/
object PleiadesPlaces extends BaseVocab("http://pleiades.stoa.org/places/vocab#") {
  
  val Name = createURI("Name")
  
  val Location = createURI("Location")
  
  val hasFeatureType = createURI("hasFeatureType") 
   
  val hasLocation = createURI("hasLocation")
  
  val hasName = createURI("hasName")

}