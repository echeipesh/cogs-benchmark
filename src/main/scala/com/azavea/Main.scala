package com.azavea

import com.azavea.ingest._
import com.azavea.run._

import cats.implicits._
import geotrellis.util.LazyLogging

object Main extends LazyLogging {
  def main(args: Array[String]): Unit = {
    val inputPath = "s3://gt-rasters/nlcd/2011/tiles"
    val avroPath  = "s3://geotrellis-test/daunnc/cog-benchmark/avro"
    val cogPath   = "s3://geotrellis-test/daunnc/cog-benchmark/cog"
    val avroLayer = "avroLayer"
    val cogLayer  = "cogLayer"

    val result = args.headOption match {
      case Some("avro") =>
        for {
          _ <- IngestAvro.ingest(inputPath, avroPath)(avroLayer)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, 16)
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer)
        } yield ()
      case Some("cog") =>
        for {
          _ <- IngestCOG.ingest(inputPath, cogPath)(cogLayer)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, (1 to 13).toList, 16)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, (1 to 13).toList)
        } yield ()
      case _ =>
        for {
          _ <- IngestAvro.ingest(inputPath, avroPath)(avroLayer)
          _ <- IngestCOG.ingest(inputPath, cogPath)(cogLayer)
          _ <- AvroBench.runValueReader(avroPath)(avroLayer, 16)
          _ <- COGBench.runValueReader(cogPath)(cogLayer, (1 to 13).toList, 16)
          _ <- AvroBench.runLayerReader(avroPath)(avroLayer)
          _ <- COGBench.runLayerReader(cogPath)(cogLayer, (1 to 13).toList)
        } yield ()
    }

    val (logs, _) = result.run
    logs.foreach(logger.info(_))
  }
}
