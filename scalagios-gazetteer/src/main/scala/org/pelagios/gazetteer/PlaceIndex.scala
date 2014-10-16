package org.pelagios.gazetteer

import java.io.File
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{ IndexWriter, IndexWriterConfig }
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.slf4j.LoggerFactory

private[gazetteer] class PlaceIndexBase(directory: File) {
  
  protected val index = FSDirectory.open(directory)
  
  protected val analyzer = new StandardAnalyzer(Version.LUCENE_48)
  
  protected val log = LoggerFactory.getLogger(getClass())
  
}

class PlaceIndex private(directory: File) extends PlaceIndexBase(directory) with PlaceIndexReader with PlaceIndexWriter

object PlaceIndex {
  
  val FIELD_URI = "uri"
  
  val FIELD_LABEL = "label"
  
  val FIELD_DESCRIPTION = "description"
    
  val FIELD_NAME = "name"
    
  val FIELD_GEOMETRY = "wkt"
    
  val FIELD_CATEGORY = "category"
    
  val FIELD_SUBJECT = "subject"
    
  val FIELD_CLOSE_MATCH = "close_match"
    
  val FIELD_EXACT_MATCH = "exact_match"
  
  val FIELD_SEED_URI = "group_uri"
  
  def open(directory: String): PlaceIndex = {
    val dir = new File(directory)
    if (!dir.exists) {
      // Initialize with an empty index
      dir.mkdirs()
      
      val writer = new IndexWriter(FSDirectory.open(dir), 
          new IndexWriterConfig(Version.LUCENE_48, new StandardAnalyzer(Version.LUCENE_48)))
      writer.close()
    }
    
    new PlaceIndex(dir)
  }

}