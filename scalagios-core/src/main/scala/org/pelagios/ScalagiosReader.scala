package org.pelagios

import java.io.InputStream
import org.openrdf.rio.{ Rio, RDFFormat, UnsupportedRDFormatException }
import org.pelagios.api.annotation.AnnotatedThing
import org.pelagios.api.dataset.Dataset
import org.pelagios.api.gazetteer.Place
import org.pelagios.api.gazetteer.patch.PlacePatch
import org.pelagios.rdf.parser.annotation.PelagiosDataParser
import org.pelagios.rdf.parser.dataset.DatasetCollector
import org.pelagios.rdf.parser.gazetteer.{ PlaceCollector, PlacePatchCollector, PlaceStreamHandler }


/** Functionality for reading Pelagios data 
  *
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
private[pelagios] trait ScalagiosReader {
  
  import Scalagios._

  /** Parses Pelagios annotations from an input stream **/
  def readAnnotations(is: InputStream, format: RDFFormat): Iterable[AnnotatedThing] = {
    val parser = Rio.createParser(format)
    val handler = new PelagiosDataParser()
    parser.setRDFHandler(handler)
    parser.parse(is, BASE_URI)
    handler.data      
  }
  
  /** Just for convenience: tries to guess the format for the specified filename **/
  def readAnnotations(is: InputStream, filename: String): Iterable[AnnotatedThing] =
    guessFormatFromFilename(filename) match {
      case Some(format) => readAnnotations(is, format)
      case _ => throw new UnsupportedRDFormatException("Cannot determine RDF format for " + filename)
  }
    
  /** Parses a Pelagios-style VoID dataset definition **/
  def readVoID(is: InputStream, format: RDFFormat): Iterable[Dataset] = {
	  val parser = Rio.createParser(format)
    val handler = new DatasetCollector()
    parser.setRDFHandler(handler)
    parser.parse(is, BASE_URI)
    handler.datasets
  }
  
  /** Just for convenience: tries to guess the format for the specified filename **/  
  def readVoID(is: InputStream, filename: String): Iterable[Dataset] =
    guessFormatFromFilename(filename) match {
      case Some(format) => readVoID(is, format)
      case _ => throw new UnsupportedRDFormatException("Cannot determine RDF format for " + filename)      
    }
  
  /** Parses a Pelagios gazetteer dump file **/
  def readPlaces(is: InputStream, format: RDFFormat): Iterable[Place] = {
	  val parser = Rio.createParser(format)
    val handler = new PlaceCollector()
    parser.setRDFHandler(handler)
    parser.parse(is, BASE_URI)
    handler.places
  }  
  
  /** Just for convenience: tries to guess the format for the specified filename **/  
  def readPlaces(is: InputStream, filename: String): Iterable[Place] =
    guessFormatFromFilename(filename) match {
      case Some(format) => readPlaces(is, format)
      case _ => throw new UnsupportedRDFormatException("Cannot determine RDF format for " + filename)      
    }
  
  /** Parses a Pelagios gazetteer patch file **/
  def readPlacePatches(is: InputStream, filename: String): Iterable[PlacePatch] = {
    val format = guessFormatFromFilename(filename)
    if (format.isDefined) {
      val parser = Rio.createParser(format.get)
      val handler = new PlacePatchCollector()
      parser.setRDFHandler(handler)
      parser.parse(is, BASE_URI)
      handler.patches
    } else {
      throw new UnsupportedRDFormatException("Cannot determine RDF format for " + filename)
    }
  }
  
  /**  Parses a 'streamable' gazetteer dump file from an input stream **/  
  def streamPlaces(is: InputStream, format: RDFFormat, streamHandler: Place => Unit, lowMemoryMode: Boolean): Unit = {
    val parser = Rio.createParser(format)
    val rdfHandler = new PlaceStreamHandler(streamHandler, lowMemoryMode)
    parser.setRDFHandler(rdfHandler)
    parser.parse(is, BASE_URI)
  }
  
  /** Just for convenience: tries to guess the format for the specified filename **/  
  def streamPlaces(is: InputStream, filename: String, streamHandler: Place => Unit, lowMemoryMode: Boolean = false): Unit =
    guessFormatFromFilename(filename) match {
      case Some(format) => streamPlaces(is, format, streamHandler, lowMemoryMode)
      case _ => throw new UnsupportedRDFormatException("Cannot determine RDF format for " + filename)      
    }
  
}
