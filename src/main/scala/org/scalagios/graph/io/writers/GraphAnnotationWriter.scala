package org.scalagios.graph.io.writers

import scala.collection.JavaConverters._

import com.tinkerpop.blueprints.pgm.TransactionalGraph
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion

import org.scalagios.api.{Dataset, GeoAnnotation}
import org.scalagios.graph.Constants._
import org.scalagios.graph.DatasetVertex
import org.scalagios.graph.io.PelagiosGraphIOBase
import org.scalagios.graph.exception.GraphIOException

trait GraphAnnotationWriter extends PelagiosGraphIOBase {
  
  /**
   * Imports annotations into a specified graph context. This method
   * will check for prefix- and RegEx-pattern associations.
   */
  def insertAnnotations(annotations: Iterable[GeoAnnotation], context: String): Unit =
    // TODO there must be a way to implement this Scala-style rather than Java-style
    insertAnnotations(annotations, context, null)
  
  /**
   * Imports annotations from a named dump file in to a specified graph context.
   * In addition to dumpfile-URL-based association, this method will also
   * check prefix- and RegEx-patterns. 
   */
  def insertAnnotations(annotations: Iterable[GeoAnnotation], context: String, dumpfile: String): Unit = {    
    val datasets = datasetIndex.get(DATASET_CONTEXT, context).iterator.asScala.map(new DatasetVertex(_))    

    if (graph.isInstanceOf[TransactionalGraph]) {
      val tGraph = graph.asInstanceOf[TransactionalGraph]
      tGraph.setMaxBufferSize(0)
      tGraph.startTransaction()
    }
    
    datasets.foreach(dataset => {
      // Evaluate if 
      // * there are NO specific datadumps associated with this dataset OR
      // * the dumpfile is EXPLICITELY LISTED among the dataset's associated datadumps 
      if (dataset.associatedDatadumps.isEmpty ||
         (dumpfile != null && dataset.associatedDatadumps.contains(dumpfile))) {
        
        if (dataset.associatedUriSpace.isDefined) {
          annotations.filter(_.uri.startsWith(dataset.associatedUriSpace.get))
            .foreach(_insertAnnotationVertex(_, dataset))
        } else if (dataset.associatedRegexPattern.isDefined) { 
          // TODO implement regex matching
          
        } else if (dumpfile != null) {
          annotations.foreach(_insertAnnotationVertex(_, dataset))
        }
      }
    })
    
    // TODO catch GraphIOException and end the transaction with Conclusion.FAILURE!
    if (graph.isInstanceOf[TransactionalGraph])
      graph.asInstanceOf[TransactionalGraph].stopTransaction(Conclusion.SUCCESS)
  }
  
  private def _insertAnnotationVertex(annotation: GeoAnnotation, dataset: DatasetVertex) = {
    // Create ANNOTATION vertex
    val annotationVertex = graph.addVertex(null)
    annotationVertex.setProperty(VERTEX_TYPE, ANNOTATION_VERTEX)
    annotationVertex.setProperty(ANNOTATION_URI, annotation.uri)
    annotationVertex.setProperty(ANNOTATION_BODY, annotation.body)
    if (annotation.title.isDefined) annotationVertex.setProperty(ANNOTATION_TITLE, annotation.title.get)
    
    // Create ANNOTATION_TARGET vertex
    val annotationTargetVertex = graph.addVertex(null)
    annotationTargetVertex.setProperty(VERTEX_TYPE, ANNOTATION_TARGET_VERTEX)
    annotationTargetVertex.setProperty(ANNOTATION_TARGET_URI, annotation.target.uri)
    if (annotation.target.title.isDefined) annotationTargetVertex.setProperty(ANNOTATION_TARGET_TITLE, annotation.target.title.get)

    // Create DATASET -- contains --> ANNOTATION relation
    graph.addEdge(null, dataset.vertex, annotationVertex, RELATION_CONTAINS)
    
    // Create ANNOTATION -- hasTarget --> ANNOTATION_TARGET relation
    graph.addEdge(null, annotationVertex, annotationTargetVertex, RELATION_HASTARGET)
    
    // Create ANNOTATION -- hasBody --> PLACE relation 
    val places = placeIndex.get(PLACE_URI, annotation.body)
    if (places.hasNext())
      graph.addEdge(null, annotationVertex, places.next(), RELATION_HASBODY)
    else
      throw GraphIOException("Place referenced by annotation not found in Graph: " + annotation.body)

    // Add to index
    annotationIndex.put(ANNOTATION_URI, annotation.uri, annotationVertex)
  }
  
}