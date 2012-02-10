package org.scalagios.graph.io

import com.weiglewilczek.slf4s.Logging

import com.tinkerpop.blueprints.pgm.Vertex
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.Neo4jBatchGraph

import org.scalagios.api.{Place, GeoAnnotation}
import org.scalagios.graph.Constants._

class Neo4jBatchWriter(graph: Neo4jBatchGraph) extends Logging {
  
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

  def insertAnnotation(annotation: GeoAnnotation): Unit = {
    val vertex = graph.addVertex(null)
    vertex.setProperty(ANNOTATION_URI, annotation.uri)
    vertex.setProperty(ANNOTATION_BODY, annotation.body)
    vertex.setProperty(ANNOTATION_TARGET, annotation.target)
    
    annotationIndex.put(ANNOTATION_URI, annotation.uri, vertex)
    
    val places = placeIndex.get(PLACE_URI, annotation.body)
    
    if (places.hasNext())
      graph.addEdge(null, vertex, places.next(), RELATION_HASBODY)      
  }
  
  def insertPlace(place: Place): Unit = {
    val vertex = graph.addVertex(null)
    vertex.setProperty(PLACE_URI, place.uri)
    if (place.label != null) vertex.setProperty(PLACE_LABEL, place.label)
    if (place.comment != null) vertex.setProperty(PLACE_COMMENT, place.comment)
    if (place.altLabels.size > 0) vertex.setProperty(PLACE_ALTLABELS, place.altLabels)
    if (place.geometryWKT != null) vertex.setProperty(PLACE_GEOMETRY, place.geometryWKT)    
    vertex.setProperty(PLACE_LON, place.lon)
    vertex.setProperty(PLACE_LAT, place.lat)
    
    // Add to index
    placeIndex.put(PLACE_URI, place.uri, vertex)
    if (place.label != null) placeIndex.put(PLACE_LABEL, place.label, vertex)
  }
  
  def createWithinRelation(place: Place): Unit = {
    if (place.within != null) {
      val origin =
        if (placeIndex.count(PLACE_URI, place.uri) > 0) placeIndex.get(PLACE_URI, place.uri).next()
        else null
        
      val destination = 
        if (placeIndex.count(PLACE_URI, place.within) > 0) placeIndex.get(PLACE_URI, place.within).next()
        else null
        
      if (origin == null || destination == null)
        logger.warn("Could not create relation: " + place.uri + " WITHIN " + place.within)
      else
        graph.addEdge(null, origin, destination, RELATION_WITHIN)
    }
  }
  
}