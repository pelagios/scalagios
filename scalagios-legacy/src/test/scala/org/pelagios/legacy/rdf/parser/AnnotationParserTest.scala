package org.pelagios.legacy.rdf.parser

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.openrdf.rio.n3.N3ParserFactory

import java.io.FileInputStream
import java.io.File
import java.util.zip.GZIPInputStream

@RunWith(classOf[JUnitRunner])
class AnnotationParserTest extends FunSuite {
  
  val SAMPLE_RDF = "test-data/legacy/test-annotations-gap.n3.gz";
  
  test("Pelagios OAC Annotation Import") {
    println("Starting OAC Annotation import")
    val startTime = System.currentTimeMillis
    
    val parser = new N3ParserFactory().getParser()
    val annotationCollector = new AnnotationCollector()
    parser.setRDFHandler(annotationCollector)
    parser.parse(new GZIPInputStream(new FileInputStream(new File(SAMPLE_RDF))), "http://googleancientplaces.wordpress.com/")
    
    println("Import complete. Took " + (System.currentTimeMillis - startTime) + " milliseconds")
    println(annotationCollector.triplesTotal + " triples total in file")
    println(annotationCollector.triplesProcessed + " triples processed during import")
    println(annotationCollector.annotationsTotal + " annotations imported")
    
    assert(annotationCollector.triplesTotal == 8467)
    assert(annotationCollector.triplesProcessed == 8467)
    assert(annotationCollector.annotationsTotal == 2116)
    
    annotationCollector.getAnnotations.foreach(annotation => assert(annotation.isValid))
    
    println("Data verified")
  }

}