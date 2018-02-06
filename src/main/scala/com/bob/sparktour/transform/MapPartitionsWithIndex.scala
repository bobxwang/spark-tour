package com.bob.sparktour.transform

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by wangxiang on 18/2/6.
  */
object MapPartitionsWithIndex extends App {

  val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("ReducerByKey")
  val sc = new SparkContext(sparkConf)

  // mapPartitions跟map类似，不过映射函数的参数由RDD中的每一个元素变成了RDD中每一个分区的迭代器，
  // 如果在映射的过程中需频繁创建额外对象，使用此将比map高效，比如将RDD的所有数据通过JDBC连接写入数据库，
  // 如果使用map，那将为每一个元素都创建一个connection，而如果使用mapPartitions，那么就是每一个分区一个connection
  val l = sc.parallelize(List(1, 2, 3, 4, 5), 4)
  l.mapPartitions(x => List(x.sum).toIterator)
  l.mapPartitions(x => x.map(y => y + 1))
  // 可以看出跟map的不同，是独立的在RDD的每一个分块上运行
  // 而mapPartitionsWithIndex跟mapPartitions不一样的是，还额外提供了一个参数用于表明现在是哪个分区的索引
  val lc = l.mapPartitionsWithIndex((x, y) => {
    println("")
    print(x)
    print("---")
    val t = y.map(z => z + x)
    t.foreach(x => {
      print(x)
      print("---")
    })
    t
  })
  lc.collect().foreach(println)
}