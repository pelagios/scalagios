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
  
  def label: Option[String]

  def comment: Option[String]

  def altLabels: Option[String]
  
  def coverage: Option[String]
  
  def featureType: Option[String]

  def lon: Double

  def lat: Double

  def within: Option[Place]
  
  def sameAs: Option[Place]
  
  def geometryWKT: Option[String]
  
  def location: Option[Geometry] = {
    val factory = new GeometryFactory()
    
    if (within.isDefined)
      within.get.location
    else if (!lon.isNaN() && !lat.isNaN())
      Some(factory.createPoint(new Coordinate(lon, lat)))
    else if (geometryWKT.isDefined)
      Some(new WKTReader(factory).read(geometryWKT.get))
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
case class DefaultPlace(var uri: String) extends Place {
  
  var label: Option[String] = None

  var comment: Option[String] = None
  
  private val altLabelsList: ListBuffer[String] = ListBuffer()
  
  def addAltLabel(altLabel: String): Unit = {
    if (!altLabel.equals(label))
      altLabelsList.append(altLabel)
  }
  
  def altLabels: Option[String] = Some(altLabelsList.mkString(", "))
  
  var coverage: Option[String] = None
  
  var featureType: Option[String] = None

  var lon: Double = Double.NaN

  var lat: Double = Double.NaN
  
  var within: Option[Place] = None
  
  var sameAs: Option[Place] = None
    
  var geometryWKT: Option[String] = None
    
}