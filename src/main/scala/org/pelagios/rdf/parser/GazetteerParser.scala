package org.pelagios.rdf.parser

import org.pelagios.api.{ Label, Location, Name, Place }
import org.openrdf.model.{ Literal, URI, Value }
import org.openrdf.model.vocabulary.RDF
import org.pelagios.rdf.vocab.{OSGeo, Pelagios, PleiadesPlaces, SKOS, W3CGeo }
import org.pelagios.rdf.vocab.DCTerms
import org.openrdf.model.vocabulary.RDFS

/**
 * A resource collector implementation that handles Pelagios Gazetteer dump files.
 */
class GazetteerParser extends ResourceCollector {
  
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
        if (predicates.filter(p => { p == OSGeo.asWKT | p == OSGeo.asGeoJSON | p == W3CGeo.lat }).size > 0) {
          // If it has any form of geometry, assume it's a location
          (PleiadesPlaces.Location, subjURI, resource)  
        } else if (predicates.filter(p => { p == SKOS.label }).size > 0) {
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
  
  def places: Iterable[Place] = {
    // All RDF resources, grouped by type (Place, Name, Location) 
    val typedResources = determineType(resources.toMap)
    
    // Just the Names
    val allNames = typedResources.get(PleiadesPlaces.Name).getOrElse(Map.empty[String, Resource])
    
    // Just the Locations
    val allLocations = typedResources.get(PleiadesPlaces.Location).getOrElse(Map.empty[String, Resource])
    
    // Places, with Names and Locations in-lined 
    typedResources.get(Pelagios.PlaceRecord).getOrElse(Map.empty[String, Resource]).map { case (uri, resource) => 
      val names = resource.get(PleiadesPlaces.hasName).map(uri => allNames.get(uri.toString).map(new NameResource(_))).toSeq.flatten
      val locations = resource.get(PleiadesPlaces.hasLocation).map(uri => allLocations.get(uri.toString).map(new LocationResource(_))).toSeq.flatten
      new PlaceResource(resource, names, locations)
    }
  }

}

/**
 * Wraps a pelagios:PlaceRecord RDF resource as a Place domain model primitive, with Names and Locations in-lined.
 */
private[parser] class PlaceResource(resource: Resource, val names: Seq[NameResource], val locations: Seq[LocationResource]) extends Place {

  def uri: String = resource.uri
  
  def title: Label = resource.getFirst(DCTerms.title).map(ResourceCollector.toLabel(_)).get
  
  def descriptions = (resource.get(RDFS.COMMENT) ++ resource.get(DCTerms.description)).map(ResourceCollector.toLabel(_))
  
  def subjects = Seq.empty[String] // TODO
  
  def closeMatches = resource.get(SKOS.closeMatch).map(_.stringValue)

}

/**
 * Wraps a pleiades:Name RDF resource as a Name domain model primitive. 
 */
private[parser] class NameResource(resource: Resource) extends Name {
  
  def labels: Seq[Label] = resource.get(SKOS.label).map(ResourceCollector.toLabel(_))
  
  def altLabels: Seq[Label] = resource.get(SKOS.altLabel).map(ResourceCollector.toLabel(_))
    
}

/**
 * Wraps a pleiades:Location RDF resource as a Location domain model primitive.
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

