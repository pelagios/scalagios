package org.scalagios.graph.io

import java.net.URL
import com.tinkerpop.blueprints.pgm.Vertex
import com.tinkerpop.blueprints.pgm.IndexableGraph
import org.scalagios.api.{Place, GeoAnnotation}
import org.scalagios.graph.Constants._

/**
 * Provides Pelagios-specific Graph DB I/O features, including
 * 
 * <ul>
 * <li>inserting GeoAnnotations</li>
 * <li>inserting Places</li>
 * </ul>
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class PelagiosGraphWriter[T <: IndexableGraph](graph: T) {
  
  // Get (or lazily create) the place index
  private val placeIndex = 
    if (graph.getIndex(INDEX_FOR_PLACES, classOf[Vertex]) == null)
      graph.createManualIndex(INDEX_FOR_PLACES, classOf[Vertex])
    else 
      graph.getIndex(INDEX_FOR_PLACES, classOf[Vertex])
    
  // Get (or lazily create) the annotation index
  private val annotationIndex = 
    if (graph.getIndex(INDEX_FOR_ANNOTATIONS, classOf[Vertex]) == null)
      graph.createManualIndex(INDEX_FOR_ANNOTATIONS, classOf[Vertex])
    else
      graph.getIndex(INDEX_FOR_ANNOTATIONS, classOf[Vertex])

  def insertAnnotations(annotations: Iterable[GeoAnnotation]): Unit = {
    annotations.foreach(annotation => {
      val vertex = graph.addVertex(null)
      vertex.setProperty(ANNOTATION_URI, annotation.uri)
      vertex.setProperty(ANNOTATION_BODY, annotation.body)
      vertex.setProperty(ANNOTATION_TARGET, annotation.target)
    
      // Add to index
      annotationIndex.put(ANNOTATION_URI, annotation.uri, vertex)
      
      // Create ANNOTATION -- hasBody --> PLACE relation 
      val places = placeIndex.get(PLACE_URI, annotation.body)
      if (places.hasNext())
        graph.addEdge(null, vertex, places.next(), RELATION_HASBODY)
      else
        throw UnknownPlaceException("Annotation references Place " + annotation.body + " but was not found in graph")
    })
  }
  
  def insertPlaces(places: Iterable[Place]): Unit = {
    places.foreach(place => {
      val normalizedURL = normalizeURL(place.uri)
      
      val vertex = graph.addVertex(null)
      vertex.setProperty(PLACE_URI, normalizedURL)
      if (place.label != null) vertex.setProperty(PLACE_LABEL, place.label)
      if (place.comment != null) vertex.setProperty(PLACE_COMMENT, place.comment)
      if (place.altLabels.size > 0) vertex.setProperty(PLACE_ALTLABELS, place.altLabels)
      if (place.geometryWKT != null) vertex.setProperty(PLACE_GEOMETRY, place.geometryWKT)    
      vertex.setProperty(PLACE_LON, place.lon)
      vertex.setProperty(PLACE_LAT, place.lat)
      
      // Add to index
      placeIndex.put(PLACE_URI, normalizedURL, vertex)
      if (place.label != null) placeIndex.put(PLACE_LABEL, place.label, vertex)
    })
    
    // Create PLACE -- within --> PLACE relations
    places.filter(place => place.within != null).foreach(place => {
      val normalizedURL = normalizeURL(place.uri)
      val normalizedWithin = normalizeURL(place.within)
      
      val origin =
        if (placeIndex.count(PLACE_URI, normalizedURL) > 0) placeIndex.get(PLACE_URI, normalizedURL).next()
        else null
        
      val destination = 
        if (placeIndex.count(PLACE_URI, normalizedWithin) > 0) placeIndex.get(PLACE_URI, normalizedWithin).next()
        else null
        
      if (origin == null || destination == null)
        throw UnknownPlaceException("Could not create relation: " + normalizedURL + " WITHIN " + normalizedWithin)
      else
        graph.addEdge(null, origin, destination, RELATION_WITHIN)      
    })
  }
  
  private def normalizeURL(s: String): String = {
    val url = new URL(s)
    url.getProtocol + "://" + url.getHost + url.getPath
  }
 
}