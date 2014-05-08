package org.pelagios.api

/** 'Agent' model entity.
  *  
  * Defined according to how the term is defined for the dcterms:Agent 
  * class. In our case, we use it as a super-class for persons,
  * organization/institutions, research groups, projects, or 
  * software (generating annotations automatically).
  *  
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Agent {
  
  /** The Agent's URI **/
  def uri: String
  
  /** foaf:name **/
  def name: Option[String]
   
}

/** A default POJO-style implementation of Agent **/
private[api] class DefaultAgent(val uri: String, val name: Option[String]) extends Agent

/** Companion object for generating DefaultAgent instances **/
object Agent extends AbstractApiCompanion {
  
  def apply(uri: String, name: ObjOrOption[String] = new ObjOrOption(None)) = new DefaultAgent(uri, name.option)
  
} 