package org.scalagios.rdf.vocab

/**
 * SKOS vocabulary terms.
 * 
 * @author Rainer Simon<rainer.simon@ait.ac.at>
 */
object SKOS extends BaseVocab {
  
  val NAMESPACE = "http://www.w3.org/2004/02/skos/core#"
    
  val altLabel = factory.createURI(NAMESPACE, "altLabel")

}