package org.scalagios.graph.io

import com.tinkerpop.blueprints.pgm.IndexableGraph

import org.scalagios.graph.io.writers.{GraphPlaceWriter, GraphDatasetWriter, GraphAnnotationWriter}

class PelagiosGraphWriter[T <: IndexableGraph](val graph: T) 
  extends GraphPlaceWriter 
  with GraphDatasetWriter
  with GraphAnnotationWriter