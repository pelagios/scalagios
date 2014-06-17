package org.pelagios.rdf.parser

import scala.collection.mutable.ArrayBuffer
import org.openrdf.model.{ URI, Value }
import org.openrdf.model.vocabulary.RDF

/** A helper structure to collect the triples for a specific RDF resource.
  *  
  * This class aggregates all predicate/object pairs for one particular
  * RDF resource. Since this is only a helper, we restrict
  * visibility to this package.
  * 
  * @constructor create a new resource with a URI
  * @param uri the URI
  */
private[parser] case class Resource(val uri:String) {
  
  val properties = new ArrayBuffer[(URI, Value)]
  
  /** Returns the values (objects) for a particular RDF property.
    *
    * @param predicate the predicate URI
    * @return the object values for this property
    */
  def get(predicate: URI): Seq[Value] = properties.filter(_._1 == predicate).map(_._2).toSeq
  
  /** Returns the first value (object) for a particular RDF predicate or 'None'
    *  
    * @param property the property URI
    * @return the first value found for that property, or 'None', if the resource
    *         does not have this property 
    */ 
  def getFirst(predicate: URI): Option[Value] = {
    val p = get(predicate)
    if (p.size > 0) 
      Some(p(0))
    else
      None
  }
  
  /** Checks if the resource has a specific RDF type.
    *  
    * @param typ the RDF type value  
    * @return true if the resource has the type
    */
  def hasType(typ: Value): Boolean = get(RDF.TYPE).contains(typ)
  
  /** Checks if the resource has any of the specified RDF types.
    *  
    * @param types the type to check for  
    * @return true if the resource has any of the types
    */
  def hasAnyType(types: Seq[Value]): Boolean = get(RDF.TYPE).find(typ => types.contains(typ)).isDefined

  /** Checks if the resource has a specific RDF predicate.
    *  
    * @param predicate the property URI
    * @return true if the resource has the property
    */
  def hasPredicate(predicate: URI): Boolean = properties.filter(_._1 == predicate).size > 0
  
  /** Checks if the resource has any (i.e. at least one) of the predicates in the list.
    * 
    * @param predicates the predicates to test for
    */
  def hasAnyPredicate(predicates: Seq[URI]): Boolean =
    properties.map(_._1).find(p => predicates.contains(p)).isDefined
    
}