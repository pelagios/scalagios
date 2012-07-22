package org.scalagios.graph.io.writers

import scala.collection.JavaConverters._
import com.tinkerpop.blueprints.pgm.{Vertex, TransactionalGraph}
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion
import org.scalagios.api.Dataset
import org.scalagios.graph.Constants._
import org.scalagios.graph.DatasetVertex
import org.scalagios.graph.io.PelagiosGraphIOBase
import org.scalagios.graph.exception.GraphIOException
import org.scalagios.graph.exception.GraphIntegrityException
import org.scalagios.graph.exception.GraphIOException
import org.scalagios.graph.GeoAnnotationVertex
import org.scalagios.api.GeoAnnotation

trait GraphDatasetWriter extends PelagiosGraphIOBase {
  
  private val UNKNOWN_LICENSE = "[unknown]"

  /**
   * Recursively insert a dataset and all its subsets into the graph 
   */
  def insertDataset(dataset: Dataset) = {      
    if (graph.isInstanceOf[TransactionalGraph]) {
      val tGraph = graph.asInstanceOf[TransactionalGraph]
      tGraph.setMaxBufferSize(0)
      tGraph.startTransaction()
    }

    // Make sure there is no dataset with same ID in the graph already
    val existing = datasetIndex.get(DATASET_URI, dataset.uri).iterator.asScala.toList    
    if (existing.size > 0)
      throw new GraphIOException("Datset " + dataset.uri + " already exists in the graph")
    
    val rootVertex = _insertDatasetVertex(dataset)
    
    // In addition, add the root dataset to the index using a f
    // fixed "virtual" URI, so that we can later grab them from the
    // index easily, irrespective of their true URI. This is really ugly,
    // but I don't see another way, since Tinkerpop does not support
    // the concept of a reference node.
    datasetIndex.put(DATASET_URI, ROOT_DATASET_URI, rootVertex)

    if (graph.isInstanceOf[TransactionalGraph])
      graph.asInstanceOf[TransactionalGraph].stopTransaction(Conclusion.SUCCESS)
  }
  
  private def _insertDatasetVertex(dataset: Dataset, parent: Option[DatasetVertex] = None): Vertex = {
    // Insert dataset into graph
    val datasetVertex = graph.addVertex(null)
    datasetVertex.setProperty(VERTEX_TYPE, DATASET_VERTEX)
    datasetVertex.setProperty(DATASET_URI, dataset.uri)
    datasetVertex.setProperty(DATASET_ROOTURI, dataset.rootUri)
    datasetVertex.setProperty(DATASET_LASTUPDATED, dataset.lastUpdated)
    datasetVertex.setProperty(DATASET_TITLE, dataset.title)
    if (dataset.description.isDefined) datasetVertex.setProperty(DATASET_DESCRIPTION, dataset.description.get)
    if (dataset.license.isDefined) 
      datasetVertex.setProperty(DATASET_LICENSE, dataset.license.get)
    else if (parent.isDefined)
      datasetVertex.setProperty(DATASET_LICENSE, parent.get.license.getOrElse(UNKNOWN_LICENSE))
      
    if (dataset.homepage.isDefined) datasetVertex.setProperty(DATASET_HOMEPAGE, dataset.homepage.get)
    if (dataset.associatedDatadumps.size > 0) datasetVertex.setProperty(DATASET_DATADUMP, dataset.associatedDatadumps.mkString(","))
    if (dataset.associatedUriSpace.isDefined) datasetVertex.setProperty(DATASET_URISPACE, dataset.associatedUriSpace.get) 
    if (dataset.associatedRegexPattern.isDefined) datasetVertex.setProperty(DATASET_URIREGEXPATTERN, dataset.associatedRegexPattern.get)
 
    // Add to index
    datasetIndex.put(DATASET_URI, dataset.uri, datasetVertex)
    datasetIndex.put(DATASET_HASH, dataset.md5, datasetVertex)
    datasetIndex.put(DATASET_TITLE, dataset.title, datasetVertex)
    if (dataset.description.isDefined) datasetIndex.put(DATASET_DESCRIPTION, dataset.description.get, datasetVertex)
    
    // Continue with subsets
    dataset.subsets.foreach(subset => {
      val subsetVertex = _insertDatasetVertex(subset, Some(DatasetVertex(datasetVertex)))
      graph.addEdge(null, datasetVertex, subsetVertex, RELATION_SUBSET)
    })
    
    datasetVertex      
  }
  
  /**
   * Remove a dataset and all its subsets from the graph 
   * returns (number of datasets, number of annotations) removed
   */
  def dropDataset(uri: String): (Int, Int) = {
    // Note: if we have more than ONE dataset in for this URI, something is wrong!
    val vertices = datasetIndex.get(DATASET_URI, uri).iterator.asScala.toList    
    if (vertices.size > 1)
      throw new GraphIntegrityException("Index has " + vertices.size + " vertices listed for dataset " + uri)
       
    var ctrAnnotations = 0
    var ctrDatasets = 0
    
    vertices.map(new DatasetVertex(_)).foreach(dataset => {
      // Drop annotation vertices
      ctrAnnotations += dataset.countAnnotations(true)
      dataset.annotations(true).grouped(2000).foreach(_dropGeoAnnotations(_))

      if (graph.isInstanceOf[TransactionalGraph]) {
        val tGraph = graph.asInstanceOf[TransactionalGraph]
        tGraph.setMaxBufferSize(0)
        tGraph.startTransaction()
      }

      // Drop dataset vertices
      ctrDatasets += _dropDatasetVertex(dataset)
      
      // TODO catch GraphIOException and make sure the transaction is closed with FAILURE
      if (graph.isInstanceOf[TransactionalGraph])
        graph.asInstanceOf[TransactionalGraph].stopTransaction(Conclusion.SUCCESS)
    })
    
    (ctrDatasets, ctrAnnotations)  
  }
  
  private def _dropGeoAnnotations(annotations: Iterable[GeoAnnotation]): Unit = {
    if (graph.isInstanceOf[TransactionalGraph]) {
      val tGraph = graph.asInstanceOf[TransactionalGraph]
      tGraph.setMaxBufferSize(0)
      tGraph.startTransaction()
    }

    annotations.map(_.asInstanceOf[GeoAnnotationVertex].vertex).foreach(vertex => {
      // Remove annotation target vertex
      vertex.getOutEdges(RELATION_HASTARGET).iterator.asScala.foreach(targetEdge => graph.removeVertex(targetEdge.getInVertex))
      
      // Remove annotation vertex
      graph.removeVertex(vertex)
      
      // Note: edges get removed automatically when vertex gets removed!
    })
        
    // Note: Neo4j will automatically keep the index in sync - we don't need to clean up manually
    // TODO Other graph DBs may not perform automatic index management - investigate!
    
    // TODO catch GraphIOException and make sure the transaction is closed with FAILURE
    if (graph.isInstanceOf[TransactionalGraph])
      graph.asInstanceOf[TransactionalGraph].stopTransaction(Conclusion.SUCCESS)
  }
  
  private def _dropDatasetVertex(dataset: DatasetVertex): Int = {
    var ctrDatasets = 0
    
    // Delete all subsets first
    dataset.subsets.foreach(subset => ctrDatasets += _dropDatasetVertex(subset))
    
    // Check if subsets are gone
    if (dataset.subsets.size > 0)
      throw new GraphIOException("Could not delete subsets for " + dataset.uri)
    
    // Delete dataset vertex
    graph.removeVertex(dataset.vertex)
    
    ctrDatasets + 1
  }

}