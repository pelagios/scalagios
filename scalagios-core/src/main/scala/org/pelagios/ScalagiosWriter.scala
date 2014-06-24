package org.pelagios

import java.io.{ File, OutputStream }
import org.openrdf.rio.RDFFormat
import org.pelagios.api.gazetteer.Place
import org.pelagios.api.annotation.AnnotatedThing
import org.pelagios.rdf.serializer.{ GazetteerSerializer, TTLTemplateSerializer, PelagiosDataSerializer }

trait ScalagiosWriter {

  def writeAnnotations(data: Iterator[AnnotatedThing], out: OutputStream, format: RDFFormat) = {
    if (format == RDFFormat.TURTLE)
      TTLTemplateSerializer.writeToStream(data, out)
    else
      PelagiosDataSerializer.writeToStream(data, out, format)
  } 
  
  def writePlaces(data: Iterator[Place], file: String, format: RDFFormat) =
    GazetteerSerializer.writeToFile(data, file, format)
  
}