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
import org.pelagios.legacy.api.{Place => LegacyPlace}
import org.pelagios.legacy.rdf.parser.PlaceCollector

/** A utility to parse & write Pelagios data.
  *
  * @author Rainer Simon <rainer.simon@ait.ac.at>  
  */
object Scalagios {
  
  /** Parses a Pelagios annotation dump file.
    *
    * @param file the dump file to parse
    * @return the list of annotated things, with annotations in-lined
    */
  def parseDataFile(file: File): Iterable[AnnotatedThing] = {
    val parser = getParser(file.getName)
    val handler = new PelagiosDataParser
    parser.setRDFHandler(handler)
    parser.parse(new FileInputStream(file), new URI(file.getAbsolutePath()).toString)
    handler.annotatedThings    
  }
  
  /** Parses a Pelagios-style gazetteer dump file.
    *
    *  @param file the gazetteer dump file to parse
    *  @return the places, with names and locations in-lined  
    */
  def parseGazetteerFile(file: File): Iterable[Place] = {
    val parser = getParser(file.getName)
    val handler = new GazetteerParser
    parser.setRDFHandler(handler)
    parser.parse(new FileInputStream(file), new URI(file.getAbsolutePath()).toString)
    handler.places
  }
  
  private[pelagios] def getParser(file: String) = file match {
    case f if f.endsWith("ttl") => new TurtleParserFactory().getParser()
    case f if f.endsWith("rdf") => new RDFXMLParserFactory().getParser()
    case f if f.endsWith("n3") => new N3ParserFactory().getParser()
    case _ => throw new UnsupportedRDFormatException("Format not supported")
  }
  
  // TODO implement serialization/write Pelagios data
  
  /** Returns a handle on the legacy import- and migration utilities. **/
  def Legacy: { def parseOAC(file: File): Iterable[GeoAnnotation]; 
                def migrateOAC(source: File, destination: File): Unit;
                def parsePleiadesDumps(files: Seq[File]): Seq[LegacyPlace];
                def migratePleiadesDumps(sources: Seq[File], destination: File) } = LegacyInterop
  
}


/** Legacy import and migration utilities. **/
private object LegacyInterop {
  
  /** Parses a data dump in the old OAC-based Pelagios format into the legacy API.
    *
    * @param file the legacy dump file
    * @return a list of legacy [[org.pelagios.legacy.api.GeoAnnotation]] objects  
    */
  def parseOAC(file: File): Iterable[GeoAnnotation] = {
    val parser = Scalagios.getParser(file.getName)
    val annotationCollector = new AnnotationCollector
    parser.setRDFHandler(annotationCollector)
    parser.parse(new FileInputStream(file), "http://pelagios.github.io/")
    annotationCollector.getAnnotations
  }
  
  /** A helper method to migrate an old OAC dump file into the new Pelagios format.
    * 
    * Note that the migration will result in a very 'underpopulated' dump file. The 
    * new Pelagios model is much richer, and has lots of information that is not
    * available in OAC dump files.
    * 
    * @param source the source file location
    * @param destination the destination file location 
    */
  def migrateOAC(source: File, destination: File) = {
    if (!destination.exists)
      destination.createNewFile
      
    val writer = new PrintWriter(destination)
      
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
  
  def parsePleiadesDumps(files: Seq[File]): Seq[LegacyPlace] = files.map(file => {
    println("Parsing file " + file.getName)
    
    val parser = Scalagios.getParser(file.getName)
    val placeCollector = new PlaceCollector      
    parser.setRDFHandler(placeCollector)
    parser.parse(new FileInputStream(file), "http://pelagios.github.io/")
    placeCollector.getPlaces     
  }).flatten
  
  def migratePleiadesDumps(sources: Seq[File], destination: File) = {
    if (!destination.exists)
      destination.createNewFile
      
    val writer = new PrintWriter(destination)
      
    // Write header
    writer.println("@prefix pelagios: <http://pelagios.github.com/terms#> .")
    writer.println("@prefix pleiades: <http://pleiades.stoa.org/places/vocab#> .")
    writer.println("@prefix dcterms: <http://purl.org/dc/terms/> .\n")   
    
    val places = parsePleiadesDumps(sources)
    places.foreach(place => {
      writer.println("<" + place.uri + "> a pelagios:PlaceRecord ;")
      writer.println("  dcterms:title \"" + place.label.getOrElse("[unknown]").replaceAll("\\\"", "\\\\\"") + "\" ;")
      if (place.comment.isDefined)
        writer.println("  dcterms:description \"" + place.comment.get.replaceAll("\\\"", "\\\\\"") + "\" ;")
      if (place.featureType.isDefined)
        writer.println("  dcterms:subject <" + place.featureType.get + "> ;")
        
      if (place.altLabels.isDefined && !place.altLabels.get.isEmpty)
        place.altLabels.get.split(",").foreach(altLabel => { 
          writer.println("  pleiades:hasName [ skos:label \"" + altLabel.trim + "\" ] ;")
        })
        
      if (place.coverage.isDefined && !place.coverage.get.isEmpty)
        place.coverage.get.split(",").foreach(altLabel => { 
          writer.println("  pleiades:hasName [ skos:label \"" + altLabel.trim + "\" ] ;")
        })
        
      writer.println("  .\n")
    })
    
    writer.flush
    writer.close
    println("Migrated " + places.size + " places")
  }
  
}