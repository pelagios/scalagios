name := "scalagios-gazetteer"

version := "2.0.0"

scalaVersion := "2.10.0"

/** Runtime dependencies **/
libraryDependencies ++= Seq(     
  "org.apache.lucene" % "lucene-analyzers-common" % "4.9.0",
  "org.apache.lucene" % "lucene-queryparser" % "4.9.0"
)

/** Test dependencies **/
libraryDependencies ++= Seq(
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
)
