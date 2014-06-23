package org.pelagios.rdf

import java.io.InputStream
import org.openrdf.rio.{ RDFFormat, RDFParserRegistry, UnsupportedRDFormatException }
import org.openrdf.rio.turtle.TurtleParserFactory
import org.openrdf.rio.rdfxml.RDFXMLParserFactory
import org.openrdf.rio.n3.N3ParserFactory
import org.pelagios.api.gazetteer.Place
import org.pelagios.rdf.parser.GazetteerParser
import org.openrdf.rio.RDFParser

class RDFPlaceReader(is: InputStream) {

  private val handler = new GazetteerParser()

  def read(filename: String): Iterable[Place] = {
    val parser = filename match {
      case f if f.endsWith("ttl") => new TurtleParserFactory().getParser()
      case f if f.endsWith("rdf") => new RDFXMLParserFactory().getParser()
      case f if f.endsWith("n3") => new N3ParserFactory().getParser()
      case _ => throw new UnsupportedRDFormatException("Format not supported")  
    }
    read(parser)
  }
  
  def read(format: RDFFormat): Iterable[Place] =
    read(RDFParserRegistry.getInstance.get(format).getParser)
  
  private def read(parser: RDFParser): Iterable[Place] = {
    parser.setRDFHandler(handler)
    parser.parse(is, "http://www.pelagios.org")
    handler.places
  }
  
  def close() = {
    is.close()
    handler.close()
  }

}