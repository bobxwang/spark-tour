#!/usr/bin/env bash

#
# submit an app to spark master
#

if [ -z "${SPARK_HOME}" ]; then
    echo "please set SPARK_HOME first..."
    exit 1
fi

"${SPARK_HOME}"/bin/spark-submit --name "bbsubmit" \
--master spark://localhost:7077 \
--class com.bob.sparktour.BasicTransformation \
--executor-memory 1024M \
--conf spark.eventLog.enabled=true \
--conf spark.eventLog.dir=/Users/bob/Works/code/github/spark-tour \
/Users/bob/Works/code/github/spark-tour/target/scala-2.11/com.bob.spark.jar