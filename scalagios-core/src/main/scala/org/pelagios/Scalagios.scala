package org.pelagios

import org.openrdf.rio.n3.N3ParserFactory
import java.io.FileInputStream
import java.io.File
import org.openrdf.rio.UnsupportedRDFormatException
import org.openrdf.rio.turtle.TurtleParserFactory
import org.openrdf.rio.rdfxml.RDFXMLParserFactory
import java.io.PrintWriter
import org.pelagios.api.annotation.AnnotatedThing
import org.pelagios.rdf.parser.PelagiosDataParser
import java.net.URI
import org.pelagios.rdf.parser.GazetteerParser
import org.pelagios.api.gazetteer.Place
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
import org.pelagios.rdf.serializer.PelagiosDataSerializer
import org.pelagios.rdf.serializer.TTLTemplateSerializer
import org.pelagios.rdf.serializer.GazetteerSerializer
import scala.io.Source
import org.pelagios.api.dataset.Dataset
import org.pelagios.rdf.parser.VoIDParser

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
  def readAnnotations(file: String): Iterable[AnnotatedThing] = {
    val f = new File(file)
    readAnnotations(new FileInputStream(f), new URI("file://" + f.getAbsolutePath).toString, getParser(f.getName))
  }
  
  /** Parses Pelagios annotations from an input stream.
    *
    * @param is the input stream
    * @param baseURI the base URI
    * @param format the RDF serialization format the data is in  
    */
  def readAnnotations(is: InputStream, baseURI: String, format: RDFFormat): Iterable[AnnotatedThing] =
    readAnnotations(is, baseURI, RDFParserRegistry.getInstance.get(format).getParser)
  
  private def readAnnotations(is: InputStream, baseURI: String, parser: RDFParser): Iterable[AnnotatedThing] = {
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
  def writeAnnotations(data: Iterable[AnnotatedThing], out: OutputStream, format: RDFFormat) = {
    if (format == RDFFormat.TURTLE)
      TTLTemplateSerializer.writeToStream(data, out)
    else
      PelagiosDataSerializer.writeToStream(data, out, format)
  }

  /** Writes Pelagios data to an RDF file.
    * 
    * @param data the data
    * @param out the output file
    * @param format the RDF serialization format
    */
  def writeAnnotations(data: Iterable[AnnotatedThing], file: String, format: RDFFormat) = {
    if (format == RDFFormat.TURTLE)
      TTLTemplateSerializer.writeToFile(data, new File(file))
    else
      PelagiosDataSerializer.writeToFile(data, new File(file), format)
  }
    
  /** Parses a Pelagios-style gazetteer dump file.
    *
    * @param file the gazetteer dump file to parse
    * @return the places, with names and locations in-lined  
    */
  def readPlaces(file: File): Iterable[Place] =
    readPlaces(new FileInputStream(file), new URI(file.getAbsolutePath()).toString, getParser(file.getName))
  
  /** Parses Pelagios-style gazetteer data from an input stream.
    * 
    * @param is the input stream
    * @param baseURI the base URI
    * @param format the RDF serialization format the data is in
    */
  def readPlaces(is: InputStream, baseURI: String, format: RDFFormat): Iterable[Place] =
    readPlaces(is, baseURI, RDFParserRegistry.getInstance.get(format).getParser)
  
  private def readPlaces(is: InputStream, baseURI:String, parser: RDFParser): Iterable[Place] = {
    val handler = new GazetteerParser
    parser.setRDFHandler(handler)
    parser.parse(is, baseURI)
    handler.places    
  }
  
  def writePlaces(data: Iterable[Place], file: String, format: RDFFormat) =
    GazetteerSerializer.writeToFile(data, file, format)
  
  private[pelagios] def getParser(file: String): RDFParser = file match {
    case f if f.endsWith("ttl") => new TurtleParserFactory().getParser()
    case f if f.endsWith("rdf") => new RDFXMLParserFactory().getParser()
    case f if f.endsWith("n3") => new N3ParserFactory().getParser()
    case _ => throw new UnsupportedRDFormatException("Format not supported")
  }
  
  /** Parses a Pelagios-style VoID file.
    *  
    * @param file the VoID file to parse
    * @return the datasets
    */
  def readVoID(file: File): Iterable[Dataset] =
    readVoID(new FileInputStream(file), new URI(file.getAbsolutePath).toString, getParser(file.getName))    
  
  def readVoID(is: InputStream, baseURI: String, parser: RDFParser): Iterable[Dataset] = {
    val handler = new VoIDParser()
    parser.setRDFHandler(handler)
    parser.parse(is, baseURI)
    handler.datasets
  }
  
}