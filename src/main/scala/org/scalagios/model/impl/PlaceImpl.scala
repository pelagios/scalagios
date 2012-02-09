package org.scalagios.model.impl

import org.scalagios.model.Place
import scala.collection.mutable.ListBuffer

class PlaceImpl(var uri: String) extends Place {
  
  var label: String = _

  var comment: String = _
  
  private val altLabelsList: ListBuffer[String] = ListBuffer()
  
  def addAltLabel(altLabel: String): Unit = {
    if (!altLabel.equals(label))
      altLabelsList.append(altLabel)
  }
  
  def altLabels = altLabelsList.mkString(", ")

  var lon: Double = _

  var lat: Double = _
  
  var within: String = _
  
  var geometryWKT: String = _
  
}