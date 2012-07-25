package org.scalagios.graph.io.writers

import scala.collection.JavaConverters._
import com.tinkerpop.blueprints.pgm.{IndexableGraph, TransactionalGraph}
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion
import org.scalagios.api.{GeoAnnotation, Place}
import org.scalagios.graph.Constants._
import org.scalagios.graph.{GeoAnnotationVertex, PlaceVertex}
import org.scalagios.graph.exception.GraphIOException
import org.scalagios.graph.io.PelagiosGraphIOBase
import org.neo4j.index.lucene.ValueContext
import org.scalagios.graph.exception.GraphIntegrityException

trait GraphPlaceWriter extends PelagiosGraphIOBase {
  
  private def BATCH_SIZE = 2000
  
  def insertPlaces(places: Iterable[Place]) = {
    // Split into batches to make transactions smaller (less memory consumption)
    places.grouped(BATCH_SIZE).foreach(batch => insertPlaceBatch(batch))
    postProcessing(places)
  }
  
  def insertPlaceBatch(places: Iterable[Place]) = {    
    if (graph.isInstanceOf[TransactionalGraph]) {
      val tGraph = graph.asInstanceOf[TransactionalGraph]
      tGraph.setMaxBufferSize(0)
      tGraph.startTransaction()
    }

    places.foreach(place => {
      val normalizedURL = normalizeURL(place.uri)
      
      // Create vertex
      val vertex = graph.addVertex(null)
      vertex.setProperty(VERTEX_TYPE, PLACE_VERTEX)
      vertex.setProperty(PLACE_URI, normalizedURL)
      if (place.label.isDefined) vertex.setProperty(PLACE_LABEL, place.label.get)
      if (place.comment.isDefined) vertex.setProperty(PLACE_COMMENT, place.comment.get)
      if (place.altLabels.size > 0) vertex.setProperty(PLACE_ALTLABELS, place.altLabels.get)
      if (place.coverage.isDefined) vertex.setProperty(PLACE_COVERAGE, place.coverage.get)
      if (place.featureType.isDefined) vertex.setProperty(PLACE_FEATURE_TYPE, place.featureType.get)
      if (place.geometryWKT.isDefined) vertex.setProperty(PLACE_GEOMETRY, place.geometryWKT.get)    
      vertex.setProperty(PLACE_LON, place.lon)
      vertex.setProperty(PLACE_LAT, place.lat)
      
      // Connect to subreference node
      graph.addEdge(null, placeSubreferenceNode, vertex, RELATION_PLACE)
      
      // Add to index
      placeIndex.put(PLACE_URI, normalizedURL, vertex)
      if (place.label.isDefined) placeIndex.put(PLACE_LABEL, place.label.get, vertex)
      if (place.comment.isDefined) placeIndex.put(PLACE_COMMENT, place.comment.get, vertex)
      if (place.altLabels.isDefined) placeIndex.put(PLACE_ALTLABELS, place.altLabels.get, vertex)
      if (place.coverage.isDefined) placeIndex.put(PLACE_COVERAGE, place.coverage.get, vertex)
      if (place.location.isDefined) {
        val centroid = place.location.get.getCentroid.getCoordinate
        placeIndex.put(PLACE_LON, new ValueContext(centroid.x).indexNumeric(), vertex)
        placeIndex.put(PLACE_LAT, new ValueContext(centroid.y).indexNumeric(), vertex)
      }
    })
    
    if (graph.isInstanceOf[TransactionalGraph])
      graph.asInstanceOf[TransactionalGraph].stopTransaction(Conclusion.SUCCESS)
  }
  
  private def connectPlaces(from: Place, to:Place, relation: String): Unit = {
      val normalizedFrom = normalizeURL(from.uri)
      val normalizedTo = normalizeURL(to.uri)
      
      val origin =
        if (placeIndex.count(PLACE_URI, normalizedFrom) > 0) placeIndex.get(PLACE_URI, normalizedFrom).next()
        else null
        
      val destination = 
        if (placeIndex.count(PLACE_URI, normalizedTo) > 0) placeIndex.get(PLACE_URI, normalizedTo).next()
        else null
        
      if (origin == null || destination == null)
        throw GraphIOException("Could not create relation: " + normalizedFrom + " " + relation + " " + normalizedTo)
      else
        graph.addEdge(null, origin, destination, relation)        
  }
  
  private def connectionExists(fromURI: String, toURI: String): Boolean = {      
    val from =
      if (placeIndex.count(PLACE_URI, fromURI) > 0)
        new PlaceVertex(placeIndex.get(PLACE_URI, fromURI).next())
      else 
        // Should never happen
        throw GraphIOException("Checking connectsWith for " + fromURI + " - but isn't in the graph")
      
      if (from.connectsWith.map(_.uri).contains(toURI))
        true
      else
        true
  }
   
  def postProcessing(places: Iterable[Place] ) {
    if (graph.isInstanceOf[TransactionalGraph]) {
      val tGraph = graph.asInstanceOf[TransactionalGraph]
      tGraph.setMaxBufferSize(0)
      tGraph.startTransaction()
    }

    // Create PLACE -- within --> PLACE relations
    places.filter(place => place.within.isDefined).foreach(place => connectPlaces(place, place.within.get, RELATION_WITHIN))
    
    // Create PLACE -- connectsWith --> PLACE relations
    places.filter(place => place.connectsWith.size > 0).foreach(place => {
      place.connectsWith.foreach(connectsWith => connectPlaces(place, connectsWith, RELATION_CONNECTS_WITH))
    })
    
    // Create PLACE -- sameAs --> PLACE relations
    places.filter(place => place.sameAs.isDefined).foreach(place => connectPlaces(place, place.sameAs.get, RELATION_SAMEAS))
    
    // If there are annotations in the DB already, re-wire them
    var floatingAnnotations = List.empty[GeoAnnotation]
    graph.getVertices().asScala.filter(_.getProperty(VERTEX_TYPE).equals(ANNOTATION_VERTEX)).foreach(annotation => {
      val hasBody = annotation.getProperty(ANNOTATION_BODY)
      
      if (placeIndex.count(PLACE_URI, hasBody) > 0) {
        val place = placeIndex.get(PLACE_URI, hasBody).next()
        graph.addEdge(null, annotation, place, RELATION_HASBODY)
      } else {
        floatingAnnotations ::= new GeoAnnotationVertex(annotation)
      }
    })
    if (floatingAnnotations.size > 0)
      throw new GraphIOException("Could not re-wire all annotations after Place import:\n" +
      	floatingAnnotations.mkString("\n"))    
    
    if (graph.isInstanceOf[TransactionalGraph])
      graph.asInstanceOf[TransactionalGraph].stopTransaction(Conclusion.SUCCESS)
  }
  
}