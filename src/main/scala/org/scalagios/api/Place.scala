package org.scalagios.api

import scala.collection.mutable.ListBuffer

/**
 * Pelagios <em>Place</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait Place {
  
  def uri: String
  
  def label: String

  def comment: String

  def altLabels: String
  
  def coverage: String

  def lon: Double

  def lat: Double

  def within: Place
  
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

/**
 * A default (POJO-style) implementation of the Pelagios
 * <em>Place</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class DefaultPlace(var uri: String) extends Place {
  
  var label: String = _

  var comment: String = _
  
  private val altLabelsList: ListBuffer[String] = ListBuffer()
  
  def addAltLabel(altLabel: String): Unit = {
    if (!altLabel.equals(label))
      altLabelsList.append(altLabel)
  }
  
  def altLabels = altLabelsList.mkString(", ")
  
  var coverage: String = _

  var lon: Double = _

  var lat: Double = _
  
  var within: Place = _
    
  var geometryWKT: String = _
    
}