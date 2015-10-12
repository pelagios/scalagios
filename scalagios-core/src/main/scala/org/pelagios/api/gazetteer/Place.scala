package org.pelagios.api.gazetteer

import com.vividsolutions.jts.geom.{ Coordinate, Geometry, GeometryFactory }
import com.vividsolutions.jts.io.WKTReader
import org.geotools.geojson.geom.GeometryJSON
import org.pelagios.api.{ AbstractApiCompanion, Image, PlainLiteral, PeriodOfTime }

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

  /** lawd:hasName
    *
    * The list of names for this place.
    */
  def names: Seq[PlainLiteral]

  /** geo:location
    *
    * A representative point location.
    */
  def location: Option[Location]

  /** dcterms:temporal
    *
    * According to the DCMI definition the "temporal coverage" or "temporal
    * characteristics of the resource". We recommend using the DCMI
    * Period Encoding Scheme: http://dublincore.org/documents/dcmi-period/
    */
  def temporalCoverage: Option[PeriodOfTime]

  /** dcterms:temporal
    *
    * Alternatively - or in addition - temporal coverage expressed as period URIs.
    */
  def timePeriods: Seq[String]

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

  /** foaf:depiction
    *
    * TODO we may want to make this an object rather than a string in the future,
    * in order to hold creator and license information along with the image URL
    * as well.
    */
  def depictions: Seq[Image]

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

}

/** A default POJO-style implementation of 'Place' **/
private[api] class DefaultPlace (

  val uri: String,

  val label: String,

  val descriptions: Seq[PlainLiteral],

  val names: Seq[PlainLiteral],

  val location: Option[Location],
  
  val temporalCoverage: Option[PeriodOfTime],
  
  val timePeriods: Seq[String],

  val category: Option[PlaceCategory.Category],

  val subjects: Seq[String],

  val depictions: Seq[Image],

  val closeMatches: Seq[String],

  val exactMatches: Seq[String]

) extends Place

/** Companion object with a pimped apply method for generating DefaultPlace instances **/
object Place extends AbstractApiCompanion {
  
  def apply(uri: String,

            label: String,

            descriptions: ObjOrSeq[PlainLiteral],

            names: ObjOrSeq[PlainLiteral],

            location: ObjOrOption[Location],
            
            temporalCoverage: ObjOrOption[PeriodOfTime],
            
            timePeriods: ObjOrSeq[String],

            placeCategory: ObjOrOption[PlaceCategory.Category],

            subjects: ObjOrSeq[String],

            depictions: ObjOrSeq[Image],

            closeMatches: ObjOrSeq[String],

            exactMatches: ObjOrSeq[String]): Place = {

    new DefaultPlace(uri, label, descriptions.seq, names.seq, location.option,temporalCoverage.option, timePeriods.seq,
      placeCategory.option, subjects.seq, depictions.seq, closeMatches.seq, exactMatches.seq)
    
  }

}
