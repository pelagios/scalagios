package org.pelagios.api.dataset

/** 'Dataset' model entity.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait Dataset {

  /** dcterms:title **/
  def title: String

  /** dcterms:license **/
  def license: String
  
  /** Last updated timestamp **/
  def lastUpdated: Long
  
  /** dcterms:description **/
  def description: Option[String]
  
  /** foaf:homepage ***/
  def homepage: Option[String]
  
  /** void:dataDump **/
  def datadumps: Seq[String]

  /** VoID file URI **/
  def voidURI: Option[String]
  
}