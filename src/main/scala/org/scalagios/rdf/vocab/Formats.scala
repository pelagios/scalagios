package org.scalagios.rdf.vocab

import org.openrdf.rio.RDFFormat
import org.openrdf.model.Value

object Formats extends BaseVocab {
  
  val NAMESPACE = "http://www.w3.org/ns/formats/"
    
  val N3 = factory.createURI(NAMESPACE, "N3")
  
  val RDF_XML = factory.createURI(NAMESPACE, "RDF_XML")
  
  def toRDFFormat(uri: Value): Option[RDFFormat] = {
	  uri match {
	    case N3 => Some(RDFFormat.N3)
	      
	    case RDF_XML => Some(RDFFormat.RDFXML)
	      
	    case _ => None
	  }
  }
  
}