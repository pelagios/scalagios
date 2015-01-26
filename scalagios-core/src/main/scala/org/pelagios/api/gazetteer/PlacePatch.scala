package org.pelagios.api.gazetteer

import org.pelagios.api.PlainLiteral

/** A 'patch' with information to replace in (or append to) an existing gazetteer record **/
case class PlacePatch(

  uri: String,

  label: Option[String],
  
  descriptions: Seq[PlainLiteral],
  
  names: Seq[PlainLiteral],

  locations: Seq[Location],
  
  category: Option[PlaceCategory.Category],
  
  subjects: Seq[String],
  
  closeMatches: Seq[String],
  
  exactMatches: Seq[String]
  
) {
  
  /** Patches a place with the information contained in this place patch.
    *
    * Warning: the patch will be applied irrespective of the URI of the
    * place provided to this method. The resulting place will have the URL
    * of the patch, rather than the provided place. If the replace argument
    * is set to true, the patch will replace information rather than append.
    */
  def patch(place: Place, replace: Boolean = true): Place = {
    if (replace) {
      Place(
        uri,
        label.getOrElse(place.label),
        { if (descriptions.size > 0) descriptions else place.descriptions },
        { if (names.size > 0) names else place.names },
        { if (locations.size > 0) locations else place.locations },
        { if (category.isDefined) category else place.category },
        { if (subjects.size > 0) subjects else place.subjects },
        { if (closeMatches.size > 0) closeMatches else place.closeMatches },
        { if (exactMatches.size > 0) exactMatches else place.exactMatches }
      )
    } else {
      Place(
        uri, // URI will be replaced in any case
        label.getOrElse(place.label), // Label will be replaced in any case
        place.descriptions ++ descriptions,
        place.names ++ names,
        place.locations ++ locations,
        { if (category.isDefined) category else place.category }, // Category will be replaced in any case
        place.subjects ++ subjects,
        place.closeMatches ++ closeMatches,
        place.exactMatches ++ exactMatches
      )
    }
  }

}

