package com.azavea

import com.azavea.ingest._
import com.azavea.run._

import geotrellis.vector._
import geotrellis.util.LazyLogging
import cats.implicits._

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
    val avroLayer = "avroLayer"
    val cogLayer  = "cogLayer"

    val layerExt = Extent(-1.4499798517584793E7, 4945781.478164045, -1.1975542095495135E7, 6961273.039987572).buffer(200000).some
    val valueExt = Extent(-1.4499798517584793E7, 6413372.421239428, -1.4421527000620775E7, 6961273.039987572).some

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
          _ <- Vector("==========READS BENCHMARK, zoom lvl 13========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 13.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(13), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 13.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(13), extent = valueExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 9========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 9.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(9), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 9.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(9), extent = valueExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 5========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 5.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(5), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 5.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(5), extent = valueExt)
        } yield ()
      case _ =>
        for {
          _ <- Vector("==========READS BENCHMARK, ingest========").tell
          _ <- IngestAvro.ingest(inputPath, avroPath)(avroLayer)
          _ <- IngestCOG.ingest(inputPath, cogPath)(cogLayer)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 13========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 13.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(13), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 13.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(13), extent = valueExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 9========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 9.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(9), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 9.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(9), extent = valueExt)
          _ <- Vector("==========READS BENCHMARK, zoom lvl 5========").tell
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer, 5.some, extent = layerExt)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, List(5), extent = layerExt)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, zoom = 5.some, extent = valueExt)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, List(5), extent = valueExt)
        } yield ()
    }

    val logs = result.written
    logs.foreach(println)
  }
}
