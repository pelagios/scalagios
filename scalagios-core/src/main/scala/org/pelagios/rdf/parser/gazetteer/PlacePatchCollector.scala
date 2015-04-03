package org.pelagios.rdf.parser.gazetteer

import org.openrdf.model.vocabulary.RDFS
import org.pelagios.api.{ PlainLiteral, PeriodOfTime }
import org.pelagios.api.gazetteer.Location
import org.pelagios.api.gazetteer.patch.PlacePatch
import org.pelagios.rdf.parser.{ Resource, ResourceCollector }
import org.pelagios.rdf.vocab.{ DCTerms, LAWD, W3CGeo, OSGeo, PelagiosPlaceCategories, SKOS }
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

    val locationsTable = resourcesOfType(W3CGeo.SpatialThing, Seq(_.hasAnyPredicate(Seq(OSGeo.asWKT, OSGeo.asGeoJSON, W3CGeo.lat))))
      .map(resource => (resource.uri -> new LocationResource(resource))).toMap
    
    resourcesOfType(LAWD.Place).map(resource => {
      val names = resource.get(LAWD.hasName).map(uri => namesTable.get(uri.stringValue)).filter(_.isDefined).map(_.get)
      val locations = resource.get(W3CGeo.location).map(uri => locationsTable.get(uri.stringValue)).filter(_.isDefined).map(_.get)
      toPlacePatch(resource, names, locations)      
    })
  } 
  
  private def toPlacePatch(resource: Resource, names: Seq[PlainLiteral], locations: Seq[Location]) = PlacePatch(
    resource.uri,
    resource.getFirst(RDFS.LABEL).map(_.stringValue),
    (resource.get(RDFS.COMMENT) ++ resource.get(DCTerms.description)).map(ResourceCollector.toPlainLiteral(_)),
    names,
    locations,
    resource.getFirst(DCTerms.temporal).map(literal => PeriodOfTime.fromString(literal.stringValue)),
    resource.getFirst(DCTerms.typ).flatMap(uri => PelagiosPlaceCategories.toCategory(uri)),
    Seq.empty[String], // TODO subjects
    resource.get(SKOS.closeMatch).map(_.stringValue),
    resource.get(SKOS.exactMatch).map(_.stringValue)
  )

}
