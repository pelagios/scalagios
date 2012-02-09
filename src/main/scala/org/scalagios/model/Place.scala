package org.scalagios.model

trait Place {
  
  def uri: String
  
  def label: String

  def comment: String

  def altLabels: String

  def lon: Double

  def lat: Double

  def within: String
  
  def geometryWKT: String

  def isValid: Boolean = {
    if (uri == null) 
      // null URI not allowed
      false
    else if (within == null && (lon == 0 && lat == 0) && geometryWKT == null)
      // Place must either have 'within' OR non-null lon/lat OR WKT geometry
      false
      
    true
  }
  
}