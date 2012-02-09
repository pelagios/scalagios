package org.scalagios.model.impl

import org.scalagios.model.GeoAnnotation

class DefaultGeoAnnotation(var uri: String) extends GeoAnnotation {

  var body: String = _
  
  var target: String = _
  
}