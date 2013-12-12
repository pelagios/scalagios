package org.pelagios.rdf.vocab

/** Vocabulary of Interlinked Datasets - http://www.w3.org/TR/void/ **/
object VoID extends BaseVocab("http://rdfs.org/ns/void#") {
  
  val Dataset = createURI("Dataset")

  val dataDump = createURI("dataDump")
  
  val inDataset = createURI("inDataset")

  val subset = createURI("subset")  
  
  val uriRegexPattern = createURI("uriRegexPattern")
  
  val uriSpace = createURI("uriSpace")
  
}