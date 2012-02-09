package org.scalagios.openrdf.parser

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.openrdf.rio.n3.N3ParserFactory
import java.io.FileInputStream
import java.io.File

@RunWith(classOf[JUnitRunner])
class AnnotationParserTest extends FunSuite {
  
  val SAMPLE_RDF = "src/test/resources/gap-triples-sample.n3";
  
  test("Pelagios OAC Annotation Import") {
    println("Starting OAC Annotation import")
    val startTime = System.currentTimeMillis
    
    val parser = new N3ParserFactory().getParser()
    val annotationCollector = new AnnotationCollector()
    parser.setRDFHandler(annotationCollector)
    parser.parse(new FileInputStream(new File(SAMPLE_RDF)), "http://googleancientplaces.wordpress.com/")
    
    println("Import complete. Took " + (System.currentTimeMillis - startTime) + " milliseconds")
    println(annotationCollector.triplesTotal + " triples total in file")
    println(annotationCollector.triplesProcessed + " triples processed during import")
    println(annotationCollector.annotationsTotal + " annotations imported")
    
    assert(annotationCollector.triplesTotal == 5548)
    assert(annotationCollector.triplesProcessed == 5547)
    assert(annotationCollector.annotationsTotal == 1849)
    
    annotationCollector.getAnnotations.foreach(annotation => assert(annotation.isValid))
    
    println("Data verified")
  }

}