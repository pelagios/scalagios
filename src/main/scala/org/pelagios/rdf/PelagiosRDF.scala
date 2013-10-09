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
import org.openrdf.model.vocabulary.RDF
import org.openrdf.model.URI
import org.openrdf.model.Resource
import org.pelagios.api.Annotation
import org.openrdf.model.ValueFactory
import org.openrdf.model.impl.BNodeImpl
import org.pelagios.rdf.vocab.GAWD

object PelagiosRDF {
  
  private def serializeAnnotatedThing(thing: AnnotatedThing, model: RioModel): Unit = {
    val vf = model.getValueFactory
    val thingResource = vf.createURI(thing.uri) 
      
    model.add(thingResource, RDF.TYPE, Pelagios.AnnotatedThing)
    model.add(thingResource, DCTerms.title, vf.createLiteral(thing.title))
    thing.description.map(description => model.add(thingResource, DCTerms.description, vf.createLiteral(description)))
    
    thing.variants.foreach(variant => {
      model.add(thingResource, Pelagios.hasVariant, vf.createURI(variant.uri))
      serializeAnnotatedThing(variant, model)
    })
      
    thing.annotations.foreach(annotation => {
      val annotationResource = vf.createURI(annotation.uri)
        
      model.add(annotationResource, RDF.TYPE, OA.Annotation)
      model.add(annotationResource, Pelagios.relationship, GAWD.attestsTo)
      annotation.hasBody.foreach(body => model.add(annotationResource, OA.hasBody, vf.createURI(body)))
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
          neighbour.unit.map(unit => model.add(bnode, Pelagios.unit, vf.createLiteral(unit)))
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
    model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#")
    model.setNamespace("gawd", GAWD.NAMESPACE)

    annotatedThings.foreach(thing => serializeAnnotatedThing(thing, model))
    
    model
  } 
  
}
