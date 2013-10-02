package org.pelagios

import org.pelagios
import java.io.File

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
  } 
  
  parser.parse(args, Config()) map { config =>
    config.mode match {
      case c if c.isEmpty => { Console.err.println("Error: You did not specify a command!"); parser.showUsage }
      case c if c.equals("profile") => profile(config)
      case c if c.equals("migrate") => migrate(config)
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

}