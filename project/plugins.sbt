addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.1")

resolvers += "dependency" at "https://github.com/jrudolph/sbt-dependency-graph.git"

// 可以像mvn一样显示依赖关系图
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")