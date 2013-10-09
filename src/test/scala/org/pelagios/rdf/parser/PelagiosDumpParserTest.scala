package org.pelagios.rdf.parser

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios
import java.io.File

@RunWith(classOf[JUnitRunner])
class PelagiosDumpParserTest extends FunSuite {

  val TEST_FILE = "src/test/resources/test-annotations-vicarello.ttl"
    
  test("Pelagios Data Dump Import") {
    // Parse test data
    val things = Scalagios.parseData(new File(TEST_FILE))
    
    // Dump contains one work with one expression - verify there is only one top-level AnnotatedThing
    assert(things.size == 1, "object graph contains too many AnnotatedThings (" + things.size + ")")

    // Verify the properties of the Work
    val work = things.toSeq(0)
    assert(work.title.equals("Vicarello Goblets"), "Work has invalid title (" + work.title + ")")
    assert(!work.realizationOf.isDefined, "Work should not have a 'realiziationOf' property")
    assert(work.annotations.size == 0, "Work should not have annotations!")
    assert(work.expressions.size == 1, "Work has invalid number of expressions (" + work.expressions.size + ")")
    
    // Verify the properties of the expression
    val expression = work.expressions(0)
    assert(expression.title.equals("Vicarello Goblet I"))
    assert(expression.realizationOf.isDefined && expression.realizationOf.get.equals(work.uri))
    assert(expression.annotations.size == 100, "wrong number of annotations") 
    assert(expression.expressions.size == 0, "Expression should not have more Expressions")
    
    // Verify the annotations
    expression.annotations.foreach(annotation => {
      assert(annotation.motivatedBy.isDefined && annotation.motivatedBy.get.equals("geotagging"), "missing or wrong: motivatedBy")
      assert(annotation.toponym.isDefined, "missing: toponym")
      assert(annotation.hasBody.size == 1, "invalid number of annotation bodies")
      assert(annotation.hasTarget.equals(expression.uri), "annotation targets should point to Expression!")
    })
  }
  
}