package org.pelagios.api

/** 'Annotation' model entity.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Annotation {
  
  def uri: String

  def hasBody: String
  
  def hasTarget: String
  
  def motivatedBy: Option[String]
  
  def toponym: Option[String]
  
  def hasNext: Option[Neighbour]
  
  // TODO def annotatedBy: Option[Agent]
  
  // TODO def annotatedAt: timestamp

  // http://lov.okfn.org/dataset/lov/details/vocabulary_pav.html  
  // TODO def pav_createdBy: Option[Agent]
  
}

/** A default POJO-style implementation of Annotation. **/
class DefaultAnnotation(val uri: String) extends Annotation {
  
  var hasBody: String = ""
    
  var hasTarget: String = ""
    
  var motivatedBy: Option[String] = Some("geotagging")
  
  var toponym: Option[String] = None
  
  var hasNext: Option[Neighbour] = None
  
}
