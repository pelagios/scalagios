package org.scalagios.api

import org.openrdf.rio.RDFFormat
import java.security.MessageDigest
import java.math.BigInteger

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
  
  def parent: Option[Dataset]
  
  def rootSet: Dataset = {
    if (parent.isEmpty)
      this
    else
      parent.get.rootSet
  }

  def subsets: List[Dataset]
  
  def annotations: Iterable[GeoAnnotation]
  
  def md5: String = {
    val md = MessageDigest.getInstance("MD5").digest(uri.getBytes())
    new BigInteger(1, md).toString(16)
  }
    
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
  
  var parent: Option[Dataset] = None
  
  var subsets: List[Dataset] = List.empty[Dataset]
  
  var annotations: Iterable[GeoAnnotation] = List.empty[GeoAnnotation]
  
  override def toString: String = uri
  
}