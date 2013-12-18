package org.pelagios.gazetteer

import org.pelagios.api.Place
import org.apache.lucene.index.{ IndexWriter, IndexWriterConfig }
import org.apache.lucene.util.Version
import org.slf4j.Logger

trait PlaceIndexWriter extends PlaceIndexReader {
    
  def addPlaces(places: Iterable[Place]) = {
    val writer = new IndexWriter(index, new IndexWriterConfig(Version.LUCENE_44, analyzer))
    
    places.foreach(place => {
      if (findByURI(GazetteerUtils.normalizeURI(place.uri)).isDefined) {
        log.warn("Place '" + place.uri + "' already in index!")
      } else {
        val closeMatches = place.closeMatches.map(uri => findByURI(GazetteerUtils.normalizeURI(uri))).filter(_.isDefined).map(_.get)
        val seedURI = if (closeMatches.size > 0) closeMatches(0).seedURI else GazetteerUtils.normalizeURI(place.uri) 
        writer.addDocument(PlaceDocument(place, Some(seedURI)))
      }   
    })
    
    writer.close()
  }

}