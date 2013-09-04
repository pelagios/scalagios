package org.scalagios

object CLI extends App {
  
  Scalagios.migrate("src/test/resources/nomisma.rdf", "nomisma-annotations-v2.ttl")

}