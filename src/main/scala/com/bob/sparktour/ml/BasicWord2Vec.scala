package com.bob.sparktour.ml

import org.apache.spark.ml.feature.Word2Vec
import org.apache.spark.sql.SparkSession

object BasicWord2Vec {

  def main(args: Array[String]) {
    val spark = SparkSession.builder()
      .appName("basicEstimator")
      .master("local[*]")
      .config("spark.sql.shuffle.partitions", "6")
      .getOrCreate()
    spark.conf.set("spark.executor.memory", "2g")
    val sqlContext = spark.sqlContext

    val documentDF = sqlContext.createDataFrame(Seq(
      "Hi I heard about Spark".split(" "),
      "I wish Java could use case classes".split(" "),
      "Logistic regression models are neat".split(" ")
    ).map(Tuple1.apply)).toDF("text")

    // Learn a mapping from words to Vectors.
    val word2Vec = new Word2Vec()
      .setInputCol("text")
      .setOutputCol("result")
      .setVectorSize(3)
      .setMinCount(0)
    val model = word2Vec.fit(documentDF)
    val result = model.transform(documentDF)
    result.select("result").take(3).foreach(println)
  }
}