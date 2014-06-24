package org.pelagios.rdf.serializer

import java.io.{ File, FileOutputStream, OutputStream, PrintWriter }
import org.fusesource.scalate.{ Template, TemplateSource, TemplateEngine }
import org.pelagios.api._
import org.pelagios.api.annotation.AnnotatedThing
import scala.io.Source

/** Template-based Turtle serializer as alternative to [[RDFSerializer]].
  *   
  * The streaming serializer of the Sesame Rio framework does not produce
  * the most 'human-friendly' & readable output: it does not support compact
  * blank node syntax; and even the custom extension from org.callimachusproject
  * does not properly inline more than one blank nodes. This template based 
  * serializer produces better formatted output.
  *
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
object TTLTemplateSerializer {

  val engine = new TemplateEngine
  
  val template = TemplateSource.fromSource("ttl.mustache", Source.fromInputStream(getClass.getResourceAsStream("ttl.mustache"), "UTF-8"))

  def toString(data: Iterable[AnnotatedThing]): String =
    engine.layout(template, Map("things" -> data))

  def writeToFile(data: Iterable[AnnotatedThing], out: File) =
    writeToStream(data, new FileOutputStream(out))
    
  def writeToStream(data: Iterable[AnnotatedThing], out: OutputStream) = {
    val printWriter = new PrintWriter(out)
    printWriter.write(toString(data))
    printWriter.flush
    printWriter.close    
  }
    
}
