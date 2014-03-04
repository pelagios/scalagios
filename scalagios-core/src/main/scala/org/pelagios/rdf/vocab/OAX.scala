package org.pelagios.rdf.vocab

/** Open Annotation Extensions **/
object OAX extends BaseVocab("http://www.w3.org/ns/openannotation/extensions/") {

  val TextOffsetSelector = createURI("TextOffsetSelector")

  val offset = createURI("offset")
  
  val range = createURI("range")

}