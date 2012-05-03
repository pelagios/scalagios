package org.scalagios.graph.io.writers

import scala.collection.JavaConverters._
import scala.collection.mutable.HashMap
import com.tinkerpop.blueprints.pgm.TransactionalGraph
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion
import org.scalagios.api.{Dataset, GeoAnnotation, Place}
import org.scalagios.graph.Constants._
import org.scalagios.graph.DatasetVertex
import org.scalagios.graph.io.PelagiosGraphIOBase
import org.scalagios.graph.exception.GraphIOException
import org.scalagios.graph.exception.GraphIntegrityException
import org.scalagios.graph.exception.GraphIOException

trait GraphAnnotationWriter extends PelagiosGraphIOBase {

  // For counting number of references to a particular places: Place URI -> no. of times referenced
  val aggregatedReferences = HashMap[String, Int]()
   
  def insertAnnotations(annotations: Iterable[GeoAnnotation], datasetUri: String, dumpfile: String = null): Unit = {
    val datasets = datasetIndex.get(DATASET_URI, datasetUri).iterator.asScala.map(new DatasetVertex(_)).toList
    
    if (datasets.size == 0)
      throw new GraphIOException("Dataset " + datasetUri + " not found in Graph")
    
    if (datasets.size > 1)
      // Should honestly never ever happen
      throw new GraphIntegrityException("More than one dataset indexed for URI " + datasetUri)
    
    _insertIntoDataset(annotations, datasets.head, dumpfile)
  }
  
  private def _insertIntoDataset(annotations: Iterable[GeoAnnotation], dataset: DatasetVertex, dumpfile: String = null): Unit = {
    if (dataset.subsets.size > 0) {
      // Annotations are (per convention) ALWAYS in the leave sets,
      // i.e. if a dataset has subsets, there can be no annotations inside 
      dataset.subsets.foreach(subset => _insertIntoDataset(annotations, subset, dumpfile))
    } else {
      if (graph.isInstanceOf[TransactionalGraph]) {
        val tGraph = graph.asInstanceOf[TransactionalGraph]
        tGraph.setMaxBufferSize(0)
        tGraph.startTransaction()
      }
    
      // Reset place reference counter
      aggregatedReferences.clear
      
      // Evaluate if 
      // * there are NO specific datadumps associated with this dataset OR
      // * the dumpfile is EXPLICITELY LISTED among the dataset's associated datadumps 
      if (dataset.associatedDatadumps.isEmpty ||
         (dumpfile != null && dataset.associatedDatadumps.contains(dumpfile))) {
        
        // Insert annotation vertices
        if (dataset.associatedUriSpace.isDefined) {
          annotations.filter(_.uri.startsWith(dataset.associatedUriSpace.get))
            .foreach(_createAnnotationVertex(_, dataset))
        } else if (dataset.associatedRegexPattern.isDefined) { 
          // TODO implement regex matching
         
        } else if (dumpfile != null && dataset.associatedDatadumps.contains(dumpfile)) {
          annotations.foreach(annotation => _createAnnotationVertex(annotation, dataset))
        }
        
        aggregatedReferences.foreach {case (key, value) => {
          val hits = placeIndex.get(PLACE_URI, key)
          if (hits.hasNext) {
            val edge = graph.addEdge(null, dataset.vertex, hits.next, RELATION_REFERENCES)
            edge.setProperty(REL_PROPERTY_REFERENCECOUNT, value)
          }  
        }}
      }  
    
      // TODO catch GraphIOException and end the transaction with Conclusion.FAILURE!
      if (graph.isInstanceOf[TransactionalGraph])
        graph.asInstanceOf[TransactionalGraph].stopTransaction(Conclusion.SUCCESS)
    }
  }
  
  private def _createAnnotationVertex(annotation: GeoAnnotation, dataset: DatasetVertex) = {
    val normalizedBody = normalizeURL(annotation.body)
    
    // Create ANNOTATION vertex
    val annotationVertex = graph.addVertex(null)
    annotationVertex.setProperty(VERTEX_TYPE, ANNOTATION_VERTEX)
    annotationVertex.setProperty(ANNOTATION_URI, annotation.uri)
    annotationVertex.setProperty(ANNOTATION_BODY, normalizedBody)
    if (annotation.title.isDefined) annotationVertex.setProperty(ANNOTATION_TITLE, annotation.title.get)
    
    // Create ANNOTATION_TARGET vertex
    val annotationTargetVertex = graph.addVertex(null)
    annotationTargetVertex.setProperty(VERTEX_TYPE, ANNOTATION_TARGET_VERTEX)
    annotationTargetVertex.setProperty(ANNOTATION_TARGET_URI, annotation.target.uri)
    if (annotation.target.title.isDefined) annotationTargetVertex.setProperty(ANNOTATION_TARGET_TITLE, annotation.target.title.get)
    if (annotation.target.thumbnail.isDefined) annotationTargetVertex.setProperty(ANNOTATION_TARGET_THUMBNAIL, annotation.target.thumbnail.get)

    // Create DATASET -- contains --> ANNOTATION relation
    graph.addEdge(null, dataset.vertex, annotationVertex, RELATION_CONTAINS)
    
    // Create ANNOTATION -- hasTarget --> ANNOTATION_TARGET relation
    graph.addEdge(null, annotationVertex, annotationTargetVertex, RELATION_HASTARGET)
    
    // Create ANNOTATION -- hasBody --> PLACE relation 
    val places = placeIndex.get(PLACE_URI, normalizedBody)
    if (places.hasNext())
      graph.addEdge(null, annotationVertex, places.next(), RELATION_HASBODY)
    else
      throw GraphIOException("Place referenced by annotation not found in Graph: " + normalizedBody)

    // Add to index
    annotationIndex.put(ANNOTATION_URI, annotation.uri, annotationVertex)
    if (annotation.title.isDefined) annotationIndex.put(ANNOTATION_TITLE, annotation.title.get, annotationVertex)
    if (annotation.target.title.isDefined) annotationIndex.put(ANNOTATION_TARGET_URI, annotation.target.title.get, annotationVertex)
    
    // Record in aggregateReferences counter
    val referenceCount = aggregatedReferences.get(normalizedBody).getOrElse(0)
    aggregatedReferences.put(normalizedBody, referenceCount + 1)
  }
  
}