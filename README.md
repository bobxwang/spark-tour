# spark optimize
* serialization(default java, Kryo)
<pre>conf.set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
conf.registerKryoClasses(Array(classOf[Myclass],classOf[Uclass]))
</pre>
* IO
	* spark.local.dir 设置为多个磁盘
	* spark.shuffle.consolidateFiles 设置为true,来合并shuffle中间文件
	* spark.streaming.concurrentJobs
- memory
	* spark.memory.fraction
	* spark.memory.storageFraction
* other things
	* 并行度 spark.default.parallelism
	* 数据本地性 
		* PROCESS_LOCAL 数据代码同一JVM进程
		* NODE_LOCAL 数据代码同一节点
		* NO_PREF 数据在任何地方都一样，无本地性偏好
		* RACK_LOCAL 数据代码处于同一机架不同机器
		* ANY 数据网络中未知，即数据代码不在同一个机架上
	* 广播大变量，一般大小大于20KB就值得考虑

# Spark Compare Map/Reduce
* spark使用线程，而MR则是启动新的JVM
* spark在shuffle期间仅将数据放入HDD一次，而MR需要两次
* 传统的MR工作流是一系列job,每个job在迭代时都会将数据存入HDFS，而spark支持DAG及管道操作，这样可以让我们执行复杂的工作时不用将数据持久化

# Spark Checkpoint
一般分布式数据集的容错有**元数据检查点**跟**记录数据的更新**，spark选择了后者。

* Lineage机制

>Lineage记录的是粗颗精确度的特定数据Transformation(map,join等),当这个RDD的部分分区数据丢失时，可通过Lineage获取足够信息来重新运算和恢复丢失的数据分区。

* 窄/宽依赖

> 判断依据是父RDD的每一个分区是不是会被多个子分区所依赖，是则为宽
    

