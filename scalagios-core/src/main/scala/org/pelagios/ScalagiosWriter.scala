package org.pelagios

import java.io.{ File, OutputStream }
import org.openrdf.rio.RDFFormat
import org.pelagios.api.gazetteer.Place
import org.pelagios.api.annotation.AnnotatedThing
import org.pelagios.rdf.serializer.{ GazetteerSerializer, TTLTemplateSerializer, PelagiosDataSerializer }

private[pelagios] trait ScalagiosWriter {
	
  import Scalagios._
  
  def writeAnnotations(data: Iterable[AnnotatedThing], out: OutputStream, format: String) = {
    if (format.equalsIgnoreCase(TURTLE))
      TTLTemplateSerializer.writeToStream(data, out)
    else
      PelagiosDataSerializer.writeToStream(data, out, format)
  } 
  
  def writePlaces(data: Iterable[Place], file: String, format: String) =
    GazetteerSerializer.writeToFile(data, file, format)
  
}
