package com.azavea

import com.azavea.ingest._
import com.azavea.run._
import geotrellis.vector._
import geotrellis.util.LazyLogging
import cats.implicits._
import geotrellis.raster.io.geotiff.compression.DeflateCompression

object Main extends Spark with LazyLogging {
  def main(args: Array[String]): Unit = {

    /**
      * 13 zoom level, nlcd layer:
      * KeyBounds(SpatialKey(1132,2673),SpatialKey(2647,3584))
      *
      * 2647 - 1132 - 1500 = 15
      * 3584 - 2674 - 800 = 100
      * 1500 elems - test dataset
      */

    val inputPath = "s3://gt-rasters/nlcd/2011/tiles"
    val avroPath  = "s3://geotrellis-test/daunnc/cog-benchmark/avro-3"
    val cogPath   = "s3://geotrellis-test/daunnc/cog-benchmark/cog-3"
    val cogCPath  = "s3://geotrellis-test/daunnc/cog-benchmark/cogc-3"
    val avroLayer = "avroLayer"
    val cogLayer  = "cogLayer"

    val avroBandLayer = "avro-multiband-band-layer"
    val avroPixelLayer = "avro-multiband-pixel-layer"

    val cogBandLayer = "cog-multiband-band-layer"
    val cogPixelLayer = "cog-multiband-pixel-layer"

    val layerExt = Extent(-1.4499798517584793E7, 4945781.478164045, -1.1975542095495135E7, 6961273.039987572).buffer(1000000).some
    val valueExt = Extent(-1.4499798517584793E7, 6413372.421239428, -1.4421527000620775E7, 6961273.039987572).some

    val multibandValueExt = Extent(-7360000, 2049000, -7350000, 2053000).some

    val result = args.headOption match {
      case Some("avro-ingest") =>
        for {
          _ <- IngestAvro.ingest(inputPath, avroPath)(avroLayer)
        } yield ()
      case Some("avro-value-read") =>
        for {
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 13.some, extent = valueExt)
        } yield ()
      case Some("avro-layer-read") =>
        for {
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, zoom = 13.some, extent = layerExt)
        } yield ()
      case Some("avro-reads") =>
        for {
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, zoom = 13.some, extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 13.some, extent = valueExt)
        } yield ()
      case Some("cog-ingest") =>
        for {
          _ <- IngestCOG.ingest(inputPath, cogPath)(cogLayer)
        } yield ()
      case Some("cog-ingest-compression") =>
        for {
          _ <- IngestCOG.ingest(inputPath, cogCPath)(cogLayer, DeflateCompression)
        } yield ()
      case Some("cog-value-read") =>
        for {
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(13), extent = valueExt)
        } yield ()
      case Some("cog-layer-read") =>
        for {
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(13), extent = layerExt)
        } yield ()
      case Some("cog-reads") =>
        for {
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(13), extent = layerExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(13), extent = valueExt)
        } yield ()
      case Some("reads-13") =>
        for {
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 13.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(13), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 13.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(13), extent = valueExt)
        } yield ()
      case Some("reads-9") =>
        for {
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 9.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(9), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 9.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(9), extent = valueExt)
        } yield ()
      case Some("reads-5") =>
        for {
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 5.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(5), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 5.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(5), extent = valueExt)
        } yield ()
      case Some("reads") =>
        for {
          _ <- Vector("==============================================").tell
          _ <- Vector(s"InputPath: $inputPath").tell
          _ <- Vector(s"AvroPath: $avroPath").tell
          _ <- Vector(s"COGPath: $cogPath").tell
          _ <- Vector(s"AvroLayer: $avroLayer").tell
          _ <- Vector(s"COGLayer: $cogLayer").tell
          _ <- Vector(s"ValueReadersExt: $valueExt").tell
          _ <- Vector(s"LayerReadersExt: $layerExt").tell
          _ <- Vector("==========READS BENCHMARK, zoom lvl 13 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 13.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(13), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 13.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(13), extent = valueExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 10 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 10.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(10), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 10.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(10), extent = valueExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 9 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 9.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(9), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 9.some, extent = layerExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(9), extent = layerExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 9========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 9.some)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(9))
          _ <- Vector("==========READS BENCHMARK, zoom lvl 8 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 8.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(8), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 8.some, extent = layerExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(8), extent = layerExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 8========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 8.some)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(8))
          _ <- Vector("==========READS BENCHMARK, zoom lvl 5 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 5.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(5), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 5.some, extent = layerExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(5), extent = layerExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 5========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 5.some)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(5))
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 5.some)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(5))
        } yield ()
      case Some("readsc") =>
        for {
          _ <- Vector("==============================================").tell
          _ <- Vector(s"InputPath: $inputPath").tell
          _ <- Vector(s"AvroPath: $avroPath").tell
          _ <- Vector(s"COGCompressedPath: $cogCPath").tell
          _ <- Vector(s"AvroLayer: $avroLayer").tell
          _ <- Vector(s"COGLayer: $cogLayer").tell
          _ <- Vector(s"ValueReadersExt: $valueExt").tell
          _ <- Vector(s"LayerReadersExt: $layerExt").tell
          _ <- Vector("==========READSC BENCHMARK, zoom lvl 13 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 13.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(13), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 13.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogCPath)(cogLayer, List(13), extent = valueExt)
          _ <- Vector("==========READSC BENCHMARK, zoom lvl 10 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 10.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(10), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 10.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogCPath)(cogLayer, List(10), extent = valueExt)
          _ <- Vector("==========READSC BENCHMARK, zoom lvl 10========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 10.some)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(10))
          _ <- Vector("==========READSC BENCHMARK, zoom lvl 9 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 9.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(9), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 9.some, extent = layerExt)
          _ <- COGBench.runValueReader(cogCPath)(cogLayer, List(9), extent = layerExt)
          _ <- Vector("==========READSC BENCHMARK, zoom lvl 9========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 9.some)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(9))
          _ <- Vector("==========READSC BENCHMARK, zoom lvl 8 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 8.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(8), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 8.some, extent = layerExt)
          _ <- COGBench.runValueReader(cogCPath)(cogLayer, List(8), extent = layerExt)
          _ <- Vector("==========READSC BENCHMARK, zoom lvl 8========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 8.some)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(8))
          _ <- Vector("==========READSC BENCHMARK, zoom lvl 5 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 5.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(5), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 5.some, extent = layerExt)
          _ <- COGBench.runValueReader(cogCPath)(cogLayer, List(5), extent = layerExt)
          _ <- Vector("==========READSC BENCHMARK, zoom lvl 5========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 5.some)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(5))
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 5.some)
          _ <- COGBench.runValueReader(cogCPath)(cogLayer, List(5))
        } yield ()
      case Some("compressed") =>
        for {
          _ <- Vector("==============================================").tell
          _ <- Vector(s"InputPath: $inputPath").tell
          _ <- Vector(s"AvroPath: $avroPath").tell
          _ <- Vector(s"COGCompressedPath: $cogCPath").tell
          _ <- Vector(s"AvroLayer: $avroLayer").tell
          _ <- Vector(s"COGLayer: $cogLayer").tell
          _ <- Vector(s"ValueReadersExt: $valueExt").tell
          _ <- Vector(s"LayerReadersExt: $layerExt").tell
          _ <- Vector("==========READS BENCHMARK, ingest========").tell
          _ <- IngestAvro.ingest(inputPath, avroPath)(avroLayer)
          _ <- IngestCOG.ingest(inputPath, cogCPath)(cogLayer)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 13 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 13.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(13), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 13.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogCPath)(cogLayer, List(13), extent = valueExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 10 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 10.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(10), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 10.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogCPath)(cogLayer, List(10), extent = valueExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 9 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 9.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(9), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 9.some, extent = layerExt)
          _ <- COGBench.runValueReader(cogCPath)(cogLayer, List(9), extent = layerExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 9========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 9.some)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(9))
          _ <- Vector("==========READS BENCHMARK, zoom lvl 5 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 5.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(5), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 5.some, extent = layerExt)
          _ <- COGBench.runValueReader(cogCPath)(cogLayer, List(5), extent = layerExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 5========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 5.some)
          _ <- COGBench.runLayerReader(cogCPath)(cogLayer, List(5))
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 5.some)
          _ <- COGBench.runValueReader(cogCPath)(cogLayer, List(5))
        } yield ()
      case Some("read-band-interleave") =>
        for {
          _ <- Vector("==============================================").tell
          _ <- Vector(s"AvroPath: $avroPath").tell
          _ <- Vector(s"AvroBandLayer: $avroBandLayer").tell
          _ <- Vector(s"COGPath: $cogPath").tell
          _ <- Vector(s"COGBandLayer: $cogBandLayer").tell
          _ <- Vector("==========READ BAND INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 13, Bands Seq(0)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroBandLayer, zoom = 13.some, extent = multibandValueExt, targetBands = Seq(0).some)
          _ <- COGBench.runValueReader(cogPath)(cogBandLayer, List(13), extent = multibandValueExt, targetBands = Seq(0).some)
          _ <- Vector("==========READ BAND INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 13, Bands Seq(0, 1, 3)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroBandLayer, zoom = 13.some, extent = multibandValueExt, targetBands = Seq(0, 1, 3).some)
          _ <- COGBench.runValueReader(cogPath)(cogBandLayer, List(13), extent = multibandValueExt, targetBands = Seq(0, 1, 3).some)
          _ <- Vector("==========READ BAND INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 13, Bands Seq(0, 1, 3, 2, 4, 8, 7, 6, 5)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroBandLayer, zoom = 13.some, extent = multibandValueExt, targetBands = Seq(0, 1, 3, 2, 4, 8, 7, 6, 5).some)
          _ <- COGBench.runValueReader(cogPath)(cogBandLayer, List(13), extent = multibandValueExt, targetBands = Seq(0, 1, 3, 2, 4, 8, 7, 6, 5).some)

          _ <- Vector("==========READ BAND INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 9, Bands Seq(0)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroBandLayer, zoom = 9.some, targetBands = Seq(0).some)
          _ <- COGBench.runValueReader(cogPath)(cogBandLayer, List(9), targetBands = Seq(0).some)
          _ <- Vector("==========READ BAND INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 9, Bands Seq(0, 1, 3)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroBandLayer, zoom = 9.some, targetBands = Seq(0, 1, 3).some)
          _ <- COGBench.runValueReader(cogPath)(cogBandLayer, List(9), targetBands = Seq(0, 1, 3).some)
          _ <- Vector("==========READ BAND INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 9, Bands Seq(0, 1, 3, 2, 4, 8, 7, 6, 5)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroBandLayer, zoom = 9.some, targetBands = Seq(0, 1, 3, 2, 4, 8, 7, 6, 5).some)
          _ <- COGBench.runValueReader(cogPath)(cogBandLayer, List(9), targetBands = Seq(0, 1, 3, 2, 4, 8, 7, 6, 5).some)

          _ <- Vector("==========READ BAND INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 7, Bands Seq(0)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroBandLayer, zoom = 7.some, targetBands = Seq(0).some)
          _ <- COGBench.runValueReader(cogPath)(cogBandLayer, List(7), targetBands = Seq(0).some)
          _ <- Vector("==========READ BAND INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 7, Bands Seq(0, 1, 3)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroBandLayer, zoom = 7.some, targetBands = Seq(0, 1, 3).some)
          _ <- COGBench.runValueReader(cogPath)(cogBandLayer, List(7), targetBands = Seq(0, 1, 3).some)
          _ <- Vector("==========READ BAND INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 7, Bands Seq(0, 1, 3, 2, 4, 8, 7, 6, 5)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroBandLayer, zoom = 7.some, targetBands = Seq(0, 1, 3, 2, 4, 8, 7, 6, 5).some)
          _ <- COGBench.runValueReader(cogPath)(cogBandLayer, List(7), targetBands = Seq(0, 1, 3, 2, 4, 8, 7, 6, 5).some)
        } yield ()
      case Some("read-pixel-interleave") =>
        for {
          _ <- Vector("==============================================").tell
          _ <- Vector(s"AvroPath: $avroPath").tell
          _ <- Vector(s"AvroPixelLayer: $avroPixelLayer").tell
          _ <- Vector(s"COGPath: $cogPath").tell
          _ <- Vector(s"COGPixelLayer: $cogPixelLayer").tell
          _ <- Vector("==========READ PIXEL INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 13, Bands Seq(0)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroPixelLayer, zoom = 13.some, extent = multibandValueExt, targetBands = Seq(0).some)
          _ <- COGBench.runValueReader(cogPath)(cogPixelLayer, List(13), extent = multibandValueExt, targetBands = Seq(0).some)
          _ <- Vector("==========READ PIXEL INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 13, Bands Seq(0, 1, 3)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroPixelLayer, zoom = 13.some, extent = multibandValueExt, targetBands = Seq(0, 1, 3).some)
          _ <- COGBench.runValueReader(cogPath)(cogPixelLayer, List(13), extent = multibandValueExt, targetBands = Seq(0, 1, 3).some)
          _ <- Vector("==========READ PIXEL INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 13, Bands Seq(0, 1, 3, 2, 4, 8, 7, 6, 5)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroPixelLayer, zoom = 13.some, extent = multibandValueExt, targetBands = Seq(0, 1, 3, 2, 4, 8, 7, 6, 5).some)
          _ <- COGBench.runValueReader(cogPath)(cogPixelLayer, List(13), extent = multibandValueExt, targetBands = Seq(0, 1, 3, 2, 4, 8, 7, 6, 5).some)

          _ <- Vector("==========READ PIXEL INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 9, Bands Seq(0)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroPixelLayer, zoom = 9.some, targetBands = Seq(0).some)
          _ <- COGBench.runValueReader(cogPath)(cogPixelLayer, List(9), targetBands = Seq(0).some)
          _ <- Vector("==========READ PIXEL INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 9, Bands Seq(0, 1, 3)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroPixelLayer, zoom = 9.some, targetBands = Seq(0, 1, 3).some)
          _ <- COGBench.runValueReader(cogPath)(cogPixelLayer, List(9), targetBands = Seq(0, 1, 3).some)
          _ <- Vector("==========READ PIXEL INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 9, Bands Seq(0, 1, 3, 2, 4, 8, 7, 6, 5)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroPixelLayer, zoom = 9.some, targetBands = Seq(0, 1, 3, 2, 4, 8, 7, 6, 5).some)
          _ <- COGBench.runValueReader(cogPath)(cogPixelLayer, List(9), targetBands = Seq(0, 1, 3, 2, 4, 8, 7, 6, 5).some)

          _ <- Vector("==========READ PIXEL INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 7, Bands Seq(0)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroPixelLayer, zoom = 7.some, targetBands = Seq(0).some)
          _ <- COGBench.runValueReader(cogPath)(cogPixelLayer, List(7), targetBands = Seq(0).some)
          _ <- Vector("==========READ PIXEL INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 7, Bands Seq(0, 1, 3)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroPixelLayer, zoom = 7.some, targetBands = Seq(0, 1, 3).some)
          _ <- COGBench.runValueReader(cogPath)(cogPixelLayer, List(7), targetBands = Seq(0, 1, 3).some)
          _ <- Vector("==========READ PIXEL INTERLEAVE TILE BANDS BENCHMARK, zoom lvl 7, Bands Seq(0, 1, 3, 2, 4, 8, 7, 6, 5)========").tell
          _ <- AvroBench.runValueReader(avroPath)(avroPixelLayer, zoom = 7.some, targetBands = Seq(0, 1, 3, 2, 4, 8, 7, 6, 5).some)
          _ <- COGBench.runValueReader(cogPath)(cogPixelLayer, List(7), targetBands = Seq(0, 1, 3, 2, 4, 8, 7, 6, 5).some)
        } yield ()
      case _ =>
        for {
          _ <- Vector("==============================================").tell
          _ <- Vector(s"InputPath: $inputPath").tell
          _ <- Vector(s"AvroPath: $avroPath").tell
          _ <- Vector(s"COGPath: $cogPath").tell
          _ <- Vector(s"AvroLayer: $avroLayer").tell
          _ <- Vector(s"COGLayer: $cogLayer").tell
          _ <- Vector(s"ValueReadersExt: $valueExt").tell
          _ <- Vector(s"LayerReadersExt: $layerExt").tell
          _ <- Vector("==========READS BENCHMARK, ingest========").tell
          _ <- IngestAvro.ingest(inputPath, avroPath)(avroLayer)
          _ <- IngestCOG.ingest(inputPath, cogPath)(cogLayer)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 13 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 13.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(13), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 13.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(13), extent = valueExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 9 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 9.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(9), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 9.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(9), extent = valueExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 9========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 9.some)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(9))
          _ <- Vector("==========READS BENCHMARK, zoom lvl 5 (bounded by extent)========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 5.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(5), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 5.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(5), extent = valueExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 5========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 5.some)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(5))
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 5.some)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(5))
        } yield ()
    }

    val logs = result.written
    logs.foreach(println)
  }
}
