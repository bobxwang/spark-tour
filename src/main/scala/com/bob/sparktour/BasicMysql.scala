package com.bob.sparktour

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkContext, SparkConf}

object BasicMysql {

  def main(args: Array[String]) {

    val url = "jdbc:mysql://192.168.2.200:3306/51banka"
    val prop = new java.util.Properties
    prop.setProperty("user", "root")
    prop.setProperty("password", "zufangbao69fc")

    val sparkConf: SparkConf = new SparkConf().setMaster("local").setAppName("ShiroTutorial")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    val areas = sqlContext.read.jdbc(url, "T_Area_Full", Array("CityCode in ('1101','1201')"), prop)
    val gp = areas.groupBy("CityCode")
    val sorted = gp.count.orderBy("count")
    sorted.show(10)
    sorted.rdd.saveAsTextFile("/Users/bob/Desktop/flight_top")
  }
}