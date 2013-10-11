package org.pelagios.rdf

import java.io.{ File, OutputStream }
import org.pelagios.api._
import org.pelagios.rdf.vocab._
import org.openrdf.model.{ BNode, Model, Resource, URI, ValueFactory }
import org.openrdf.model.impl.LinkedHashModel
import org.openrdf.model.vocabulary.RDF
import org.openrdf.rio.{ Rio, RDFFormat, RDFWriter }
import org.openrdf.model.vocabulary.RDFS
import org.callimachusproject.io.TurtleStreamWriterFactory
import java.io.FileOutputStream

/** Utility object to serialize Pelagios data to RDF.
  *  
  * TODO agents are currently serialized as blank nodes - change this!  
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
    
    // foaf:homepage
    thing.homepage.map(homepage => model.add(rdfThing, FOAF.homepage, f.createURI(homepage)))
    
    // foaf:thumbnails
    thing.thumbnails.foreach(thumbnail => model.add(rdfThing, FOAF.thumbnail, f.createURI(thumbnail)))
    
    // dcterms:bibliographicCitation
    thing.bibliographicCitations.foreach(citation => model.add(rdfThing, DCTerms.bibliographicCitation, f.createURI(citation)))
    
    // dcterms:subject
    thing.subjects.foreach(subject => model.add(rdfThing, DCTerms.subject, f.createURI(subject)))
    
    // rdfs:seeAlso
    thing.seeAlso.foreach(seeAlso => model.add(rdfThing, RDFS.SEEALSO, f.createURI(seeAlso)))    
    
    // frbr:realizationOf
    thing.realizationOf.map(work => model.add(rdfThing, FRBR.realizationOf, f.createURI(work.uri)))
    
    // Expressions
    thing.expressions.foreach(expression => serializeAnnotatedThing(expression, model))
      
    // Annotations
    thing.annotations.foreach(annotation => {
      val rdfAnnotation = f.createURI(annotation.uri)
      model.add(rdfAnnotation, RDF.TYPE, OA.Annotation)
      
      // oa:hasBody
      annotation.places.foreach(body => model.add(rdfAnnotation, OA.hasBody, f.createURI(body)))
      
      // oa:hasTarget
      model.add(rdfAnnotation, OA.hasTarget, f.createURI(annotation.hasTarget))
      
      // oa:motivatedBy
      annotation.motivatedBy.map(motivation => model.add(rdfAnnotation, OA.motivatedBy, f.createLiteral(motivation)))
      
      // oa:annotatedBy
      annotation.annotatedBy.map(annotator => model.add(rdfAnnotation, OA.annotatedBy, serializeAgent(annotator, model)))
      
      // oa:annotatedAt
      annotation.annotatedAt.map(date => model.add(rdfAnnotation, OA.annotatedAt, f.createLiteral(date)))
      
      // dcterms:creator
      annotation.creator.map(creator => model.add(rdfAnnotation, DCTerms.creator, serializeAgent(creator, model)))
      
      // dcterms:created
      annotation.created.map(date => model.add(rdfAnnotation, DCTerms.created, f.createLiteral(date)))
            
      // pelagios:toponym
      annotation.toponym.map(toponym => model.add(rdfAnnotation, Pelagios.toponym, f.createLiteral(annotation.toponym.get)))
      
      // pelagios:hasNext
      annotation.hasNeighbour.foreach(neighbour => {
        val relation = if (neighbour.directional) Pelagios.hasNext else Pelagios.hasNeighbour
        if (neighbour.hasMetadata) {
          val bnode = f.createBNode()
          model.add(bnode, Pelagios.neighbourURI, f.createURI(neighbour.annotation.uri))
          neighbour.distance.map(distance => model.add(bnode, Pelagios.neighbourDistance, f.createLiteral(neighbour.distance.get)))
          neighbour.unit.map(unit => model.add(bnode, Pelagios.distanceUnit, f.createLiteral(unit)))
          model.add(rdfAnnotation, relation, bnode)
        } else {
          model.add(rdfAnnotation, relation, f.createURI(neighbour.annotation.uri))
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
    model.setNamespace("frbr", FRBR.NAMESPACE)
    model.setNamespace("dcterms", DCTerms.NAMESPACE)
    model.setNamespace("pelagios", Pelagios.NAMESPACE)
    annotatedThings.foreach(thing => serializeAnnotatedThing(thing, model))
    model
  }

  /** Writes a list of [[AnnotatedThing]]s to an output stream.
    *
    * @param annotatedThings the annotated things
    * @param out the output stream
    * @param format the RDF serialization format
    */
  def writeToStream(annotatedThings: Iterable[AnnotatedThing], out: OutputStream, format: RDFFormat) = {
    if (format.equals(RDFFormat.TURTLE)) {
      // A little hack - for turtle we'll use a custom serializer that handles blank nodes
      Rio.write(toRDF(annotatedThings), new TurtleStreamWriterFactory().createWriter(out, null))
    } else {
      Rio.write(toRDF(annotatedThings), out, format)
    } 
  }
  
  /** Writes a list of [[AnnotatedThing]]s to an RDF output file.
    *
    * @param annotatedThings the annotated things
    * @param out the output file
    * @param format the RDF serialization format
    */
  def writeToFile(annotatedThings: Iterable[AnnotatedThing], out: File, format: RDFFormat) =
    writeToStream(annotatedThings, new FileOutputStream(out), format)
  
}
