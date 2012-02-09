package org.scalagios.model

trait GeoAnnotation {
  
  def uri: String
  
  def body: String
  
  def target: String
  
  def isValid: Boolean = (uri != null && target != null && body != null) 

}