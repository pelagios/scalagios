package org.pelagios.api

import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.{ Coordinate, Geometry, GeometryFactory }
import org.geotools.geojson.geom.GeometryJSON
import com.vividsolutions.jts.io.WKTWriter
import java.io.StringWriter

/** Pelagios 'Location' model primitive.
  *  
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Location {
  
  def geometry: Geometry

  def descriptions: Seq[Label]
  
  /** The geometry as Well-Known-Text **/
  lazy val wkt: String =
    new WKTWriter().write(geometry)

  /** The geometry as GeoJSON **/
  lazy val geoJSON: String = {
    val writer = new StringWriter()
    new GeometryJSON().write(geometry, writer)
    writer.toString
  }
  
  /** The geometry as lat/lon.
    *  
    * Complex shapes will be collapsed to their centroid.
    */
  lazy val latLon: (Double, Double) = {
    val coord = geometry.getCentroid.getCoordinate
    (coord.y, coord.x)
  }
  
}

/** A default POJO-style implementation of Location. **/
class DefaultLocation(val geometry: Geometry, val descriptions: Seq[Label] = Seq.empty[Label]) {
  
  def this(lat: Double, lon: Double, descriptions: Seq[Label] = Seq.empty[Label]) =
    this(Location.fromLatLon(lat, lon), descriptions)
  
}

/** Helper methods to convert to/from WKT, GeoJSON or LatLon. **/
object Location {
  
  private val wktReader = new WKTReader
  
  private val geoJson = new GeometryJSON 
  
  private val factory = new GeometryFactory
  
  /** Constructs a location from a WKT string **/
  def parseWKT(wkt: String) = wktReader.read(wkt)
  
  /** Constructs a location from a GeoJSON string **/
  def parseGeoJSON(json: String) = geoJson.read(json.trim)
    
  /** Constructs a location from a lat/lon coordinate **/
  def fromLatLon(lat: Double, lon: Double) =
    factory.createPoint(new Coordinate(lon, lat))
  
}