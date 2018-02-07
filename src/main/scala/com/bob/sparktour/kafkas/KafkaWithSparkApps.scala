package com.bob.sparktour.kafkas

/**
  * Spark Stream 整合 Kafka
  * 1, 利用 Spark 的 Receiver 和 Kafka 高层次的Api
  *   1.1, 默认配置下会丢失数据,为保证不丢失,可在Spark Stream中使用WAL日志(WAL日志可存储在HDFS上),在失败时从WAL中恢复
  * 2, 利用 Spark 的 Direct API(1.3后引入), 低层次的 Kafka Api
  *   2.1, 定期从Kafka的 topic+partition 中查询最新偏移量,再根据定义的偏移量范围在每个batch里处理数据
  *   2.2, 此方案需要自己更新zk中的偏移量
  */
object KafkaWithSparkApps extends App {

}