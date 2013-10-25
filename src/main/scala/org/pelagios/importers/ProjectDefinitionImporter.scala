package org.pelagios.importers

import org.pelagios.api.AnnotatedThing
import net.liftweb.json._
import java.io.File
import scala.io.Source

/** A helper object to import data based on a JSON import definition.
  * 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
object ProjectDefinitionImporter {
  
  def importFromDefinition(path: String): Seq[AnnotatedThing] = {
    val jsonFile = new File(path)
    val basepath = jsonFile.getParent
    
    parseJSONDefinition(Source.fromFile(jsonFile).getLines.mkString("\n")).map{ case (thing, files) => {
      // TODO parse CSVs and attach annotations to things      
      
      thing
    }}
  }
    
  private def parseJSONDefinition(json: String): Seq[(AnnotatedThing, Seq[String])] = {
    val jsonObj = parse(json)
    
    val uri = for { JField("uri", JString(uri)) <- jsonObj } yield uri
    val title = for { JField("title", JString(title)) <- jsonObj } yield title    
    val csv = (for { JField("csv", JArray(csv)) <- jsonObj } yield csv).flatten.map(_.values.toString).toSeq  
    
    val thing = AnnotatedThing(uri(0), title(0))
    
    Seq((thing, csv))  
  }

}