package org.scalagios.api

import scala.collection.mutable.ListBuffer
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.io.WKTReader

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
  
  def location: Option[Geometry] = {
    val factory = new GeometryFactory()
    
    if (within != null)
      within.location
    else if (!lon.isNaN() && !lat.isNaN())
      Some(factory.createPoint(new Coordinate(lon, lat)))
    else if (geometryWKT != null)
      Some(new WKTReader(factory).read(geometryWKT))
    else
      None
  }
  
  // TODO implement hasConnectionWith relation

  def isValid: Boolean = {
    if (uri == null) 
      // null URI not allowed
      false
    else if (within == null && (lon == Double.NaN && lat == Double.NaN) && geometryWKT == null)
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

  var lon: Double = Double.NaN

  var lat: Double = Double.NaN
  
  var within: Place = _
    
  var geometryWKT: String = _
    
}