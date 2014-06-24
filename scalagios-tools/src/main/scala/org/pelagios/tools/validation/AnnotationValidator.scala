package org.pelagios.tools.validation

import org.pelagios.Scalagios
import java.io.FileInputStream

object AnnotationValidator extends App {
  
  val INPUT_FILE = "/home/simonr/Downloads/pelagios.rdf"
    
  val is = new FileInputStream(INPUT_FILE)
  val annotations = Scalagios.readAnnotations(is, INPUT_FILE)
  annotations.foreach(thing => {
    println(thing.title)
    println(thing.temporal.map(t => t.start))
    thing.annotations.foreach(annotation => {
      println("  " + annotation.relation.map(_.toString).getOrElse("") + " " + annotation.place)
      println("  annotated by " + annotation.annotatedBy)
      println("  annotated at " + annotation.annotatedAt)
    })
  })
  is.close()

}