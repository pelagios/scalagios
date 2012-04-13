package org.scalagios.rdf.parser

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.net.URL
import java.util.zip.GZIPInputStream
import org.openrdf.rio.turtle.TurtleParserFactory
import java.io.FileInputStream

@RunWith(classOf[JUnitRunner])
class PleiadesParserTest extends FunSuite {
  
  val PLEIADES_DUMP = "src/test/resources/places-20120401.ttl.gz"
  
  test("Pleiades RDF Import") {
    println("Starting Pleiades import")
    val startTime = System.currentTimeMillis   

    val inputStream = new GZIPInputStream(new FileInputStream(PLEIADES_DUMP))

    // Parse with RIO RDF Parser
    val parser = new TurtleParserFactory().getParser()
    val placeCollector = new PlaceCollector
    parser.setRDFHandler(placeCollector);
    parser.parse(inputStream, "http://pleiades.stoa.org")
    
    println("Pleiades import complete. Took " + (System.currentTimeMillis - startTime)/1000 + " seconds")
    println(placeCollector.triplesTotal + " triples total in file")
    println(placeCollector.triplesProcessed + " triples processed during import")
    println(placeCollector.placesTotal + " places imported")
    
    // Just a few plausibility checks
    assert(placeCollector.triplesTotal > 200000)
    assert(placeCollector.triplesProcessed > 140000)
    assert(placeCollector.placesTotal > 35000)
    
    var counter = 0
    placeCollector.getPlaces.foreach(place => { 
      assert(place.isValid)
      if (place.within.isDefined) {
        counter += 1
        assert(placeCollector.placesBuffer.get(place.within.get.uri) != null, 
            place.uri + " is within " + place.within + " - but target not found in dataset")
      }
    })
    
    println(counter + " within relations declared in file")
    println("Data verified. Took " + (System.currentTimeMillis - startTime)/1000 + " seconds")
  }

}