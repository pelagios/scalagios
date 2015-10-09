package org.pelagios.rdf.vocab

object GeoSPARQL extends BaseVocab("http://www.opengis.net/ont/geosparql#") {

  val hasGeometry  = createURI("hasGeometry")

  val asWKT = createURI("asWKT")

}
