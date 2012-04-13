package org.scalagios.graph.io.write

import com.tinkerpop.blueprints.pgm.IndexableGraph

import org.scalagios.graph.io.writers.{GraphPlaceWriter, GraphDatasetWriter}

class PelagiosGraphWriter[T <: IndexableGraph](val graph: T) extends GraphPlaceWriter with GraphDatasetWriter {
  
  /*
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
    if (dataset.description != null) datasetVertex.setProperty(DATASET_DESCRIPTION, dataset.description)
      
    // Add to index
    datasetIndex.put(DATASET_URI, dataset.uri, datasetVertex)
    datasetIndex.put(DATASET_CONTEXT, dataset.context, datasetVertex)
    datasetIndex.put(DATASET_HASH, dataset.md5, datasetVertex)
    datasetIndex.put(DATASET_TITLE, dataset.title, datasetVertex)
    if (dataset.description != null) datasetIndex.put(DATASET_DESCRIPTION, dataset.description, datasetVertex)
    
    // Insert annotations which are children of this dataset
    if (dataset.associatedUriSpace.isDefined)
      annotations.filter(_.uri.startsWith(dataset.associatedUriSpace.get)).foreach(annotation => _insertAnnotation(annotation))
      
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
      throw GraphIOException("Place referenced by annotation not found in Graph: " + annotation.body)
  }
  */
   
}