#!/usr/bin/env bash

#
# submit an app to spark master
#

if [ -z "${SPARK_HOME}" ]; then
    echo "please set SPARK_HOME first..."
    exit 1
fi


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
#--master mesos://192.168.2.22:5050
#--deploy-mode cluster \
#--executor-memory 1024M \
#--num-executors 50 \
#/Users/bob/Works/code/github/spark-tour/target/scala-2.11/com.bob.spark.jar