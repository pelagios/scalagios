package org.scalagios.api

import org.openrdf.rio.RDFFormat

/**
 * Pelagios <em>Dataset</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait Dataset {
  
  def uri: String
  
  def title: String
  
  def description: String
  
  def license: String
  
  def homepage: String
  
  def datadump: String
  
  def dumpFormat: RDFFormat
  
  def uriSpace: String

  def subsets: List[Dataset]
    
  def isValid: Boolean = (!uri.isEmpty() && !title.isEmpty())

}

/**
 * A default (POJO-style) implementation of the Pelagios
 * <em>Dataset</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class DefaultDataset(var uri: String) extends Dataset {

  var title: String = _
  
  var description: String = _
  
  var license: String = _
  
  var homepage: String = _
  
  var datadump: String = _
  
  var dumpFormat: RDFFormat = _
  
  var uriSpace: String = _
  
  var subsets: List[Dataset] = List.empty[Dataset]
  
  override def toString: String = uri
  
}