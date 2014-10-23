package org.pelagios.gazetteer

import org.pelagios.api.gazetteer.Place
import org.apache.lucene.index.{ DirectoryReader, Term }
import org.apache.lucene.search.{ BooleanClause, BooleanQuery, IndexSearcher, TermQuery, TopScoreDocCollector }
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
import org.apache.lucene.util.Version
import net.sf.junidecode.Junidecode

trait PlaceIndexReader extends PlaceIndexBase {
  
  def isEmpty: Boolean = {
    // TODO creating a new reader of every access has some overhead - could be improved
    val reader = DirectoryReader.open(index)
    val isEmpty = reader.numDocs() == 0
    reader.close()
    isEmpty
  }
  
  def findByURI(uri: String): Option[PlaceDocument] = {
    val q = new BooleanQuery()
    q.add(new TermQuery(new Term(PlaceIndex.FIELD_URI, GazetteerUtils.normalizeURI(uri))), BooleanClause.Occur.MUST)
    
    // TODO creating a new reader of every access has some overhead - could be improved
    val reader = DirectoryReader.open(index)
    val searcher = new IndexSearcher(reader)
    
    val collector = TopScoreDocCollector.create(1, true)
    searcher.search(q, collector)
    
    val places = collector.topDocs.scoreDocs.map(scoreDoc => new PlaceDocument(searcher.doc(scoreDoc.doc)))
    reader.close()
    if (places.size > 0)
      return Some(places(0))
    else
      None
  }
  
  def findByByCloseMatch(uri: String): Seq[PlaceDocument] = {
    val q = new BooleanQuery()
    q.add(new TermQuery(new Term(PlaceIndex.FIELD_CLOSE_MATCH, GazetteerUtils.normalizeURI(uri))), BooleanClause.Occur.MUST)
    
    val reader = DirectoryReader.open(index)
    val searcher = new IndexSearcher(reader)
    
    val collector = TopScoreDocCollector.create(1, true)
    searcher.search(q, collector)
    
    val places = collector.topDocs.scoreDocs.map(scoreDoc => new PlaceDocument(searcher.doc(scoreDoc.doc)))
    reader.close()
    places
  }
  
  def query(query: String, fuzzy: Boolean = false): Iterable[PlaceDocument] = {    
    // We only support keyword queries, and remove all special characters that may mess it up
    val invalidChars = Seq("(", ")", "[", "]")
    val normalizedQuery = invalidChars.foldLeft(query)((normalized, invalidChar) => normalized.replace(invalidChar, ""))
    val transliteratedQuery = Junidecode.unidecode(normalizedQuery)    

    val fields = Seq(PlaceIndex.FIELD_LABEL, PlaceIndex.FIELD_NAME).toArray    
    val suffix = if (fuzzy) " ~" else ""
    val expandedQuery =
      if (normalizedQuery == transliteratedQuery) 
        normalizedQuery + suffix
      else
        normalizedQuery + suffix + " OR " + transliteratedQuery + suffix
        
    val q = new MultiFieldQueryParser(Version.LUCENE_4_9, fields, analyzer).parse(expandedQuery)
    
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
    Network(places, buildGraph(places))
  }
  
  private def buildGraph(places: Seq[PlaceDocument]): Seq[(Int, Int)] = {
    val edges = places.map(from => from.closeMatches.map(to => (from.uri, to))).flatten
    def idx(uri: String): Int = places.indexWhere(_.uri == uri)
    edges.map(tuple => (idx(tuple._1), idx(tuple._2)))
  }

}

