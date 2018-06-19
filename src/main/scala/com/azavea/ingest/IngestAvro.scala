package com.azavea.ingest

import com.azavea._

import geotrellis.raster._
import geotrellis.raster.resample._
import geotrellis.proj4._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.s3._
import geotrellis.spark.io.index._
import geotrellis.spark.pyramid._
import geotrellis.spark.tiling._
import geotrellis.vector._

import cats.Id
import cats.data.WriterT
import cats.implicits._
import com.amazonaws.services.s3.AmazonS3URI
import org.apache.spark.rdd._


object IngestAvro extends Bench {
  def ingest(inputPath: String, outputPath: String)(name: String): Logged = {
    val res = timedCreateWriter(name) {
      val s3InputPath = new AmazonS3URI(inputPath)
      val s3OutputPath = new AmazonS3URI(outputPath)

      val inputRdd: RDD[(ProjectedExtent, MultibandTile)] =
        S3GeoTiffRDD.spatialMultiband(s3InputPath.getBucket, s3InputPath.getKey)

      val (_, rasterMetaData) = TileLayerMetadata.fromRdd(inputRdd, FloatingLayoutScheme(512))

      val tiled: RDD[(SpatialKey, MultibandTile)] =
        inputRdd
          .tileToLayout(rasterMetaData.cellType, rasterMetaData.layout, Bilinear)
      // .repartition(100)

      val layoutScheme = ZoomedLayoutScheme(WebMercator, tileSize = 256)

      val (zoom, reprojected): (Int, RDD[(SpatialKey, MultibandTile)] with Metadata[TileLayerMetadata[SpatialKey]]) =
        MultibandTileLayerRDD(tiled, rasterMetaData).reproject(WebMercator, layoutScheme, Bilinear)

      val attributeStore = S3AttributeStore(s3OutputPath.getBucket, s3OutputPath.getKey)
      val writer = S3LayerWriter(attributeStore)

      Pyramid.upLevels(reprojected, layoutScheme, zoom, Bilinear) { (rdd, z) =>
        val layerId = LayerId(name, z)
        // If the layer exists already, delete it out before writing
        // if (attributeStore.layerExists(layerId)) S3LayerDeleter(attributeStore).delete(layerId)
        writer.write(layerId, rdd, ZCurveKeyIndexMethod)
      }

      ()
    }

    res.mapWritten(time => Vector(s"IngestAvro.ingest:: ${"%,d".format(time)}"))
  }
}
