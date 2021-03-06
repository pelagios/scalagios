package org.pelagios.rdf.serializer

import java.io.{ File, FileOutputStream, OutputStream }
import org.callimachusproject.io.TurtleStreamWriterFactory
import org.openrdf.model.{ URI, BNode, Model }
import org.openrdf.model.impl.LinkedHashModel
import org.openrdf.model.vocabulary.{ RDF, RDFS }
import org.openrdf.rio.{ Rio, RDFFormat, UnsupportedRDFormatException }
import org.pelagios.Scalagios
import org.pelagios.api._
import org.pelagios.api.annotation.{ AnnotatedThing, SpecificResource, Transcription, TranscriptionType }
import org.pelagios.api.annotation.selector.TextOffsetSelector
import org.pelagios.rdf.vocab._
import org.openrdf.model.vocabulary.DCTERMS

/** Utility object to serialize Pelagios data to RDF.
  *  
  * TODO agents are currently serialized as blank nodes - change this!  
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
object PelagiosDataSerializer {
  
  private def serializeAgent(agent: Agent, model: Model): URI = {
    val f = model.getValueFactory()
    val agentNode = f.createURI(agent.uri.get)
    model.add(agentNode, RDF.TYPE, FOAF.Agent)
    agent.name.map(n => model.add(agentNode, FOAF.name, f.createLiteral(n)))
    agentNode
  }
  
  private def serializeTranscription(transcription: Transcription, model: Model): BNode = {
    val f = model.getValueFactory()
    val bnode = f.createBNode()
    model.add(bnode, RDFS.LABEL, f.createLiteral(transcription.chars))
    
    val bodyType = transcription.nameType match {
      case TranscriptionType.Toponym => Pelagios.Toponym
      case TranscriptionType.Metonym => Pelagios.Metonym
      case TranscriptionType.Ethnonym => Pelagios.Ethnonym
    }
    model.add(bnode, RDF.TYPE, bodyType)
    
    transcription.lang.map(lang => model.add(bnode, DCTerms.language, f.createLiteral(lang)))
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
    
    // dcterms:isPartOf
    thing.isPartOf.map(work => model.add(rdfThing, DCTerms.isPartOf, f.createURI(work.uri)))
    
    // Expressions
    thing.parts.foreach(expression => serializeAnnotatedThing(expression, model))
      
    // Annotations
    thing.annotations.foreach(annotation => {
      val rdfAnnotation = f.createURI(annotation.uri)

      // oa:hasTarget
      if (annotation.hasTarget.hasSelector.isDefined) {
        val selector = annotation.hasTarget.hasSelector.get.asInstanceOf[TextOffsetSelector]        
        val selectorNode = f.createBNode()
        model.add(selectorNode, RDF.TYPE, OAX.TextOffsetSelector)
        model.add(selectorNode, OAX.offset, f.createLiteral(selector.offset))
        model.add(selectorNode, OAX.range, f.createLiteral(selector.range))

        val specificResource = annotation.hasTarget.asInstanceOf[SpecificResource]
        val specificResourceNode = f.createBNode()
        model.add(specificResourceNode, RDF.TYPE, OA.SpecificResource)
        model.add(specificResourceNode, OA.hasSource, f.createLiteral(specificResource.hasSource.get.uri))
        model.add(specificResourceNode, OA.hasSelector, selectorNode)
        
        model.add(rdfAnnotation, RDF.TYPE, OA.Annotation)
        model.add(rdfAnnotation, OA.hasTarget, specificResourceNode)
      } else {
        model.add(rdfAnnotation, RDF.TYPE, OA.Annotation)
        model.add(rdfAnnotation, OA.hasTarget, f.createURI(annotation.hasTarget.asInstanceOf[AnnotatedThing].uri))
      }
      
      // oa:hasBody (place)
      annotation.places.foreach(uri => model.add(rdfAnnotation, OA.hasBody, f.createURI(uri)))
      
      // oa:hasBody (transcription)
      annotation.transcription.map(transcription => model.add(rdfAnnotation, OA.hasBody, serializeTranscription(transcription, model)))
            
      // pelagios:relation
      annotation.relation.map(relation => model.add(rdfAnnotation, Pelagios.relation, f.createLiteral(relation.toString)))
      
      // oa:annotatedBy
      annotation.annotatedBy.map(annotator => model.add(rdfAnnotation, OA.annotatedBy, serializeAgent(annotator, model)))
      
      // oa:annotatedAt
      annotation.annotatedAt.map(date => model.add(rdfAnnotation, OA.annotatedAt, f.createLiteral(date)))
      
      // oa:serializedBy
      annotation.serializedBy.map(serializer => model.add(rdfAnnotation, OA.serializedBy, serializeAgent(serializer, model)))
      
      // dcterms:creator
      annotation.creator.map(creator => model.add(rdfAnnotation, DCTerms.creator, serializeAgent(creator, model)))
      
      // dcterms:created
      annotation.created.map(date => model.add(rdfAnnotation, DCTerms.created, f.createLiteral(date)))
            
      // TODO pelagios:toponym
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
    model.setNamespace("oax", OAX.NAMESPACE)
    model.setNamespace("xs", "http://www.w3.org/2001/XMLSchema#")
    model.setNamespace("dcterms", DCTerms.NAMESPACE)
    model.setNamespace("pelagios", Pelagios.NAMESPACE)
    model.setNamespace("foaf", FOAF.NAMESPACE)
    
    annotatedThings.foreach(thing => serializeAnnotatedThing(thing, model))
    model
  }

  /** Writes a list of [[AnnotatedThing]]s to an output stream.
    *
    * @param annotatedThings the annotated things
    * @param out the output stream
    * @param format the RDF serialization format
    */
  def writeToStream(annotatedThings: Iterable[AnnotatedThing], out: OutputStream, format: String) = {
    if (format.equalsIgnoreCase(Scalagios.TURTLE)) {
      // A little hack - for turtle we'll use a custom serializer that handles blank nodes
      Rio.write(toRDF(annotatedThings), new TurtleStreamWriterFactory().createWriter(out, null))
    } else {
      Scalagios.getFormatForExtension(format) match {
        case Some(format) => Rio.write(toRDF(annotatedThings), out, format)
        case _ => throw new UnsupportedRDFormatException("Cannot determine RDF format for " + format)   
      }
    } 
  }
  
  /** Writes a list of [[AnnotatedThing]]s to an RDF output file.
    *
    * @param annotatedThings the annotated things
    * @param out the output file
    * @param format the RDF serialization format
    */
  def writeToFile(annotatedThings: Iterable[AnnotatedThing], out: File, format: String) =
    writeToStream(annotatedThings, new FileOutputStream(out), format)
  
}
