package org.pelagios.rdf.parser

import java.io.File
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios
import org.pelagios.rdf.RDFPlaceReader
import java.io.FileInputStream
import org.pelagios.api.gazetteer.Place
import org.openrdf.rio.RDFFormat

@RunWith(classOf[JUnitRunner])
class StreamingGazetteerParserTest extends FunSuite {

  // val TEST_FILE = "../test-data/test-places-pleiades.ttl"
  val TEST_FILE = "/home/simonr/Workspaces/bitbucket/pelagios3-scripts/wikidata-place-extractor/data/wikidata.ttl"
  
  test("Streaming Gazetteer Dump Import") {
    val is = new FileInputStream(TEST_FILE)
    
    def handler(place: Place) = {
      println(place.title)
    }
    
    Scalagios.readPlaceStream(is, RDFFormat.TURTLE, handler)
  }
  
}