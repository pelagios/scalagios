package org.pelagios

import org.openrdf.rio.{ RDFFormat, RDFParser, UnsupportedRDFormatException }
import org.openrdf.rio.n3.N3ParserFactory
import org.openrdf.rio.rdfxml.RDFXMLParserFactory
import org.openrdf.rio.turtle.TurtleParserFactory

/** A utility to parse & write Pelagios data.
  *
  * @author Rainer Simon <rainer.simon@ait.ac.at>  
  */
object Scalagios extends ScalagiosReader with ScalagiosWriter {
  
  val TURTLE = "ttl"
  
  val RDFXML = "rdf"
  
  val N3 = "n3"
  
  /** OpenRDF requires a 'base URI' for parsing RDF from file - but doesn't actually seem to do anything with it **/
  private[pelagios] val BASE_URI = "http://pelagios.org"
    
  /** Gets a parser for a file extension **/
  private[pelagios] def getParser(format: String): RDFParser = format.toLowerCase match {
    case f if f.endsWith(TURTLE) => new TurtleParserFactory().getParser()
    case f if f.endsWith(RDFXML) => new RDFXMLParserFactory().getParser()
    case f if f.endsWith(N3) => new N3ParserFactory().getParser()
    case _ => throw new UnsupportedRDFormatException("Format not supported")
  }
  
  private[pelagios] def getFormat(format: String): RDFFormat = format.toLowerCase match {
    case f if f.endsWith(TURTLE) => RDFFormat.TURTLE
    case f if f.endsWith(RDFXML) => RDFFormat.RDFXML
    case f if f.endsWith(N3) => RDFFormat.N3
    case _ => throw new UnsupportedRDFormatException("Format not supported")
  }
  
}
