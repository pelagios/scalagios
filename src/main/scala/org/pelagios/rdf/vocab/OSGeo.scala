package org.pelagios.rdf.vocab

/** Ordnance Survey Geometry Ontology - http://data.ordnancesurvey.co.uk/ontology/geometry/ **/
object OSGeo extends BaseVocab("http://data.ordnancesurvey.co.uk/ontology/geometry/") {
  
  val asWKT = createURI("asWKT")
  
  val asGeoJSON = createURI("asGeoJSON")
    
  val extent = createURI("extent")

}