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
import org.openrdf.rio.RDFFormat
import java.io.InputStream
import org.openrdf.rio.RDFParser
import org.openrdf.rio.RDFParserFactory
import org.openrdf.rio.RDFParserRegistry
import java.io.OutputStream
import org.openrdf.model.Model
import org.openrdf.model.impl.TreeModel
import org.openrdf.rio.Rio
import java.io.FileOutputStream
import org.openrdf.rio.RDFWriterFactory
import org.callimachusproject.io.TurtleStreamWriterFactory
import java.io.Writer
import org.pelagios.rdf.Serializer

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
  def parseData(file: File): Iterable[AnnotatedThing] =
    parseData(new FileInputStream(file), new URI(file.getAbsolutePath()).toString, getParser(file.getName))
  
  /** Parses Pelagios annotations from an input stream.
    *
    * @param is the input stream
    * @param baseURI the base URI
    * @param format the RDF serialization format the data is in  
    */
  def parseData(is: InputStream, baseURI: String, format: RDFFormat): Iterable[AnnotatedThing] =
    parseData(is, baseURI, RDFParserRegistry.getInstance.get(format).getParser)
  
  private def parseData(is: InputStream, baseURI: String, parser: RDFParser): Iterable[AnnotatedThing] = {
    val handler = new PelagiosDataParser
    parser.setRDFHandler(handler)
    parser.parse(is, baseURI)
    handler.data      
  }

  /** Writes Pelagios data to an output stream.
    * 
    * @param data the data
    * @param out the output stream
    * @param format the RDF serialization format
    */
  def writeData(data: Iterable[AnnotatedThing], out: OutputStream, format: RDFFormat) =
    Serializer.writeToStream(data, out, format)

  /** Writes Pelagios data to an RDF file.
    * 
    * @param data the data
    * @param out the output file
    * @param format the RDF serialization format
    */
  def writeData(data: Iterable[AnnotatedThing], out: File, format: RDFFormat) =
    Serializer.writeToFile(data, out, format)
    
  /** Parses a Pelagios-style gazetteer dump file.
    *
    *  @param file the gazetteer dump file to parse
    *  @return the places, with names and locations in-lined  
    */
  def parseGazetteer(file: File): Iterable[Place] =
    parseGazetteer(new FileInputStream(file), new URI(file.getAbsolutePath()).toString, getParser(file.getName))
  
  /** Parses Pelagios-style gazetteer data from an input stream.
    * 
    * @param is the input stream
    * @param baseURI the base URI
    * @param format the RDF serialization format the data is in
    */
  def parseGazetteer(is: InputStream, baseURI: String, format: RDFFormat): Iterable[Place] =
    parseGazetteer(is, baseURI, RDFParserRegistry.getInstance.get(format).getParser)
  
  private def parseGazetteer(is: InputStream, baseURI:String, parser: RDFParser): Iterable[Place] = {
    val handler = new GazetteerParser
    parser.setRDFHandler(handler)
    parser.parse(is, baseURI)
    handler.places    
  }
  
  private[pelagios] def getParser(file: String): RDFParser = file match {
    case f if f.endsWith("ttl") => new TurtleParserFactory().getParser()
    case f if f.endsWith("rdf") => new RDFXMLParserFactory().getParser()
    case f if f.endsWith("n3") => new N3ParserFactory().getParser()
    case _ => throw new UnsupportedRDFormatException("Format not supported")
  }
  
  /** Returns a handle on the legacy import- and migration utilities. **/
  def Legacy: { def parseOAC(file: File): Iterable[GeoAnnotation]; 
                def parsePleiadesDumps(files: Seq[File]): Seq[LegacyPlace] } = LegacyInterop
  
}

/** Legacy import utilities. **/
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