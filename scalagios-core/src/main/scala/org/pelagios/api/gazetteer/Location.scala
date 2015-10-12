package org.pelagios.api.gazetteer

import com.vividsolutions.jts.geom.{ Coordinate, Geometry, GeometryFactory }
import com.vividsolutions.jts.io.{ WKTReader, WKTWriter }
import java.io.StringWriter
import org.geotools.geojson.geom.GeometryJSON

/** A wrapper around the Pelagios location concept.
  *
  * A location can consist of a "representative point" and a detail geometry. It is ok to provide either
  * one (point) or the other (geometry) in RDF. But in the data model, we want to have uniform access to
  * both point AND geometry, even if only one of them was set in the RDF.
  */
class Location private (val pointLocation: Coordinate, val geometry: Geometry) {
  
  lazy val asWKT: String = new WKTWriter().write(geometry)

  lazy val asGeoJSON: String = {
    val writer = new StringWriter()
    new GeometryJSON().write(geometry, writer)
    writer.toString
  }
  
  
}

object Location {
  
  private val factory = new GeometryFactory()
  
  private val wktReader = new WKTReader()
  
  private val geoJson = new GeometryJSON()
  
  def create(pointLocation: Option[Coordinate], geometry: Option[Geometry]): Option[Location] =
    if (pointLocation.isDefined && geometry.isDefined)
      // Source data includes both an explicit representative point, and a geometry
      Some(new Location(pointLocation.get, geometry.get))
    else if (pointLocation.isDefined)
      // Source data includes just a point - re-use the point as geometry
      Some(new Location(pointLocation.get, factory.createPoint(pointLocation.get)))
    else if (geometry.isDefined)
      // Source data includes just a geometry - use centroid as representative coordinate
      Some(new Location(geometry.get.getCentroid.getCoordinate, geometry.get))
    else
      None
      
  
  /** Helper to parse a WKT string **/
  def fromWKT(wkt: String): Option[Location] = try {
    val geom = Option(wktReader.read(wkt))
    Location.create(None, geom)
  } catch {
    case t: Throwable => None
  }
  
  /** Helper to parse GeoJSON **/
  def fromGeoJSON(json: String): Option[Location] = try {
    val geom = Option(geoJson.read(json.trim))
    Location.create(None, geom)
  } catch {
    case t: Throwable => None
  }
  
}