package com.azavea

import com.azavea.ingest._
import com.azavea.run._

import cats.effect.IO
import cats.implicits._

object Main {
  def main(args: Array[String]): Unit = {
    val inputPath = "s3://gt-rasters/nlcd/2011/tiles"
    val avroPath = "s3://geotrellis-test/daunnc/cog-benchmark/avro"
    val cogPath = "s3://geotrellis-test/daunnc/cog-benchmark/cog"

    val avroLayer = "avroLayer"
    val cogLayer = "cogLayer"

    val result: IO[List[String]] = (args.headOption match {
      case Some("avro") =>
        List(
          // ingest
          IO { IngestAvro.ingest(inputPath, avroPath)(avroLayer) },
          // avro reads
          IO { AvroBench.runValueReader(avroPath)(avroLayer, 16) },
          IO { AvroBench.runLayerReader(avroPath)(avroLayer) }
        )

      case Some("cog") =>
        List(
          // ingest
          IO { IngestCOG.ingest(inputPath, cogPath)(cogLayer) },
          // cog reads
          IO { COGBench.runValueReader(cogPath)(cogLayer, (1 to 13).toList, 16) },
          IO { COGBench.runLayerReader(cogPath)(cogLayer, (1 to 13).toList) }
        )
      case _ =>
        List(
          // ingest
          IO { IngestAvro.ingest(inputPath, avroPath)(avroLayer) },
          IO { IngestCOG.ingest(inputPath, cogPath)(cogLayer) },
          // avro reads
          IO { AvroBench.runValueReader(avroPath)(avroLayer, 16) },
          IO { AvroBench.runLayerReader(avroPath)(avroLayer) },
          // cog reads
          IO { COGBench.runValueReader(cogPath)(cogLayer, (1 to 13).toList, 16) },
          IO { COGBench.runLayerReader(cogPath)(cogLayer, (1 to 13).toList) }
        )
    }).sequence

    println(result.unsafeRunSync().mkString(", "))
  }
}
