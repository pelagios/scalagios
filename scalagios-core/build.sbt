name := "scalagios-core"

version := "2.0.0"

scalaVersion := "2.10.0"

resolvers += "Open Source Geospatial Foundation Repository" at "http://download.osgeo.org/webdav/geotools/"

/** Runtime dependencies **/
libraryDependencies ++= Seq(
  "org.openrdf.sesame" % "sesame-rio-n3" % "2.7.11",
  "org.openrdf.sesame" % "sesame-rio-rdfxml" % "2.7.11",   
  "org.fusesource.scalate" % "scalate-core_2.10" % "1.6.1",           
  "com.vividsolutions" % "jts" % "1.13",
  "org.geotools" % "gt-geojson" % "10.5",
  "net.liftweb" %% "lift-json" % "2.5"
)

/** Test dependencies **/
libraryDependencies ++= Seq(
  "junit" % "junit" % "4.11" % "test",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
)
