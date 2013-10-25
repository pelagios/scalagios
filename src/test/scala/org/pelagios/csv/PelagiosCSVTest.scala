package org.pelagios.csv

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.pelagios.api.AnnotatedThing
import org.pelagios.importers.CSVImporter

@RunWith(classOf[JUnitRunner])
class PelagiosCSVTest extends FunSuite {
  
  val TEST_FILE = "src/test/resources/test-csv-bordeaux.csv"
  
  test("Testing CSV pasing") {
    val egd = AnnotatedThing("http://example.org/data/things", "My EGD")
    val annotations = CSVImporter.readFromFile(TEST_FILE, egd)
    annotations.foreach(annotation => {
      annotation.transcription.map(transcription => println(transcription.chars))
    })
  }

}