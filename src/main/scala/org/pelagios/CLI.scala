package org.pelagios

import org.pelagios
import java.io.File

case class Config(input: File = new File("."), mode: String = "")

object CLI extends App {
  
  val parser = new scopt.OptionParser[Config]("Scalagios") {
    head("Scalagios", "2.0.0-BETA")
    
    help("help") text("prints this usage text")
    
    note("")

    cmd("profile") action { (_, c) =>
      c.copy(mode = "profile") } text("") children(
        opt[File]('i', "input") required() valueName("<file>") action { (x, c) =>
          c.copy(input = x) } text("the Pelagios data file to profile (required)")
      )
  } 
  
  parser.parse(args, Config()) map { config =>
    config.mode match {
      case c if c.isEmpty => { Console.err.println("Error: You did not specify a command!"); parser.showUsage }
      case c if c.equals("profile") => profile(config)
      case c => 
    }
  } getOrElse {
    // 
  }
  
  def profile(config: Config) = {
    println(config)
  }

}