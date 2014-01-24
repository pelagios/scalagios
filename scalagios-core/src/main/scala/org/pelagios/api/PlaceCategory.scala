package org.pelagios.api

object PlaceCategory extends Enumeration {

  type Category = Value
  
  val SETTLEMENT = Value("SETTLEMENT")
  
  val REGION = Value("REGION")
  
  val ETHNOS = Value("ETHNOS")
  
  val NATURAL_FEATURE = Value("NATURAL_FEATURE")
  
  val MAN_MADE_STRUCTURE = Value("MAN_MADE_STRUCTURE")
  
}