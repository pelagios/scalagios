lazy val core = project.in(file("scalagios-core"))

lazy val legacy = project.in(file("scalagios-legacy")).dependsOn(core)

lazy val gazetteer = project.in(file("scalagios-gazetteer")).dependsOn(core)
 