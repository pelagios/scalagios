package org.pelagios.rdf.parser

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.pelagios.Scalagios
import java.io.File
import org.pelagios.api.Transcription

@RunWith(classOf[JUnitRunner])
class PelagiosDataParserTest extends FunSuite {

  val TEST_FILE = "src/test/resources/test-annotations-vicarello.ttl"
    
  test("Pelagios Data Dump Import") {
    // Parse test data
    val things = Scalagios.parseData(new File(TEST_FILE))
    
    // Dump contains one work with one expression - verify there is only one top-level AnnotatedThing
    assert(things.size == 1, "object graph contains too many AnnotatedThings (" + things.size + ")")

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
      // assert(annotation.transcription.isDefined, "missing: toponym")
      // assert(annotation.transcription.get.nameType == Transcription.Toponym)
      assert(annotation.place.size == 1, "invalid number of annotation bodies")
      assert(annotation.hasTarget.equals(expression.uri), "annotation targets should point to Expression!")
    })
    
    // Verify annotation neighbourhood relations
    val annotationsWithNeighbours = expression.annotations.filter(_.hasNeighbour.size > 0)
    assert(annotationsWithNeighbours.size == 96, "annotations don't specify neighbours")
    annotationsWithNeighbours.foreach(annotation => {
      assert(annotation.hasNeighbour.size == 1, "annotations should have exactly one neighbour")
      assert(annotation.hasNeighbour(0).directional, "neighbourhood relation should be directional")
      assert(annotation.hasNeighbour(0).distance.isDefined, "neighbourhood relation should define a distance")
      assert(annotation.hasNeighbour(0).unit.isDefined, "neighbourhood relation should define a unit")
      assert(expression.annotations.contains(annotation.hasNeighbour(0).annotation), "neighbour annotation does not exist")
    })
  }
  
}