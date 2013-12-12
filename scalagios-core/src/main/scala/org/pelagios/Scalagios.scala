package org.pelagios

import org.openrdf.rio.n3.N3ParserFactory
import java.io.FileInputStream
import java.io.File
import org.openrdf.rio.UnsupportedRDFormatException
import org.openrdf.rio.turtle.TurtleParserFactory
import org.openrdf.rio.rdfxml.RDFXMLParserFactory
import java.io.PrintWriter
import org.pelagios.api.AnnotatedThing
import org.pelagios.rdf.parser.PelagiosDataParser
import java.net.URI
import org.pelagios.rdf.parser.GazetteerParser
import org.pelagios.api.Place
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
import org.pelagios.rdf.serializer.RDFSerializer
import org.pelagios.rdf.serializer.TTLTemplateSerializer

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
  def readFromFile(file: String): Iterable[AnnotatedThing] = {
    val f = new File(file)
    readFromStream(new FileInputStream(f), new URI("file://" + f.getAbsolutePath).toString, getParser(f.getName))
  }
  
  /** Parses Pelagios annotations from an input stream.
    *
    * @param is the input stream
    * @param baseURI the base URI
    * @param format the RDF serialization format the data is in  
    */
  def readFromStream(is: InputStream, baseURI: String, format: RDFFormat): Iterable[AnnotatedThing] =
    readFromStream(is, baseURI, RDFParserRegistry.getInstance.get(format).getParser)
  
  private def readFromStream(is: InputStream, baseURI: String, parser: RDFParser): Iterable[AnnotatedThing] = {
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
  def writeToStream(data: Iterable[AnnotatedThing], out: OutputStream, format: RDFFormat = RDFFormat.TURTLE) = {
    if (format == RDFFormat.TURTLE)
      TTLTemplateSerializer.writeToStream(data, out)
    else
      RDFSerializer.writeToStream(data, out, format)
  }

  /** Writes Pelagios data to an RDF file.
    * 
    * @param data the data
    * @param out the output file
    * @param format the RDF serialization format
    */
  def writeToFile(data: Iterable[AnnotatedThing], file: String, format: RDFFormat = RDFFormat.TURTLE) = {
    if (format == RDFFormat.TURTLE)
      TTLTemplateSerializer.writeToFile(data, new File(file))
    else
      RDFSerializer.writeToFile(data, new File(file), format)
  }
    
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
  
}