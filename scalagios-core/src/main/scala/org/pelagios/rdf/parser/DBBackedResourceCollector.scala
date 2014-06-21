package org.pelagios.rdf.parser

import org.openrdf.model.{ Statement, URI }
import org.openrdf.model.impl.ValueFactoryImpl
import org.openrdf.rio.helpers.RDFHandlerBase
import org.slf4j.LoggerFactory
import org.h2.tools.DeleteDbFiles
import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.meta.MTable

class Triples(tag: Tag) extends Table[(Option[Int], String, String, String)](tag, "TRIPLES") {
  
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  
  def subj = column[String]("SUBJ")
  
  def pred = column[String]("PRED")
  
  def obj = column[String]("OBJ")
  
  def * = (id.?, subj, pred, obj)
  
}

class DBBackedResourceCollector extends RDFHandlerBase {
  
  private val JDBC_URL = "jdbc:h2:file:~/h2test.db"
    
  private val DRIVER = "org.h2.Driver"
    
  private val logger = LoggerFactory.getLogger(classOf[DBBackedResourceCollector])

  private val query = TableQuery[Triples]

  // Drop database if it exists
  Database.forURL(JDBC_URL, driver = "org.h2.Driver").withSession { implicit session =>
    if (MTable.getTables("TRIPLES").list().size > 0) {
      logger.info("Old database file detected - deleting")
      close()
    }
  }
      
  private val startTime = System.currentTimeMillis
  
  private var counter = 0
  
  private implicit val session = Database.forURL(JDBC_URL, driver = DRIVER).createSession
  
  Database.forURL(JDBC_URL, driver = "org.h2.Driver").withSession { implicit session =>
    query.ddl.create
  }
  
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
    session.close()   
    logger.info("Imported " + countAllTriples + " triples")
    logger.info("Took " + (System.currentTimeMillis - startTime) + "ms")
  }
  
  def close() =
    DeleteDbFiles.execute("~", "h2test", true)
    
  def countAllTriples(): Int =
    Database.forURL(JDBC_URL, driver = "org.h2.Driver").withSession { implicit session =>
      Query(query.length).first 
    }
  
  def resourcesOfType(uri: URI): Iterator[String] =
    Database.forURL(JDBC_URL, driver = "org.h2.Driver").withSession { implicit session => 
      query.where(_.pred === uri.stringValue).map(_.subj).iterator
    }
  
  def getResource(uri: String): Option[Resource] = {
    val f = ValueFactoryImpl.getInstance
     
    Database.forURL(JDBC_URL, driver = "org.h2.Driver").withSession { implicit session => 
      val t = query.where(_.subj === uri).list
      if (t.size > 0) {
        val r = Resource(uri)
        t.foreach(triple => r.properties.append((f.createURI(triple._3), f.createURI(triple._4))))
        Some(r)
      } else {
        None
      }
    }
  }

}
