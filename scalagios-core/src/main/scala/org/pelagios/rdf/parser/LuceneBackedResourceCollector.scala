package org.pelagios.rdf.parser

import java.io.File
import org.openrdf.rio.helpers.RDFHandlerBase
import org.slf4j.LoggerFactory
import org.openrdf.model.{ URI, Statement }
import org.openrdf.model.impl.ValueFactoryImpl
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{ Document, Field, StringField }
import org.apache.lucene.index.{ IndexWriter, IndexWriterConfig, Term }
import org.apache.lucene.search.{ SearcherManager, SearcherFactory, TermQuery, TopScoreDocCollector }
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.apache.lucene.search.BooleanQuery
import org.openrdf.model.vocabulary.RDF
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.MatchAllDocsQuery
import org.apache.commons.io.FileUtils

class Triple(val doc: Document) {

  lazy val subj = doc.get(Triple.SUBJECT)
  
  lazy val pred = doc.get(Triple.PREDICATE)
  
  lazy val obj = doc.get(Triple.OBJECT)
  
}

object Triple {
  
  val SUBJECT = "subject"
    
  val PREDICATE = "predicate"
    
  val OBJECT = "object"
  
  def apply(subj: String, pred: String, obj: String) = {
    val doc = new Document()
    doc.add(new StringField(Triple.SUBJECT, subj, Field.Store.YES))
    doc.add(new StringField(Triple.PREDICATE, pred, Field.Store.YES))
    doc.add(new StringField(Triple.OBJECT, obj, Field.Store.YES))
    new Triple(doc)
  }
  
}

class LuceneBackedResourceCollector extends RDFHandlerBase {

  private val IDX_HOME = "/home/simonr"
  
  private val IDX_NAME = "scalagios-staging-idx"
    
  private val logger = LoggerFactory.getLogger(classOf[LuceneBackedResourceCollector])
      
  private val startTime = System.currentTimeMillis
  
  private val idx = {
    val file = new File(IDX_HOME, IDX_NAME)
    if (file.exists)
      FileUtils.deleteDirectory(new File(IDX_HOME, IDX_NAME))
      
    file.mkdirs
    val initializer = new IndexWriter(FSDirectory.open(file), 
        new IndexWriterConfig(Version.LUCENE_48, new StandardAnalyzer(Version.LUCENE_48)))
    initializer.close()      
      
    FSDirectory.open(file)
  }
  
  private val idxWriter = new IndexWriter(idx, new IndexWriterConfig(Version.LUCENE_48, new StandardAnalyzer(Version.LUCENE_48)))
  
  private val searcherManager = new SearcherManager(idx, new SearcherFactory())
  
  private var counter = 0
   
  override def handleStatement(s: Statement): Unit = {
    counter += 1
    val triple = Triple(s.getSubject.stringValue, s.getPredicate.stringValue, s.getObject.stringValue)
    idxWriter.addDocument(triple.doc)
    if (counter % 50000 == 0)
      logger.info("Imported " + counter + " triples to staging index")
  }
  
  override def endRDF(): Unit = {
    idxWriter.commit()
    idxWriter.close()
    searcherManager.maybeRefresh()
    logger.info("File parsing complete")
    logger.info("Imported " + countAllTriples + " triples")
    logger.info("Took " + (System.currentTimeMillis - startTime) + "ms")
  }
    
  lazy val countAllTriples: Int = {
    val searcher = searcherManager.acquire()
    searcher.getIndexReader.numDocs
  }
  
  def resourcesOfType(uri: URI): Iterator[String] = {
    val q = new BooleanQuery()
    q.add(new TermQuery(new Term(Triple.PREDICATE, RDF.TYPE.stringValue)), BooleanClause.Occur.MUST)
    q.add(new TermQuery(new Term(Triple.OBJECT, uri.stringValue)), BooleanClause.Occur.MUST)
    
    val searcher = searcherManager.acquire()
    val topDocsCollector = TopScoreDocCollector.create(countAllTriples, false)
    searcher.search(q, topDocsCollector)
    topDocsCollector.topDocs.scoreDocs.map(scoreDoc => searcher.doc(scoreDoc.doc).get(Triple.SUBJECT)).toIterator
  }
  
  def getResource(uri: String): Option[Resource] = {    
    val q = new TermQuery(new Term(Triple.SUBJECT, uri))
    val searcher = searcherManager.acquire()
    val topDocsCollector = TopScoreDocCollector.create(countAllTriples, false)
    searcher.search(q, topDocsCollector)
    val triples = topDocsCollector.topDocs.scoreDocs.map(scoreDoc => new Triple(searcher.doc(scoreDoc.doc)))
    
    val f = ValueFactoryImpl.getInstance 
    if (triples.size > 0) {
      val r = Resource(uri)
      triples.foreach(triple => {
        val pred = f.createURI(triple.pred)
        val obj = triple.obj match {
          case s if s.startsWith("http://") => f.createURI(s)
          case s if s.startsWith("node") => f.createBNode(s)
          case s => f.createLiteral(s)
        }
        r.properties.append((pred, obj))
      })
      Some(r)
    } else {
      None
    }
  }
  
  def close() = {
    FileUtils.deleteDirectory(new File(IDX_HOME, IDX_NAME))
    searcherManager.close()
  }
  
}