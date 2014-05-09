package org.pelagios.rdf.vocab

import org.openrdf.model.URI
import org.openrdf.model.Value
import org.pelagios.api.gazetteer.PlaceCategory

object PelagiosPlaceCategories extends BaseVocab("http://pelagios.github.io/vocab/placeTypes#"){

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
  
  def toCategory(uri: Value): Option[PlaceCategory.Category] = {
    if (uri == settlement)
      Some(PlaceCategory.SETTLEMENT)
    else if (uri == region)
      Some(PlaceCategory.REGION)
    else if (uri == naturalFeature)
      Some(PlaceCategory.NATURAL_FEATURE)
    else if (uri == ethnos)
      Some(PlaceCategory.ETHNOS)
    else if (uri == manMadeStructure)
      Some(PlaceCategory.MAN_MADE_STRUCTURE)
    else
      None
  }
  
}