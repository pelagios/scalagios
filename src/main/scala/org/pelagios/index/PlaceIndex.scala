package org.pelagios.index

import java.io.File
import org.pelagios.api.Place
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.document.{ Document, Field, StringField, TextField }
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{ IndexReader, IndexWriter, IndexWriterConfig }
import org.apache.lucene.search.{ IndexSearcher, TopScoreDocCollector }
import org.pelagios.api.DefaultPlace
import org.pelagios.api.Label

class PlaceIndex(directory: File) {
  
  import PlaceIndex._
  
  private val index = FSDirectory.open(directory)
  private val analyzer = new StandardAnalyzer(Version.LUCENE_44)
  
  /** Add places to the index
    * 
    * @param places the places to add
    */
  def addPlaces(places: Iterable[Place]): Unit = {    
    val writer = new IndexWriter(index, new IndexWriterConfig(Version.LUCENE_44, analyzer))
    
    places.foreach(place => {
      val doc = new Document
      doc.add(new StringField(FIELD_URI, place.uri, Field.Store.YES))
      doc.add(new TextField(FIELD_TITLE, place.title.label, Field.Store.YES))
      place.descriptions.foreach(description => doc.add(new TextField(FIELD_DESCRIPTION, description.label, Field.Store.YES)))
      place.names.foreach(name => {
        name.labels.foreach(label => doc.add(new TextField(FIELD_NAME_LABEL, label.label, Field.Store.YES)))
        name.altLabels.foreach(altLabel => doc.add(new TextField(FIELD_NAME_ALTLABEL, altLabel.label, Field.Store.YES)))
      })
      writer.addDocument(doc)
    })

    writer.close
  }
  
  def query(query: String): Seq[Place] = {
    val q = new QueryParser(Version.LUCENE_44, FIELD_TITLE, analyzer).parse(query)
    
    val reader = IndexReader.open(index)
    val searcher = new IndexSearcher(reader)
    
    val collector = TopScoreDocCollector.create(HITS_PER_PAGE, true)
    searcher.search(q, collector)
    
    collector.topDocs.scoreDocs.map(scoreDoc => {
      val doc = searcher.doc(scoreDoc.doc)
      val place = new DefaultPlace(doc.get(FIELD_URI))
      place.title = new Label(doc.get(FIELD_TITLE))
      place
    })
  }
  
}

object PlaceIndex {
  
  val HITS_PER_PAGE = 20
  
  val FIELD_URI = "uri"
  val FIELD_TITLE = "title"
  val FIELD_DESCRIPTION = "description"
  val FIELD_NAME_LABEL = "label"
  val FIELD_NAME_ALTLABEL = "alt-label"
  
  /** Opens a place index from a folder on disk.
    *  
    * If no index exists on the specified location, an empty index will be created.  
    * 
    * @param directory the root directory
    * @return a place index
    */
  def open(directory: File): PlaceIndex = new PlaceIndex(directory)
  
}