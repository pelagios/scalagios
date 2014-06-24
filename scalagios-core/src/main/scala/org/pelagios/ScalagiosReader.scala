package org.pelagios

import java.io.InputStream
import org.openrdf.rio.{ RDFFormat, RDFParser, RDFParserRegistry }
import org.pelagios.api.annotation.AnnotatedThing
import org.pelagios.api.dataset.Dataset
import org.pelagios.api.gazetteer.Place
import org.pelagios.rdf.parser.annotation.PelagiosDataParser
import org.pelagios.rdf.parser.dataset.DatasetCollector
import org.pelagios.rdf.parser.gazetteer.{ PlaceCollector, PlaceStreamHandler }

/** Functionality for reading Pelagios data 
  *
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait ScalagiosReader {
  
  import Scalagios._

  /** Parses Pelagios annotations from an input stream, using a parser for the specified format.
    *  
    * @param is the input stream
    * @param format the RDF serialization format the data is in  
    */
  def readAnnotations(is: InputStream, format: RDFFormat): Iterator[AnnotatedThing] =
    readAnnotations(is, RDFParserRegistry.getInstance.get(format).getParser)

  /** Parses Pelagios annotations from an input stream, using a parser determined based on file extension.
    *  
    * @param is the input stream
    * @param filename the source filename 
    */
  def readAnnotations(is: InputStream, filename: String): Iterator[AnnotatedThing] =
    readAnnotations(is, getParser(filename))
  
  private def readAnnotations(is: InputStream, parser: RDFParser): Iterator[AnnotatedThing] = {
    val handler = new PelagiosDataParser()
    parser.setRDFHandler(handler)
    parser.parse(is, BASE_URI)
    handler.data      
  }

  
  
  
  
  /** Parses a Pelagios-style VoID dataset definition from an input stream, using a parser for the specified format.
    *  
    * @param is the input stream
    * @param format the RDF serialization format the data is in  
    */
  def readVoID(is: InputStream, format: RDFFormat): Iterator[Dataset] =
    readVoID(is, RDFParserRegistry.getInstance.get(format).getParser)
    
  /** Parses a Pelagios-style VoID dataset definition from an input stream, using a parser determined based on file extension.
    *  
    * @param is the input stream
    * @param filename the source filename 
    */
  def readVoID(is: InputStream, filename: String): Iterator[Dataset] =
    readVoID(is, getParser(filename))
    
  private def readVoID(is: InputStream, parser: RDFParser): Iterator[Dataset] = {
    val handler = new DatasetCollector()
    parser.setRDFHandler(handler)
    parser.parse(is, BASE_URI)
    handler.datasets
  }
  
  
  
  
  
  /** Parses a Pelagios gazetteer dump file from an input stream, using a parser for the specified format.
    *
    * @param is the input stream
    * @param format the RDF serialization format the data is in  
    */
  def readPlaces(is: InputStream, format: RDFFormat): Iterator[Place] =
    readPlaces(is, RDFParserRegistry.getInstance.get(format).getParser)
    
  /** Parses a Pelagios gazetteer dump file from an input stream, using a parser determined based on file extension.
    *  
    * @param is the input stream
    * @param filename the source filename 
    */
  def readPlaces(is: InputStream, filename: String): Iterator[Place] =
    readPlaces(is, getParser(filename))
  
  private def readPlaces(is: InputStream, parser: RDFParser): Iterator[Place] = {
    val handler = new PlaceCollector()
    parser.setRDFHandler(handler)
    parser.parse(is, BASE_URI)
    handler.places
  }  
    
  /**  Parses a 'streamable' gazetteer dump file from an input stream, using a parser determined based on file extension.
    *
    * @param is the input stream
    * @param filename the source filename 
    * @param streamHandler the handler function
    */  
  def streamPlaces(is: InputStream, filename: String, streamHandler: Place => Unit): Unit = {
    val parser = getParser(filename)
    val rdfHandler = new PlaceStreamHandler(streamHandler)
    parser.setRDFHandler(rdfHandler)
    parser.parse(is, BASE_URI)
  }
  
}