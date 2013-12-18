package org.pelagios.gazetteer

import org.pelagios.api.Place
import org.apache.lucene.index.{ DirectoryReader, Term }
import org.apache.lucene.search.{ BooleanClause, BooleanQuery, IndexSearcher, TermQuery, TopScoreDocCollector }
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
import org.apache.lucene.util.Version

trait PlaceIndexReader extends PlaceIndexBase {
  
  /** Retrieves a name by its URI
    * @param uri the URI  
    */
  def findByURI(uri: String): Option[PlaceDocument] = {
    val q = new BooleanQuery()
    q.add(new TermQuery(new Term(PlaceIndex.FIELD_URI, GazetteerUtils.normalizeURI(uri))), BooleanClause.Occur.MUST)
    
    // TODO creating a new reader of every access has some overhead - could be improved
    val reader = DirectoryReader.open(index)
    val searcher = new IndexSearcher(reader)
    
    val collector = TopScoreDocCollector.create(1, true)
    searcher.search(q, collector)
    
    val places = collector.topDocs.scoreDocs.map(scoreDoc => new PlaceDocument(searcher.doc(scoreDoc.doc)) )
    reader.close()
    if (places.size > 0)
      return Some(places(0))
    else
      None
  }
  
  def query(query: String, fuzzy: Boolean = false): Iterable[PlaceDocument] = {
    val fields = Seq(PlaceIndex.FIELD_TITLE, PlaceIndex.FIELD_NAME).toArray    
    val suffix = if (fuzzy) " ~" else ""
    val q = new MultiFieldQueryParser(Version.LUCENE_44, fields, analyzer).parse(query + suffix)
    
    // TODO creating a new reader of every access has some overhead - could be improved
    val reader = DirectoryReader.open(index)
    val searcher = new IndexSearcher(reader)
    
    val collector = TopScoreDocCollector.create(50, true)
    searcher.search(q, collector)
    
    val places = collector.topDocs.scoreDocs.map(scoreDoc => new PlaceDocument(searcher.doc(scoreDoc.doc)))
    reader.close()
    places
  }
  
  def getNetwork(place: PlaceDocument): Network = {
    val q = new BooleanQuery()
    q.add(new TermQuery(new Term(PlaceIndex.FIELD_SEED_URI, place.seedURI)), BooleanClause.Occur.MUST)
    
    val reader = DirectoryReader.open(index)
    val searcher = new IndexSearcher(reader)
    
    // TODO creating a new reader of every access has some overhead - could be improved
    val collector = TopScoreDocCollector.create(50, true)
    searcher.search(q, collector)
    
    val places = collector.topDocs.scoreDocs.map(scoreDoc => new PlaceDocument(searcher.doc(scoreDoc.doc)))
    reader.close()
    
    // TODO temporary hack - for now we only return the list of places (and no links)
    Network(places, Seq.empty[(Int, Int)])
  }

}

case class Network(places: Seq[Place], links: Seq[(Int, Int)])