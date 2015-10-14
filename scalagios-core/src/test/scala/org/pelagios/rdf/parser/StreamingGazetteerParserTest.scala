package org.pelagios.rdf.parser

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import java.io.FileInputStream
import org.pelagios.api.gazetteer.Place
import org.pelagios.Scalagios
import org.openrdf.rio.RDFFormat
import java.util.zip.GZIPInputStream
import org.openrdf.rio.UnsupportedRDFormatException


@RunWith(classOf[JUnitRunner])
class StreamingGazetteerParserTest extends FunSuite with UsesTestData {

  private val TEST_FILE = getFile("test-places-pleiades.ttl")
  private val DATA_DARE = "../test-data/dare-20140324.ttl.gz"
  // private val TEST_FILE = getFile("wikidata.ttl")
  
  test("Streaming Gazetteer Dump Import") {
    val is = new GZIPInputStream(new FileInputStream(DATA_DARE))
        
    def streamHandler(place: Place): Unit = {
      // println(place.label)
      val languages = place.names.flatMap(_.lang).distinct
      if (languages.size > 0)
        println(languages.mkString(", "))
    }
    
    val format = Scalagios.guessFormatFromFilename(TEST_FILE.getName)
    if (format.isDefined)
      Scalagios.readPlacesFromStream(is, format.get, streamHandler, false)
    else
      throw new UnsupportedRDFormatException("No format found for: " + TEST_FILE.getName) // Should never happen
    
    is.close()
  }
  
}