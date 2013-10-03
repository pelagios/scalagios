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
  
}