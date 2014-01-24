package org.pelagios.rdf.vocab

import org.pelagios.api.PlaceType
import org.openrdf.model.URI

object PelagiosPlaceTypes extends BaseVocab("http://pelagios.github.io/vocab/placeTypes#"){

  val settlement = createURI("settlement")
  
  val region = createURI("region")

  val naturalFeature = createURI("naturalFeature")

  val ethnos = createURI("ethnos")

  val manMadeStructure = createURI("manMadeStructure")
  
  def fromType(placeType: PlaceType.Type): URI = placeType match {
    case PlaceType.SETTLEMENT => settlement
    case PlaceType.REGION => region
    case PlaceType.NATURAL_FEATURE => naturalFeature
    case PlaceType.ETHNOS => ethnos
    case PlaceType.MAN_MADE_STRUCTURE => manMadeStructure
  }  
  
}