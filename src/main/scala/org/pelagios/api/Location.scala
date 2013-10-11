package org.pelagios.api

import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.{ Coordinate, Geometry, GeometryFactory }

/** Pelagios 'Location' model primitive.
  *  
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Location {

  /** The WKT geometry representation, if specified **/
  def wkt: Option[String]

  /** The GeoJSON geometry representation, if specified **/
  def geoJson: Option[String]
  
  /** The Longitude/Latitude tupe, if specified **/
  def lonlat: Option[(Double, Double)]
  
  /** The list of descriptions (in multiple languages) **/
  def descriptions: Seq[Label]

  /** The geometry of the location, if available
    *
    * The geometry is determined based on whatever representation
    * is available - WKT, GeoJSON or lat/lon.   
    */
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

/** A default POJO-style implementation of Location. **/
class DefaultLocation extends Location {
  
  var wkt: Option[String] = None
  
  var geoJson: Option[String] = None
  
  var lonlat: Option[(Double, Double)] = None
  
  var descriptions = Seq.empty[Label]
  
}