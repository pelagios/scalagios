package org.pelagios.tools.gazetteer

import org.pelagios.Scalagios
import java.io.{ FileInputStream, PrintWriter }

/** Quick hack to convert Pelagios gazetteer RDF to JSON **/
object GazetteerToJSON extends App {
  
  private val GAZETTEER_DUMP = "/home/simonr/Workspaces/pelagios/scalagios/test-data/pleiades-20120826-migrated.ttl"
    
  val places = Scalagios.readPlaces(new FileInputStream(GAZETTEER_DUMP), Scalagios.TURTLE)
  
  val json = places.foldLeft(Seq.empty[String])((list, next) => {
    val centroid = next.getCentroid
    if (centroid.isDefined) {
     val json = "{ \"lat\": " + centroid.get.y + ", \"lon\": " + centroid.get.x + ", \"value\": 1, \"title\": \"" + next.title + "\" }"
     list :+ json
    } else {
     list
    }
  })
  
  val printWriter = new PrintWriter("/home/simonr/foo.json")
  printWriter.write("var places = { data:[" + json.mkString(",\n") + " ]}")
  printWriter.flush()
  printWriter.close()
  
}
