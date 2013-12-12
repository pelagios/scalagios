package org.pelagios.gazetteer

import org.apache.lucene.document.{ Document, Field, StringField, TextField }
import org.apache.lucene.index.IndexableField
import org.pelagios.api.{ Label, Location, Name, Place }
import scala.collection.JavaConversions._
import com.vividsolutions.jts.io.WKTWriter

/** An implementation of the [[Place]] API primitive backed by a Lucene Document.
  *  
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
class PlaceDocument private (doc: Document) extends Place {

  import PlaceDocument._
  
  lazy val uri: String = doc.get(FIELD_URI)
  
  lazy val title: String = doc.get(FIELD_TITLE)
  
  lazy val descriptions: Seq[Label] =
    doc.getFields().filter(_.name.startsWith(FIELD_DESCRIPTION)).map(toLabel(_))
  
  lazy val names: Seq[Name] = 
    doc.getFields().filter(_.name.startsWith(FIELD_DESCRIPTION)).map(field => Name(toLabel(field)))
  
  lazy val locations: Seq[Location] = doc.getValues(FIELD_GEOMETRY).map(wkt => Location(Location.parseWKT(wkt))).toSeq
  
  lazy val subjects: Seq[String] = doc.getValues(FIELD_SUBJECT).toSeq
  
  lazy val closeMatches: Seq[String] = doc.getValues(FIELD_CLOSE_MATCH).toSeq
  
  // TODO move into abstract super-class once we do doc wrappers for other API primitives!
  private def toLabel(field: IndexableField): Label = {
    val language = if (field.name.indexOf('_') > -1) Some(field.name.substring(field.name.indexOf('_') + 1)) else None
    Label(field.stringValue(), language)    
  }
  
}

/** Constants and helper to create PlaceDocuments from a Places **/
object PlaceDocument {
  
  val FIELD_URI = "uri"
  
  val FIELD_TITLE = "title"
  
  val FIELD_DESCRIPTION = "description"
    
  val FIELD_NAME = "name"
    
  val FIELD_GEOMETRY = "wkt"
    
  val FIELD_SUBJECT = "subject"
    
  val FIELD_CLOSE_MATCH = "close_match"
  
  val FIELD_SEED_URI = "group_uri"
  
  def apply(place: Place, seedURI: Option[String]) = {
    val wktWriter = new WKTWriter()
    
    val doc = new Document()
    doc.add(new StringField(FIELD_URI, PlaceIndex.normalizeURI(place.uri), Field.Store.YES))
    doc.add(new TextField(FIELD_TITLE, place.title, Field.Store.YES))
    place.descriptions.foreach(description => {
      val fieldName = description.lang.map(FIELD_DESCRIPTION + "_" + _).getOrElse(FIELD_DESCRIPTION)
      doc.add(new TextField(fieldName, description.label, Field.Store.YES))
    })
    place.names.foreach(name => {
      name.labels.foreach(label => {
        val fieldName = label.lang.map(FIELD_NAME + "_" + _).getOrElse(FIELD_NAME) 
        doc.add(new TextField(fieldName, label.label, Field.Store.YES)) 
      })
    })
    place.locations.foreach(location => doc.add(new StringField(FIELD_GEOMETRY, wktWriter.write(location.geometry), Field.Store.YES)))
    place.subjects.foreach(subject => doc.add(new StringField(FIELD_SUBJECT, subject, Field.Store.YES)))
    place.closeMatches.foreach(closeMatch => doc.add(new StringField(FIELD_CLOSE_MATCH, closeMatch, Field.Store.YES)))
    doc.add(new StringField(FIELD_SEED_URI, seedURI.getOrElse(place.uri), Field.Store.YES))
    
    doc
  }

}