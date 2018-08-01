package com.azavea.run

import com.azavea._

import geotrellis.raster._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.cog.COGLayerStorageMetadata
import geotrellis.spark.io.s3._
import geotrellis.spark.io.s3.cog._
import geotrellis.vector.Extent
import cats.effect.IO
import cats.implicits._
import com.amazonaws.services.s3.AmazonS3URI
import spire.syntax.cfor._
import org.apache.spark.SparkContext

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object COGBench extends Bench {
  def availableZoomLevels(path: String)(name: String): List[Int] = {
    val s3Path = new AmazonS3URI(path)
    val attributeStore = S3AttributeStore(s3Path.getBucket, s3Path.getKey)
    attributeStore.availableZoomLevels(name).toList
  }

  def runValueReader(path: String)(name: String, zoomLevels: List[Int], extent: Option[Extent] = None, threads: Int = 16, targetBands: Option[Seq[Int]] = None)(implicit sc: SparkContext): Logged = {
    val pool = Executors.newFixedThreadPool(threads)
    implicit val ec = ExecutionContext.fromExecutor(pool)

    val s3Path = new AmazonS3URI(path)
    val attributeStore = S3AttributeStore(s3Path.getBucket, s3Path.getKey)

    val COGLayerStorageMetadata(cogLayerMetadata, _) = attributeStore.readMetadata[COGLayerStorageMetadata[SpatialKey]](LayerId(name, 0))

    val layersData: List[(LayerId, KeyBounds[SpatialKey], TileLayerMetadata[SpatialKey])] =
      zoomLevels
        .map(LayerId(name, _))
        .map { layerId =>
          val metadata = cogLayerMetadata.tileLayerMetadata(layerId.zoom)
          (layerId, metadata.bounds match { case kb: KeyBounds[SpatialKey] => kb }, metadata)
        }

    val valueReader = new S3COGValueReader(attributeStore)

    val res: IO[List[(Long, Long)]] =
      layersData
        .map { case (layerId, kb, metadata) =>
          IO.shift(ec) *> IO {
            val gb @ GridBounds(minCol, minRow, maxCol, maxRow) = extent.map(metadata.mapTransform.extentToBounds).getOrElse(kb.toGridBounds)
            val reader = valueReader.reader[SpatialKey, MultibandTile](layerId)

            val read =
              targetBands match {
                case Some(bands) => (key: SpatialKey) => reader.readSubsetBands(key, bands)
                case None => (key: SpatialKey) => reader.read(key)
              }

            val (time, _) = timedCreateLong {
              cfor(minCol)(_ < maxCol, _ + 1) { col =>
                cfor(minRow)(_ < maxRow, _ + 1) { row =>
                  try {
                    read(SpatialKey(col, row))
                  } catch { case _ => }
                }
              }
            }

            (time, gb.size)
          }
        }
        .parSequence

    val benchmark =
      targetBands match {
        case Some(_) => "runValueReaderBandSubset"
        case None => "runValueReader"
      }

    for {
      _ <- {
        val calculated = res.unsafeRunSync()
        pool.shutdown()
        val averageTime = calculated.map(_._1).sum / calculated.length
        val averageCount = calculated.map(_._2).sum / calculated.length

        ((calculated
          .toVector
          .map { case (time, size) => s"COGBench.${benchmark}:: ${"%,d".format(time / size)} ms" }
          :+ s"COGBench.${benchmark}:: total time: ${averageTime} ms"
          :+ s"COGBench.${benchmark}:: avg number of tiles: ${averageCount}")
          ++ zoomLevels.toVector.map(zoom => s"COGBench.${benchmark}:: zoom levels: $zoom")
        ).tell
      }
    } yield ()
  }


  def runLayerReader(path: String)(name: String, zoomLevels: List[Int], extent: Option[Extent] = None, targetBands: Option[Seq[Int]] = None)(implicit sc: SparkContext): Logged = {
    val s3Path = new AmazonS3URI(path)
    val attributeStore = S3AttributeStore(s3Path.getBucket, s3Path.getKey)

    val layersData: List[LayerId] = zoomLevels.map(LayerId(name, _))
    val layerReader = new S3COGLayerReader(attributeStore)

    val res: IO[List[(Long, Long)]] =
      layersData
        .map { layerId =>
          IO {
            timedCreateLong {
              (extent, targetBands) match {
                case (Some(ext), _) =>
                  layerReader
                    .query[SpatialKey, MultibandTile](layerId)
                    .where(Intersects(ext))
                    .result
                    .count()

                case (Some(ext), Some(bands)) =>
                  layerReader
                    .querySubsetBands[SpatialKey](layerId, bands)
                    .where(Intersects(ext))
                    .result
                    .count()

                case (None, _) =>
                  layerReader
                    .read[SpatialKey, MultibandTile](layerId)
                    .count()

                case (None, Some(bands)) =>
                  layerReader
                    .readSubsetBands[SpatialKey](layerId, bands)
                    .count()
              }
            }
          }
        }
        .sequence

    for {
      _ <- {
        val calculated = res.unsafeRunSync()
        val averageTime = calculated.map(_._1).sum / calculated.length
        val averageCount = calculated.map(_._2).sum / calculated.length
        (Vector(
          s"COGBench.runLayerReader:: avg number of tiles: ${averageCount}",
          s"COGBench.runLayerReader:: ${"%,d".format(averageTime)} ms"
        ) ++ zoomLevels.toVector.map(zoom => s"COGBench.runLayerReader:: zoom levels: $zoom")).tell
      }
    } yield ()
  }
}
