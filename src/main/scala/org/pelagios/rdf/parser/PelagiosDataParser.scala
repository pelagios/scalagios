package org.pelagios.rdf.parser

import java.util.Date
import org.pelagios.api._
import org.pelagios.rdf.vocab._
import org.openrdf.model.vocabulary.RDFS
import org.openrdf.model.URI
import org.openrdf.model.vocabulary.RDF

/** An implementation of [[org.pelagios.rdf.parser.ResourceCollector]] to handle Pelagios data dump files.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class PelagiosDataParser extends ResourceCollector {

  def annotations = 
    resources.values.filter(_.hasType(OA.Annotation)).map(new AnnotationResource(_))
  
  def annotatedThings = {
    val annotationsPerThing = annotations.groupBy(_.hasTarget)
      
    resources.values
      .filter(_.hasType(Pelagios.AnnotatedThing)).map(resource => 
        new AnnotatedThingResource(
            resource, 
            Seq.empty[AnnotatedThingResource],
            annotationsPerThing.get(resource.uri).getOrElse(Seq.empty[AnnotationResource]).toSeq))
  }
  
  /** Private helper method that determines explicit type information to the collected resources.
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
      if (types.contains(Pelagios.AnnotatedThing)) {
        (Pelagios.AnnotatedThing, subjURI, resource)    
      } else if (types.contains(OA.Annotation)) {
        (OA.Annotation, subjURI, resource)  
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
      
}

/** Wraps a pelagios:AnnotatedThing RDF resource as an AnnotatedThing domain model primitive, with
 *  Annotations in-lined.
  *  
  * @constructor create a new AnnotatedThing
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotatedThingResource(resource: Resource, val expressions: Seq[AnnotatedThingResource], val annotations: Seq[AnnotationResource]) extends AnnotatedThing {

  def uri = resource.uri
  
  def title = resource.getFirst(DCTerms.title).map(_.stringValue).getOrElse("[NO TITLE]") // 'NO TITLE' should never happen!
  
  def identifier = resource.getFirst(DCTerms.identifier).map(_.stringValue)

  def description = resource.getFirst(DCTerms.description).map(_.stringValue)
  
  def sources = resource.get(DCTerms.source).map(_.stringValue)
  
  // TODO
  def temporal: Option[PeriodOfTime] = None

  // TODO 
  def creator: Option[Agent] = None

  // TODO    
  def contributors: Seq[Agent] = Seq.empty[Agent]
  
  def languages = resource.get(DCTerms.language).map(_.stringValue)
  
  def homepage = resource.getFirst(FOAF.homepage).map(_.stringValue)
  
  def thumbnails = resource.get(FOAF.thumbnail).map(_.stringValue)
  
  def bibliographicCitations = resource.get(DCTerms.bibliographicCitation).map(_.stringValue)
  
  def subjects = resource.get(DCTerms.subject).map(_.stringValue)
  
  def seeAlso = resource.get(RDFS.SEEALSO).map(_.stringValue)  
  
  // TODO
  def realizationOf = None
  
}

/** Wraps an oa:Annotation RDF resource as an Annotation domain model primitive.
  *  
  * @constructor create a new AnnotationResource
  * @param resource the RDF resource to wrap
  */
private[parser] class AnnotationResource(resource: Resource) extends Annotation {

  def uri = resource.uri
  
  def hasBody = resource.get(OA.hasBody).map(_.stringValue)
  
  def hasTarget = resource.getFirst(OA.hasTarget).map(_.stringValue).getOrElse("_:empty") // '_:empty' should never happen!
  
  def motivatedBy: Option[String] = Some(resource.getFirst(OA.motivatedBy).map(_.stringValue).getOrElse("geotagging")) // Default to geotagging
  
  // TODO 
  def annotatedBy: Option[Agent] = None

  // TODO
  def annotatedAt: Option[Date] = None
  
  // TODO
  def creator: Option[Agent] = None
  
  // TODO
  def created: Option[Date] = None
  
  def toponym: Option[String] = resource.getFirst(Pelagios.toponym).map(_.stringValue)
  
  // TODO
  def hasNeighbour: Seq[Neighbour] = Seq.empty[Neighbour] // TODO implement!   
  
}

