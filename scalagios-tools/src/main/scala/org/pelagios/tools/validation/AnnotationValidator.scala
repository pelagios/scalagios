package org.pelagios.tools.validation

import org.pelagios.Scalagios

object AnnotationValidator extends App {
  
  val INPUT_FILE = "/home/simonr/Downloads/UVirginiaNumismatics.rdf"
    
  val annotations = Scalagios.readAnnotations(INPUT_FILE)
  annotations.foreach(thing => {
    println(thing.title)
    println(thing.temporal)
    thing.annotations.foreach(annotation => {
      println("  " + annotation.relation.map(_.toString).getOrElse("") + " " + annotation.place)
      println("  annotated by " + annotation.annotatedBy)
      println("  annotated at " + annotation.annotatedAt)
    })
  })
}