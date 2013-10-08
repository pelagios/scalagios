package org.pelagios.rdf

import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.Property
import org.openrdf.model.URI
import org.openrdf.model.Resource
import com.hp.hpl.jena.rdf.model.Model
import org.pelagios.api.AnnotatedThing
import org.pelagios.rdf.vocab.Pelagios
import org.pelagios.rdf.vocab.DCTerms
import org.pelagios.rdf.vocab.OA
import org.openrdf.model.vocabulary.RDF
import org.pelagios.api.Annotation
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.datatypes.RDFDatatype
import java.lang.Double

object PelagiosRDFJena {

  private val jenaFactory = ModelFactory.createDefaultModel()

  implicit def asJena(property: URI): Property = jenaFactory.createProperty(property.stringValue)
  
  implicit def asJena(resource: Resource) = jenaFactory.createResource(resource.stringValue)
  
  private def serializeAnnotatedThing(thing: AnnotatedThing, model: Model): Unit = {
    val thingResource = model.createResource(thing.uri)
    thingResource.addProperty(RDF.TYPE, Pelagios.AnnotatedThing) 
    thingResource.addProperty(DCTerms.title, thing.title)
    
    thing.variants.foreach(variant => serializeAnnotatedThing(variant, model))
    
    thing.annotations.foreach(annotation => {
      val annotationResource = model.createResource(annotation.uri)
        
      annotationResource.addProperty(RDF.TYPE, OA.Annotation)
      annotationResource.addProperty(OA.hasBody, model.createResource(annotation.hasBody))
      annotationResource.addProperty(OA.hasTarget, model.createResource(annotation.hasTarget))
        
      annotation.hasNext.map(neighbour => {
        val neighbourResource = model.createResource()
        neighbourResource.addProperty(Pelagios.neighbour, neighbour.annotation.uri)        
        neighbour.distance.map(distance => neighbourResource.addProperty(Pelagios.distance, model.createTypedLiteral(new Double(distance))))
        neighbour.unit.map(unit => neighbourResource.addProperty(Pelagios.unit, model.createLiteral(unit)))
        annotationResource.addProperty(Pelagios.hasNext, neighbourResource)
      })      
    }) 
      
    // model.createList(thing.annotations.map(toRDFNode(_)).toArray)    
  }
  
  def toRDFJena(annotatedThings: Iterable[AnnotatedThing]): Model = {
    val model = ModelFactory.createDefaultModel()
    model.setNsPrefix("pelagios", Pelagios.NAMESPACE)
    model.setNsPrefix("dcterms", DCTerms.NAMESPACE)
    model.setNsPrefix("oa", OA.NAMESPACE)
        
    annotatedThings.foreach(thing => serializeAnnotatedThing(thing, model))
    
    model
  }
  
}