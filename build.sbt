val specs2Version = "3.7"
def specs2(which: String) = "org.specs2" %% ("specs2-"+which) % specs2Version % "test"

val slickVersion = "3.1.1"
def slickDep(module: String) = {
  "com.typesafe.slick" %% module % slickVersion
}

lazy val schemaGenTaskKey = TaskKey[Seq[File]]("schema-gen")
lazy val schemaGenTask = (
  fullClasspath in Compile,
  runner in Compile,
  sourceManaged in Test,
  streams
).map { (cp, r, output, s) =>
    val outDir = (output/"slick-codegen").getPath
    IO.delete((output ** "*.scala").get)
    toError(r.run("me.dwnld.slick.codegen.WideTableGenerator", cp.files, Array(outDir), s.log))
    (output ** "*.scala").get.toSet.toSeq
  }

lazy val genUtils = Project(id = "slick-gen-wide", base = file("gen")).settings(
  name := "slick-gen-wide",
  description := "Code generation utilties for wide tables in Scala 2.11",
  version := "0.0.1",
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
  libraryDependencies ++= Seq(
    slickDep("slick"),
    slickDep("slick-codegen")
  )
)

lazy val genTest = Project(
  id = "slick-gen-wide-test",
  base = file("gen-test")
).settings(
  scalaVersion := "2.11.8",
  sourceGenerators in Test <+= schemaGenTask,
  schemaGenTaskKey <<= schemaGenTask,
  libraryDependencies ++= Seq(
    "com.h2database" % "h2" % "1.4.191",
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    specs2("core")
  )
).dependsOn(genUtils)