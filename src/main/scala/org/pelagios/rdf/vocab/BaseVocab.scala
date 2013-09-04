package org.pelagios.rdf.vocab

import org.openrdf.model.impl.ValueFactoryImpl

private[vocab] trait BaseVocab {

    protected val factory = ValueFactoryImpl.getInstance()
  
}