package org.scalagios.rdf.pleiades

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.net.URL
import java.util.zip.GZIPInputStream
import org.openrdf.rio.turtle.TurtleParserFactory

@RunWith(classOf[JUnitRunner])
class PleiadesReaderTest extends FunSuite {
  
  test("Pleiades RDF Import") {
    println("Starting Pleiades import")
    val startTime = System.currentTimeMillis   

    // Get GZIP stream directly from Pleiades site
    val connection = 
      new URL("http://atlantides.org/downloads/pleiades/rdf/places-latest.ttl.gz")
      .openConnection()
      
    val inputStream = new GZIPInputStream(connection.getInputStream())

    // Instantiate RIO RDF Parser
    val parser = new TurtleParserFactory().getParser()
    val handler = new PlaceCollector
    
    parser.setRDFHandler(handler);
    parser.parse(inputStream, "http://pleiades.stoa.org")
    
    println("Pleiades import complete. Took " + (System.currentTimeMillis - startTime)/1000 + " seconds")
    
    println(handler.triplesTotal + " triples counted")
    println(handler.triplesProcessed + " triples processed")
    println(handler.placesTotal + " places processed")
    
    assert(true)
  }

}