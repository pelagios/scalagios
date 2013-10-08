package org.pelagios.api

/** 'AnnotatedThing' model entity.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait AnnotatedThing {
  
  def uri: String
  
  def title: String

  def homepages: Seq[String]
    
  def description: Option[String]
  
  def variants: Seq[AnnotatedThing]
  
  def annotations: Seq[Annotation]
  
}

/** A default POJO-style implementation of AnnotatedThing. **/
class DefaultAnnotatedThing(val uri: String, val title: String) extends AnnotatedThing {
  
  var homepages: Seq[String] = Seq.empty[String]
  
  var description: Option[String] = None
  
  var variants: Seq[AnnotatedThing] = Seq.empty[AnnotatedThing]
  
  var annotations: Seq[Annotation] = Seq.empty[Annotation]
  
}
