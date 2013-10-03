package org.pelagios.rdf.vocab

/** W3C Basic Geo Vocabulary - http://www.w3.org/2003/01/geo/ **/
object W3CGeo extends BaseVocab("http://www.w3.org/2003/01/geo/wgs84_pos#") {
    
  val lat = createURI("lat")
  
  val long = createURI("long")

}