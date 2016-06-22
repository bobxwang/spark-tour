#!/usr/bin/env bash

#
# submit an app to spark master
#

if [ -z "${SPARK_HOME}" ]; then
    echo "please set SPARK_HOME first..."
    exit 1
fi

# 可以在提交时通过增加–driver-java-options选项来添加CMS(Concurrent Mark and Seeep) GC相关参数

# 在spark和stream如kafka集成时，可以采用receiver方式(在此实现kafka consumer的功能来接收)，也可采用自己周期性的主动查询kafka消息分区
# 的offset来获取，进而去定义在每个batch中需要处理的消息(此方案还处于试验阶段--参考官网说明)

# 如果采用的是receiver，有可能造成数据丢失(Driver或Worker宕机)，除非我们开启WAL(Write Ahead Log)，不过会造成Receiver吞吐量下降。

# below is run on standalone mode
#"${SPARK_HOME}"/bin/spark-submit --name "bbsubmit" \
#--master spark://localhost:7077 \
#--class com.bob.sparktour.BasicTransformation \
#--executor-memory 1024M \
#--conf spark.eventLog.enabled=true \
#--conf spark.eventLog.dir=/Users/bob/Works/code/github/spark-tour \
#/Users/bob/Works/code/github/spark-tour/target/scala-2.11/com.bob.spark.jar


# below is run on yarn mode
# should export HADOOP_CONF_DIR
#"${SPARK_HOME}"/bin/spark-submit --name "bbruninyarn" \
#--class com.bob.sparktour.BasicTransformation \
#--master yarn \
#--deploy-mode cluster \
#--executor-memory 1024M \
#--num-executors 50 \
#/Users/bob/Works/code/github/spark-tour/target/scala-2.11/com.bob.spark.jar


#below is run on mesos
# if the os is mac,the value will be ****.dylib
# should export MESOS_NATIVE_JAVA_LIBRARY=/usr/local/lib/libmesos.so
#"${SPARK_HOME}"/bin/spark-submit --name "bbruninyarn" \
#--class com.bob.sparktour.BasicTransformation \
#--master mesos://192.168.2.22:5050 \
#--conf spark.cores.max=1 \
#--deploy-mode cluster \
#--executor-memory 1024M \
#--num-executors 50 \
#/Users/bob/Works/code/github/spark-tour/target/scala-2.11/com.bob.spark.jar