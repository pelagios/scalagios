package org.pelagios.api

import java.io.StringWriter
import com.vividsolutions.jts.io.{ WKTReader, WKTWriter }
import com.vividsolutions.jts.geom.{ Coordinate, Geometry, GeometryFactory }
import org.geotools.geojson.geom.GeometryJSON

/** Pelagios 'Location' model primitive.
  *  
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Location {
  
  def geometry: Geometry

  def descriptions: Seq[PlainLiteral]
  
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
private[api] class DefaultLocation(val geometry: Geometry, val descriptions: Seq[PlainLiteral]) extends Location

/** Companion object with a pimped apply method for generating DefaultAnnotation instances.
  *  
  * Includes helper methods to convert to/from WKT, GeoJSON or LatLon.   
  */
object Location extends AbstractApiCompanion {

  private val wktReader = new WKTReader
  
  private val geoJson = new GeometryJSON 
  
  private val factory = new GeometryFactory
  
  def apply(geometry: Geometry): Location =
    new DefaultLocation(geometry, Seq.empty[PlainLiteral])
    
  def apply(geometry: Geometry, descriptions: ObjOrSeq[PlainLiteral]): Location =
    new DefaultLocation(geometry, descriptions.seq)
  
  def apply(lat: Double, lon: Double, descriptions: ObjOrSeq[PlainLiteral] = new ObjOrSeq(Seq.empty[PlainLiteral])): Location = 
    new DefaultLocation(Location.fromLatLon(lat, lon), descriptions.seq)
    
  /** Constructs a location from a WKT string **/
  def parseWKT(wkt: String) = wktReader.read(wkt)
  
  /** Constructs a location from a GeoJSON string **/
  def parseGeoJSON(json: String) = geoJson.read(json.trim)
    
  /** Constructs a location from a lat/lon coordinate **/
  def fromLatLon(lat: Double, lon: Double) =
    factory.createPoint(new Coordinate(lon, lat))
  
}