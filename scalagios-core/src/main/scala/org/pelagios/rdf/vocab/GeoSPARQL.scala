package org.pelagios.rdf.vocab

object GeoSPARQL extends BaseVocab("http://www.opengis.net/ont/geosparql#") {

  val Geometry = createURI("Geometry")
  
  val hasGeometry  = createURI("hasGeometry")

  val asWKT = createURI("asWKT")

}
