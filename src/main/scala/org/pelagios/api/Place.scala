package org.pelagios.api

/** Pelagios 'Place' model entity.
 *  
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

class DefaultPlace(val uri: String) extends Place {
  
  var title = new Label("unknown")
  
  var descriptions = Seq.empty[Label]
  
  var names = Seq.empty[Name]
  
  var locations = Seq.empty[Location]
  
  var subjects = Seq.empty[String]
  
  var closeMatches = Seq.empty[String]
  
} 