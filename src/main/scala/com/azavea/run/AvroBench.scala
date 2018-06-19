package com.azavea.run

import com.azavea._
import geotrellis.raster._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.s3._

import cats.effect.IO
import cats.implicits._
import com.amazonaws.services.s3.AmazonS3URI

import spire.syntax.cfor._

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.util.Try

object AvroBench extends Bench {
  def runValueReader(path: String)(name: String, threads: Int = 1, zoom: Option[Int] = None): Logged = {
    val pool = Executors.newFixedThreadPool(threads)
    implicit val ec = ExecutionContext.fromExecutor(pool)

    val s3Path = new AmazonS3URI(path)
    val attributeStore = S3AttributeStore(s3Path.getBucket, s3Path.getKey)

    val zoomLevels = zoom match {
      case z @ Some(_) => z.toList
      case _ => attributeStore.availableZoomLevels(name).toList
    }

    val layersData: List[(LayerId, KeyBounds[SpatialKey])] =
      zoomLevels
        .map(LayerId(name, _))
        .map { layerId =>
          val LayerAttributes(_, metadata, _, _) = attributeStore.readLayerAttributes[S3LayerHeader, TileLayerMetadata[SpatialKey], SpatialKey](layerId)
          (layerId, metadata.bounds match { case kb: KeyBounds[SpatialKey] => kb })
        }

    val valueReader = new S3ValueReader(attributeStore)

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
        Vector(s"AvroBench.runValueReader:: ${"%,d".format(averageTime)}").tell
      }
    } yield ()
  }


  def runLayerReader(path: String)(name: String, zoom: Option[Int] = None, iterations: Option[Int] = None): Logged = {
    val s3Path = new AmazonS3URI(path)
    val attributeStore = S3AttributeStore(s3Path.getBucket, s3Path.getKey)

    val zoomLevels = (zoom, iterations) match {
      case (Some(z), Some(i)) => Array.fill(i)(z).toList
      case (z @ Some(_), _) => z.toList
      case (_, Some(i)) => (0 to i).map { _ => attributeStore.availableZoomLevels(name).toList }.reduce (_ ::: _)
      case _ => attributeStore.availableZoomLevels(name).toList
    }

    val layersData: List[LayerId] = zoomLevels.map(LayerId(name, _))
    val layerReader = new S3LayerReader(attributeStore)

    val res: IO[List[(Long, Unit)]] =
      layersData
        .map { layerId =>
          IO {
            timedCreateLong(layerId.toString) {
              layerReader.read[SpatialKey, MultibandTile, TileLayerMetadata[SpatialKey]](layerId).count(): Unit
            }
          }
        }
        .sequence

    for {
      _ <- {
        val calculated = res.unsafeRunSync()
        val averageTime = calculated.map(_._1).sum / calculated.length
        Vector(s"AvroBench.runLayerReader:: ${"%,d".format(averageTime)}").tell
      }
    } yield ()
  }
}
