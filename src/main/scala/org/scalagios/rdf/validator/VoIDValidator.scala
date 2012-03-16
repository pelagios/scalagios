package org.scalagios.rdf.validator

import java.io.InputStream
import org.openrdf.rio.RDFFormat
import org.openrdf.rio.RDFParserRegistry
import org.scalagios.rdf.parser.DatasetCollector

class VoIDValidator(private val format: RDFFormat) {
  
  private val factory = RDFParserRegistry.getInstance().get(format) 
  
  def validate(inputStream: InputStream): List[Issue] = {
    val parser = factory.getParser()
    parser.setRDFHandler(new DatasetCollector)

    var issues = List[Issue]() 
    
    try {
     parser.parse(inputStream, "http://pelagios.org") 
    } catch {
      case t: Throwable => issues = issues ++ List(new Issue(t.getMessage()))
    }
    
    issues
  }

}