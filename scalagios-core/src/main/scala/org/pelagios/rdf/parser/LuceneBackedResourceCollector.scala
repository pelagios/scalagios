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
import org.openrdf.model.Value
import org.apache.lucene.store.NIOFSDirectory

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
  
  private val f = ValueFactoryImpl.getInstance 
      
  private val startTime = System.currentTimeMillis
  
  private val idx = {
    val file = new File(IDX_HOME, IDX_NAME)
    if (file.exists)
      FileUtils.deleteDirectory(new File(IDX_HOME, IDX_NAME))
      
    file.mkdirs
    val initializer = new IndexWriter(FSDirectory.open(file), 
        new IndexWriterConfig(Version.LUCENE_48, new StandardAnalyzer(Version.LUCENE_48)))
    initializer.close()      
      
    new NIOFSDirectory(file)
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
    val count = searcher.getIndexReader.numDocs
    searcherManager.release(searcher)
    count
  }
  
  def resourcesOfType(uri: URI): Iterator[String] = {
    val q = new BooleanQuery()
    q.add(new TermQuery(new Term(Triple.PREDICATE, RDF.TYPE.stringValue)), BooleanClause.Occur.MUST)
    q.add(new TermQuery(new Term(Triple.OBJECT, uri.stringValue)), BooleanClause.Occur.MUST)
    
    new Iterator[String] {
      
      private val BATCH_SIZE: Int = 50000
      
      private var hasAnotherBatch: Boolean = true
      
      private var offset: Int = 0
      
      private var currentBatch =  {
        val (total, batch) = getNextBatch()
        logger.info(total + " resources of type " + uri.stringValue)
        batch
      }
      
      private var cursor: Int = 0
      
      private var totalHits: Option[Int] = None
      
      private def getNextBatch(): (Int, Seq[String]) = {
        logger.info("Streaming next batch from index: " + offset + " to " + (offset + BATCH_SIZE))
        
        val searcher = searcherManager.acquire()
        val topDocsCollector = TopScoreDocCollector.create(offset + BATCH_SIZE, true)
        searcher.search(q, topDocsCollector)
        
        val total = topDocsCollector.getTotalHits
        if (total <= offset + BATCH_SIZE)
          hasAnotherBatch = false
          
        val docs = topDocsCollector.topDocs(offset, BATCH_SIZE).scoreDocs
        offset = offset + docs.size 
          
        val batch = docs.map(scoreDoc => searcher.doc(scoreDoc.doc).get(Triple.SUBJECT))
        searcherManager.release(searcher)
        (total, batch)
      }
      
      override def hasNext: Boolean =
        hasAnotherBatch || (cursor < currentBatch.size)
      
      override def next(): String = {
        if (cursor == currentBatch.size) {
          if (!hasAnotherBatch)
            throw new NoSuchElementException
            
          currentBatch = getNextBatch()._2
          cursor = 0
        }
        
        val str = currentBatch(cursor)
        cursor += 1
        str
      }
      
    }
  }
  
  def getResources(uris: Seq[String]): Seq[Resource] = {
    val searcher = searcherManager.acquire()
    val q = new BooleanQuery()
    uris.foreach(uri => 
      q.add(new TermQuery(new Term(Triple.SUBJECT, uri)), BooleanClause.Occur.SHOULD))
    
    val topDocsCollector = TopScoreDocCollector.create(countAllTriples, true)
    searcher.search(q, topDocsCollector)
    val allTriples = topDocsCollector.topDocs.scoreDocs.map(scoreDoc => new Triple(searcher.doc(scoreDoc.doc)))
    
    def toValue(rdfValue: String): Value = rdfValue match {
      case s if s.startsWith("http://") => f.createURI(s)
      case s if s.startsWith("node") => f.createBNode(s)
      case s => f.createLiteral(s)
    }
    
    val resources = allTriples.groupBy(_.subj).toSeq.map { case (subj, triples) => {
      val resource = Resource(subj)
      triples.foreach(triple => {
        val pred = f.createURI(triple.pred)
        resource.properties.append((pred, toValue(triple.obj)))
      })
      resource
    }}

    searcherManager.release(searcher)
    resources 
  }
  
  def getResource(uri: String): Option[Resource] = {     
    val searcher = searcherManager.acquire()
    val q = new TermQuery(new Term(Triple.SUBJECT, uri))
    val topDocsCollector = TopScoreDocCollector.create(countAllTriples, true)
    searcher.search(q, topDocsCollector)
    val triples = topDocsCollector.topDocs.scoreDocs.map(scoreDoc => new Triple(searcher.doc(scoreDoc.doc)))
    
    val f = ValueFactoryImpl.getInstance 
    val result = if (triples.size > 0) {
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

    searcherManager.release(searcher)
    result
  }
  
  def close() = {
    FileUtils.deleteDirectory(new File(IDX_HOME, IDX_NAME))
    searcherManager.close()
  }
  
}