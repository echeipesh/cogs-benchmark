package com.azavea

import cats.data.Writer
import org.apache.spark.{SparkConf, SparkContext}

trait Bench {
  lazy val name = this.getClass.getName.split("\\$").last.split("\\.").last.toLowerCase
  lazy val valueReaderName = s"${name}-runValueReader"
  lazy val layerReaderName = s"${name}-runLayerReader"

  def timedCreateLong[T](f: => T): (Long, T) = {
    val s = System.currentTimeMillis
    val result = f
    val e = System.currentTimeMillis
    val rt = e - s

    (rt, result)
  }

  def timedCreateWriter[T](f: => T): Writer[Long, T] = {
    val (rt, result) = timedCreateLong[T](f)
    Writer(rt, result)
  }
}
