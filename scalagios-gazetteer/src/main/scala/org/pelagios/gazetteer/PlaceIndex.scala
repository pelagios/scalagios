package org.pelagios.gazetteer

import java.io.File
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{ IndexWriter, IndexWriterConfig }
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.slf4j.LoggerFactory
import org.pelagios.Scalagios
import java.io.FileInputStream

private[gazetteer] class PlaceIndexBase(directory: File) {
  
  protected val index = FSDirectory.open(directory)
  
  protected val analyzer = new StandardAnalyzer(Version.LUCENE_4_9)
  
  protected val log = LoggerFactory.getLogger(getClass())
  
}

class PlaceIndex private(directory: File) extends PlaceIndexBase(directory) with PlaceIndexReader with PlaceIndexWriter {
  
  /** Applies a gazetteer patch file to this index **/
  def applyPatch(file: String, replace: Boolean, propagate: Boolean) = {
    val patches = Scalagios.readPlacePatches(new FileInputStream(file), file)
    log.info("Loaded " + patches.size + " patch records")
    
    val writer = new IndexWriter(index, new IndexWriterConfig(Version.LUCENE_4_9, analyzer))
    patches.foreach(patch => {
      val affectedPlaces = 
        if (propagate) { 
          val patchTarget = findByURI(patch.uri)
          if (patchTarget.isEmpty)
            Seq.empty[PlaceDocument]
          else
            getNetwork(patchTarget.get).places
        } else {
          findByURI(patch.uri).map(Seq(_)).getOrElse(Seq.empty[PlaceDocument])
        }
      
      if (affectedPlaces.size == 0) {
        log.warn("Could not patch place " + patch.uri + " - not in index")
      } else {
        log.info("Applying patch for " + patch.uri)
        if (affectedPlaces.size > 1)
          log.info("Propagating patch to " + (affectedPlaces.size - 1) + " network members")
        
        affectedPlaces.foreach(affectedPlace => {
          val patchedPlace = patch.patch(affectedPlace, replace)
          updatePlace(affectedPlace, patchedPlace, writer)
          log.info("Applied patch to " + affectedPlace.uri)
        })
      }
    })
    writer.close()
  }
  
}

object PlaceIndex {
  
  val FIELD_URI = "uri"
  
  val FIELD_LABEL = "label"
  
  val FIELD_DESCRIPTION = "description"
    
  val FIELD_NAME = "name"
    
  val FIELD_NAME_LITERALS = "name_literals"
    
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
          new IndexWriterConfig(Version.LUCENE_4_9, new StandardAnalyzer(Version.LUCENE_4_9)))
      writer.close()
    }
    
    new PlaceIndex(dir)
  }

}