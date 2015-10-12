package org.pelagios.tools.migration.pleiades

import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import org.openrdf.rio.RDFFormat
import org.pelagios.Scalagios
import org.pelagios.api.{ Image, PlainLiteral }
import org.pelagios.api.gazetteer.{ Location, Place, PlaceCategory }
import org.pelagios.legacy.LegacyInterop

object ConvertPleiadesDump extends App {
  
  import org.pelagios.rdf.vocab.PleiadesPlaceTypes._
  
  private val PLEIADES_DATA = "../test-data/legacy/test-places-pleiades.2012.07.ttl.gz"
  
  private val is = new GZIPInputStream(new FileInputStream(PLEIADES_DATA))    
    
  // Note: is 'Oasis' a settlement or rather a natural feature?  
  private val SETTLEMENT = Set(settlement, settlementModern, urbanArea, station, port, oasis, productionCenter, fort)
    .map(_.stringValue)

  private val NATURAL_FEATURE = Set(estuary, mountain, cave, lake, hill, cape, river, pass, spring, ridge, rapid, 
    island, whirlpool, saltMarsh, plain)
    .map(_.stringValue)
      
  // Centuriation?
  private val REGION = Set(region, waterInland, waterOpen, valley, coast, province, forest, bay, centuriation)
    .map(_.stringValue)

  private val ETHNOS = Set(people)
    .map(_.stringValue)
   
  // Are 'Well', 'Tunnel' man-made structures or natural features? Are 'Mine', 'Temple' settlements? 
  private val MAN_MADE_STRUCTURE = Set(bridge, villa, wall, bath, church, lighthouse, aqueduct, waterWheel, dam, 
    road, mosque, tunnel, canal, cemetery, tumulus, well, tunnel, estate, mine, temple, reservoir)
    .map(_.stringValue)
 
  private val typeMappings = Seq(
      (SETTLEMENT -> PlaceCategory.SETTLEMENT),
      (NATURAL_FEATURE -> PlaceCategory.NATURAL_FEATURE),
      (REGION -> PlaceCategory.REGION),
      (ETHNOS -> PlaceCategory.ETHNOS),
      (MAN_MADE_STRUCTURE -> PlaceCategory.MAN_MADE_STRUCTURE))

  val importedPlaces = LegacyInterop.parsePleiadesRDF(is, "http://pleiades.stoa.org/", RDFFormat.TURTLE).map(legacy => {
    val descriptions = legacy.comment.map(comment => Seq(PlainLiteral(comment))).getOrElse(Seq.empty[PlainLiteral])
  
    val names = 
      (legacy.altLabels.map(_.split(",").toSeq).getOrElse(Seq.empty[String]) ++
       legacy.coverage.map(_.split(",").toSeq).getOrElse(Seq.empty[String]))
      .map(name => PlainLiteral(name))
      
    val placeCategory = legacy.featureType.map(getPlaceType(_)).flatten
    
    Place(
      legacy.uri,
      legacy.label.getOrElse("[unnamed]"),
      descriptions, 
      names,
      legacy.location.flatMap(geom => Location.create(None, Some(geom))),
      None, // temporal coverage
      Seq.empty[String], // time periods
      placeCategory,
      Seq.empty[String], // subject
      Seq.empty[Image], // Depictions,
      Seq.empty[String], // closeMatches
      Seq.empty[String] // exactMatches
    )
  })
  
  Scalagios.writePlaces(importedPlaces, "/home/simonr/Downloads/migrated.ttl", Scalagios.TURTLE)
  
  println("Migrated " + importedPlaces.size + " places")
  
  private def getPlaceType(pleiadesType: String): Option[PlaceCategory.Category] = {
    val mapping = typeMappings.find { case (pleiadesTypes, pelagiosType) =>
      pleiadesTypes.contains(pleiadesType)
    }    
    mapping.map(_._2)
  }
  
}
