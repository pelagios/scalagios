package org.pelagios.api

/**
 * 'AnnotatedThing' model entity.
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait AnnotatedThing {
  
  def uri: String
  
  def title: Option[String]
  
  def description: Option[String]

  def annotations: Seq[Annotation]
  
}
