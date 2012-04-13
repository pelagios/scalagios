package org.scalagios.rdf.vocab

/**
 * OSGeo vocabulary terms.
 * 
 * @author Rainer Simon<rainer.simon@ait.ac.at>
 */
object OSGeo extends BaseVocab {
  
  val NAMESPACE = "http://data.ordnancesurvey.co.uk/ontology/geometry/"
    
  val asWKT = factory.createURI(NAMESPACE, "asWKT")
    
  val extent = factory.createURI(NAMESPACE, "extent")

}