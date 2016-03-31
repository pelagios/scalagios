name := "scalagios-tools"

version := "0.1.0"

scalaVersion := "2.11.7"

packSettings

packMain := Map("geoparser" -> "org.pelagios.tools.geoparsing.TextToCSV")

/** Runtime dependencies **/
libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.3.1"
)

/** Test dependencies **/
libraryDependencies ++= Seq(
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
)
