package org.pelagios.api.annotation

object Relation extends Enumeration {
  
  type Type = Value
  
  val attestsTo = Value("ATTESTS_TO")
  
  val foundAt = Value("FOUND_AT")
  
  val locatedAt = Value("LOCATED_AT")

}