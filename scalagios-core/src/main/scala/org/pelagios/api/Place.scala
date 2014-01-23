package org.pelagios.api

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryCollection
import com.vividsolutions.jts.geom.GeometryFactory

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
  
  /** dcterms:type
    *
    * Place type according to the minimalistic Pleagios feature-type vocabulary 
    */
  def placeType: PlaceType.Type
  
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
  
  /** Utility method that computes the centroid of all locations for this place **/
  def getCentroid: Option[Coordinate] = {
    if (locations.size > 0) {
      val f = new GeometryFactory()
      val geometries = locations.map(_.geometry).toArray
      val collection = f.createGeometryCollection(geometries)
      Some(collection.getCentroid().getCoordinate)
    } else {
      None
    }
  }
  
}

/** A default POJO-style implementation of 'Place' **/
private[api] class DefaultPlace (
    
  val uri: String,
  
  val title: String,
  
  val descriptions: Seq[Label] = Seq.empty[Label],
  
  val names: Seq[Name] = Seq.empty[Name],
  
  val locations: Seq[Location] = Seq.empty[Location],
  
  val subjects: Seq[String] = Seq.empty[String],
  
  val closeMatches: Seq[String] = Seq.empty[String]

) extends Place
  
/** Companion object with a pimped apply method for generating DefaultPlace instances **/
object Place extends AbstractApiCompanion {
  
  def apply(uri: String,
  
            title: String,
  
            descriptions: ObjOrSeq[Label] = new ObjOrSeq(Seq.empty[Label]),
  
            names: ObjOrSeq[Name] = new ObjOrSeq(Seq.empty[Name]),
  
            locations: ObjOrSeq[Location] = new ObjOrSeq(Seq.empty[Location]),
  
            subjects: ObjOrSeq[String] = new ObjOrSeq(Seq.empty[String]),
  
            closeMatches: ObjOrSeq[String] = new ObjOrSeq(Seq.empty[String])): Place = {
    
    new DefaultPlace(uri, title, descriptions.seq, names.seq, locations.seq, subjects.seq, closeMatches.seq)
  }
  
}
