package org.pelagios.rdf.vocab

object OSGeo extends BaseVocab {
  
  val NAMESPACE = "http://data.ordnancesurvey.co.uk/ontology/geometry/"
    
  val asWKT = factory.createURI(NAMESPACE, "asWKT")
    
  val extent = factory.createURI(NAMESPACE, "extent")

}