package org.pelagios.api

/** A helper that provides utility implicits for a cleaner API
  *
  * @author Rainer Simon <rainer.simon@ait.ac.at>  
  */
private[api] abstract class AbstractApiCompanion {
  
  implicit def toOption[T](t: T): Option[T] = if (t == null) None else Some(t)
  
  implicit def toStringOrSeq[T](t: T): ObjOrSeq[T] = new ObjOrSeq(Seq(t))
  
  implicit def toStringOrSeq[T](t: Seq[T]): ObjOrSeq[T] = new ObjOrSeq(t)
  
  class ObjOrSeq[T](val seq: Seq[T])

}