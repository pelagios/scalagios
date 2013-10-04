package org.pelagios.rdf.parser

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios
import java.io.File

@RunWith(classOf[JUnitRunner])
class GazetteerParserTest extends FunSuite {
  
  val TEST_FILE = "src/test/resources/test-places-pleiades.ttl"

  test("Gazetteer Dump Import") {
    println("Starting gazetteer data import")
    val startTime = System.currentTimeMillis
    
    val places = Scalagios.parseGazetteer(new File(TEST_FILE))
    places.foreach(place => {
      println(place.title)
    })  
    
    println(places.size + " places")
  }
  
}