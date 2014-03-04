package org.pelagios.tools.validation

import org.pelagios.Scalagios

object AnnotationValidator extends App {
  
  val INPUT_FILE = "/home/simonr/Downloads/pelagios.rdf"
    
  val annotations = Scalagios.readAnnotations(INPUT_FILE)
  annotations.foreach(thing => {
    println(thing.title)
    println(thing.temporal.map(t => t.start.getYear()))
    thing.annotations.foreach(annotation => {
      println("  " + annotation.relation.map(_.toString).getOrElse("") + " " + annotation.place)
      println("  annotated by " + annotation.annotatedBy)
      println("  annotated at " + annotation.annotatedAt)
    })
  })

}