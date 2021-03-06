package com.bob.sparktour.transform

import org.apache.spark.{SparkConf, SparkContext}

case class Juice(volumn: Int) {
  def add(juice: Juice): Juice = Juice(volumn + juice.volumn)
}

case class Fruit(king: String, weight: Int) {
  def makeJuice: Juice = Juice(weight * 100)
}

object BasicTransformation {

  def main(args: Array[String]) {

    System.setProperty("spark.executor.memory", "128M")
    val sparkConf: SparkConf = new SparkConf()
    if (args.size == 2) {
      sparkConf.setMaster(args(0))
      sparkConf.setAppName(args(1))
    }
    val sc = new SparkContext(sparkConf)
    sc.getConf.toDebugString
    sc.makeRDD(0 to 1000)

    /**
      * if masterURL is local, uses 1 thread only
      * is local[n],uses n threads
      * is local[*],uses as many threads as the number of processors available to jvm, using Runtime.getRuntime.availableProcessors
      */

    val x = sc.parallelize(Array("Joseph", "Jimmy", "Tina", "Thomas", "James", "Cory", "Christine", "Jackeline", "Juan"), 3)
    x.collect.foreach(println)

    val ll = List(1, 2, 3, 5, 6, 8, 3, 4, 2, 8)
    ll.groupBy(x => x >= 4)

    val rd = sc.makeRDD(1 to 5, 2)
    println(rd.fold(0)(_ + _))

    /*
    * 优先用reduceByKey而不是groupByKey，因为spark知道它可以在每个分区移动数据前将输出数据与一个共用的key结合
    * when call on a dataset of (K,V) pairs,return a ds of (K,V) pairs, where the V is for each key aggregated using
    * the given reduce function, which must by of type (V,V) => V
    * while the groupByKey is return a ds with (K,Iterable<V>) pairs
    * */
    val words = Array("one", "two", "two", "three", "three", "three")
    val wordsRDD = sc.parallelize(words).map(word => (word, 1))
    wordsRDD.reduceByKey(_ + _).collect().foreach(println)
    wordsRDD.groupByKey().map(w => (w._1, w._2.sum)).collect().foreach(println)


    val data = sc.parallelize(
      List(
        ("gogu", 1),
        ("gogu", 1),
        ("gogu", 2),
        ("gogu", 3),
        ("dorel", 10),
        ("dorel", 20),
        ("dorel", 10),
        ("gigel", 100),
        ("gigel", 100)
      )
    )
    import scala.collection.mutable.{HashSet => MutableHashSet}
    val results = data
      .aggregateByKey(MutableHashSet.empty[Int])(
        (accum, newVal) => {
          accum += newVal
        }, //in each partition, add the new elements to the hashset
        (partX, partY) => {
          partX ++= partY
        } //combine the hashsets
      )
    results.collect.foreach(tpl => println(tpl._1 + " : " + tpl._2.size))

    usingCombine(sc)
  }

  /**
    * 数据分析中，处理key,value的pair数据是极为常见的场景，
    * 从函数层面看，这类操作具有共同特征，即将类型[(K,V)]转成[(K,C)]，这里V，C可以是相同类型也可以是不同类型
    * 这数据处理不是单纯的对pair的value进行map，而是针对不同的key值对原有的value进行联合,因而不仅类型可能不同，元素个数也不同
    *
    * 下面是一个具体使用例子
    *
    * @param sc
    */
  def usingCombine(sc: SparkContext): Unit = {
    val appleone = Fruit("apple", 5)
    val appletwo = Fruit("apple", 8)
    val orangeone = Fruit("orange", 10)
    val orangetwo = Fruit("orange", 20)

    val f2j = (f: Fruit) => f.makeJuice
    val fmc = (c: Juice, f: Fruit) => c.add(f.makeJuice)
    val cmc = (c1: Juice, c2: Juice) => c1.add(c2)

    val fruit = sc.parallelize(List(("apple", appleone), ("orange", orangeone), ("apple", appletwo), ("orange", orangetwo)))
    val juice = fruit.combineByKey(f2j, fmc, cmc)
    juice.foreach(x => {
      println(x._1)
      println(x._2)
    })
  }
}

/** Action
  *
  * reduce(func): 说白了就是聚集，但是传入的函数是两个参数返回一个，这个函数必须满足交换律和结合律，就像monadid的那个函数
  * collect(): 一般在filter或足够小的结果的时候，再用collect封装返回一个数组
  * take(n): 返回前n个elements，这个是driver program返回的
  * saveAsSequenceFile(path): 只能用于key-value对上，保存到txtFile或hdfs上
  */