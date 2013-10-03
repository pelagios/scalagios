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
                def parsePleiadesDumps(files: Seq[File]): Seq[LegacyPlace] } = LegacyInterop
  
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
  
  def parsePleiadesDumps(files: Seq[File]): Seq[LegacyPlace] = files.map(file => {
    println("Parsing file " + file.getName)
    
    val parser = Scalagios.getParser(file.getName)
    val placeCollector = new PlaceCollector      
    parser.setRDFHandler(placeCollector)
    parser.parse(new FileInputStream(file), "http://pelagios.github.io/")
    placeCollector.getPlaces     
  }).flatten
  
}