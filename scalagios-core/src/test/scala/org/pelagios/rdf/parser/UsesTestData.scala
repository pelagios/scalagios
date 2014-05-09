package org.pelagios.rdf.parser

import java.io.File

trait UsesTestData {
  
  private lazy val baseDir = {
    val executionDir = new File(".").getCanonicalFile()
    println("Execution directory is " + executionDir.getAbsolutePath)
    if (executionDir.list().contains("test-data"))
      new File(executionDir, "test-data")
    else
      new File(executionDir.getParentFile(), "test-data")
  }
  
  def getFile(name: String) = new File(baseDir, name)

}