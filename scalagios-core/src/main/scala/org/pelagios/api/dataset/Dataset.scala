package org.pelagios.api.dataset

import org.pelagios.api.AbstractApiCompanion

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

/** A default POJO-style implementation of Dataset. **/
private[api] class DefaultDataset(
    
  val title: String,

  val license: String,
  
  val lastUpdated: Long,
  
  val description: Option[String] = None,
  
  val homepage: Option[String] = None,
  
  val datadumps: Seq[String] = Seq.empty[String],

  val voidURI: Option[String] = None) extends Dataset
  
/** Companion object with a pimped apply method for generating DefaultDataset instances **/
object Dataset extends AbstractApiCompanion {

  def apply(title: String, license: String, lastUpdated: Long,
      
      description: ObjOrOption[String] = new ObjOrOption(None),
      
      homepage: ObjOrOption[String] = new ObjOrOption(None),
      
      datadumps: ObjOrSeq[String] = new ObjOrSeq(Seq.empty),
      
      voidURI: ObjOrOption[String] = new ObjOrOption(None)): Dataset = {
    
    new DefaultDataset(title, license, lastUpdated, description.option, homepage.option, datadumps.seq, voidURI.option)
  }
  
}
