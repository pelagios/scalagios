package org.scalagios.rdf.validator

import java.io.InputStream
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.RDFParserRegistry
import org.scalagios.rdf.parser.DatasetCollector

class VoIDValidator {
  
  def validate(inputStream: InputStream, format: RDFFormat): List[Issue] = {
    val parser = RDFParserRegistry.getInstance().get(format).getParser()
    parser.setRDFHandler(new DatasetCollector)
    parser.parse(inputStream, null)
    
    List[Issue]()
  }

}