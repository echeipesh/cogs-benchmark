package com.azavea

import geotrellis.util.LazyLogging

import org.apache.spark.{SparkConf, SparkContext}

trait Bench extends LazyLogging {
  def timedCreateLong[T](id: String)(f: => T): (Long, T) = {
    val s = System.currentTimeMillis
    val result = f
    val e = System.currentTimeMillis
    val rt = e - s

    (rt, result)
  }

  @transient lazy val conf =
    new SparkConf()
      .setIfMissing("spark.master", "local[*]")
      .setAppName("IngestAvro")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.kryo.registrator", "geotrellis.spark.io.kryo.KryoRegistrator")

  @transient lazy val sparkContext = new SparkContext(conf)
}
