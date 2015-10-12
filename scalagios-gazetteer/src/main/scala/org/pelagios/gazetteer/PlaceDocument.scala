package org.pelagios.gazetteer

import com.vividsolutions.jts.geom.{ Coordinate, Geometry }
import org.apache.lucene.document.{ Document, Field, StringField, TextField }
import org.apache.lucene.index.IndexableField
import org.pelagios.api.PlainLiteral
import org.pelagios.api.gazetteer.{ Location, Place, PlaceCategory }
import scala.collection.JavaConversions._
import com.vividsolutions.jts.io.WKTWriter
import org.geotools.geojson.geom.GeometryJSON
import org.apache.lucene.document.StoredField
import org.pelagios.api.{ Image, PeriodOfTime }

/** An implementation of the [[Place]] API primitive backed by a Lucene Document.
  *  
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class PlaceDocument private[gazetteer] (doc: Document) extends Place {
  
  val uri: String = doc.get(PlaceIndex.FIELD_URI)
  
  val label: String = doc.get(PlaceIndex.FIELD_LABEL)
  
  val descriptions: Seq[PlainLiteral] =
    doc.getFields().filter(_.name.startsWith(PlaceIndex.FIELD_DESCRIPTION)).map(toLabel(_))
  
  val names: Seq[PlainLiteral] = {
    val literals = doc.get(PlaceIndex.FIELD_NAME_LITERALS)
    if (literals == null)
      Seq.empty[PlainLiteral]
    else
      literals.split("\n").map(lit=> {
        val separatorIdx = lit.lastIndexOf("@")
        val name = lit.substring(0, separatorIdx)
        if (separatorIdx + 1 == lit.size) {
          PlainLiteral(name)
        } else {
          PlainLiteral(name, Some(lit.substring(separatorIdx + 1)))
        }
      })
  }
  
  val location: Option[Location] = toOption(doc.get(PlaceIndex.FIELD_GEOMETRY)).flatMap(json => Location.fromGeoJSON(json))
  
  val temporalCoverage: Option[PeriodOfTime] = None // TODO implement
  
  val timePeriods: Seq[String] = Seq.empty[String]
  
  val category: Option[PlaceCategory.Category] = toOption(doc.get(PlaceIndex.FIELD_CATEGORY)).map(PlaceCategory.withName(_))
  
  val subjects: Seq[String] = doc.getValues(PlaceIndex.FIELD_SUBJECT).toSeq
  
  val depictions: Seq[Image] = Seq.empty[Image] // TODO implement
  
  val closeMatches: Seq[String] = doc.getValues(PlaceIndex.FIELD_CLOSE_MATCH).toSeq
  
  val exactMatches: Seq[String] = doc.getValues(PlaceIndex.FIELD_EXACT_MATCH).toSeq
  
  val seedURI: String = doc.get(PlaceIndex.FIELD_SEED_URI)
  
  private def toOption(string: String): Option[String] =
    if (string == null) None else Option(string)
  
  // TODO move into abstract super-class once we do doc wrappers for other API primitives!
  private def toLabel(field: IndexableField): PlainLiteral = {
    val language = if (field.name.indexOf('_') > -1) Some(field.name.substring(field.name.indexOf('_') + 1)) else None
    PlainLiteral(field.stringValue(), language)    
  }
  
}

/** Constants and helper to create PlaceDocuments from a Places **/
object PlaceDocument {
  
  def apply(place: Place, seedURI: Option[String]) = {
    val doc = new Document()
    doc.add(new StringField(PlaceIndex.FIELD_URI, GazetteerUtils.normalizeURI(place.uri), Field.Store.YES))
    doc.add(new TextField(PlaceIndex.FIELD_LABEL, place.label, Field.Store.YES))
    place.descriptions.foreach(description => {
      val fieldName = description.lang.map(PlaceIndex.FIELD_DESCRIPTION + "_" + _).getOrElse(PlaceIndex.FIELD_DESCRIPTION)
      doc.add(new TextField(fieldName, description.chars, Field.Store.YES))
    })
    
    // Index place names
    place.names.foreach(name => {
      doc.add(new TextField(PlaceIndex.FIELD_NAME, name.chars, Field.Store.YES)) 
    })
    
    // Store plain literals
    if (place.names.size > 0) {
      val literals = place.names.map(name => name.chars + "@" + name.lang.getOrElse(""))
      doc.add(new StoredField(PlaceIndex.FIELD_NAME_LITERALS, literals.mkString("\n")))
    }
    
    place.location.map(l => doc.add(new StoredField(PlaceIndex.FIELD_GEOMETRY, l.asGeoJSON)))
    if (place.category.isDefined)
      doc.add(new StringField(PlaceIndex.FIELD_CATEGORY, place.category.get.toString, Field.Store.YES))
    place.subjects.foreach(subject => doc.add(new StringField(PlaceIndex.FIELD_SUBJECT, subject, Field.Store.YES)))
    place.closeMatches.foreach(closeMatch => doc.add(new StringField(PlaceIndex.FIELD_CLOSE_MATCH, GazetteerUtils.normalizeURI(closeMatch), Field.Store.YES)))
    doc.add(new StringField(PlaceIndex.FIELD_SEED_URI, seedURI.getOrElse(place.uri), Field.Store.YES))
    
    doc
  }

}