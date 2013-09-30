package org.pelagios.api

import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.{ Coordinate, Geometry, GeometryFactory }

/**
 * 'Location' model primitive.
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait Location {

  def wkt: Option[String]
  
  def geoJson: Option[String]
  
  def lonlat: Option[(Double, Double)]
  
  def descriptions: Seq[Label]

  def geometry: Option[Geometry] = {
    val factory = new GeometryFactory
    
    if (wkt.isDefined)
      Some(new WKTReader(factory).read(wkt.get))
    else if (geoJson.isDefined)
      // TODO implement!
      None
    else if (lonlat.isDefined)
      lonlat.map(ll => factory.createPoint(new Coordinate(ll._1, ll._2)))
    else
      None
  }
  
}