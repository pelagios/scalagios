package org.pelagios.api

/** Pelagios 'Place' model entity.
  *  
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Place {
  
  /** A URI for the place in the namespace of the contributing gazetteer **/ 
  def uri: String
  
  /** dcterms:title
    *  
    * A title or 'screen name' for the place, for use as an identification label in
    * the user interface. This should *not* be considered a 'primary name' for the
    * place, but serves purely presentational purposes. 
    */ 
  def title: String
  
  /** dcterms:description
    *   
    * A description for the place; optionally multiple in different languages.
    */
  def descriptions: Seq[Label]
  
  /** pleiades:hasName
    *
    * The list of names for this place.
    */      
  def names: Seq[Name]

  /** pleiades:hasLocation
    * 
    * The list of locations for this place.   
    */
  def locations: Seq[Location]
  
  /** dcterms:subject 
    *  
    * Subject terms for this place (free text or URIs). We recommend using this 
    * property to express feature types (if the contributing gazetteer has such a 
    * concept). 
    */
  def subjects: Seq[String]
  
  /** skos:closeMatch
    * 
    * Mappings to places in other gazetteers.
    */
  def closeMatches: Seq[String]
  
}

class DefaultPlace(val uri: String) extends Place {
  
  var title = "unknown"
  
  var descriptions = Seq.empty[Label]
  
  var names = Seq.empty[Name]
  
  var locations = Seq.empty[Location]
  
  var subjects = Seq.empty[String]
  
  var closeMatches = Seq.empty[String]
  
} 