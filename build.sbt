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

logLevel := Level.Warn

val spark_scope = System.getProperty("spark.scope", "compile")

/* 会导入hadoop,不会导入hbase,hadoop版本为2.2.0 */
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "1.6.1" % spark_scope,
  "org.apache.spark" %% "spark-core" % "1.6.1" % spark_scope,
  "org.apache.spark" %% "spark-streaming" % "1.6.1" % spark_scope,
  "org.apache.spark" %% "spark-mllib" % "1.6.1" % spark_scope
).map(_.exclude("org.apache.hadoop", "hadoop-yarn-server-nodemanager")
  .exclude("com.google.code.findbugs", "jsr305")
  .exclude("org.spark-project.spark", "unused")
  .exclude("com.google.guava", "guava")
  .exclude("commons-beanutils", "commons-beanutils-core")
  .exclude("commons-collections", "commons-collections")
  .exclude("commons-logging", "commons-logging")
  .exclude("com.esotericsoftware.minlog", "minlog"))

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"

/* hadoop加载的是2.5的serlet-api */
libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-client" % "2.5.1" % spark_scope,
  "org.apache.hadoop" % "hadoop-common" % "2.5.1" % spark_scope,
  "org.apache.hadoop" % "hadoop-hdfs" % "2.5.1" % spark_scope,
  "org.apache.hadoop" % "hadoop-mapreduce-client-app" % "2.5.1" % spark_scope,
  "org.apache.hadoop" % "hadoop-mapreduce-client-shuffle" % "2.5.1" % spark_scope,
  "org.apache.hadoop" % "hadoop-yarn-client" % "2.5.1" % spark_scope,
  "org.apache.hadoop" % "hadoop-yarn-common" % "2.5.1" % spark_scope,
  "org.apache.hadoop" % "hadoop-yarn-server-common" % "2.5.1" % spark_scope
).map(_.exclude("commons-beanutils", "commons-beanutils-core")
  .exclude("javax.servlet", "servlet-api"))

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.36"

libraryDependencies += ("org.apache.spark" %% "spark-streaming-kafka" % "1.6.1")
  .exclude("org.spark-project.spark", "unused")

/* hbase稳定版本为偶数，开发人员预揽版本为奇数 */
/* hbase1.2.1配套的hadoop版本为2.5.1 */
libraryDependencies ++= Seq(
  "org.apache.hbase" % "hbase-client" % "1.2.1" % spark_scope,
  "org.apache.hbase" % "hbase-common" % "1.2.1" % spark_scope
)

val curator = "3.1.0"

/* The Curator Framework high level API. This is built on top of the client and should pull it in automatically. */
libraryDependencies ++= Seq(
  "org.apache.curator" % "curator-recipes" % "2.9.1",
  "org.apache.curator" % "curator-examples" % "2.9.1"
)

libraryDependencies ~= {
  _ map {
    case m if m.organization == "com.typesafe.play" =>
      m.exclude("commons-logging", "commons-logging").
        exclude("com.typesafe.play", "sbt-link")
    case m => m
  }
}

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run))