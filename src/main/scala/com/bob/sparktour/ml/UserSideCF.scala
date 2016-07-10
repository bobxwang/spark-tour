package com.bob.sparktour.ml

import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}


/**
 * Created by bob on 16/7/10.
 */
object UserSideCF {

  def buildModel(rdd: RDD[Rating]): MatrixFactorizationModel = {
    ALS.train(rdd, 10, 20, 0.01)
  }

  def splitData(): Array[RDD[Rating]] = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("UserCF")
    val sc = new SparkContext(sparkConf)
    val lines = sc.textFile("file:///Users/bob/Works/code/github/spark-tour/src/main/resources/ml-100k/u.data")
    val rstings = lines.map(l => {
      val tok = l.split("\t")
      new Rating(tok(0).toInt, tok(1).toInt, tok(2).toDouble)
    })
    rstings.randomSplit(Array(0.6d, 0.4d), 11L)
  }

  def getMSE(ratings: RDD[Rating], model: MatrixFactorizationModel): Double = {
    val userProducts = ratings.map(rating => (rating.user, rating.product))
    val predictions = model.predict(userProducts).map(r => {
      ((r.user, r.product), r.rating)
    })
    val ratesAndPreds = ratings.map(r => {
      ((r.user, r.product), r.rating)
    })
    val joins = ratesAndPreds.join(predictions)
    joins.map(o => o._2._1 - o._2._2).mean
  }

  def main(args: Array[String]) {
    val splits = splitData()
    val model = buildModel(splits(0))
    val MSE = getMSE(splits(0), model) // 训练数据的MSE
    println(s"Mean Squared Error = ${MSE}")
    val MSE1 = getMSE(splits(1), model)
    println(s"Mean Squared Error1 = ${MSE1}") // 测试数据的MSE
  }

}