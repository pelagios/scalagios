package org.pelagios

import java.io.InputStream
import org.openrdf.rio.{ RDFFormat, Rio }
import org.pelagios.rdf.parser.{ TripleCounter, PlaceCounter }

private[pelagios] trait ScalagiosCounter {
  
  import Scalagios._

  def countTriples(is: InputStream, format: RDFFormat): Long = {
    val parser = Rio.createParser(format)
    val handler = new TripleCounter()
    parser.setRDFHandler(handler)
    parser.parse(is, BASE_URI)
    handler.getTotal()    
  }
  
  def countTriples(is: InputStream, filename: String): Long =    
    countTriples(is, guessFormatFromFilename(filename).get)
  
  def countPlaces(is: InputStream, format: RDFFormat): Long = {
    val parser = Rio.createParser(format)
    val handler = new PlaceCounter()
    parser.setRDFHandler(handler)
    parser.parse(is, BASE_URI)
    handler.getTotal()        
  }
  
  def countPlaces(is: InputStream, filename: String): Long =
    countPlaces(is, guessFormatFromFilename(filename).get)
  
}