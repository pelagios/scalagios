package org.scalagios.api

import java.math.BigInteger
import java.security.MessageDigest

/**
 * Pelagios <em>Dataset</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait Dataset {
  
  /**
   * The Dataset's original source URI (mandatory)
   */
  def uri: String
  
  /**
   * The Context URI in the graph (mandatory)
   */
  def context: String
  
  /**
   * The title (mandatory)
   */
  def title: String
  
  /**
   * The description
   */
  def description: Option[String]
  
  /**
   * The license
   */
  def license: Option[String]

  /**
   * A (human-readably) Web page with information about the dataset
   */
  def homepage: Option[String]
  
  /**
   * Association information (i.e. strategy for associating
   * annotations with the dataset)
   */
  def associatedDatadumps: List[String]
  def associatedUriSpace: Option[String]
  def associatedRegexPattern: Option[String]
  
  /**
   * Subsets
   */
  def subsets: Iterable[Dataset]

  /**
   * Annotations
   */
  def annotations: Iterable[GeoAnnotation]

  /**
   * Utility method that produces an MD5 hash of the URI
   */
  def md5: String = {
    val md = MessageDigest.getInstance("MD5").digest(uri.getBytes())
    new BigInteger(1, md).toString(16)
  }
    
  /**
   * Utility method that checks if all mandatory properties are set
   */
  def isValid: Boolean = (!uri.isEmpty() && !context.isEmpty() && !title.isEmpty())

}

/**
 * A default (POJO-style) implementation of the Pelagios
 * <em>Dataset</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class DefaultDataset(val uri: String, val context: String) extends Dataset {

  var title: String = _ // mandatory
  
  var description: Option[String] = None
  
  var license: Option[String] = None
  
  var homepage: Option[String] = None
  
  var associatedDatadumps: List[String] = List.empty[String]
  
  var associatedUriSpace: Option[String] = None
  
  var associatedRegexPattern: Option[String] = None
  
  var subsets: List[Dataset] = List.empty[Dataset]
  
  var annotations: Iterable[GeoAnnotation] = List.empty[GeoAnnotation]
  
}