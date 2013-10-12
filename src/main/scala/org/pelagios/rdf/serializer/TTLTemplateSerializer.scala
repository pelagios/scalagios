package org.pelagios.rdf.serializer

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
object TemplateBasedSerializer {

}