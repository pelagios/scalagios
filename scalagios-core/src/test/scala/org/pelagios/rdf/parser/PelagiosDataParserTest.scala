package org.pelagios.rdf.parser

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios
import org.pelagios.api._
import org.pelagios.api.annotation.TranscriptionType
import java.io.FileInputStream

@RunWith(classOf[JUnitRunner])
class PelagiosDataParserTest extends FunSuite with UsesTestData {

  private val TEST_FILE = getFile("opencontext.ttl")
    
  test("Pelagios Data Dump Import") {
    // Parse test data        
    val is = new FileInputStream(TEST_FILE)
    val things = Scalagios.readAnnotations(is, TEST_FILE.getName).toList

    // Dump contains one work with one expression - verify there is only one top-level AnnotatedThing
    assert(things.size == 234, "object graph contains wrong number of AnnotatedThings (" + things.size + ")")
    
    val thingsWithDepiction = things.filter(!_.depictions.isEmpty)
    assert(thingsWithDepiction.size == 214)
    
    val iiifDepictions = thingsWithDepiction.flatMap(_.depictions).filter(_.iiifEndpoint.isDefined)
    assert(iiifDepictions.size == 1)
    assert(iiifDepictions.head.iiifEndpoint == Some("http://www.example.com/iiif/DSCN1459/info.json"))
    
    is.close()
  }
  
}