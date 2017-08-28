package org.pelagios.rdf.parser.gazetteer

import com.vividsolutions.jts.geom.{ Coordinate, Geometry }
import com.vividsolutions.jts.io.WKTReader
import org.geotools.geojson.geom.GeometryJSON
import org.openrdf.model.vocabulary.RDFS
import org.pelagios.api.{ Image, PlainLiteral, PeriodOfTime }
import org.pelagios.api.gazetteer.{ Location, Place }
import org.pelagios.rdf.parser.{ Resource, ResourceCollector }
import org.pelagios.rdf.vocab._
import org.slf4j.LoggerFactory


/** An implementation of [[org.pelagios.rdf.parser.ResourceCollector]] to handle Gazetteer dump files.
  *
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class PlaceCollector extends ResourceCollector {

  protected val logger = LoggerFactory.getLogger(classOf[PlaceCollector])

  /** The Places collected by the parser.
    *
    * @return the list of Places
    */
  lazy val places: Iterable[Place] = {
    logger.info("Building names table")
    val namesTable = resourcesOfType(LAWD.PlaceName, Seq(_.hasPredicate(LAWD.primaryForm)))
      .map(resource => (resource.uri -> resource.getFirst(LAWD.primaryForm).map(ResourceCollector.toPlainLiteral(_)).get)).toMap

    logger.info("Building locations table")
    val coordinatesTable = resourcesOfType(W3CGeo.SpatialThing, Seq(_.hasPredicate(W3CGeo.lat)))
      .map(resource => (resource.uri -> PlaceResource.toCoordinate(resource))).toMap
      
    logger.info("Building geometries table")
    val geometriesTable = resourcesOfType(GeoSPARQL.Geometry, Seq(_.hasAnyPredicate(Seq(GeoSPARQL.asWKT, OSGeo.asGeoJSON))))
      .map(resource => (resource.uri -> PlaceResource.toGeometry(resource))).toMap
      
    logger.info("Building images table")
    val imagesTable = resourcesOfType(FOAF.Image)
      .map(resource => (resource.uri -> PlaceResource.toImage(resource))).toMap
      
    logger.info("Wrapping RDF to domain model")
    resourcesOfType(LAWD.Place).map(resource => {
      val names = resource.get(LAWD.hasName).flatMap(uri => namesTable.get(uri.stringValue))
      val coordinate = resource.getFirst(W3CGeo.location).flatMap(uri => coordinatesTable.get(uri.stringValue)).flatten
      val geometry = resource.getFirst(GeoSPARQL.hasGeometry).flatMap(uri => geometriesTable.get(uri.stringValue)).flatten
      val images = resource.get(FOAF.depiction).map { uri => 
        imagesTable.get(uri.stringValue) match {
          case Some(image) =>
            // The image is represented as a dedicated resource, with extra meta
            image
            
          case None =>
            // No image resource, just use the URL
            Image(uri.toString)
        }
      }
      new PlaceResource(resource, names, Location.create(coordinate, geometry), images)
    })
  }

}

/** Wraps a pelagios:PlaceRecord resource as a Place domain model primitive, with Names and Locations in-lined.
 *
 *  @constructor create a new PlaceResource
 *  @param resource the RDF resource to wrap
 *  @param names the names connected to the resource
 *  @param locations the locations connected to the resource
 */
private[parser] class PlaceResource(val resource: Resource, val names: Seq[PlainLiteral], val location: Option[Location], val depictions: Seq[Image]) extends Place {

  val uri = resource.uri

  val label = resource.getFirst(RDFS.LABEL).map(_.stringValue).getOrElse("[NO TITLE]") // 'NO TITLE' should never happen!

  val descriptions = (resource.get(RDFS.COMMENT) ++ resource.get(DCTerms.description)).map(ResourceCollector.toPlainLiteral(_))
  
  // TODO support URIs as well as periods
  val temporalCoverage: Option[PeriodOfTime] =
    try {
      resource.getFirst(DCTerms.temporal).map(literal => PeriodOfTime.fromString(literal.stringValue))
    } catch {
      case t: Throwable => {
        // Vast majority of resources will be fine, so no need to create a logger, unless needed
        val logger = LoggerFactory.getLogger(classOf[PlaceResource])
        logger.warn("Error parsing dcterms:temporal on " + uri)
        logger.warn(t.getMessage)
        None
      }
    }
    
  val timePeriods = Seq.empty[String] // TODO implement
    
  val category = resource.getFirst(DCTerms.typ).flatMap(uri => PelagiosPlaceCategories.toCategory(uri))

  val subjects = resource.get(DCTerms.subject).map(_.stringValue)

  val closeMatches = resource.get(SKOS.closeMatch).map(_.stringValue)

  val exactMatches = resource.get(SKOS.exactMatch).map(_.stringValue)

}

private[parser] object PlaceResource {
  
  protected val logger = LoggerFactory.getLogger(classOf[PlaceResource])
  
  private val wktReader = new WKTReader()
  
  private val geoJson = new GeometryJSON()
  
  /** Converts a W3C location resource to a coordinate **/
  def toCoordinate(resource: Resource): Option[Coordinate] = {
    val latStr = resource.getFirst(W3CGeo.lat).map(_.stringValue)
    val longStr = resource.getFirst(W3CGeo.long).map(_.stringValue)
    
    if (latStr.isDefined && longStr.isDefined) {
      try {
        Some(new Coordinate(
          longStr.get.toDouble,
          latStr.get.toDouble  
        ))
      } catch {
        case t: Throwable => {
          logger.warn("Invalid coordinates: " + latStr + "/" + longStr)
          None
        }
      }
    } else {
      logger.warn("Invalid coordinates: " + latStr + "/" + longStr)
      None
    }
  }
  
  /** Converts a GeoSPARQL geometry resource to a geometry **/ 
  def toGeometry(resource: Resource): Option[Geometry] = {
    val asWKT = resource.getFirst(GeoSPARQL.asWKT).map(_.stringValue)
    val asGeoJSON = resource.getFirst(OSGeo.asGeoJSON).map(_.stringValue)
    
    try {
      if (asWKT.isDefined) {
        Some(wktReader.read(asWKT.get))
      } else if (asGeoJSON.isDefined) {
        Some(geoJson.read(asGeoJSON.get))
      } else {
        logger.warn("Found geometry resource, but no geometry attached: " + resource.toString)
        None
      }
    } catch {
      case t:Throwable => {
        logger.warn("Invalid geometry: " + resource.toString)
        None
      }
    }
  }
  
  /** Converts a foaf:Image resource to an Image object **/
  def toImage(resource: Resource): Image = {
    val title = resource.getFirst(DCTerms.title).map(_.stringValue)
    val license = resource.getFirst(DCTerms.license).map(_.stringValue)
    Image(resource.uri, title, license)
  }
  
}
