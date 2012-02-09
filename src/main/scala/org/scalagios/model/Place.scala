package org.scalagios.model

trait Place {
  
  def id: String
  
  def label: String

  def comment: String

  def altLabels: String

  def lon: Double

  def lat: Double

  def geometryWKT: String

}