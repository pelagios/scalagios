package org.pelagios.legacy

import org.pelagios.Scalagios

object CLI extends App {
  
  Scalagios.Legacy.migrateOAC("src/test/resources/nomisma.rdf", "nomisma-annotations-v2.ttl")

}