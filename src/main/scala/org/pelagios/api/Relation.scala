package org.pelagios.api

object Relation extends Enumeration {
  
  type Type = Value
  
  val attestsTo = Value("attestsTo")
  
  val foundAt = Value("foundAt")
  
  val locatedAt = Value("locatedAt")

}