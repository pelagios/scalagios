package org.pelagios.rdf

import scala.collection.JavaConverters._
import org.pelagios.api.AnnotatedThing
import org.openrdf.model.{Model => RioModel}
import org.pelagios.rdf.vocab.Pelagios
import org.pelagios.rdf.vocab.DCTerms
import org.openrdf.model.impl.LinkedHashModel
import java.io.OutputStream
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.RDFWriter
import org.openrdf.rio.Rio
import org.pelagios.rdf.vocab.OA
import org.openrdf.model.impl.TreeModel
import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.rdf.model.Model
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.URI
import org.openrdf.model.Resource
import com.hp.hpl.jena.rdf.model.Property
import org.pelagios.api.Annotation
import com.hp.hpl.jena.rdf.model.RDFNode
import org.openrdf.model.ValueFactory
import org.openrdf.model.impl.BNodeImpl
  

object PelagiosRDF {

  /*
  private val jenaFactory = ModelFactory.createDefaultModel()

  implicit def asJena(property: URI): Property = jenaFactory.createProperty(property.stringValue)
  
  implicit def asJena(resource: Resource) = jenaFactory.createResource(resource.stringValue)
  
  def toRDFJena(annotatedThings: Iterable[AnnotatedThing]): Model = {
    val model = ModelFactory.createDefaultModel()
    model.setNsPrefix("pelagios", Pelagios.NAMESPACE)
    model.setNsPrefix("dcterms", DCTerms.NAMESPACE)
    model.setNsPrefix("oa", OA.NAMESPACE)
    
    annotatedThings.foreach(thing => {
      val thingResource = model.createResource(thing.uri)
      thingResource.addProperty(RDF.TYPE, Pelagios.AnnotatedThing) 
      thing.title.map(title => thingResource.addProperty(DCTerms.title, title))
      
      def toRDFNode(annotation: Annotation): RDFNode = {
        val annotationResource = model.createResource(annotation.uri)
        
        annotationResource.addProperty(RDF.TYPE, OA.Annotation)
        annotationResource.addProperty(OA.hasBody, model.createResource(annotation.hasBody))
        annotationResource.addProperty(OA.hasTarget, model.createResource(annotation.hasTarget))
        
        annotation.hasNext.map(neighbour => {
          val neighbourResource = model.createResource()
          neighbourResource.addProperty(Pelagios.neighbour, neighbour.annotation.uri)
          neighbourResource.addProperty(Pelagios.distance, model.createLiteral(neighbour.distance.get))
          annotationResource.addProperty(Pelagios.hasNext, neighbourResource)
        })
        
        annotationResource        
      } 
      
      model.createList(thing.annotations.map(toRDFNode(_)).toArray)
    })
    
    model
  }*/
  
  private def serializeAnnotatedThing(thing: AnnotatedThing, model: RioModel): Unit = {
    val vf = model.getValueFactory
    val thingResource = vf.createURI(thing.uri) 
      
    model.add(thingResource, RDF.TYPE, Pelagios.AnnotatedThing)
    model.add(thingResource, DCTerms.title, vf.createLiteral(thing.title))
    if (thing.description.isDefined)
      model.add(thingResource, DCTerms.description, vf.createLiteral(thing.description.get))
    
    thing.variants.foreach(variant => serializeAnnotatedThing(variant, model))
      
    thing.annotations.foreach(annotation => {
      val annotationResource = vf.createURI(annotation.uri)
        
      model.add(annotationResource, RDF.TYPE, OA.Annotation)
      model.add(annotationResource, OA.hasBody, vf.createURI(annotation.hasBody))
      model.add(annotationResource, OA.hasTarget, vf.createURI(annotation.hasTarget))
      annotation.motivatedBy.map(motivation => model.add(annotationResource, OA.motivatedBy, vf.createLiteral(motivation)))
      annotation.toponym.map(toponym => model.add(annotationResource, Pelagios.toponym, vf.createLiteral(annotation.toponym.get)))
        
      annotation.hasNext.map(neighbour => {
        if (neighbour.distance.isDefined) {
          // Serialize as blank node
          val bnode = vf.createBNode()
          model.add(annotationResource, Pelagios.hasNext, bnode)
          model.add(bnode, Pelagios.neighbour, vf.createURI(neighbour.annotation.uri))
          model.add(bnode, Pelagios.distance, vf.createLiteral(neighbour.distance.get))
        } else {
          // Serialize just the neighbour URI
          model.add(annotationResource, Pelagios.hasNext, vf.createURI(neighbour.annotation.uri))
        }
      })
    })
  }
  
  def toRDF(annotatedThings: Iterable[AnnotatedThing]): RioModel = {
    val model = new LinkedHashModel()
    model.setNamespace("oa", OA.NAMESPACE)
    model.setNamespace("dcterms", DCTerms.NAMESPACE)
    model.setNamespace("pelagios", Pelagios.NAMESPACE)

    annotatedThings.foreach(thing => serializeAnnotatedThing(thing, model))
    
    model
  } 
  
}
