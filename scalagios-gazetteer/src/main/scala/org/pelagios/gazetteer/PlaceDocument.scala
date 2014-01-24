package org.pelagios.gazetteer

import org.apache.lucene.document.{ Document, Field, StringField, TextField }
import org.apache.lucene.index.IndexableField
import org.pelagios.api.{ Label, Location, Name, Place, PlaceCategory }
import scala.collection.JavaConversions._
import com.vividsolutions.jts.io.WKTWriter

/** An implementation of the [[Place]] API primitive backed by a Lucene Document.
  *  
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class PlaceDocument private[gazetteer] (doc: Document) extends Place {
  
  lazy val uri: String = doc.get(PlaceIndex.FIELD_URI)
  
  lazy val title: String = doc.get(PlaceIndex.FIELD_TITLE)
  
  lazy val descriptions: Seq[Label] =
    doc.getFields().filter(_.name.startsWith(PlaceIndex.FIELD_DESCRIPTION)).map(toLabel(_))
  
  lazy val names: Seq[Name] = 
    doc.getFields().filter(_.name.startsWith(PlaceIndex.FIELD_DESCRIPTION)).map(field => Name(toLabel(field)))
  
  lazy val locations: Seq[Location] = doc.getValues(PlaceIndex.FIELD_GEOMETRY).map(wkt => Location(Location.parseWKT(wkt))).toSeq
  
  lazy val placeType: Option[PlaceCategory.Category] = None
  
  lazy val subjects: Seq[String] = doc.getValues(PlaceIndex.FIELD_SUBJECT).toSeq
  
  lazy val closeMatches: Seq[String] = doc.getValues(PlaceIndex.FIELD_CLOSE_MATCH).toSeq
  
  lazy val seedURI: String = doc.get(PlaceIndex.FIELD_SEED_URI)
  
  // TODO move into abstract super-class once we do doc wrappers for other API primitives!
  private def toLabel(field: IndexableField): Label = {
    val language = if (field.name.indexOf('_') > -1) Some(field.name.substring(field.name.indexOf('_') + 1)) else None
    Label(field.stringValue(), language)    
  }
  
}

/** Constants and helper to create PlaceDocuments from a Places **/
object PlaceDocument {
  
  def apply(place: Place, seedURI: Option[String]) = {
    val wktWriter = new WKTWriter()
    
    val doc = new Document()
    doc.add(new StringField(PlaceIndex.FIELD_URI, GazetteerUtils.normalizeURI(place.uri), Field.Store.YES))
    doc.add(new TextField(PlaceIndex.FIELD_TITLE, place.title, Field.Store.YES))
    place.descriptions.foreach(description => {
      val fieldName = description.lang.map(PlaceIndex.FIELD_DESCRIPTION + "_" + _).getOrElse(PlaceIndex.FIELD_DESCRIPTION)
      doc.add(new TextField(fieldName, description.label, Field.Store.YES))
    })
    place.names.foreach(name => {
      name.labels.foreach(label => {
        val fieldName = label.lang.map(PlaceIndex.FIELD_NAME + "_" + _).getOrElse(PlaceIndex.FIELD_NAME) 
        doc.add(new TextField(fieldName, label.label, Field.Store.YES)) 
      })
    })
    place.locations.foreach(location => doc.add(new StringField(PlaceIndex.FIELD_GEOMETRY, wktWriter.write(location.geometry), Field.Store.YES)))
    place.subjects.foreach(subject => doc.add(new StringField(PlaceIndex.FIELD_SUBJECT, subject, Field.Store.YES)))
    place.closeMatches.foreach(closeMatch => doc.add(new StringField(PlaceIndex.FIELD_CLOSE_MATCH, closeMatch, Field.Store.YES)))
    doc.add(new StringField(PlaceIndex.FIELD_SEED_URI, seedURI.getOrElse(place.uri), Field.Store.YES))
    
    doc
  }

}