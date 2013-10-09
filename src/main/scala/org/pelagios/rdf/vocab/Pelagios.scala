package org.pelagios.rdf.vocab

/** Pelagios custom metadata terms **/
object Pelagios extends BaseVocab("http://pelagios.github.io/terms#") {

  val AnnotatedThing = createURI("AnnotatedThing")
  
  val PlaceRecord = createURI("PlaceRecord") 
  
  val distanceUnit = createURI("distanceUnit")
  
  val hasNeighbour = createURI("hasNeighbour")

  val hasNext = createURI("hasNext")
  
  val neighbourDistance = createURI("neighbourDistance")
  
  val neighbourURI = createURI("neighbourURI")

  val toponym = createURI("toponym")
  
}