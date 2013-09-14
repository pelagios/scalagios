package org.pelagios.api

/**
 * 'Place' model entity.
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait Place {
  
  def uri: String
  
  def title: Label
  
  def descriptions: Seq[Label]
  
  def names: Seq[Name]

  def locations: Seq[Location]
  
  def subjects: Seq[String]
  
  def closeMatches: Seq[String]
  
}
