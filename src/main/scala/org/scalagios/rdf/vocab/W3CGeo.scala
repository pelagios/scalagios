package org.scalagios.rdf.vocab

/**
 * W3CGeo vocabulary terms.
 * 
 * @author Rainer Simon<rainer.simon@ait.ac.at>
 */
object W3CGeo extends BaseVocab {
  
  val NAMESPACE = "http://www.w3.org/2003/01/geo/wgs84_pos#"
    
  val lat = factory.createURI(NAMESPACE, "lat")
  
  val long = factory.createURI(NAMESPACE, "long")

}