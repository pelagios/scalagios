package org.pelagios.gazetteer

object GazetteerUtils {
    
  def normalizeURI(uri: String) = {
    val noFragment = if (uri.indexOf('#') > -1) uri.substring(0, uri.indexOf('#')) else uri
    if (noFragment.endsWith("/"))
      noFragment.substring(0, noFragment.size - 1)
    else 
      noFragment
  }
  
  def getID(uri: String) = {
    // TODO resolve URIs to ID shortcodes e.g. "pleiades:123456"
    uri
  }
  
  def getURI(id: String) = {
    // TODO resolve shortcodes to URIs, e.g. "pleiades:123456" -> "http://pleiades.stoa.org/places/123456"
  }

}