package org.scalagios.graph.io.write

import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph

import org.scalagios.graph.io.writers.{Neo4jPlaceWriter, GraphDatasetWriter}

class PelagiosNeo4jWriter(val graph: Neo4jGraph) extends Neo4jPlaceWriter with GraphDatasetWriter