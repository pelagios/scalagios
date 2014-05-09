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
  
  /** dcterms:description **/
  def description: Option[String]
  
  /** foaf:homepage ***/
  def homepage: Option[String]
  
  /** dcterms:subject **/
  def subjects: Seq[String]
  
  /** void:dataDump **/
  def datadumps: Seq[String]

  /** VoID file URI **/
  def voidURI: Option[String]
  
}

/** A default POJO-style implementation of Dataset. **/
private[api] class DefaultDataset(
    
  val title: String,

  val license: String,
  
  val description: Option[String] = None,
  
  val homepage: Option[String] = None,
  
  val subjects: Seq[String] = Seq.empty[String],
  
  val datadumps: Seq[String] = Seq.empty[String],

  val voidURI: Option[String] = None) extends Dataset
  
/** Companion object with a pimped apply method for generating DefaultDataset instances **/
object Dataset extends AbstractApiCompanion {

  def apply(title: String, license: String,
      
      description: ObjOrOption[String] = new ObjOrOption(None),
      
      homepage: ObjOrOption[String] = new ObjOrOption(None),
      
      subjects: ObjOrSeq[String] = new ObjOrSeq(Seq.empty),
      
      datadumps: ObjOrSeq[String] = new ObjOrSeq(Seq.empty),
      
      voidURI: ObjOrOption[String] = new ObjOrOption(None)): Dataset = {
    
    new DefaultDataset(title, license, description.option, homepage.option, subjects.seq, datadumps.seq, voidURI.option)
  }
  
}
