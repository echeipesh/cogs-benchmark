package com.azavea

import com.azavea.ingest._
import com.azavea.run._

import cats.effect.IO
import cats.implicits._

object Main {
  def main(args: Array[String]): Unit = {
    val result: IO[List[String]] =
      List(
        // ingest
        IO { IngestAvro.ingest("inputPath", "outputPath")("avro-bench") },
        IO { IngestCOG.ingest("inputPath", "COGoutputPath")("cog-bench") },
        // avro reads
        IO { AvroBench.runValueReader("outputPath")("avroBench", 16) },
        IO { AvroBench.runLayerReader("outputPath")("avroBench") },
        // cog reads
        IO { COGBench.runValueReader("outputPath")("avroBench", (1 to 13).toList, 16) },
        IO { COGBench.runLayerReader("outputPath")("avroBench", (1 to 13).toList) }
      ).sequence

    println(result.unsafeRunSync().mkString(", "))
  }
}
