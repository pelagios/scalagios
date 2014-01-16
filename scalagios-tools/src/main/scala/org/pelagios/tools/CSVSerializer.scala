package org.pelagios.tools

import java.io.File
import java.io.PrintWriter
import org.pelagios.api.Place
import org.pelagios.tools.geoparser.NamedEntity

object CSVSerializer {
  
  private val SEPARATOR = ";"
  private val NEWLINE = "\n"
  private val NOT_VERIFIED = "NOT_VERIFIED"
  
  def serialize(annotations: Seq[(NamedEntity, Option[Place])], gdocPart: String = ""): String = {
    val header = Seq("idx", "gdoc_part", "status", "toponym", "offset", "gazetteer_uri").mkString(SEPARATOR) + SEPARATOR + NEWLINE
    annotations.zipWithIndex.foldLeft(header) { case (csv, (annotation, idx)) => {
      val line = 
        idx + SEPARATOR + 
        gdocPart + SEPARATOR +
        NOT_VERIFIED + SEPARATOR +
        annotation._1.term + SEPARATOR +
        annotation._1.offset + SEPARATOR +
        annotation._2.map(_.uri).getOrElse("") + SEPARATOR
        
      csv + line + NEWLINE
    }}
  }
  
  def writeToFile(file: File, annotations: Seq[(NamedEntity, Option[Place])], gdocPart: String = "") = {
    val printWriter = new PrintWriter(file)
    
    val header = Seq("idx", "gdoc_part", "status", "toponym", "offset", "gazetteer_uri").mkString(SEPARATOR) + SEPARATOR + NEWLINE
    printWriter.write(header)
    
    annotations.zipWithIndex.foreach { case (annotation, idx) => {
      val line =
        idx + SEPARATOR + 
        gdocPart + SEPARATOR +
        NOT_VERIFIED + SEPARATOR +
        annotation._1.term + SEPARATOR +
        annotation._1.offset + SEPARATOR +
        annotation._2.map(_.uri).getOrElse("") + SEPARATOR +
        NEWLINE
        
      printWriter.write(line)
    }}
    
    printWriter.flush
    printWriter.close    
  }

}