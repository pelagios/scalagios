package org.scalagios.graph.io.writers

import scala.collection.JavaConverters._

import com.tinkerpop.blueprints.pgm.{Vertex, TransactionalGraph}
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion

import org.scalagios.api.Dataset
import org.scalagios.graph.Constants._
import org.scalagios.graph.DatasetVertex
import org.scalagios.graph.io.PelagiosGraphIOBase
import org.scalagios.graph.exception.GraphIOException

trait GraphDatasetWriter extends PelagiosGraphIOBase {

  def insertDataset(dataset: Dataset): Vertex = {  
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
    
    // Continue with this dataset's subsets
    dataset.subsets.foreach(subset => {
      val subsetVertex = insertDataset(subset)
      graph.addEdge(null, datasetVertex, subsetVertex, RELATION_SUBSET)
    })
    
    datasetVertex  
  }
  
  def dropDataset(uri: String): Int = {
    var ctr = 0
    
    if (graph.isInstanceOf[TransactionalGraph]) {
      val tGraph = graph.asInstanceOf[TransactionalGraph]
      tGraph.setMaxBufferSize(0)
      tGraph.startTransaction()
    }
        
    datasetIndex.get(DATASET_URI, uri).iterator.asScala.
      foreach(vertex => ctr += _dropDatasetVertex(new DatasetVertex(vertex)))
             
    // TODO catch GraphImportException and make sure the transaction is closed with FAILURE
    if (graph.isInstanceOf[TransactionalGraph])
      graph.asInstanceOf[TransactionalGraph].stopTransaction(Conclusion.SUCCESS)
    
    ctr  
  }
  
  private def _dropDatasetVertex(dataset: DatasetVertex): Int = {
    var ctr = 0
    
    // Delete all subsets first
    dataset.subsets.foreach(subset => ctr += _dropDatasetVertex(subset))
    
    // Check if subsets are gone
    if (dataset.subsets.size > 0)
      throw new GraphIOException("Could not delete subsets for " + dataset.uri)
    
    // Delete dataset vertex
    dataset.vertex.getInEdges(RELATION_SUBSET).iterator().asScala.foreach(graph.removeEdge(_))
    graph.removeVertex(dataset.vertex)
    
    ctr + 1
  }

}