package org.pelagios.rdf.parser

import org.openrdf.model.{ Statement, URI }
import org.openrdf.model.impl.ValueFactoryImpl
import org.openrdf.rio.helpers.RDFHandlerBase
import org.slf4j.LoggerFactory
import org.h2.tools.DeleteDbFiles
import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.meta.MTable
import org.openrdf.model.vocabulary.RDF

class Triples(tag: Tag) extends Table[(Option[Int], String, String, String)](tag, "triples") {
  
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  
  def subj = column[String]("SUBJ")
  
  def pred = column[String]("PRED")
  
  def obj = column[String]("OBJ")
  
  def * = (id.?, subj, pred, obj)
  
  /** Indices **/
  
  def subjIdx = index("idx_subj", subj, unique = false)
  
  def predIdx = index("idx_pred", pred, unique = false)
  
  def objIdx = index("idx_obj", obj, unique = false)
  
}

class H2BackedResourceCollector extends RDFHandlerBase {
  
  private val DB_HOME = "~"
  
  private val DB_NAME = "h2test"
  
  private val JDBC_URL = "jdbc:h2:file:" + DB_HOME + "/" + DB_NAME + ".db"
    
  private val DRIVER = "org.h2.Driver"
    
  private val logger = LoggerFactory.getLogger(classOf[H2BackedResourceCollector])
  
  private val query = TableQuery[Triples]

  private implicit val session = Database.forURL(JDBC_URL, driver = DRIVER).createSession
  
  // Drop database if it exists
  if (MTable.getTables("triples").list().size > 0) {
    logger.info("Old database file detected - deleting")
    DeleteDbFiles.execute(DB_HOME, DB_NAME, true)
  }
      
  private val startTime = System.currentTimeMillis
  
  private var counter = 0
  
  query.ddl.create
  
  override def handleStatement(s: Statement): Unit = {
    counter += 1
    query.insert(None, 
      s.getSubject.stringValue,
      s.getPredicate.stringValue, 
      s.getObject.stringValue)(session)      
    
    if (counter % 50000 == 0)
      logger.info("Imported " + counter + " triples to staging DB")
  }
  
  override def endRDF(): Unit = {
    logger.info("File parsing complete")
    logger.info("Imported " + countAllTriples + " triples")
    logger.info("Took " + (System.currentTimeMillis - startTime) + "ms")
  }
    
  def countAllTriples(): Int =
    Query(query.length).first 
  
  def resourcesOfType(uri: URI): Iterator[String] = {
    logger.info("Retrieving resources of type " + uri)
    val startTime = System.currentTimeMillis
    val it = query.where(_.pred === RDF.TYPE.stringValue).filter(_.obj === uri.stringValue).map(_.subj).iterator
    logger.info("Done - took " + (System.currentTimeMillis - startTime) + "ms")
    it
  }
  
  def getResource(uri: String): Option[Resource] = {    
    val f = ValueFactoryImpl.getInstance 
    val t = query.where(_.subj === uri).list
    if (t.size > 0) {
      val r = Resource(uri)
      t.foreach(triple => {
        val pred = f.createURI(triple._3)
        val obj = triple._4 match {
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
    session.close()
    DeleteDbFiles.execute("~", "h2test", true)
  }
  
}
