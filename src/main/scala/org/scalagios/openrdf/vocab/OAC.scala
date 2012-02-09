package org.scalagios.openrdf.vocab

/**
 * OAC Annotation vocabulary terms.
 * 
 * @author Rainer Simon<rainer.simon@ait.ac.at>
 */
object OAC extends BaseVocab {
  
  val NAMESPACE = "http://www.openannotation.org/ns/"
  
  val ANNOTATION = factory.createURI(NAMESPACE, "Annotation")
  
  val HAS_BODY = factory.createURI(NAMESPACE, "hasBody")
  
  val HAS_TARGET = factory.createURI(NAMESPACE, "hasTarget")
  
}