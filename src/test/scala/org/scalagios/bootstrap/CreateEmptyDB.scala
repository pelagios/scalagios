package org.scalagios.bootstrap

import java.io.{File, FileInputStream}
import java.util.zip.GZIPInputStream
import org.openrdf.rio.n3.N3ParserFactory
import org.openrdf.rio.turtle.TurtleParserFactory
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import org.scalagios.graph.io.PelagiosGraphWriter
import org.scalagios.rdf.parser.{PlaceCollector, AnnotationCollector, DatasetCollector}

object CreateEmptyDB {
  
  private val ANNOTATION_BASEURI = "http://gap.alexandriaarchive.org/bookdata/GAPtriples/"
  
  def main(args: Array[String]): Unit = {
    println("Building emtpy graph DB")
    print("Importing places... ")
        
    // Parse sample Pleiades dump
    val inputStream = new GZIPInputStream(new FileInputStream("src/test/resources/places-20120401.ttl.gz"))
    val parser = new TurtleParserFactory().getParser()
    val placeCollector = new PlaceCollector()
    parser.setRDFHandler(placeCollector);
    parser.parse(inputStream, "http://pleiades.stoa.org")
    
    // Import to Graph DB
    val neo4j = new Neo4jGraph("neo4j")
    val writer = new PelagiosGraphWriter(neo4j)
    writer.insertPlaces(placeCollector.getPlaces) 
    neo4j.shutdown
    println("DB complete.")
  }

}