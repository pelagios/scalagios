package org.pelagios.rdf.vocab

/** Pelagios custom metadata terms **/
object Pelagios extends BaseVocab("http://pelagios.github.io/terms#") {

  val AnnotatedThing = createURI("AnnotatedThing")
  
  val Ethnonym = createURI("Ethnonym")
    
  val Metonym = createURI("Metonym")
  
  val Neighbour = createURI("Neighbour")
  
  val PlaceRecord = createURI("PlaceRecord") 
  
  val Toponym = createURI("Toponym")
  
  val Transcription = createURI("Transcription")

  val relation = createURI("relation")
  
}