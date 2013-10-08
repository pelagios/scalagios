package org.pelagios.api

/** 'Neighbour' model primitive.
  *
  * @constructor create a new neighbour
  * @param annotation the neighbouring annotation
  * @param the distance (if applicable)
  * @author Rainer Simon <rainer.simon@ait.ac.at> 
  */
case class Neighbour(val annotation: Annotation, val distance: Option[Double] = None, val unit: Option[String] = None) { } 