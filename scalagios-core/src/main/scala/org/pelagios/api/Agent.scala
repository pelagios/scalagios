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

  /** foaf:name **/
  def name: String
  
  /** The Agent's URI **/
  def uri: Option[String]
   
}

/** A default POJO-style implementation of Agent **/
private[api] class DefaultAgent(val name: String, val uri: Option[String]) extends Agent

/** Companion object for generating DefaultAgent instances **/
object Agent extends AbstractApiCompanion {
  
  def apply(name: String, uri: ObjOrOption[String] = new ObjOrOption(None)) = new DefaultAgent(name, uri.option)
  
} 