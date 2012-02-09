package org.scalagios.model

trait Place {
  
  def uri: String
  
  def label: String

  def comment: String

  def altLabels: String

  def lon: Double

  def lat: Double

  def within: String
  
  def geometryWKT: String

}