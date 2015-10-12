package org.pelagios.rdf.parser.gazetteer

import com.vividsolutions.jts.geom.{ Coordinate, Geometry }
import org.openrdf.model.vocabulary.RDFS
import org.pelagios.api.{ Image, PlainLiteral, PeriodOfTime }
import org.pelagios.api.gazetteer.Location
import org.pelagios.api.gazetteer.patch.PlacePatch
import org.pelagios.rdf.parser.{ Resource, ResourceCollector }
import org.pelagios.rdf.vocab._
import org.slf4j.LoggerFactory

class PlacePatchCollector extends ResourceCollector {
  
  protected val logger = LoggerFactory.getLogger(classOf[PlacePatchCollector])

  /** The Patches collected by the parser.
    *  
    * @return the list of Patches
    */
  lazy val patches: Iterable[PlacePatch] = {
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
      val images = resource.get(FOAF.depiction).flatMap(uri => imagesTable.get(uri.stringValue))
      toPlacePatch(resource, names, Location.create(coordinate, geometry), images)
    })
  } 
  
  private def toPlacePatch(resource: Resource, names: Seq[PlainLiteral], location: Option[Location], depictions: Seq[Image]) = PlacePatch(
    resource.uri,
    resource.getFirst(RDFS.LABEL).map(_.stringValue),
    (resource.get(RDFS.COMMENT) ++ resource.get(DCTerms.description)).map(ResourceCollector.toPlainLiteral(_)),
    names,
    location,
    resource.getFirst(DCTerms.temporal).map(literal => PeriodOfTime.fromString(literal.stringValue)),
    Seq.empty[String], // TODO support period URIs as well as time periods 
    resource.getFirst(DCTerms.typ).flatMap(uri => PelagiosPlaceCategories.toCategory(uri)),
    Seq.empty[String], // TODO subjects
    depictions,
    resource.get(SKOS.closeMatch).map(_.stringValue),
    resource.get(SKOS.exactMatch).map(_.stringValue)
  )

}
