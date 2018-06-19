package com.azavea.run

import com.azavea._

import geotrellis.raster._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.cog.COGLayerStorageMetadata
import geotrellis.spark.io.s3._
import geotrellis.spark.io.s3.cog._

import cats.effect.IO
import cats.implicits._
import com.amazonaws.services.s3.AmazonS3URI
import spire.syntax.cfor._

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.util.Try

object COGBench extends Bench {
  def availableZoomLevels(path: String)(name: String): List[Int] = {
    val s3Path = new AmazonS3URI(path)
    val attributeStore = S3AttributeStore(s3Path.getBucket, s3Path.getKey)
    attributeStore.availableZoomLevels(name).toList
  }

  def runValueReader(path: String)(name: String, zoomLevels: List[Int], threads: Int = 1): Logged = {
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
            val reader = valueReader.reader[SpatialKey, MultibandTile](layerId)

            timedCreateLong("layerId") {
              cfor(minCol)(_ < maxCol, _ + 1) { col =>
                cfor(minRow)(_ < maxRow, _ + 1) { row =>
                  Try(reader.read(SpatialKey(col, row))) // skip all errors
                }
              }
            }
          }
        }
        .parSequence

    for {
      _ <- {
        val calculated = res.unsafeRunSync()
        pool.shutdown()
        val averageTime = calculated.map(_._1).sum / calculated.length
        Vector(s"COGBench.runValueReader:: ${"%,d".format(averageTime)}").tell
      }
    } yield ()
  }


  def runLayerReader(path: String)(name: String, zoomLevels: List[Int]): Logged = {
    val s3Path = new AmazonS3URI(path)
    val attributeStore = S3AttributeStore(s3Path.getBucket, s3Path.getKey)

    val layersData: List[LayerId] = zoomLevels.map(LayerId(name, _))
    val layerReader = new S3COGLayerReader(attributeStore)

    val res: IO[List[(Long, Unit)]] =
      layersData
        .map { layerId =>
          IO {
            timedCreateLong(layerId.toString) {
              layerReader.read[SpatialKey, MultibandTile](layerId).count(): Unit
            }
          }
        }
        .sequence

    for {
      _ <- {
        val calculated = res.unsafeRunSync()
        val averageTime = calculated.map(_._1).sum / calculated.length
        Vector(s"COGBench.runLayerReader:: ${"%,d".format(averageTime)}").tell
      }
    } yield ()
  }
}
