package org.pelagios.rdf.parser

import org.openrdf.rio.helpers.RDFHandlerBase
import org.pelagios.api.Place
import org.pelagios.api.Label
import org.pelagios.api.Name
import org.pelagios.api.Location

private[parser] class NameResource(resource: Resource) {
  
}

private[parser] class LocationResource(resource: Resource) {
  
}

private[parser] class PlaceResource(resource: Resource, names: Seq[NameResource], locations: Seq[LocationResource]) extends Place {

  def uri = resource.uri
  
  def title = {
    Label("foo")
  }
  
  def descriptions = Seq.empty[Label]
  
  def names = Seq.empty[Name]

  def locations = Seq.empty[Location]
  
  def subjects = Seq.empty[String]
  
  def closeMatches = Seq.empty[String]

}


class GazetteerParser extends RDFHandlerBase {

}