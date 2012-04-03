package org.scalagios.graph.io

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import com.tinkerpop.blueprints.pgm.{Vertex, IndexableGraph}
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion
import org.scalagios.api.{Dataset, GeoAnnotation, Place}
import org.scalagios.graph.GeoAnnotationVertex
import org.scalagios.graph.Constants._
import com.tinkerpop.blueprints.pgm.TransactionalGraph

/**
 * Provides Pelagios-specific Graph DB I/O (write) features.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class PelagiosGraphWriter[T <: IndexableGraph](graph: T) extends PelagiosGraphIOBase(graph) {
  
  def insertAnnotations(rootDatasets: Iterable[Dataset], annotations: Iterable[GeoAnnotation]): Unit = {
    if (graph.isInstanceOf[TransactionalGraph]) {
      val tGraph = graph.asInstanceOf[TransactionalGraph]
      tGraph.setMaxBufferSize(0)
      tGraph.startTransaction()
    }
    
    rootDatasets.foreach(dataset => {
      val rootVertex = _insertDataset(dataset, annotations)
      
      // In addition, add each root dataset to the index using a
      // fixed "virtual" URI, so that we can later grab them from the
      // index, irrespective of their true URI. This is really ugly,
      // but I don't see another way, since Tinkerpop does not support
      // the concept of a reference node.
      datasetIndex.put(DATASET_URI, VIRTUAL_ROOT_URI, rootVertex)
    })    
    
    // TODO catch GraphImportException and make sure the transaction is closed with FAILURE
    if (graph.isInstanceOf[TransactionalGraph])
      graph.asInstanceOf[TransactionalGraph].stopTransaction(Conclusion.SUCCESS)
  }
  
  private def _insertDataset(dataset: Dataset, annotations: Iterable[GeoAnnotation]): Vertex = {
    // Insert dataset into graph
    val datasetVertex = graph.addVertex(null)
    datasetVertex.setProperty(VERTEX_TYPE, DATASET_VERTEX)
    datasetVertex.setProperty(DATASET_URI, dataset.uri)
    datasetVertex.setProperty(DATASET_TITLE, dataset.title)
      
    // Add to index
    datasetIndex.put(DATASET_URI, dataset.uri, datasetVertex)
    
    // Insert annotations which are children of this dataset
    if (dataset.uriSpace != null)
      annotations.filter(_.uri.startsWith(dataset.uriSpace)).foreach(annotation => _insertAnnotation(annotation))
      
    // Continue with this dataset's subsets
    dataset.subsets.foreach(subset => {
      val subsetVertex = _insertDataset(subset, annotations)
      graph.addEdge(null, datasetVertex, subsetVertex, RELATION_SUBSET)
    })
    
    datasetVertex
  }

  private def _insertAnnotation(annotation: GeoAnnotation): Unit = {
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
      throw GraphImportException("Place referenced by annotation not found in Graph: " + annotation.body)
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
      if (place.coverage != null) vertex.setProperty(PLACE_COVERAGE, place.coverage)
      if (place.geometryWKT != null) vertex.setProperty(PLACE_GEOMETRY, place.geometryWKT)    
      vertex.setProperty(PLACE_LON, place.lon)
      vertex.setProperty(PLACE_LAT, place.lat)
      
      // Add to index
      placeIndex.put(PLACE_URI, normalizedURL, vertex)
      if (place.label != null) placeIndex.put(PLACE_LABEL, place.label, vertex)
      if (place.altLabels != null) placeIndex.put(PLACE_ALTLABELS, place.altLabels, vertex)
      if (place.coverage != null) placeIndex.put(PLACE_COVERAGE, place.coverage, vertex)
    })
    
    // Create PLACE -- within --> PLACE relations
    places.filter(place => place.within != null).foreach(place => {
      val normalizedURL = normalizeURL(place.uri)
      val normalizedWithin = normalizeURL(place.within.uri)
      
      val origin =
        if (placeIndex.count(PLACE_URI, normalizedURL) > 0) placeIndex.get(PLACE_URI, normalizedURL).next()
        else null
        
      val destination = 
        if (placeIndex.count(PLACE_URI, normalizedWithin) > 0) placeIndex.get(PLACE_URI, normalizedWithin).next()
        else null
        
      if (origin == null || destination == null)
        throw GraphImportException("Could not create relation: " + normalizedURL + " WITHIN " + normalizedWithin)
      else
        graph.addEdge(null, origin, destination, RELATION_WITHIN)      
    })
    
    // If there are annotations in the DB already, re-wire them
    var floatingAnnotations = new ListBuffer[GeoAnnotation]()
    graph.getVertices().asScala.filter(_.getProperty(VERTEX_TYPE).equals(ANNOTATION_VERTEX)).foreach(annotation => {
      val hasBody = annotation.getProperty(ANNOTATION_BODY)
      
      if (placeIndex.count(PLACE_URI, hasBody) > 0) {
        val place = placeIndex.get(PLACE_URI, hasBody).next()
        graph.addEdge(null, annotation, place, RELATION_HASBODY)
      } else {
        floatingAnnotations.append(new GeoAnnotationVertex(annotation))
      }
    })
    if (floatingAnnotations.size > 0)
      throw new GraphImportException("Could not re-wire all annotations after Place import:\n" +
      	floatingAnnotations.mkString("\n"))
    
    if (graph.isInstanceOf[TransactionalGraph])
      graph.asInstanceOf[TransactionalGraph].stopTransaction(Conclusion.SUCCESS)
  }
   
}