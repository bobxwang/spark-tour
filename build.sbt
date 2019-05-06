organization := "com.bob"

name := "spark-tour"

version := "1.0"

scalaVersion := "2.11.8"

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

///* 当想快速构建项目时可以将一些第三方库放在3rdlibs这个目录下，默认是lib目录，我们可以通过如下进行更改 */
//unmanagedBase <<= baseDirectory { base => base / "3rdlibs" }

logLevel := Level.Warn

val spark_scope = System.getProperty("spark.scope", "compile")

val spark_version = "2.4.2"
val hadoop_version = "2.7.3"

/* 会导入hadoop,不会导入hbase,hadoop版本为2.2.0 */
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-hive" % spark_version % spark_scope,
  "org.apache.spark" %% "spark-sql" % spark_version % spark_scope,
  "org.apache.spark" %% "spark-core" % spark_version % spark_scope,
  "org.apache.spark" %% "spark-streaming" % spark_version % spark_scope,
  "org.apache.spark" %% "spark-mllib" % spark_version % spark_scope,
  "org.apache.spark" %% "spark-streaming-kafka-0-8" % spark_version % spark_scope
).map(_.exclude("org.apache.hadoop", "hadoop-yarn-server-nodemanager")
  .exclude("com.google.code.findbugs", "jsr305")
  .exclude("org.spark-project.spark", "unused")
  .exclude("com.google.guava", "guava")
  .exclude("commons-beanutils", "commons-beanutils-core")
  .exclude("commons-collections", "commons-collections")
  .exclude("commons-logging", "commons-logging")
  .exclude("com.esotericsoftware.minlog", "minlog"))

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"

/* hadoop加载的是2.5的servlet-api */
libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-client" % hadoop_version % spark_scope,
  "org.apache.hadoop" % "hadoop-common" % hadoop_version % spark_scope,
  "org.apache.hadoop" % "hadoop-hdfs" % hadoop_version % spark_scope,
  "org.apache.hadoop" % "hadoop-mapreduce-client-app" % hadoop_version % spark_scope,
  "org.apache.hadoop" % "hadoop-mapreduce-client-shuffle" % hadoop_version % spark_scope,
  "org.apache.hadoop" % "hadoop-yarn-client" % hadoop_version % spark_scope,
  "org.apache.hadoop" % "hadoop-yarn-common" % hadoop_version % spark_scope,
  "org.apache.hadoop" % "hadoop-yarn-server-common" % hadoop_version % spark_scope
).map(_.exclude("commons-beanutils", "commons-beanutils-core")
  .exclude("javax.servlet", "servlet-api"))

libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.15"

/* hbase稳定版本为偶数，开发人员预揽版本为奇数 */
/* hbase1.2.1配套的hadoop版本为2.5.1 */
libraryDependencies ++= Seq(
  "org.apache.hbase" % "hbase-client" % "1.2.1" % spark_scope,
  "org.apache.hbase" % "hbase-common" % "1.2.1" % spark_scope
)

val curator = "3.1.0"

/* The Curator Framework high level API. This is built on top of the client and should pull it in automatically. */
//libraryDependencies ++= Seq(
//  "org.apache.curator" % "curator-recipes" % "2.9.1",
//  "org.apache.curator" % "curator-examples" % "2.9.1"
//)

libraryDependencies ~= {
  _ map {
    case m if m.organization == "com.typesafe.play" =>
      m.exclude("commons-logging", "commons-logging").
        exclude("com.typesafe.play", "sbt-link")
    case m => m
  }
}

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in(Compile, run), runner in(Compile, run))