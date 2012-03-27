package org.scalagios.graph.io

import java.net.URL
import scala.collection.JavaConverters._
import com.tinkerpop.blueprints.pgm.Vertex
import com.tinkerpop.blueprints.pgm.IndexableGraph
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion
import org.scalagios.api.{Dataset, GeoAnnotation, Place}
import org.scalagios.graph.Constants._
import com.tinkerpop.blueprints.pgm.TransactionalGraph

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
      
      
  def insertAnnotations(rootDatasets: Iterable[Dataset], annotations: Iterable[GeoAnnotation]): Unit = {
    if (graph.isInstanceOf[TransactionalGraph]) {
      val tGraph = graph.asInstanceOf[TransactionalGraph]
      tGraph.setMaxBufferSize(0)
      tGraph.startTransaction()
    }
    
    rootDatasets.foreach(dataset => insertDataset(dataset, annotations))
    
    if (graph.isInstanceOf[TransactionalGraph])
      graph.asInstanceOf[TransactionalGraph].stopTransaction(Conclusion.SUCCESS)
  }
  
  private def insertDataset(dataset: Dataset, annotations: Iterable[GeoAnnotation]): Vertex = {
    // Insert dataset into graph
    val datasetVertex = graph.addVertex(null)
    datasetVertex.setProperty(VERTEX_TYPE, DATASET_VERTEX)
    datasetVertex.setProperty(DATASET_URI, dataset.uri)
    datasetVertex.setProperty(DATASET_TITLE, dataset.title)
      
    // Insert annotations which are children of this dataset
    if (dataset.uriSpace != null)
      annotations.filter(_.uri.startsWith(dataset.uriSpace)).foreach(annotation => insertAnnotation(annotation))
      
    // Continue with this dataset's subsets
    dataset.subsets.foreach(subset => {
      val subsetVertex = insertDataset(subset, annotations)
      graph.addEdge(null, datasetVertex, subsetVertex, RELATION_SUBSET)
    })
    
    datasetVertex
  }

  private def insertAnnotation(annotation: GeoAnnotation): Unit = {
    // Create annotation (plus target) node
    val annotationVertex = graph.addVertex(null)
    annotationVertex.setProperty(VERTEX_TYPE, ANNOTATION_VERTEX)
    annotationVertex.setProperty(ANNOTATION_URI, annotation.uri)
    annotationVertex.setProperty(ANNOTATION_BODY, annotation.body)
        
    val annotationTargetVertex = graph.addVertex(null)
    annotationTargetVertex.setProperty(VERTEX_TYPE, ANNOTATION_TARGET_VERTEX)
    annotationTargetVertex.setProperty(ANNOTATION_TARGET_URI, annotation.target.uri)
    if (annotation.target.title != null)
      annotationTargetVertex.setProperty(ANNOTATION_TARGET_TITLE, annotation.target.title)
        
    graph.addEdge(null, annotationVertex, annotationTargetVertex, RELATION_HASTARGET)
        
    // Add to index
    annotationIndex.put(ANNOTATION_URI, annotation.uri, annotationVertex)
    
    // Create ANNOTATION -- hasBody --> PLACE relation 
    val places = placeIndex.get(PLACE_URI, annotation.body)
    if (places.hasNext())
      graph.addEdge(null, annotationVertex, places.next(), RELATION_HASBODY)
    else
      throw UnknownPlaceException("Annotation references Place " + annotation.body + " but was not found in graph")
  }
  
  def insertPlaces(places: Iterable[Place]): Unit = {
    if (graph.isInstanceOf[TransactionalGraph]) {
      val tGraph = graph.asInstanceOf[TransactionalGraph]
      tGraph.setMaxBufferSize(0)
      tGraph.startTransaction()
    }

    places.foreach(place => {
      val normalizedURL = normalizeURL(place.uri)
      
      val vertex = graph.addVertex(null)
      vertex.setProperty(VERTEX_TYPE, PLACE_VERTEX)
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
    
    graph.getVertices().asScala.filter(_.getProperty(VERTEX_TYPE).equals(ANNOTATION_VERTEX)).foreach(v => {
      // TODO re-wire connections between annotations and places
      // TODO record floating annotations!
    })
    
    if (graph.isInstanceOf[TransactionalGraph])
      graph.asInstanceOf[TransactionalGraph].stopTransaction(Conclusion.SUCCESS)
  }
  
  private def normalizeURL(s: String): String = {
    val url = new URL(s)
    url.getProtocol + "://" + url.getHost + url.getPath
  }
 
}