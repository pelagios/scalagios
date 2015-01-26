package org.pelagios.api.gazetteer

import com.vividsolutions.jts.geom.{ Coordinate, GeometryFactory }
import org.pelagios.api.{ AbstractApiCompanion, PlainLiteral }

/** Pelagios 'Place' model entity.
  *  
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
trait Place {
  
  /** A URI for the place in the namespace of the contributing gazetteer **/ 
  def uri: String
  
  /** rdfs:label
    *  
    * A title or 'screen name' for the place, for use as an identification label in
    * the user interface. This should *not* be considered a 'primary name' for the
    * place, but serves purely presentational purposes. 
    */ 
  def label: String
  
  /** dcterms:description
    *   
    * A description for the place; optionally multiple in different languages.
    */
  def descriptions: Seq[PlainLiteral]
  
  /** pleiades:hasName
    *
    * The list of names for this place.
    */      
  def names: Seq[PlainLiteral]

  /** pleiades:hasLocation
    * 
    * The list of locations for this place.   
    */
  def locations: Seq[Location]
  
  /** dcterms:type
    *
    * Place type according to the minimalistic Pelagios feature-type vocabulary 
    */
  def category: Option[PlaceCategory.Category]
  
  /** dcterms:subject 
    *  
    * Subject terms for this place (free text or URIs). We recommend using this 
    * property to express feature types (if the contributing gazetteer has such a 
    * concept). 
    */
  def subjects: Seq[String]
  
  /** skos:closeMatch
    * 
    * 'Vague' mappings to places in other gazetteers.
    */
  def closeMatches: Seq[String]
  
  /** skos:exactMatch
    *
    * Exact mappings to places in other gazetteers.   
    */
  def exactMatches: Seq[String]
  
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
  
  val label: String,
  
  val descriptions: Seq[PlainLiteral] = Seq.empty[PlainLiteral],
  
  val names: Seq[PlainLiteral] = Seq.empty[PlainLiteral],
  
  val locations: Seq[Location] = Seq.empty[Location],
  
  val category: Option[PlaceCategory.Category] = None,
  
  val subjects: Seq[String] = Seq.empty[String],
  
  val closeMatches: Seq[String] = Seq.empty[String],
  
  val exactMatches: Seq[String] = Seq.empty[String]

) extends Place
  
/** Companion object with a pimped apply method for generating DefaultPlace instances **/
object Place extends AbstractApiCompanion {
  
  def apply(uri: String,
  
            label: String,
  
            descriptions: ObjOrSeq[PlainLiteral] = new ObjOrSeq(Seq.empty[PlainLiteral]),
  
            names: ObjOrSeq[PlainLiteral] = new ObjOrSeq(Seq.empty[PlainLiteral]),
  
            locations: ObjOrSeq[Location] = new ObjOrSeq(Seq.empty[Location]),
            
            placeCategory: ObjOrOption[PlaceCategory.Category] = new ObjOrOption(None),

            subjects: ObjOrSeq[String] = new ObjOrSeq(Seq.empty[String]),
   
            closeMatches: ObjOrSeq[String] = new ObjOrSeq(Seq.empty[String]),
            
            exactMatches: ObjOrSeq[String] = new ObjOrSeq(Seq.empty[String])): Place = {
    
    new DefaultPlace(uri, label, descriptions.seq, names.seq, locations.seq, placeCategory.option, subjects.seq, closeMatches.seq, exactMatches.seq)
  }
  
}
