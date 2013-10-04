package org.pelagios.api

/** Pelagios 'Name' model primitive.
  *  
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Name {
  
  def labels: Seq[Label]
  
  def altLabels: Seq[Label]
  
}

case class DefaultName extends Name {
  
  var labels = Seq.empty[Label]
  
  var altLabels = Seq.empty[Label]
  
}