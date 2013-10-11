package org.pelagios.rdf.parser

import org.pelagios.api._
import org.pelagios.rdf.vocab._
import org.openrdf.model.{ Literal, URI, Value }
import org.openrdf.model.vocabulary.{ RDF, RDFS }

/** An implementation of [[org.pelagios.rdf.parser.ResourceCollector]] to handle Gazetteer dump files.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class GazetteerParser extends ResourceCollector {
  
  /** The Places collected by the parser.
   *  
    * @return the list of Places
    */
  def places: Iterable[Place] = {
    val allNames = resourcesOfType(PleiadesPlaces.Name, Seq(_.hasPredicate(SKOS.label)))
      .map(new NameResource(_))
    val allLocations = resourcesOfType(PleiadesPlaces.Location, Seq(_.hasAnyPredicate(Seq(OSGeo.asWKT, OSGeo.asGeoJSON, W3CGeo.lat))))
      .map(new LocationResource(_))
      
    resourcesOfType(Pelagios.PlaceRecord).map(resource => {
      val names = resource.get(PleiadesPlaces.hasName).map(uri => allNames.filter(_.resource.uri.equals(uri.stringValue))).flatten
      val locations = resource.get(PleiadesPlaces.hasLocation).map(uri => allLocations.filter(_.resource.uri.equals(uri.stringValue))).flatten
      new PlaceResource(resource, names, locations)
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
private[parser] class PlaceResource(val resource: Resource, val names: Seq[NameResource], val locations: Seq[Location]) extends Place {

  def uri = resource.uri
  
  def title = resource.getFirst(DCTerms.title).map(_.stringValue).getOrElse("[NO TITLE]") // 'NO TITLE' should never happen!
  
  def descriptions = (resource.get(RDFS.COMMENT) ++ resource.get(DCTerms.description)).map(ResourceCollector.toLabel(_))
  
  // TODO
  def subjects = Seq.empty[String]
  
  def closeMatches = resource.get(SKOS.closeMatch).map(_.stringValue)

}

/** Wraps a pleiades:Name RDF resource as a Name domain model primitive.
  *
  * @constructor create a new NameResource
  * @param resource the RDF resource to wrap   
  */
private[parser] class NameResource(val resource: Resource) extends Name {
  
  def labels: Seq[Label] = resource.get(SKOS.label).map(ResourceCollector.toLabel(_))
  
  def altLabels: Seq[Label] = resource.get(SKOS.altLabel).map(ResourceCollector.toLabel(_))
    
}

/** Wraps a pleiades:Location RDF resource as a Location domain model primitive.
  *  
  * @constructor create a new LocationResource
  * @param resource the RDF resource to wrap
  */
private[parser] class LocationResource(val resource: Resource) extends Location {
  
  val geometry = {
    val wkt = resource.getFirst(OSGeo.asWKT).map(_.stringValue)
    val geoJSON = resource.getFirst(OSGeo.asGeoJSON).map(_.stringValue)
    val (lon, lat) = (resource.getFirst(W3CGeo.long).map(_.asInstanceOf[Double]), 
                      resource.getFirst(W3CGeo.lat).map(_.asInstanceOf[Double]))

    if (wkt.isDefined)
      Location.parseWKT(wkt.get)
    else if (geoJSON.isDefined)
      Location.parseGeoJSON(geoJSON.get)
    else if (lat.isDefined && lon.isDefined)
      Location.fromLatLon(lat.get, lon.get)
    else
      // The spec prohibits Locations without geometry - purely for defensive purposes
      Location.fromLatLon(0, 0) 
  }
  
  def descriptions: Seq[Label] =
    (resource.get(DCTerms.description) ++ resource.get(RDFS.LABEL)).map(ResourceCollector.toLabel(_))

}

