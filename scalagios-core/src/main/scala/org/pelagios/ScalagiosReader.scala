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
private[pelagios] trait ScalagiosReader {
  
  import Scalagios._

  /** Parses Pelagios annotations from an input stream in the specified RDF format.
    *  
    * @param is the input stream
    * @param format the RDF serialization format 
    */
  def readAnnotations(is: InputStream, format: String): Iterable[AnnotatedThing] = {
    val parser = getParser(format)
    val handler = new PelagiosDataParser()
    parser.setRDFHandler(handler)
    parser.parse(is, BASE_URI)
    handler.data      
  }
    
  /** Parses a Pelagios-style VoID dataset definition from an input stream in the specified RDF format.
    *  
    * @param is the input stream
    * @param format the RDF serialization format
    */    
  def readVoID(is: InputStream, format: String): Iterable[Dataset] = {
	val parser = getParser(format)
    val handler = new DatasetCollector()
    parser.setRDFHandler(handler)
    parser.parse(is, BASE_URI)
    handler.datasets
  }
  
  /** Parses a Pelagios gazetteer dump file from an input stream in the specified RDF format.
    *
    * @param is the input stream
    * @param format the RDF serialization format  
    */
  def readPlaces(is: InputStream, format: String): Iterable[Place] = {
	val parser = getParser(format)
    val handler = new PlaceCollector()
    parser.setRDFHandler(handler)
    parser.parse(is, BASE_URI)
    handler.places
  }  
    
  /**  Parses a 'streamable' gazetteer dump file from an input stream in the specified RDF format.
    *
    * @param is the input stream
    * @param format the RDF serialization format  
    * @param streamHandler the handler function
    */  
  def streamPlaces(is: InputStream, format: String, streamHandler: Place => Unit): Unit = {
    val parser = getParser(format)
    val rdfHandler = new PlaceStreamHandler(streamHandler)
    parser.setRDFHandler(rdfHandler)
    parser.parse(is, BASE_URI)
  }
  
}
