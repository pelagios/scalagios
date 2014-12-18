package org.pelagios.rdf.parser

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import java.io.FileInputStream
import org.pelagios.api.gazetteer.Place
import org.pelagios.Scalagios
import org.openrdf.rio.RDFFormat
import java.util.zip.GZIPInputStream


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
    
    Scalagios.streamPlaces(is, TEST_FILE.getName, streamHandler)
    is.close()
  }
  
}