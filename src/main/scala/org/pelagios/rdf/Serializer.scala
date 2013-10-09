package org.pelagios.rdf

import java.io.{ File, OutputStream }
import org.pelagios.api._
import org.pelagios.rdf.vocab._
import org.openrdf.model.{ BNode, Model, Resource, URI, ValueFactory }
import org.openrdf.model.impl.LinkedHashModel
import org.openrdf.model.vocabulary.RDF
import org.openrdf.rio.{ Rio, RDFFormat, RDFWriter }

/** Utility object to serialize Pelagios data to RDF.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
object Serializer {
  
  private def serializeAgent(agent: Agent, model: Model): BNode = {
    val f = model.getValueFactory()
    val bnode = f.createBNode()
    model.add(bnode, FOAF.name, f.createLiteral(agent.name))
    bnode
  }
  
  private def serializeAnnotatedThing(thing: AnnotatedThing, model: Model): Unit = {
    val f = model.getValueFactory()
    val rdfThing = f.createURI(thing.uri) 
    model.add(rdfThing, RDF.TYPE, Pelagios.AnnotatedThing)
    
    // dcterms:title
    model.add(rdfThing, DCTerms.title, f.createLiteral(thing.title))
    
    // dcterms:identifier
    thing.identifier.map(id => model.add(rdfThing, DCTerms.identifier, f.createURI(id)))
    
    // dcterms:description
    thing.description.map(description => model.add(rdfThing, DCTerms.description, f.createLiteral(description)))
    
    // dcterms:source
    thing.sources.foreach(source => model.add(rdfThing, DCTerms.source, f.createURI(source)))
    
    // dcterms:temporal - TODO
    
    // dcterms:creator
    thing.creator.map(creator => model.add(rdfThing, DCTerms.creator, serializeAgent(creator, model)))
    
    // dcterms:contributor
    thing.contributors.foreach(contributor => model.add(rdfThing, DCTerms.contributor, serializeAgent(contributor, model)))
    
    // dcterms:language
    thing.languages.foreach(lang => model.add(rdfThing, DCTerms.language, f.createLiteral(lang)))
    
    thing.expressions.foreach(variant => {
      model.add(rdfThing, Pelagios.hasVariant, f.createURI(variant.uri))
      serializeAnnotatedThing(variant, model)
    })
      
    thing.annotations.foreach(annotation => {
      val annotationResource = f.createURI(annotation.uri)
        
      model.add(annotationResource, RDF.TYPE, OA.Annotation)
      model.add(annotationResource, Pelagios.relationship, GAWD.attestsTo)
      annotation.hasBody.foreach(body => model.add(annotationResource, OA.hasBody, f.createURI(body)))
      model.add(annotationResource, OA.hasTarget, f.createURI(annotation.hasTarget))
      annotation.motivatedBy.map(motivation => model.add(annotationResource, OA.motivatedBy, f.createLiteral(motivation)))
      annotation.toponym.map(toponym => model.add(annotationResource, Pelagios.toponym, f.createLiteral(annotation.toponym.get)))
        
      annotation.hasNext.map(neighbour => {
        if (neighbour.distance.isDefined) {
          // Serialize as blank node
          val bnode = f.createBNode()
          model.add(bnode, Pelagios.neighbour, f.createURI(neighbour.annotation.uri))
          model.add(bnode, Pelagios.distance, f.createLiteral(neighbour.distance.get))
          neighbour.unit.map(unit => model.add(bnode, Pelagios.unit, f.createLiteral(unit)))
          model.add(annotationResource, Pelagios.hasNext, bnode)
        } else {
          // Serialize just the neighbour URI
          model.add(annotationResource, Pelagios.hasNext, f.createURI(neighbour.annotation.uri))
        }
      })
    })
  }
  
  /** Builds an RDF Model from a list of [[AnnotatedThing]]s.
    * 
    * @param annotatedThings the annotated things
    * @return the RDF model
    */
  def toRDF(annotatedThings: Iterable[AnnotatedThing]): Model = {
    val model = new LinkedHashModel()
    model.setNamespace("oa", OA.NAMESPACE)
    model.setNamespace("dcterms", DCTerms.NAMESPACE)
    model.setNamespace("pelagios", Pelagios.NAMESPACE)
    model.setNamespace("gawd", GAWD.NAMESPACE)
    annotatedThings.foreach(thing => serializeAnnotatedThing(thing, model))
    model
  } 
  
  /** Writes a list of [[AnnotatedThing]]s to an RDF output file.
    *
    * @param annotatedThings the annotated things
    * @param out the output file
    * @param format the RDF serialization format to use 
    */
  def writeToFile(annotatedThings: Iterable[AnnotatedThing], out: File, format: RDFFormat) = {
    // TODO implement!
  }
  
}
