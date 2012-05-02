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
   * The URI of this datasets' root dataset (mandatory)
   * 
   * Note: if this dataset is a root dataset, <code>rootUri</code> is
   * the same as <code>uri</code> 
   */
  def rootUri: String
  
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
  
  def countAnnotations(nested: Boolean = false): Int = {
    if (nested)
      _listAnnotations(None).size + subsets.map(subset => _recursiveCount(subset)).foldLeft(0)((x, y) => x + y)
    else
      _listAnnotations(None).size
  }
  
  private def _recursiveCount(dataset: Dataset): Int = {
    // TODO I'm sure there's a better, more functional way to do this...
    var count = dataset._listAnnotations(None).size
    dataset.subsets.foreach(subset => count += _recursiveCount(subset))
    count
  }

  protected def _listAnnotations(hasBody: Option[String]): Iterable[GeoAnnotation]
  
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
  def isValid: Boolean = (!uri.isEmpty() && !context.isEmpty() && !rootUri.isEmpty && !title.isEmpty())

}

/**
 * A default (POJO-style) implementation of the Pelagios
 * <em>Dataset</em> model primitive.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class DefaultDataset(val uri: String, val context: String) extends Dataset {

  def rootUri: String =
    if (parent.isEmpty)
      uri
    else
      parent.get.rootUri        
  
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
  
}