package org.pelagios.api

/** Pelagios 'Label' model primitive.
  * 
  * @constructor create a new label
  * @param label the label
  * @param lang the (optional) language code 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
case class Label(label: String, lang: Option[String] = None)