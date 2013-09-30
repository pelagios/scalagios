package org.pelagios

import org.openrdf.rio.n3.N3ParserFactory
import org.pelagios.legacy.rdf.parser.AnnotationCollector
import java.io.FileInputStream
import java.io.File
import org.openrdf.rio.UnsupportedRDFormatException
import org.openrdf.rio.turtle.TurtleParserFactory
import org.openrdf.rio.rdfxml.RDFXMLParserFactory
import org.pelagios.legacy.api.GeoAnnotation
import java.io.PrintWriter
import org.pelagios.api.AnnotatedThing
import org.pelagios.rdf.parser.PelagiosDataParser
import java.net.URI
import org.pelagios.rdf.parser.GazetteerParser
import org.pelagios.api.Place

/**
 * A utility to parse & write Pelagios data.
 */
object Scalagios {
  
  /**
   * Parses a Pelagios data dump file.
   */
  def parseDataFile(file: File): Iterable[AnnotatedThing] = {
    val parser = getParser(file.getName)
    val handler = new PelagiosDataParser
    parser.setRDFHandler(handler)
    parser.parse(new FileInputStream(file), new URI(file.getAbsolutePath()).toString)
    handler.annotatedThings    
  }
  
  /**
   * Parses a Pelagios-style gazetteer dump file.
   */
  def parseGazetteerFile(file: File): Iterable[Place] = {
    val parser = getParser(file.getName)
    val handler = new GazetteerParser
    parser.setRDFHandler(handler)
    parser.parse(new FileInputStream(file), new URI(file.getAbsolutePath()).toString)
    handler.places
  }
  
  private def getParser(file: String) = file match {
    case f if f.endsWith("ttl") => new TurtleParserFactory().getParser()
    case f if f.endsWith("rdf") => new RDFXMLParserFactory().getParser()
    case f if f.endsWith("n3") => new N3ParserFactory().getParser()
    case _ => throw new UnsupportedRDFormatException("Format not supported")
  }
  
  // TODO implement serialization/write Pelagios data
  
  /**
   * Returns a handle on the legacy import- and migration utilities.
   */
  def Legacy: { def parseOAC(file: String): Iterable[GeoAnnotation]; 
                def migrateOAC(source: String, destination: String): Unit } = LegacyInterop
  
}

/**
 * Legacy import and migration utilities.
 */
private object LegacyInterop {
  
  /**
   * A legacy method that parses a data dump in the old (OAC-based) Pelagios
   * format, auto-detecting the RDF serialization by the file extension.
   */
  def parseOAC(file: String): Iterable[GeoAnnotation] = {
    val parser = file match {
      case f if f.endsWith("ttl") => new TurtleParserFactory().getParser()
      case f if f.endsWith("rdf") => new RDFXMLParserFactory().getParser()
      case f if f.endsWith("n3") => new N3ParserFactory().getParser()
      case _ => throw new UnsupportedRDFormatException("Format not supported")
    } 
    
    val annotationCollector = new AnnotationCollector()
    parser.setRDFHandler(annotationCollector)
    parser.parse(new FileInputStream(new File(file)), "http://pelagios.github.io/")
    
    println(annotationCollector.triplesTotal + " triples total in file")
    println(annotationCollector.triplesProcessed + " triples processed during import")
    println(annotationCollector.annotationsTotal + " annotations imported")
    
    annotationCollector.getAnnotations
  }
  
  def migrateOAC(source: String, destination: String) = {
    val destFile = new File(destination)
    if (!destFile.exists)
      destFile.createNewFile
      
    val writer = new PrintWriter(destFile)
      
    // Write header
    writer.println("@prefix pelagios: <http://pelagios.github.io/terms#> .")
    writer.println("@prefix dcterms: <http://purl.org/dc/terms/> .")
    writer.println("@prefix foaf: <http://xmlns.com/foaf/0.1/> .")
    writer.println("@prefix oa: <http://www.w3.org/ns/oa#> .\n")
      
    parseOAC(source).foreach(annotation => {
      // Annotated thing
      writer.println("<" + annotation.target.uri + "> a pelagios:AnnotatedThing ;")
      if (annotation.target.title.isDefined)
        writer.println("  dcterms:title \"" + annotation.target.title.get.replaceAll("\\\"", "\\\\\"") + "\" ;")
      else if (annotation.title.isDefined)
        writer.println("  dcterms:title \"" + annotation.title.get.replaceAll("\\\"", "\\\\\"") + "\" ;")
      writer.println("  .")
      
      // Annotation
      writer.println("<" + annotation.uri + "> a oa:Annotation ;")
      writer.println("  oa:hasBody <" + annotation.body + "> ;")
      writer.println("  oa:hasTarget <" + annotation.target.uri + "> ;")
      writer.println("  oa:motivatedBy oa:geotagging ;")
      writer.println("  .\n")
    })
    
    writer.flush
    writer.close
  }
  
}