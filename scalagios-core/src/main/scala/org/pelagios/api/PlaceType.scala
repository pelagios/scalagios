package org.pelagios.api

object PlaceType extends Enumeration {

  type Type = Value
  
  val SETTLEMENT = Value("SETTLEMENT")
  
  val REGION = Value("REGION")
  
  val ETHNOS = Value("ETHNOS")
  
  val NATURAL_FEATURE = Value("NATURAL_FEATURE")
  
  val MAN_MADE_STRUCTURE = Value("MAN_MADE_STRUCTURE")
  
}