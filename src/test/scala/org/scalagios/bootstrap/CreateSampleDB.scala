package org.scalagios.bootstrap

import java.io.{File, FileInputStream}
import java.util.zip.GZIPInputStream
import org.openrdf.rio.n3.N3ParserFactory
import org.openrdf.rio.turtle.TurtleParserFactory
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import org.scalagios.graph.io.write.PelagiosGraphWriter
import org.scalagios.rdf.parser.{PlaceCollector, AnnotationCollector, DatasetCollector}

object CreateSampleDB {
  
  private val ANNOTATION_BASEURI = "http://gap.alexandriaarchive.org/bookdata/GAPtriples/"
  
  def main(args: Array[String]): Unit = {
    println("Building sample graph DB")
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
    println("done.")
    
    // Parse VoID RDF
    print("Importing sample GAP data... ")
    val ttlParser = new TurtleParserFactory().getParser()
    val datasetCollector = new DatasetCollector(ANNOTATION_BASEURI)
    ttlParser.setRDFHandler(datasetCollector)
    ttlParser.parse(new FileInputStream(new File("src/test/resources/gap-void-sample.ttl")), 
        ANNOTATION_BASEURI)
    
    // Parse annotation RDF
    val n3Parser = new N3ParserFactory().getParser()
    val annotationCollector = new AnnotationCollector()
    n3Parser.setRDFHandler(annotationCollector)
    n3Parser.parse(new FileInputStream(new File("src/test/resources/gap-triples-sample.n3")), 
        ANNOTATION_BASEURI)
    
    /* Import data to Graph
    writer.insertAnnotations(datasetCollector.getRootDatasets, annotationCollector.getAnnotations)
    */  
    neo4j.shutdown()
    println("done.")
    
    println("Sample DB complete")
  }

}