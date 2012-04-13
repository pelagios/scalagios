package org.scalagios.graph.io.write

import com.tinkerpop.blueprints.pgm.IndexableGraph

import org.scalagios.graph.io.writers.{GraphPlaceWriter, GraphDatasetWriter}

class PelagiosGraphWriter[T <: IndexableGraph](val graph: T) extends GraphPlaceWriter with GraphDatasetWriter