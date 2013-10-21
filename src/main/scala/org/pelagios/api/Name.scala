package org.pelagios.api

/** Pelagios 'Name' model primitive.
  *  
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Name {
  
  def labels: Seq[Label]
  
  def altLabels: Seq[Label]
  
}

/** A default POJO-style implementation of Name. **/
private[api] class DefaultName(val labels: Seq[Label], val altLabels: Seq[Label]) extends Name

/** Companion object for generating DefaultName instances **/
object Name extends AbstractApiCompanion {
  
  def apply(labels: ObjOrSeq[Label], altLabels: ObjOrSeq[Label] = new ObjOrSeq(Seq.empty[Label])) = {
    new DefaultName(labels.seq, altLabels.seq)
  }
  
}