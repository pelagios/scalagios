package org.pelagios.gazetteer

import org.pelagios.api.Place

class PlaceIndexReader {
  
  /** Retrieves a name by its URI
    * @param uri the URI  
    */
  def findByURI(uri: String): PlaceDocument = {
    null // TODO do
  }
  
  def query(query: String, fuzzy: Boolean = false): Iterable[PlaceDocument] = {
    null // TODO do
  }
  
  def getNetwork(place: PlaceDocument): Network = {
    null // TODO do
  }

}

case class Network(places: Seq[Place], links: Seq[(Int, Int)])