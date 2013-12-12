package org.pelagios.rdf.parser

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios
import org.pelagios.api._

@RunWith(classOf[JUnitRunner])
class PelagiosDataParserTest extends FunSuite {

  val TEST_FILE = "src/test/resources/test-annotations-vicarello.ttl"
    
  test("Pelagios Data Dump Import") {
    // Parse test data
    val things = Scalagios.readFromFile(TEST_FILE)
    
    // Dump contains one work with one expression - verify there is only one top-level AnnotatedThing
    assert(things.size == 1, "object graph contains wrong number of AnnotatedThings (" + things.size + ")")

    // Verify the properties of the Work
    val work = things.toSeq(0)
    assert(work.title.equals("Vicarello Beakers"), "Work has invalid title (" + work.title + ")")
    assert(!work.realizationOf.isDefined, "Work should not have a 'realiziationOf' property")
    assert(work.annotations.size == 0, "Work should not have annotations!")
    assert(work.expressions.size == 4, "Work has invalid number of expressions (" + work.expressions.size + ")")
    
    // Verify the properties of the expression
    val expressions = work.expressions.filter(_.title.equals("Vicarello Beaker 3"))
    assert(expressions.size == 1)
    
    val expression = expressions(0)
    assert(expression.realizationOf.isDefined, "Expression does not provide associated Work")
    assert(expression.realizationOf.get.uri.equals(work.uri), "Expression's work URI is wrong")
    assert(expression.annotations.size == 109, "wrong number of annotations (" + expression.annotations.size + ")") 
    assert(expression.expressions.size == 0, "Expression should not have more Expressions")
    
    // Verify the annotations
    expression.annotations.foreach(annotation => {
      assert(annotation.place.size == 1, "invalid number of place bodies")
      assert(annotation.transcription.isDefined, "missing: toponym")
      assert(annotation.transcription.get.nameType == TranscriptionType.Toponym)
      assert(annotation.hasTarget.equals(expression.uri), "annotation targets should point to Expression!")
    })
    
    /* Verify layout
    things.foreach(work => assert(work.layout == None, "work should not have layout"))
    expressions.foreach(expression => {
      assert(expression.layout.isDefined, "expression should have layout")
      assert(expression.layout.get.links.size > 0, "layout should not be empty")
    })
    val annotationsWithNeighbours = expression.annotations.filter(_.links.size > 0)
    assert(annotationsWithNeighbours.size == 106, "there should be 106 annotation with layout neighbours")
    annotationsWithNeighbours.foreach(annotation => {
      assert(annotation.links.size == 1, "annotation should have exactly one link")
      assert(annotation.links(0).from.uri.equals(annotation.uri))
    })
    */
  }
  
}