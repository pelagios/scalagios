package org.pelagios.api

/** Pelagios 'Label' model primitive.
  * 
  * @constructor create a new label
  * @param label the label
  * @param lang the (optional) ISO 639-2 language code 
  * @author Rainer Simon <rainer.simon@ait.ac.at>
  */
case class Label(val label: String, val lang: Option[String] = None)