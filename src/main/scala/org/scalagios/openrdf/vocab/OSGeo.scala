package org.scalagios.openrdf.vocab

/**
 * OSGeo vocabulary terms.
 * 
 * @author Rainer Simon<rainer.simon@ait.ac.at>
 */
object OSGeo extends BaseVocab {
  
  val NAMESPACE = "http://data.ordnancesurvey.co.uk/ontology/geometry/"
    
  val EXTENT = factory.createURI(NAMESPACE, "extent")
  
  val asWKT = factory.createURI(NAMESPACE, "asWKT")

}