package org.pelagios.json

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios

import java.io.File

@RunWith(classOf[JUnitRunner])
class JSONSerializerTest extends FunSuite {
  
  val TEST_FILE = "src/test/resources/test-places-pleiades.ttl"
  
  test("Testing Annotation-to-GeoJSON serialization") {
    // Parse test data
    val places = Scalagios.parseGazetteer(new File(TEST_FILE))
    
    val json = JSONSerializer.serializePlaces(places.toSeq, true)
    println(json)
  }

}