package com.azavea

import org.apache.spark.{SparkConf, SparkContext}

trait Spark {
  @transient lazy val conf: SparkConf =
    new SparkConf()
      .setIfMissing("spark.master", "local[*]")
      .setAppName("CogBenchmark")
      .set("spark.serializer", classOf[org.apache.spark.serializer.KryoSerializer].getName)
      .set("spark.kryo.registrator", classOf[geotrellis.spark.io.kryo.KryoRegistrator].getName)

  @transient implicit lazy val sc: SparkContext = new SparkContext(conf)
}
