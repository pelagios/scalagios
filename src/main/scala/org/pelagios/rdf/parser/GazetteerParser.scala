package org.pelagios.rdf.parser

import org.pelagios.api.{ Label, Location, Name, Place }
import org.openrdf.model.{ Literal, URI, Value }
import org.openrdf.model.vocabulary.RDF
import org.pelagios.rdf.vocab.{OSGeo, Pelagios, PleiadesPlaces, SKOS, W3CGeo }
import org.pelagios.rdf.vocab.DCTerms
import org.openrdf.model.vocabulary.RDFS

/** An implementation of [[org.pelagios.rdf.parser.ResourceCollector]] to handle Gazetteer dump files.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class GazetteerParser extends ResourceCollector {
  
  /** Private helper method that adds explicit type information to the collected resources.
    *
    * The method will go through the resources, and first look for explicit RDF types of 
    * pelagios:PlaceRecord, pleiades:Name, and pleiades:Location. If no explicit RDF type
    * is provided, it will attempt to determine the type based on other properties.
    * 
    * @param resources the RDF resources
    * @return a map of resources, grouped by the RDF types pelagios:PlaceRecord, pleiades:Name
    *         and pleiades:Location
    */
  private def determineType(resources: Map[String, Resource]): Map[URI, Map[String, Resource]] = {
    val typedResources = resources.map { case (subjURI, resource) => {        
      val types = resource.get(RDF.TYPE)
      
      // The easy case: resource is explicitly typed      
      if (types.contains(PleiadesPlaces.Name)) {
        (PleiadesPlaces.Name, subjURI, resource)    
      } else if (types.contains(PleiadesPlaces.Location)) {
        (PleiadesPlaces.Location, subjURI, resource)  
      } else if (types.contains(Pelagios.PlaceRecord)) {
        (Pelagios.PlaceRecord, subjURI, resource)  
      } else {
        // No explicit typing - guess based on properties
        val predicates = resource.properties.map(_._1)
        if (predicates.filter(p => { p.equals(OSGeo.asWKT) | p.equals(OSGeo.asGeoJSON) | p.equals(W3CGeo.lat) }).size > 0) {
          // If it has any form of geometry, assume it's a location
          (PleiadesPlaces.Location, subjURI, resource)  
        } else if (predicates.filter(_.equals(SKOS.label)).size > 0) {
          // If it has a skos:label, assume it's a name
          (PleiadesPlaces.Name, subjURI, resource)  
        } else {
          // Everything that's untyped, and has neither geometry nor a skos:label is treated as Place
          (Pelagios.PlaceRecord, subjURI, resource)  
        }
      }
    }}
    
    typedResources.groupBy(_._1).mapValues(_.map { case (typeURI, subjURI, resource) => subjURI -> resource }.toMap)
  } 
  
  /** The Places collected by the parser.
   *  
    * @return the list of Places
    */
  def places: Iterable[Place] = {
    // All RDF resources, grouped by type (Place, Name, Location) 
    val typedResources = determineType(resources.toMap)
    
    // Just the Names
    val allNames = typedResources.get(PleiadesPlaces.Name).getOrElse(Map.empty[String, Resource])
    
    // Just the Locations
    val allLocations = typedResources.get(PleiadesPlaces.Location).getOrElse(Map.empty[String, Resource])
    
    // Places, with Names and Locations in-lined 
    typedResources.get(Pelagios.PlaceRecord).getOrElse(Map.empty[String, Resource]).map { case (uri, resource) => 
      val names = resource.get(PleiadesPlaces.hasName).map(uri => allNames.get(uri.stringValue).map(new NameResource(_))).toSeq.flatten
      val locations = resource.get(PleiadesPlaces.hasLocation).map(uri => allLocations.get(uri.toString).map(new LocationResource(_))).toSeq.flatten
      new PlaceResource(resource, names, locations)
    }
  }

}

/** Wraps a pelagios:PlaceRecord resource as a Place domain model primitive, with Names and Locations in-lined.
 *  
 *  @constructor create a new PlaceResource
 *  @param resource the RDF resource to wrap
 *  @param names the names connected to the resource
 *  @param locations the locations connected to the resource
 */
private[parser] class PlaceResource(resource: Resource, val names: Seq[NameResource], val locations: Seq[LocationResource]) extends Place {

  def uri: String = resource.uri
  
  def title: Label = resource.getFirst(DCTerms.title).map(ResourceCollector.toLabel(_)).get
  
  def descriptions = (resource.get(RDFS.COMMENT) ++ resource.get(DCTerms.description)).map(ResourceCollector.toLabel(_))
  
  def subjects = Seq.empty[String] // TODO
  
  def closeMatches = resource.get(SKOS.closeMatch).map(_.stringValue)

}

/** Wraps a pleiades:Name RDF resource as a Name domain model primitive.
  *
  * @constructor create a new NameResource
  * @param resource the RDF resource to wrap   
  */
private[parser] class NameResource(resource: Resource) extends Name {
  
  def labels: Seq[Label] = resource.get(SKOS.label).map(ResourceCollector.toLabel(_))
  
  def altLabels: Seq[Label] = resource.get(SKOS.altLabel).map(ResourceCollector.toLabel(_))
    
}

/** Wraps a pleiades:Location RDF resource as a Location domain model primitive.
  *  
  * @constructor create a new LocationResource
  * @param resource the RDF resource to wrap
  */
private[parser] class LocationResource(resource: Resource) extends Location {

  def wkt: Option[String] = resource.getFirst(OSGeo.asWKT).map(_.stringValue)
  
  def geoJson: Option[String] = resource.getFirst(OSGeo.asGeoJSON).map(_.stringValue)
  
  def lonlat: Option[(Double, Double)] = {
    val lon = resource.getFirst(W3CGeo.long).map(_.asInstanceOf[Double])
    val lat = resource.getFirst(W3CGeo.lat).map(_.asInstanceOf[Double])
    if (lon.isDefined && lat.isDefined)
      Some((lon.get, lat.get))
    else
      None
  }
  
  def descriptions: Seq[Label] =
    (resource.get(DCTerms.description) ++ resource.get(RDFS.LABEL)).map(ResourceCollector.toLabel(_))
  
}

