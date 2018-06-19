package com.azavea

import cats.data.Writer
import org.apache.spark.{SparkConf, SparkContext}

trait Bench {
  lazy val name = this.getClass.getName.split("\\$").last.split("\\.").last.toLowerCase
  lazy val valueReaderName = s"${name}-runValueReader"
  lazy val layerReaderName = s"${name}-runLayerReader"

  def timedCreateLong[T](id: String)(f: => T): (Long, T) = {
    val s = System.currentTimeMillis
    val result = f
    val e = System.currentTimeMillis
    val rt = e - s

    (rt, result)
  }

  def timedCreateWriter[T](id: String)(f: => T): Writer[Long, T] = {
    val (rt, result) = timedCreateLong[T](id)(f)
    Writer(rt, result)
  }

  @transient lazy val conf: SparkConf =
    new SparkConf()
      .setIfMissing("spark.master", "local[*]")
      .setAppName("CogBenchmark")
      .set("spark.serializer", classOf[org.apache.spark.serializer.KryoSerializer].getName)
      .set("spark.kryo.registrator", classOf[geotrellis.spark.io.kryo.KryoRegistrator].getName)

  @transient implicit lazy val sc: SparkContext = new SparkContext(conf)
}
