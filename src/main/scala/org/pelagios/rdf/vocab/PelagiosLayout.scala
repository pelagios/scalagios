package org.pelagios.rdf.vocab

object PelagiosLayout extends BaseVocab("http://pelagios.github.io/layout#") {
  
  val Link = createURI("Link")
  
  val distance = createURI("distance")
  
  val hasLink = createURI("hasLink")

  val hasNext = createURI("hasNext")
  
  val next = createURI("next")
  
  val unit = createURI("unit")
  
}