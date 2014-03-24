package org.pelagios.gazetteer

case class Network(places: Seq[PlaceDocument], links: Seq[(Int, Int)]) {
  
  private val _seedURIs = places.groupBy(_.seedURI).keys
  
  if (_seedURIs.size != 1)
    throw new IllegalArgumentException("Network contains places with different seed URIs: " + _seedURIs.mkString(", "))
  
  val seedURI = _seedURIs.head
  
  /** Networks are equal if their seed URI is equal **/
  override def equals(o: Any) = o match{
    case other: Network => other.seedURI.equals(seedURI)
    case _ => false
  }
  
  override def hashCode = seedURI.hashCode()
  
}

object Network {
  
  /** Turns a list of networks into a list of conflated places.
    *
    * This function takes care that identical networks in the list will be removed.
    * @param networks the networks to conflate  
    */
  def conflateNetworks(networks: Seq[Network], prefURISpace: Option[String] = None,
    prefLocationSource: Option[String] = None, prefDescriptionSource: Option[String] = None): Seq[ConflatedPlace] = {
    
    val duplicatesRemoved = networks.foldLeft(Seq.empty[Network])((all, next) => {
      if (all.contains(next))
        all
      else
        all :+ next
    })
    
    duplicatesRemoved.map(new ConflatedPlace(_, prefURISpace, prefLocationSource, prefDescriptionSource))
  }
  
}