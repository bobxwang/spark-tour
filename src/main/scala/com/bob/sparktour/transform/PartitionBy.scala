package com.bob.sparktour.transform

import org.apache.spark.{Partitioner, SparkConf, SparkContext}

class UDPartitioner(numParts: Int) extends Partitioner {
  override def numPartitions: Int = numParts

  override def getPartition(key: Any): Int = key.hashCode() % numPartitions
}

object PartitionBy extends App {

  val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("ReducerByKey")
  val sc = new SparkContext(sparkConf)

  val l = List(Tuple2(1, "A"), Tuple2(2, "b"), Tuple2(1, "A"), Tuple2(3, "c"))
  val lRdd = sc.parallelize(l)

  // 生成新的ShuffleRDD, 将原RDD进行重新分区
  val rs = lRdd.partitionBy(new UDPartitioner(3))
  rs.foreach(println)

  // it will print as belows:
  // (1,A)
  // (2,b)
  // (3,c)
  // (1,A)
  // 上面的顺序不一定,每次运行都会不一样
}