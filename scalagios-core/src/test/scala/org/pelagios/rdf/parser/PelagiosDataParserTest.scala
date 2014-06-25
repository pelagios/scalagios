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

  private val TEST_FILE = getFile("test-annotations-vicarello.ttl")
    
  test("Pelagios Data Dump Import") {
    // Parse test data
    val is = new FileInputStream(TEST_FILE)
    val things = Scalagios.readAnnotations(is, TEST_FILE.getName).toList
    
    // Dump contains one work with one expression - verify there is only one top-level AnnotatedThing
    assert(things.size == 1, "object graph contains wrong number of AnnotatedThings (" + things.size + ")")

    // Verify the properties of the Work
    val work = things.toSeq(0)
    assert(work.title.equals("Vicarello Beakers"), "Work has invalid title (" + work.title + ")")
    assert(!work.isPartOf.isDefined, "Work should not have a 'realiziationOf' property")
    assert(work.annotations.size == 0, "Work should not have annotations!")
    assert(work.parts.size == 4, "Work has invalid number of expressions (" + work.parts.size + ")")
    
    // Verify the properties of the expression
    val expressions = work.parts.filter(_.title.equals("Vicarello Beaker 3"))
    assert(expressions.size == 1)
    
    val expression = expressions(0)
    assert(expression.isPartOf.isDefined, "Annotated thing does not indicate a parent thing")
    assert(expression.isPartOf.get.uri.equals(work.uri), "Parent part URI is wrong")
    assert(expression.annotations.size == 109, "wrong number of annotations (" + expression.annotations.size + ")") 
    assert(expression.parts.size == 0, "Annotated thing should not have more parts")
    
    // Verify the annotations
    expression.annotations.foreach(annotation => {
      assert(annotation.place.size == 1, "invalid number of place bodies")
      assert(annotation.transcription.isDefined, "missing: toponym")
      assert(annotation.transcription.get.nameType == TranscriptionType.Toponym)
      assert(annotation.hasTarget.equals(expression), "annotation targets should point to Expression!")
    })
    
    is.close()
  }
  
}