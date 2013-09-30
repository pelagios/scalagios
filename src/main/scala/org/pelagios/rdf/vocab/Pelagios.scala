package org.pelagios.rdf.vocab

object Pelagios extends BaseVocab {
  
  val NAMESPACE = "http://pelagios.github.io/terms#"

  val AnnotatedThing = factory.createURI(NAMESPACE, "AnnotatedThing")
  
  val PlaceRecord = factory.createURI(NAMESPACE, "PlaceRecord")

}