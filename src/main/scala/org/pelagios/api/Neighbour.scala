package org.pelagios.api

/** 'Neighbour' model primitive.
  *
  * @constructor create a new neighbour
  * @param annotation the neighbouring annotation
  * @param directional set to true if the direction of the link is relevant (defaults to false)
  * @param distance the distance (if applicable, defaults to None)
  * @param unit the unit distance is measured in (defaults to None)
  * @author Rainer Simon <rainer.simon@ait.ac.at> 
  */
class Neighbour(val annotation: Annotation, val directional: Boolean = false, val distance: Option[Double] = None, val unit: Option[String] = None)