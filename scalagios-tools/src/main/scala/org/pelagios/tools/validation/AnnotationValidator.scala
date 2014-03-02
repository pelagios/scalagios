package org.pelagios.tools.validation

import org.pelagios.Scalagios

object AnnotationValidator extends App {
  
  val INPUT_FILE = "/home/simonr/Downloads/UVirginiaNumismatics.rdf"
    
  val annotations = Scalagios.readAnnotations(INPUT_FILE)
  annotations.foreach(thing => {
    println(thing.title)
    thing.annotations.foreach(annotation => {
      println("  " + annotation.relation.map(_.toString).getOrElse("") + " " + annotation.place)
    })
  })
}