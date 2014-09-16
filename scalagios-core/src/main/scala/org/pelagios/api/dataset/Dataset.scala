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
  def publisher: Option[String]

  /** dcterms:license **/
  def license: Option[String]
  
  /** The parent dataset, in case this dataset is a subset (as defined through void:subset) **/
  def isSubsetOf: Option[Dataset]
  
  /** dcterms:description **/
  def description: Option[String]
  
  /** foaf:homepage ***/
  def homepage: Option[String]
  
  /** dcterms:subject **/
  def subjects: Seq[String]
  
  /** void:dataDump **/
  def datadumps: Seq[String]
  
  /** Subsets to this dataset, as defined through void:subset **/
  def subsets: Seq[Dataset]
  
}

/** A default POJO-style implementation of Dataset. **/
private[api] class DefaultDataset(

  val uri: String,
  
  val title: String,

  val publisher: Option[String],
  
  val license: Option[String],
  
  val isSubsetOf: Option[Dataset] = None,
  
  val description: Option[String] = None,
  
  val homepage: Option[String] = None,
  
  val subjects: Seq[String] = Seq.empty[String],
  
  val datadumps: Seq[String] = Seq.empty[String],
  
  val subsets: Seq[Dataset] = Seq.empty[Dataset]) extends Dataset
  
/** Companion object with a pimped apply method for generating DefaultDataset instances **/
object Dataset extends AbstractApiCompanion {

  def apply(uri: String, title: String, 
  
      publisher: ObjOrOption[String] = new ObjOrOption(None),
      
      license: ObjOrOption[String] = new ObjOrOption(None),
      
      isSubsetOf: ObjOrOption[Dataset] = new ObjOrOption(None),
      
      description: ObjOrOption[String] = new ObjOrOption(None),
      
      homepage: ObjOrOption[String] = new ObjOrOption(None),
      
      subjects: ObjOrSeq[String] = new ObjOrSeq(Seq.empty),
      
      datadumps: ObjOrSeq[String] = new ObjOrSeq(Seq.empty),
      
      subsets: Seq[Dataset] = Seq.empty[Dataset]): Dataset = {
    
    new DefaultDataset(uri, title, publisher.option, license.option,  isSubsetOf.option, description.option, homepage.option, subjects.seq, datadumps.seq, subsets)
  }
  
}
