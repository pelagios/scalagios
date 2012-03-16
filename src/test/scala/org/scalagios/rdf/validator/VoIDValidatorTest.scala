package org.scalagios.rdf.validator

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.openrdf.rio.turtle.TurtleParserFactory
import org.openrdf.rio.RDFFormat
import java.io.FileInputStream
import java.io.File

@RunWith(classOf[JUnitRunner])
class VoIDValidatorTest extends FunSuite {
  
  val SAMPLE_RDF = "src/test/resources/gap-void-sample.ttl"
  
  test("VoID Validation") {
    println("Starting VoID validation")
    val startTime = System.currentTimeMillis
    
    val validator = new VoIDValidator(RDFFormat.TURTLE)
    val issues = validator.validate(new FileInputStream(new File(SAMPLE_RDF)))
    issues.foreach(issue => println(issue.message))
  }
  
}