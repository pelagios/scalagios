package org.pelagios

import org.pelagios
import java.io.File
import org.pelagios.index.PlaceIndex
import scala.io.Source
import java.io.PrintWriter

case class Config(input: File = new File("."), output: File = new File("."), format: String = "", mode: String = "")

object CLI extends App {
  
  val parser = new scopt.OptionParser[Config]("Scalagios") {
    head("Scalagios", "2.0.0-BETA")
    
    help("help") text("prints this usage text")
    
    note("")

    cmd("profile") action { (_, c) =>
      c.copy(mode = "profile") } text("Validates and renders an overview of the contents of a Pelagios data dump.\n") children(
        opt[File]('i', "input") required() valueName("<file>") action { (x, c) =>
          c.copy(input = x) } text("the Pelagios data file to profile (required)\n")
      )
      
    cmd ("migrate") action { (_, c) => 
      c.copy(mode = "migrate") } text("Migrates data from older formats. "
          + "Supported migration paths are Pleiades RDF to Pelagios Gazetteer interconnection format, "
          + "and deprecated (OAC-based) Pelagios annotations to new (OA-based) annotations\n") children(
        opt[String]('f', "format") required() valueName("<format>") action { (x, c) =>
          c.copy(format = x) } text("'pleiades' or 'oac' (required)\n"),
        opt[File]('s', "source") required() valueName("<source>") action { (x, c) =>
          c.copy(input = x) } text("the source file (or folder of files) to migrate (required)\n"),
        opt[File]('d', "destination") required() valueName("<destination>") action { (x, c) =>
          c.copy(output = x) } text("the destination file to migrate to (required)\n")
      )
      
    cmd ("gmatch") action { (_, c) => 
      c.copy(mode = "gmatch") } text("Performs gazetteer matching on a list of placenames.\n") children(
        opt[File]('i', "input") required() valueName("<input>") action { (x, c) =>
          c.copy(input = x) } text("the source file to match (required)\n"),
        opt[File]('o', "output") required() valueName("<output>") action { (x, c) =>
          c.copy(output = x) } text("the output file for results (required)\n")
      )
  } 
  
  parser.parse(args, Config()) map { config =>
    config.mode match {
      case c if c.isEmpty => { Console.err.println("Error: You did not specify a command!"); parser.showUsage }
      case c if c.equals("profile") => profile(config)
      case c if c.equals("migrate") => migrate(config)
      case c if c.equals("gmatch") => gmatch(config)
      case c => 
    }
  } getOrElse {
    // 
  }
  
  def profile(config: Config) = {
    println(config)
  }
  
  def migrate(config: Config) = {
    config.format match {
      case f if f.equals("pleiades") => {
        Scalagios.Legacy.migratePleiadesDumps(config.input.listFiles, config.output)
      }
    }
  }
  
  def gmatch(config: Config) = {
    // TODO only build index if it isn't there!
    print("Loading test data from file... ")
    val places = Scalagios.parseGazetteerFile(new File("places-new.ttl"))
    println(places.size + " places.")
    
    print("Writing index... ")
    val index = PlaceIndex.open(new File("test-idx"))
    index.addPlaces(places)
    println("done.")
    
    // Matching...
    val writer = new PrintWriter(config.output)
    
    Source.fromFile(config.input).getLines.foreach(placename => {  
      val results = index.query(placename)
      if (results.size > 0)
        writer.println(placename + "; " + results(0).uri + "; " + results(0).title.label + ";")
      else
        writer.println(placename + "; no match;;")
    })
            
    writer.flush
    writer.close
    println("Done.")
  }

}