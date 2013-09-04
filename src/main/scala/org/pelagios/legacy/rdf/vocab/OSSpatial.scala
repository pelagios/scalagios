package org.pelagios.legacy.rdf.vocab

object OSSpatial extends BaseVocab {
  
  val NAMESPACE = "http://data.ordnancesurvey.co.uk/ontology/spatialrelations/"
    
  val within = factory.createURI(NAMESPACE, "within")

}