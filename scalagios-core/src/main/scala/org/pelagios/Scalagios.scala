package org.pelagios

import org.openrdf.rio.{ RDFParser, UnsupportedRDFormatException }
import org.openrdf.rio.n3.N3ParserFactory
import org.openrdf.rio.rdfxml.RDFXMLParserFactory
import org.openrdf.rio.turtle.TurtleParserFactory

/** A utility to parse & write Pelagios data.
  *
  * @author Rainer Simon <rainer.simon@ait.ac.at>  
  */
object Scalagios extends ScalagiosReader with ScalagiosWriter {
  
  /** OpenRDF requires a 'base URI' for parsing RDF from file - but doesn't actually seem to do anything with it **/
  private[pelagios] val BASE_URI = "http://pelagios.org"
    
  /** Gets a parser for a file extension **/
  private[pelagios] def getParser(filename: String): RDFParser = filename match {
    case f if f.endsWith("ttl") => new TurtleParserFactory().getParser()
    case f if f.endsWith("rdf") => new RDFXMLParserFactory().getParser()
    case f if f.endsWith("n3") => new N3ParserFactory().getParser()
    case _ => throw new UnsupportedRDFormatException("Format not supported")
  }
  
}