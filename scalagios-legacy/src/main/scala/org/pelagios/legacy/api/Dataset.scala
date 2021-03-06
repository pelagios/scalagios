package org.pelagios.legacy.api

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
   * The URI of this datasets' root dataset (mandatory)
   * 
   * Note: if this dataset is a root dataset, <code>rootUri</code> is
   * the same as <code>uri</code> 
   */
  def rootUri: String
  
  /**
   * Last updated timestamp (mandatory)
   */
  def lastUpdated: Long
  
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
   * Annotations contained in the dataset. If the 'nested' parameter is
   * set to <code>true</code>, the method will also look in the subsets 
   * of this dataset. Otherwise, only annotations directly contained in
   * this dataset are returned.
   */
  def annotations(nested: Boolean = false): Iterable[GeoAnnotation] = {
    if (nested)
      _listAnnotations(None) ++ subsets.map(subset => _recursiveList(subset, None)).flatten
    else
      _listAnnotations(None)
  }
  
  /**
   * Annotations contained in the dataset, but restricted to those referencing
   * a particular place (i.e. which have a defined hasBody URI). If the 'nested' 
   * parameter is set to <code>true</code>, the method will also look in the 
   * subsets of this dataset.
   */
  def annotations(hasBody: String, nested: Boolean): Iterable[GeoAnnotation] = {
    if (nested)
      _listAnnotations(Some(hasBody)) ++ subsets.map(subset => _recursiveList(subset, Some(hasBody))).flatten
    else
      _listAnnotations(Some(hasBody))
  }
  
  private def _recursiveList(dataset: Dataset, hasBody: Option[String]): Iterable[GeoAnnotation] = {
    // TODO I'm sure there's a better, more functional way to do this...
    var list = dataset._listAnnotations(hasBody)
    dataset.subsets.foreach(subset => list ++= _recursiveList(subset, hasBody))
    list
  }

  protected def _listAnnotations(hasBody: Option[String]): Iterable[GeoAnnotation]
  
  def countAnnotations(nested: Boolean = false): Int
  
  /**
   * Utility method that checks whether this dataset is a child of the dataset with
   * the specified URI. The check runs recursively across the entire hierarchy (up to the root 
   * level), i.e. it does not just check for direct parent/child relationship
   */
  def isChildOf(uri: String): Boolean
  
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
  def isValid: Boolean = (uri != null && rootUri != null && lastUpdated != 0 && title != null)

}

/**
 * A default (POJO-style) implementation of the Pelagios
 * <em>Dataset</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class DefaultDataset(val uri: String) extends Dataset {

  def rootUri: String =
    if (parent.isEmpty)
      uri
    else
      parent.get.rootUri  
      
  var lastUpdated: Long = _ // mandatory
  
  var title: String = _ // mandatory
  
  var description: Option[String] = None
  
  var license: Option[String] = None
  
  var homepage: Option[String] = None
  
  var associatedDatadumps: List[String] = List.empty[String]
  
  var associatedUriSpace: Option[String] = None
  
  var associatedRegexPattern: Option[String] = None
  
  var subsets: List[Dataset] = List.empty[Dataset]
  
  /**
   * Note: this is not a <code>Dataset</code> method. We use this only in
   * the <code>DefaultDataset</code> impl to determine <code>rootUri</code>
   * at runtime!
   */
  var parent: Option[DefaultDataset] = None
  
  private var _annotations = List.empty[GeoAnnotation]
  
  private[api] def setAnnotations(annotations: List[DefaultGeoAnnotation]) = _annotations = annotations
  
  protected def _listAnnotations(hasBody: Option[String]): Iterable[GeoAnnotation] = {
    if (hasBody.isDefined)
      _annotations.filter(_.body.equals(hasBody.get))
    else
      _annotations
  }
  
  def countAnnotations(nested: Boolean = false): Int = annotations(nested).size // No need for optimization in this case
  
  def isChildOf(uri: String) =
    if (parent.isEmpty)
      false
    else
      if (parent.get.uri.equals(uri))
        true
      else
        parent.get.isChildOf(uri)
        
}