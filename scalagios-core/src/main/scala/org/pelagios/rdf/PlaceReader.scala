package org.pelagios.rdf

import java.io.InputStream
import org.openrdf.rio.RDFFormat
import org.pelagios.api.gazetteer.Place
import org.pelagios.rdf.parser.GazetteerParser
import org.openrdf.rio.RDFParserRegistry

class PlaceReader(is: InputStream) {

  private val handler = new GazetteerParser()

  def read(baseURI:String, format: RDFFormat): Iterable[Place] = {
    val parser = RDFParserRegistry.getInstance.get(format).getParser
    parser.setRDFHandler(handler)
    parser.parse(is, "http://www.pelagios.org")
    handler.places
  }
  
  def close() = {
    is.close()
    handler.close()
  }

}