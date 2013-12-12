package org.pelagios.legacy.graph.io

import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import org.pelagios.legacy.graph.io.writers.{Neo4jPlaceWriter, GraphDatasetWriter, GraphAnnotationWriter}

class PelagiosNeo4jWriter(val graph: Neo4jGraph) 
  extends Neo4jPlaceWriter 
  with GraphDatasetWriter
  with GraphAnnotationWriter