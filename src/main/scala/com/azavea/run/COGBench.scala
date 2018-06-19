package com.azavea.run

import com.azavea.Bench
import geotrellis.raster._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.s3._
import geotrellis.spark.io.s3.cog._
import cats.effect.IO
import cats.implicits._
import com.amazonaws.services.s3.AmazonS3URI
import org.apache.spark._
import spire.syntax.cfor._
import java.util.concurrent.Executors

import geotrellis.spark.io.cog.COGLayerStorageMetadata

import scala.concurrent.ExecutionContext

object COGBench extends Bench {
  def availableZoomLevels(path: String)(name: String): List[Int] = {
    val s3Path = new AmazonS3URI(path)
    val attributeStore = S3AttributeStore(s3Path.getBucket, s3Path.getKey)
    attributeStore.availableZoomLevels(name).toList
  }

  def runValueReader(path: String)(name: String, zoomLevels: List[Int], threads: Int = 1)(implicit sc: SparkContext): Unit = {
    val pool = Executors.newFixedThreadPool(threads)
    implicit val ec = ExecutionContext.fromExecutor(pool)

    val s3Path = new AmazonS3URI(path)
    val attributeStore = S3AttributeStore(s3Path.getBucket, s3Path.getKey)

    val COGLayerStorageMetadata(cogLayerMetadata, _) = attributeStore.readMetadata[COGLayerStorageMetadata[SpatialKey]](LayerId(name, 0))

    val layersData: List[(LayerId, KeyBounds[SpatialKey])] =
      zoomLevels
        .map(LayerId(name, _))
        .map { layerId =>
          val metadata = cogLayerMetadata.tileLayerMetadata(layerId.zoom)
          (layerId, metadata.bounds match { case kb: KeyBounds[SpatialKey] => kb })
        }

    val valueReader = new S3COGValueReader(attributeStore)

    val res: IO[List[(Long, Unit)]] =
      layersData
        .map { case (layerId, KeyBounds(SpatialKey(minCol, minRow), SpatialKey(maxCol, maxRow))) =>
          IO.shift(ec) *> IO {
            val reader = valueReader.reader[SpatialKey, Tile](layerId)

            timedCreateLong("layerId") {
              cfor(minCol)(_ < maxCol, _ + 1) { col =>
                cfor(minRow)(_ < maxRow, _ + 1) { row =>
                  reader.read(SpatialKey(col, row))
                }
              }
            }
          }
        }
        .parSequence

    val calculated = res.unsafeRunSync()
    pool.shutdown()

    val averageTime = calculated.map(_._1).sum / calculated.length
    val t = "%,d".format(averageTime)
    logger.info(s"runValueReader:: $t")
  }


  def runLayerReader(path: String)(name: String, threads: Int = 1, zoomLevels: List[Int])(implicit sc: SparkContext): Unit = {
    val pool = Executors.newFixedThreadPool(threads)
    implicit val ec = ExecutionContext.fromExecutor(pool)

    val s3Path = new AmazonS3URI(path)
    val attributeStore = S3AttributeStore(s3Path.getBucket, s3Path.getKey)

    val layersData: List[LayerId] = zoomLevels.map(LayerId(name, _))
    val layerReader = new S3COGLayerReader(attributeStore)

    val res: IO[List[(Long, Unit)]] =
      layersData
        .map { layerId =>
          IO.shift(ec) *> IO {
            timedCreateLong("layerId") {
              layerReader.read[SpatialKey, Tile](layerId).count(): Unit
            }
          }
        }
        .parSequence

    val calculated = res.unsafeRunSync()
    pool.shutdown()

    val averageTime = calculated.map(_._1).sum / calculated.length
    val t = "%,d".format(averageTime)
    logger.info(s"runValueReader:: $t")
  }
}
