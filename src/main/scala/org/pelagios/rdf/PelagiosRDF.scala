package org.pelagios.rdf

import scala.collection.JavaConverters._
import org.pelagios.api.AnnotatedThing
import org.openrdf.model.Model
import org.openrdf.model.vocabulary.RDF
import org.pelagios.rdf.vocab.Pelagios
import org.pelagios.rdf.vocab.DCTerms
import org.openrdf.model.impl.LinkedHashModel
import java.io.OutputStream
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.RDFWriter
import org.openrdf.rio.Rio
import org.pelagios.rdf.vocab.OA
import org.openrdf.model.impl.TreeModel

object PelagiosRDF {
  
  def toRDF(annotatedThings: Iterable[AnnotatedThing]): Model = {
    val model = new LinkedHashModel()
    val vf = model.getValueFactory

    model.setNamespace("pelagios", Pelagios.namespace)
    model.setNamespace("dcterms", DCTerms.namespace)
    model.setNamespace("oa", OA.namespace)
    
    annotatedThings.foreach(thing => {
      val thingResource = vf.createURI(thing.uri) 
      
      model.add(thingResource, RDF.TYPE, Pelagios.AnnotatedThing)
      if (thing.title.isDefined)
        model.add(thingResource, DCTerms.title, vf.createLiteral(thing.title.get))
      if (thing.description.isDefined)
        model.add(thingResource, DCTerms.description, vf.createLiteral(thing.description.get))
      
      thing.annotations.foreach(annotation => {
        val annotationResource = vf.createURI(annotation.uri)
        
        model.add(annotationResource, RDF.TYPE, OA.Annotation)
        model.add(annotationResource, OA.hasBody, vf.createURI(annotation.hasBody))
        model.add(annotationResource, OA.hasTarget, vf.createURI(annotation.hasTarget))
        annotation.motivatedBy.map(motivation => model.add(annotationResource, OA.motivatedBy, vf.createLiteral(motivation)))
        annotation.toponym.map(toponym => model.add(annotationResource, Pelagios.toponym, vf.createLiteral(annotation.toponym.get)))
        
        annotation.hasNext.map(neighbour => {
          /* if (neighbour.distance.isDefined) {
            // Serialize as blank node
            val bnode = vf.createBNode()
            model.add(bnode, Pelagios.neighbour, vf.createURI(neighbour.annotation.uri))
            model.add(bnode, Pelagios.distance, vf.createLiteral(neighbour.distance.get))
            model.add(annotationResource, Pelagios.hasNext, bnode)
          } else { */
            // Serialize just the neighbour URI
            model.add(annotationResource, Pelagios.hasNext, vf.createURI(neighbour.annotation.uri))
          // }
        })
      })
    })
    
    model
  } 
  
}
