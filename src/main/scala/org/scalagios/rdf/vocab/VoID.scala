package org.scalagios.rdf.vocab

object VoID extends BaseVocab {
  
  val NAMESPACE = "http://rdfs.org/ns/void#"
    
  val Dataset = factory.createURI(NAMESPACE, "Dataset")

  val subset = factory.createURI(NAMESPACE, "subset")
  
  val dataDump = factory.createURI(NAMESPACE, "dataDump")
  
  val feature = factory.createURI(NAMESPACE, "feature")
  
  val uriSpace = factory.createURI(NAMESPACE, "uriSpace")
  
}