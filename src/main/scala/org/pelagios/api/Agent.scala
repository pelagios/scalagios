package org.pelagios.api

/** 'Agent' model entity.
  *  
  * Defined according to how the term is defined for the dcterms:Agent 
  * class. In our case, we use it as a super-class for persons,
  * organization/institutions, research groups, projects, or 
  * software (generating annotations automatically). 
  * 
  * TODO this may need extensions in the future
  *  
  * @constructor create a new Agent
  * @param name foaf:name
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class Agent(val name: String)