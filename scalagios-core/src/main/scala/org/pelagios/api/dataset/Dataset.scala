package org.pelagios.api.dataset

import org.pelagios.api.AbstractApiCompanion

/** 'Dataset' model entity.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
trait Dataset {
  
  /** VoID file URI **/
  def uri: String

  /** dcterms:title **/
  def title: String
  
  /** dcterms:publisher **/
  def publisher: String

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
  
}

/** A default POJO-style implementation of Dataset. **/
private[api] class DefaultDataset(

  val uri: String,
  
  val title: String,

  val publisher: String,
  
  val license: String,
  
  val description: Option[String] = None,
  
  val homepage: Option[String] = None,
  
  val subjects: Seq[String] = Seq.empty[String],
  
  val datadumps: Seq[String] = Seq.empty[String]) extends Dataset
  
/** Companion object with a pimped apply method for generating DefaultDataset instances **/
object Dataset extends AbstractApiCompanion {

  def apply(uri: String, title: String, publisher: String, license: String,
      
      description: ObjOrOption[String] = new ObjOrOption(None),
      
      homepage: ObjOrOption[String] = new ObjOrOption(None),
      
      subjects: ObjOrSeq[String] = new ObjOrSeq(Seq.empty),
      
      datadumps: ObjOrSeq[String] = new ObjOrSeq(Seq.empty)): Dataset = {
    
    new DefaultDataset(uri, title, publisher, license, description.option, homepage.option, subjects.seq, datadumps.seq)
  }
  
}
