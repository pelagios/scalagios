package org.pelagios.rdf.vocab

import org.pelagios.api.PlaceCategory
import org.openrdf.model.URI

object PelagiosPlaceTypes extends BaseVocab("http://pelagios.github.io/vocab/placeTypes#"){

  val settlement = createURI("settlement")
  
  val region = createURI("region")

  val naturalFeature = createURI("naturalFeature")

  val ethnos = createURI("ethnos")

  val manMadeStructure = createURI("manMadeStructure")
  
  def fromCategory(placeType: PlaceCategory.Category): URI = placeType match {
    case PlaceCategory.SETTLEMENT => settlement
    case PlaceCategory.REGION => region
    case PlaceCategory.NATURAL_FEATURE => naturalFeature
    case PlaceCategory.ETHNOS => ethnos
    case PlaceCategory.MAN_MADE_STRUCTURE => manMadeStructure
  }  
  
}