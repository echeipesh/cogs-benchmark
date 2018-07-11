package com.azavea.run

import com.azavea._

import geotrellis.raster._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.s3._
import geotrellis.vector.Extent
import cats.effect.IO
import cats.implicits._
import com.amazonaws.services.s3.AmazonS3URI
import spire.syntax.cfor._
import org.apache.spark.SparkContext

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object AvroBench extends Bench {
  def runValueReader(path: String)(name: String, threads: Int = 16, zoom: Option[Int] = None, extent: Option[Extent] = None, targetBands: Option[Seq[Int]] = None)(implicit sc: SparkContext): Logged = {
    val pool = Executors.newFixedThreadPool(threads)
    implicit val ec = ExecutionContext.fromExecutor(pool)

    val s3Path = new AmazonS3URI(path)
    val attributeStore = S3AttributeStore(s3Path.getBucket, s3Path.getKey)

    val zoomLevels = zoom match {
      case z @ Some(_) => z.toList
      case _ => attributeStore.availableZoomLevels(name).toList
    }

    val layersData: List[(LayerId, KeyBounds[SpatialKey], TileLayerMetadata[SpatialKey])] =
      zoomLevels
        .map(LayerId(name, _))
        .map { layerId =>
          val LayerAttributes(_, metadata, _, _) = attributeStore.readLayerAttributes[S3LayerHeader, TileLayerMetadata[SpatialKey], SpatialKey](layerId)
          (layerId, metadata.bounds match { case kb: KeyBounds[SpatialKey] => kb }, metadata)
        }

    val valueReader = new S3ValueReader(attributeStore)

    val res: IO[List[(Long, Long)]] =
      layersData
        .map { case (layerId, kb, metadata) =>
          IO.shift(ec) *> IO {
            val gb @ GridBounds(minCol, minRow, maxCol, maxRow) = extent.map(metadata.mapTransform.extentToBounds).getOrElse(kb.toGridBounds)
            val reader = valueReader.reader[SpatialKey, MultibandTile](layerId)

            val read =
              targetBands match {
                case Some(bands) => (key: SpatialKey) => reader.read(key).subsetBands(bands)
                case None => (key: SpatialKey) => reader.read(key)
              }

            val (time, _) = timedCreateLong {
              cfor(minCol)(_ < maxCol, _ + 1) { col =>
                cfor(minRow)(_ < maxRow, _ + 1) { row =>
                  try {
                    read(SpatialKey(col, row)) // skip all errors
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
          .map { case (time, size) => s"AvroBench.${benchmark}:: ${"%,d".format(time / size)} ms" }
          :+ s"AvroBench.${benchmark}:: total time: ${averageTime} ms"
          :+ s"AvroBench.${benchmark}:: avg number of tiles: ${averageCount}")
          ++ zoomLevels.toVector.map(zoom => s"AvroBench.${benchmark}:: zoom levels: $zoom")
        ).tell
      }
    } yield ()
  }

  def runLayerReader(path: String)(name: String, zoom: Option[Int] = None, extent: Option[Extent] = None, iterations: Option[Int] = None)(implicit sc: SparkContext): Logged = {
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

    val res: IO[List[(Long, Long)]] =
      layersData
        .map { layerId =>
          IO {
            timedCreateLong {
              extent match {
                case Some(ext) =>
                  layerReader
                    .query[SpatialKey, MultibandTile, TileLayerMetadata[SpatialKey]](layerId)
                    .where(Intersects(ext))
                    .result
                    .count()
                case _ =>
                  layerReader
                    .read[SpatialKey, MultibandTile, TileLayerMetadata[SpatialKey]](layerId)
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
          s"AvroBench.runLayerReader:: avg number of tiles: ${averageCount}",
          s"AvroBench.runLayerReader:: ${"%,d".format(averageTime)} ms"
        ) ++ zoom.toVector.map(zoom => s"AvroBench.runLayerReader:: zoom levels: $zoom")).tell
      }
    } yield ()
  }
}
