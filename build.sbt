organization := "com.bob"

name := "spark-tour"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions ++= Seq(
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:higherKinds",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Xfatal-warnings"
)

javaOptions += "-server -Xss1m -Xmx2g"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "1.6.1",
  "org.apache.spark" %% "spark-core" % "1.6.1",
  "org.apache.spark" %% "spark-streaming" % "1.6.1",
  "org.apache.spark" %% "spark-mllib" % "1.6.1"
).map(_.exclude("org.apache.hadoop", "hadoop-yarn-server-nodemanager")
  .exclude("com.google.code.findbugs", "jsr305")
  .exclude("commons-beanutils", "commons-beanutils")
  .exclude("org.spark-project.spark", "unused"))

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.36"

libraryDependencies += ("org.apache.spark" %% "spark-streaming-kafka" % "1.6.1").exclude("org.spark-project.spark", "unused")