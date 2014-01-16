name := "scalagios-tools"

version := "0.1.0"

scalaVersion := "2.10.0"

/** Runtime dependencies **/
libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.2.0"
)

/** Test dependencies **/
libraryDependencies ++= Seq(
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
)
