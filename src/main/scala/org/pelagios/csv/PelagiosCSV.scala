package org.pelagios.csv

import scala.io.Source
import org.pelagios.api._

/** A helper class to work with CSV data.
  *  
  * Ordering of the columns is, in general, irrelevant. Data will be imported
  * based on the column names. The first row of the CSV file must hold the column
  * names. The following columns are supported:
  * 
  * - "Transcription": zero or one column holding the toponym transcriptions.
  * - "Gazetteer URI": one or more columns holding gazetteer URIs.
  * - "Gazetteer ID": one or more columns holding gazetteer ID shorthands. Supported
  *   formats are 'pleiades:<id>', 'plplus:<id>', 'geonames:<id>'.
  * - "Tag": zero or more columns holding tags
  */
object PelagiosCSV {
  
  def readFromFile(file: String, thing: AnnotatedThing) = {
    val lines = Source.fromFile(file).getLines
    val header = lines.take(1).toSeq.head
    val headerFields = header.split(";", -1).toSeq.zipWithIndex 

    // Parse out relevant column indices
    val TRANSCRIPTION_IDX: Option[Int] = { val idx = headerFields.indexWhere(_._1.equalsIgnoreCase("transcription"))
                                           if (idx < 0) None else Some(idx) }
  
    val GAZETTEER_URI_IDX: Seq[Int]     = headerFields.filter { case (name, idx) => name.equalsIgnoreCase("gazetteer uri") } map (_._2)
    val GAZETTEER_ID_IDX: Seq[Int]      = headerFields.filter { case (name, idx) => name.equalsIgnoreCase("gazetteer id") } map (_._2)
    val TAG_IDX: Seq[Int]              = headerFields.filter { case (name, idx) => name.equalsIgnoreCase("tag") } map (_._2)
  
    // Parse data
    val data = lines.filter(!_.isEmpty)
    
    data.map(_.split(";", -1)).zipWithIndex.foldLeft(Seq.empty[Annotation]) { case (result, (fields, rowIndex)) => {
      val transcription = TRANSCRIPTION_IDX.map(fields(_)).map(Transcription(_, TranscriptionType.Toponym))
    
      val gazetteerURIs = GAZETTEER_URI_IDX.map(fields(_)).filter(!_.isEmpty) ++
                          GAZETTEER_ID_IDX.map(idx => resolveGazetteerID(fields(idx))).filter(_.isDefined).map(_.get)
    
      val tags = TAG_IDX.map(fields(_)).filter(!_.isEmpty).map(Tag(_))
    
      // We require at least one gazetteer reference OR a transcription
      if (transcription.isDefined || gazetteerURIs.size > 0)
        result :+ Annotation("#annotation/" + rowIndex, thing, transcription = transcription, place = gazetteerURIs, tags = tags)
      else
        result
    }}
  }
  
  private def resolveGazetteerID(id: String): Option[String] = {
    id.toLowerCase match {
      case s if s.startsWith("pleiades:") => Some("http://pleiades.stoa.org/places/" + id.substring(9))
      case s if s.startsWith("plplus:") => Some("http://pleiades.stoa.org/places/" + id.substring(7))
      case s if s.startsWith("geonames:") => Some("http://sws.geonames.org/" + id.substring(9))
      case _ => None
    }
  }
  
}