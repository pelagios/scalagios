package org.pelagios.gazetteer

import java.io.InputStream
import org.apache.lucene.index.{ IndexWriter, IndexWriterConfig, Term }
import org.apache.lucene.util.Version
import org.pelagios.Scalagios
import org.pelagios.api.gazetteer.Place
import org.slf4j.Logger
import org.openrdf.rio.UnsupportedRDFormatException

trait PlaceIndexWriter extends PlaceIndexReader {
    
  private def addPlace(place: Place, writer: IndexWriter) = {
    if (findByURI(GazetteerUtils.normalizeURI(place.uri)).isDefined) {
      log.warn("Place '" + place.uri + "' already in index!")
    } else {
      // Places that this place lists as closeMatch
      val closeMatchesOut = place.closeMatches.map(uri => findByURI(GazetteerUtils.normalizeURI(uri))).filter(_.isDefined).map(_.get)

      // Places in the index that list this place as closeMatch
      val closeMatchesIn = findByByCloseMatch(GazetteerUtils.normalizeURI(place.uri))
        
      val closeMatches = closeMatchesOut ++ closeMatchesIn
        
      // All closeMatches need to share the same seed URI
      val seedURI =
        if (closeMatches.size > 0) 
          closeMatches(0).seedURI
        else
          GazetteerUtils.normalizeURI(place.uri)
 
      // Update seed URIs where necessary
      updateSeedURI(closeMatches.filter(!_.seedURI.equals(seedURI)), seedURI, writer)
        
      // Add new document to index
      writer.addDocument(PlaceDocument(place, Some(seedURI)))
    }       
  }
  
  def updatePlace(oldPlace: PlaceDocument, updatedPlace: Place, writer: IndexWriter) = {
    writer.deleteDocuments(new Term(PlaceIndex.FIELD_URI, oldPlace.uri))
    writer.addDocument(PlaceDocument(updatedPlace, Some(oldPlace.seedURI)))
  }
  
  def addPlaces(places: Iterable[Place]) = {
    val writer = new IndexWriter(index, new IndexWriterConfig(Version.LUCENE_4_9, analyzer))
    places.foreach(addPlace(_, writer))
    writer.close()
  }
  
  def addPlaceStream(is: InputStream, filename: String, lowMemoryMode: Boolean = false) = {
    val writer = new IndexWriter(index, new IndexWriterConfig(Version.LUCENE_4_9, analyzer))
    val format = Scalagios.guessFormatFromFilename(filename)
    if (format.isDefined)
      Scalagios.readPlacesFromStream(is, format.get, place => addPlace(place, writer), lowMemoryMode)
    else 
      throw new UnsupportedRDFormatException("Cannot determine format for: " + filename)
    writer.close()
  }
    
  def updateSeedURI(places: Seq[PlaceDocument], seedURI: String, writer: IndexWriter) = {
    places.foreach(place => {
      // Delete doc from index
      writer.deleteDocuments(new Term(PlaceIndex.FIELD_URI, place.uri))
      
      // Update seed URI and re-add
      writer.addDocument(PlaceDocument(place, Some(seedURI)))
    })
  }

}