package com.bob.sparktour

import org.apache.spark.{SparkConf, SparkContext}

case class Juice(volumn: Int) {
  def add(juice: Juice): Juice = Juice(volumn + juice.volumn)
}

case class Fruit(king: String, weight: Int) {
  def makeJuice: Juice = Juice(weight * 100)
}

object BasicTransformation {

  def main(args: Array[String]) {

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

    //    System.setProperty("spark.executor.memory", "128M")

    val x = sc.parallelize(Array("Joseph", "Jimmy", "Tina", "Thomas", "James", "Cory", "Christine", "Jackeline", "Juan"), 3)
    x.collect.foreach(println)

    // mapPartitions跟map类似，不过映射函数的参数由RDD中的每一个元素变成了RDD中每一个分区的迭代器，如果在映射的过程中需频繁创建额外对象，使用此将比map高效，比如将RDD的所有数据通过JDBC连接写入数据库，如果使用map，那将为每一个元素都创建一个connection，而如果使用mapPartitions，那么就是每一个分区一个connection
    val l = sc.parallelize(List(1, 2, 3, 4, 5), 4)
    l.mapPartitions(x => List(x.sum).toIterator)
    l.mapPartitions(x => x.map(y => y + 1)) // 可以看出跟map的不同，是独立的在RDD的每一个分块上运行
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

    val ll = List(1, 2, 3, 5, 6, 8, 3, 4, 2, 8)
    ll.groupBy(x => x >= 4)

    val rd = sc.makeRDD(1 to 5, 2)
    println(rd.fold(0)(_ + _))

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