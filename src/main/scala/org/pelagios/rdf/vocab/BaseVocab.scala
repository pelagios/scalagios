package org.pelagios.rdf.vocab

import org.openrdf.model.impl.ValueFactoryImpl

/** RDF vocabulary base class
  * 
  * @param namespace the vocabulary namespace
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
private[vocab] class BaseVocab(namespace: String) {

    private val factory = ValueFactoryImpl.getInstance()
    
    /** Creates a vocabulary term URI based on namespace and local term
      * 
      * @param term the local term
      * @return the URI
      */
    protected def createURI(term: String) = factory.createURI(namespace, term)
  
}