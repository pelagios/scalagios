package org.pelagios.gazetteer

import org.apache.lucene.document.Document
import org.pelagios.api.{ Label, Location, Name, Place }
import scala.collection.JavaConversions._
import org.apache.lucene.document.Fieldable

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
  private def toLabel(field: Fieldable): Label = {
    val language = if (field.name.indexOf('_') > -1) Some(field.name.substring(field.name.indexOf('_') + 1)) else None
    Label(field.stringValue(), language)    
  }
  
}

object PlaceDocument {
  
  val FIELD_URI = "uri"
  
  val FIELD_TITLE = "title"
  
  val FIELD_DESCRIPTION = "description"
    
  val FIELD_GEOMETRY = "wkt"
    
  val FIELD_SUBJECT = "subject"
    
  val FIELD_CLOSE_MATCH = "close_match"
  
  val FIELD_SEED_URI = "group_uri"
  
  def apply(place: Place) = {
    /*
    val doc = new Document()
    doc.add(new StringField(FIELD_URI, normalizeURI(place.uri), Field.Store.YES))
    doc.add(new StringField(FIELD_GROUP_ID, groupId, Field.Store.YES))
    doc.add(new TextField(FIELD_TITLE, place.title, Field.Store.YES))
    place.descriptions.foreach(description => doc.add(new TextField(FIELD_DESCRIPTION, description.label, Field.Store.YES)))
    place.names.foreach(name => {
      name.labels.foreach(label => {
        doc.add(new TextField(FIELD_NAME_LABEL, label.label, Field.Store.YES)) 
      })
      name.altLabels.foreach(altLabel => doc.add(new TextField(FIELD_NAME_ALTLABEL, altLabel.label, Field.Store.YES)))
    })
      
    place.locations.foreach(location => doc.add(new StringField(FIELD_GEOMETRY, wktWriter.write(location.geometry), Field.Store.YES)))
    doc
    */
  }  
  

  
}