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
  
  private def normalizeURI(uri: String) = {
    val noFragment = if (uri.indexOf('#') > -1) uri.substring(0, uri.indexOf('#')) else uri
    if (noFragment.endsWith("/"))
      noFragment.substring(0, noFragment.size - 1)
    else 
      noFragment
  }

}

case class Network(places: Seq[Place], links: Seq[(Int, Int)])