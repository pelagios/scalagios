package org.pelagios.rdf.parser

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import java.io.FileInputStream
import org.pelagios.api.gazetteer.Place
import org.pelagios.Scalagios
import org.openrdf.rio.RDFFormat


@RunWith(classOf[JUnitRunner])
class StreamingGazetteerParserTest extends FunSuite with UsesTestData {

  private val TEST_FILE = getFile("test-places-pleiades.ttl")
  // private val TEST_FILE = getFile("wikidata.ttl")
  
  test("Streaming Gazetteer Dump Import") {
    val is = new FileInputStream(TEST_FILE)
        
    def streamHandler(place: Place): Unit = {
      println(place.label)
    }
    
    Scalagios.streamPlaces(is, TEST_FILE.getName, streamHandler)
    is.close()
  }
  
}