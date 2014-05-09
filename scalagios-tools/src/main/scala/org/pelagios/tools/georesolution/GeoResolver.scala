package org.pelagios.tools.georesolution

import org.pelagios.api.Place
import org.pelagios.gazetteer.PlaceIndex

class GeoResolver(index: PlaceIndex) {

  def matchToponym(toponym: String): Option[Place] = {
    val results = index.query(toponym + "~")
        
    if (results.size > 0) {
      val exactMatch = results.find(isExactMatch(toponym, _))
      if (exactMatch.isDefined) {
        exactMatch
      } else {
        results.find(isPrefixMatch(toponym, _))
      }
    } else { 
      None
    }    
  }
  
  /** Note: we support Option[String] for easier handling of incomplete 
    * toponym lists.
    * 
    * TODO eliminate code duplication
    */
  def matchToponymList(toponyms: Seq[Option[String]]): Seq[Option[Place]] = {    
    // Step 1: get a list of candidates for every place in the list
    val candidates = toponyms.map(toponym => {
      if (toponym.isDefined) {
        val sanitized = toponym.get.replace("[", " ").replace("]", " ").replace("{", " ").replace("}", " ").replace("*", " ").replace("?", " ").replace("+", " ")
        (toponym, index.query("\"" + sanitized + "\"~"))
      } else {
        (toponym, Seq.empty[Place])
      }
    })
    
    // Step 2: do we have exact or prefix matches? If so, discard the rest!
    val truncatedCandidates = candidates.map { case (toponym, candidateList) => {
      if (toponym.isDefined) {
        val exactMatches = candidateList.filter(p => isExactMatch(toponym.get, p) || isPrefixMatch(toponym.get, p))
        if (exactMatches.size > 0) {
          (toponym, exactMatches)
        } else {
          (toponym, candidateList)
        }
      } else {
        (toponym, Seq.empty[Place])
      }
    }}
    
    def distance(a: Place, b: Place): Double = {
      def centroidA = a.getCentroid
      def centroidB = b.getCentroid
      
      if (centroidA.isDefined && centroidB.isDefined) {
        val dX = Math.abs(centroidA.get.x - centroidB.get.x)
        val dY = Math.abs(centroidA.get.y - centroidB.get.y)
        dX * dX + dY * dY
      } else {
        Double.MaxValue        
      }
    }

    // Step 3: rank remaining candidates by distance from previous in list
    def rankByDistance(candidateLists: Seq[Seq[Place]], allRanked: Seq[Option[Place]]): Seq[Option[Place]] = {
      if (candidateLists.isEmpty) {
        allRanked
      } else {
        val nextCandidates = candidateLists.head
        
        if (nextCandidates.size == 0) {
          // No candidates in this list - recurse
          rankByDistance(candidateLists.tail, None +: allRanked)
        } else if (nextCandidates.size == 1) {
          rankByDistance(candidateLists.tail, Some(nextCandidates(0)) +: allRanked)
        } else {
          val lastIdentified = allRanked.find(_.isDefined).map(_.get)
          val ranked = if (lastIdentified.isDefined) {
            val zippedWithDistance = nextCandidates.map(nc => (nc, distance(nc, lastIdentified.get)))
            zippedWithDistance.sortBy(_._2)
          } else {
            nextCandidates.map(p => (p, 0.0))
          }
          
          val closest = //if (ranked(0)._2 < 1)
              Some(ranked(0)._1)
          //  else
          //    None
          
          rankByDistance(candidateLists.tail, closest +: allRanked)
        }
      }
    }
    
    // The pain... I messed up the order in the recursion somewhere
    // TODO eliminate the final .reverse
    rankByDistance(truncatedCandidates.map(_._2.toSeq), Seq.empty[Option[Place]]).reverse
  }
  
  def isExactMatch(toponym: String, place: Place): Boolean = {
    val labels = place.title +: place.names.map(_.chars)
    labels.exists(_.equalsIgnoreCase(toponym))
  } 
  
  def isPrefixMatch(toponym: String, place: Place): Boolean = {
    val prefix = if (toponym.length > 4)
                   toponym.substring(0, toponym.length - 2).toLowerCase
                 else
                   toponym.toLowerCase
                   
    val labels = place.title +: place.names.map(_.chars)
    labels.exists(_.toLowerCase.startsWith(prefix))
  }
  
}