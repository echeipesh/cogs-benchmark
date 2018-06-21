# GeoTrellis COGs benchmark (S3 backend)

This project performs S3 IO measurements.

The intention is to compare the classic [GeoTrellis Avro Ingest](https://github.com/pomadchin/cogs-benchmark/blob/master/src/main/scala/com/azavea/ingest/IngestAvro.scala)
and the new [GeoTrellis COG Ingest](https://github.com/pomadchin/cogs-benchmark/blob/master/src/main/scala/com/azavea/ingest/IngestCOG.scala)

These tests include `ValueReader`s tests, it means that no Spark is required for these tests.

Currently spark throws the following exception for these tests: 
```bash
java.util.concurrent.TimeoutException: Futures timed out after [100000 milliseconds]
	at scala.concurrent.impl.Promise$DefaultPromise.ready(Promise.scala:219)
	at scala.concurrent.impl.Promise$DefaultPromise.result(Promise.scala:223)
	at org.apache.spark.util.ThreadUtils$.awaitResult(ThreadUtils.scala:201)
	at org.apache.spark.deploy.yarn.ApplicationMaster.runDriver(ApplicationMaster.scala:401)
	at org.apache.spark.deploy.yarn.ApplicationMaster.run(ApplicationMaster.scala:254)
	at org.apache.spark.deploy.yarn.ApplicationMaster$$anonfun$main$1.apply$mcV$sp(ApplicationMaster.scala:764)
	at org.apache.spark.deploy.SparkHadoopUtil$$anon$2.run(SparkHadoopUtil.scala:67)
	at org.apache.spark.deploy.SparkHadoopUtil$$anon$2.run(SparkHadoopUtil.scala:66)
	at java.security.AccessController.doPrivileged(Native Method)
	at javax.security.auth.Subject.doAs(Subject.java:422)
	at org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:1698)
	at org.apache.spark.deploy.SparkHadoopUtil.runAsSparkUser(SparkHadoopUtil.scala:66)
	at org.apache.spark.deploy.yarn.ApplicationMaster$.main(ApplicationMaster.scala:762)
	at org.apache.spark.deploy.yarn.ApplicationMaster.main(ApplicationMaster.scala)
```


Data set info:  
- URI: `s3://gt-rasters/nlcd/2011/tiles`
- Metadata: `16 Objects - 5.3 GB`

Was performed a `SpatialIngest`
- Avro Layer: 
  - URI: `s3://geotrellis-test/daunnc/cog-benchmark/avro-3`
  - Max zoom level: `13`
  - KeyBounds: `KeyBounds(SpatialKey(1132,2673),SpatialKey(2647,3584))`
  - Size (all zoom levels): `1824189 Objects - 10.0 GB`
  - Ingest time (~): `3,017,823 ms`
  - CMD: `cd scripts; make make benchmark-avro-ingest`
- COG Layer (no compression): 
  - URI: `s3://geotrellis-test/daunnc/cog-benchmark/cog-3`
  - Max zoom level: `13`
  - KeyBounds: `KeyBounds(SpatialKey(1132,2673),SpatialKey(2647,3584))`
  - Size (all zoom levels): `5522 Objects - 114.7 GB`
  - Ingest time (~): `2,664,556 ms`
  - CMD: `cd scripts; make make benchmark-cog-ingest`
- COG Layer (deflate compression):
  - URI: `s3://geotrellis-test/daunnc/cog-benchmark/cogc-3`
  - Max zoom level: `13`
  - KeyBounds: `KeyBounds(SpatialKey(1132,2673),SpatialKey(2647,3584))`
  - Size (all zoom levels): `5522 Objects - 9.7 GB`
  - Ingest time (~): `2,775,428 ms`
  - CMD: `cd scripts; make make benchmark-cog-ingest-compression`


## Avro vs COGs
  
```text

==============================================
InputPath: s3://gt-rasters/nlcd/2011/tiles
AvroPath: s3://geotrellis-test/daunnc/cog-benchmark/avro-3
COGPath: s3://geotrellis-test/daunnc/cog-benchmark/cog-3
AvroLayer: avroLayer
COGLayer: cogLayer
ValueReadersExt: Some(Extent(-1.4499798517584793E7, 6413372.421239428, -1.4421527000620775E7, 6961273.039987572))
LayerReadersExt: Some(Extent(-1.5499798517584793E7, 3945781.478164045, -1.0975542095495135E7, 7961273.039987572))
==========READS BENCHMARK, zoom lvl 13 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 444857
AvroBench.runLayerReader:: 160,327 ms
AvroBench.runLayerReader:: zoom levels: 13
COGBench.runLayerReader:: avg number of tiles: 444857
COGBench.runLayerReader:: 97,879 ms
COGBench.runLayerReader:: zoom levels: 13
AvroBench.runValueReader:: 61 ms
AvroBench.runValueReader:: total time: 109873 ms
AvroBench.runValueReader:: avg number of tiles: 1792
AvroBench.runValueReader:: zoom levels: 13
COGBench.runValueReader:: 117 ms
COGBench.runValueReader:: total time: 211029 ms
COGBench.runValueReader:: avg number of tiles: 1792
COGBench.runValueReader:: zoom levels: 13
==========READS BENCHMARK, zoom lvl 10 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 7098
AvroBench.runLayerReader:: 4,469 ms
AvroBench.runLayerReader:: zoom levels: 10
COGBench.runLayerReader:: avg number of tiles: 7098
COGBench.runLayerReader:: 12,921 ms
COGBench.runLayerReader:: zoom levels: 10
AvroBench.runValueReader:: 30 ms
AvroBench.runValueReader:: total time: 1387 ms
AvroBench.runValueReader:: avg number of tiles: 45
AvroBench.runValueReader:: zoom levels: 10
COGBench.runValueReader:: 81 ms
COGBench.runValueReader:: total time: 3649 ms
COGBench.runValueReader:: avg number of tiles: 45
COGBench.runValueReader:: zoom levels: 10
==========READS BENCHMARK, zoom lvl 9 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 1794
AvroBench.runLayerReader:: 2,101 ms
AvroBench.runLayerReader:: zoom levels: 9
COGBench.runLayerReader:: avg number of tiles: 1794
COGBench.runLayerReader:: 8,946 ms
COGBench.runLayerReader:: zoom levels: 9
AvroBench.runValueReader:: 16 ms
AvroBench.runValueReader:: total time: 268 ms
AvroBench.runValueReader:: avg number of tiles: 16
AvroBench.runValueReader:: zoom levels: 9
COGBench.runValueReader:: 46 ms
COGBench.runValueReader:: total time: 746 ms
COGBench.runValueReader:: avg number of tiles: 16
COGBench.runValueReader:: zoom levels: 9
==========READS BENCHMARK, zoom lvl 9========
AvroBench.runLayerReader:: avg number of tiles: 5495
AvroBench.runLayerReader:: 3,474 ms
AvroBench.runLayerReader:: zoom levels: 9
COGBench.runLayerReader:: avg number of tiles: 5495
COGBench.runLayerReader:: 24,452 ms
COGBench.runLayerReader:: zoom levels: 9
==========READS BENCHMARK, zoom lvl 8 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 460
AvroBench.runLayerReader:: 1,557 ms
AvroBench.runLayerReader:: zoom levels: 8
COGBench.runLayerReader:: avg number of tiles: 460
COGBench.runLayerReader:: 3,827 ms
COGBench.runLayerReader:: zoom levels: 8
AvroBench.runValueReader:: 0 ms
AvroBench.runValueReader:: total time: 0 ms
AvroBench.runValueReader:: avg number of tiles: 5
AvroBench.runValueReader:: zoom levels: 8
COGBench.runValueReader:: 0 ms
COGBench.runValueReader:: total time: 0 ms
COGBench.runValueReader:: avg number of tiles: 5
COGBench.runValueReader:: zoom levels: 8
==========READS BENCHMARK, zoom lvl 8========
AvroBench.runLayerReader:: avg number of tiles: 1418
AvroBench.runLayerReader:: 1,177 ms
AvroBench.runLayerReader:: zoom levels: 8
COGBench.runLayerReader:: avg number of tiles: 1437
COGBench.runLayerReader:: 11,274 ms
COGBench.runLayerReader:: zoom levels: 8
==========READS BENCHMARK, zoom lvl 5 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 12
AvroBench.runLayerReader:: 527 ms
AvroBench.runLayerReader:: zoom levels: 5
COGBench.runLayerReader:: avg number of tiles: 12
COGBench.runLayerReader:: 2,066 ms
COGBench.runLayerReader:: zoom levels: 5
AvroBench.runValueReader:: 0 ms
AvroBench.runValueReader:: total time: 0 ms
AvroBench.runValueReader:: avg number of tiles: 1
AvroBench.runValueReader:: zoom levels: 5
COGBench.runValueReader:: 0 ms
COGBench.runValueReader:: total time: 0 ms
COGBench.runValueReader:: avg number of tiles: 1
COGBench.runValueReader:: zoom levels: 5
==========READS BENCHMARK, zoom lvl 5========
AvroBench.runLayerReader:: avg number of tiles: 34
AvroBench.runLayerReader:: 984 ms
AvroBench.runLayerReader:: zoom levels: 5
COGBench.runLayerReader:: avg number of tiles: 34
COGBench.runLayerReader:: 1,502 ms
COGBench.runLayerReader:: zoom levels: 5
AvroBench.runValueReader:: 50 ms
AvroBench.runValueReader:: total time: 1764 ms
AvroBench.runValueReader:: avg number of tiles: 35
AvroBench.runValueReader:: zoom levels: 5
COGBench.runValueReader:: 76 ms
COGBench.runValueReader:: total time: 2664 ms
COGBench.runValueReader:: avg number of tiles: 35
COGBench.runValueReader:: zoom levels: 5

```  

## Avro vs Compressed COGs

```text

==============================================
InputPath: s3://gt-rasters/nlcd/2011/tiles
AvroPath: s3://geotrellis-test/daunnc/cog-benchmark/avro-3
COGCompressedPath: s3://geotrellis-test/daunnc/cog-benchmark/cogc-3
AvroLayer: avroLayer
COGLayer: cogLayer
ValueReadersExt: Some(Extent(-1.4499798517584793E7, 6413372.421239428, -1.4421527000620775E7, 6961273.039987572))
LayerReadersExt: Some(Extent(-1.5499798517584793E7, 3945781.478164045, -1.0975542095495135E7, 7961273.039987572))
==========READSC BENCHMARK, zoom lvl 13 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 444857
AvroBench.runLayerReader:: 172,229 ms
AvroBench.runLayerReader:: zoom levels: 13
COGBench.runLayerReader:: avg number of tiles: 444857
COGBench.runLayerReader:: 59,319 ms
COGBench.runLayerReader:: zoom levels: 13
AvroBench.runValueReader:: 63 ms
AvroBench.runValueReader:: total time: 114060 ms
AvroBench.runValueReader:: avg number of tiles: 1792
AvroBench.runValueReader:: zoom levels: 13
COGBench.runValueReader:: 88 ms
COGBench.runValueReader:: total time: 158582 ms
COGBench.runValueReader:: avg number of tiles: 1792
COGBench.runValueReader:: zoom levels: 13
==========READSC BENCHMARK, zoom lvl 10 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 7098
AvroBench.runLayerReader:: 3,631 ms
AvroBench.runLayerReader:: zoom levels: 10
COGBench.runLayerReader:: avg number of tiles: 7098
COGBench.runLayerReader:: 9,502 ms
COGBench.runLayerReader:: zoom levels: 10
AvroBench.runValueReader:: 62 ms
AvroBench.runValueReader:: total time: 2802 ms
AvroBench.runValueReader:: avg number of tiles: 45
AvroBench.runValueReader:: zoom levels: 10
COGBench.runValueReader:: 60 ms
COGBench.runValueReader:: total time: 2741 ms
COGBench.runValueReader:: avg number of tiles: 45
COGBench.runValueReader:: zoom levels: 10
==========READSC BENCHMARK, zoom lvl 10========
AvroBench.runLayerReader:: avg number of tiles: 21583
AvroBench.runLayerReader:: 7,701 ms
AvroBench.runLayerReader:: zoom levels: 10
COGBench.runLayerReader:: avg number of tiles: 21611
COGBench.runLayerReader:: 27,782 ms
COGBench.runLayerReader:: zoom levels: 10
==========READSC BENCHMARK, zoom lvl 9 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 1794
AvroBench.runLayerReader:: 1,611 ms
AvroBench.runLayerReader:: zoom levels: 9
COGBench.runLayerReader:: avg number of tiles: 1794
COGBench.runLayerReader:: 9,634 ms
COGBench.runLayerReader:: zoom levels: 9
AvroBench.runValueReader:: 48 ms
AvroBench.runValueReader:: total time: 776 ms
AvroBench.runValueReader:: avg number of tiles: 16
AvroBench.runValueReader:: zoom levels: 9
COGBench.runValueReader:: 42 ms
COGBench.runValueReader:: total time: 672 ms
COGBench.runValueReader:: avg number of tiles: 16
COGBench.runValueReader:: zoom levels: 9
==========READSC BENCHMARK, zoom lvl 9========
AvroBench.runLayerReader:: avg number of tiles: 5495
AvroBench.runLayerReader:: 2,973 ms
AvroBench.runLayerReader:: zoom levels: 9
COGBench.runLayerReader:: avg number of tiles: 5495
COGBench.runLayerReader:: 21,951 ms
COGBench.runLayerReader:: zoom levels: 9
==========READSC BENCHMARK, zoom lvl 8 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 460
AvroBench.runLayerReader:: 1,077 ms
AvroBench.runLayerReader:: zoom levels: 8
COGBench.runLayerReader:: avg number of tiles: 460
COGBench.runLayerReader:: 2,278 ms
COGBench.runLayerReader:: zoom levels: 8
AvroBench.runValueReader:: 0 ms
AvroBench.runValueReader:: total time: 0 ms
AvroBench.runValueReader:: avg number of tiles: 5
AvroBench.runValueReader:: zoom levels: 8
COGBench.runValueReader:: 0 ms
COGBench.runValueReader:: total time: 0 ms
COGBench.runValueReader:: avg number of tiles: 5
COGBench.runValueReader:: zoom levels: 8
==========READSC BENCHMARK, zoom lvl 8========
AvroBench.runLayerReader:: avg number of tiles: 1418
AvroBench.runLayerReader:: 1,665 ms
AvroBench.runLayerReader:: zoom levels: 8
COGBench.runLayerReader:: avg number of tiles: 1437
COGBench.runLayerReader:: 3,062 ms
COGBench.runLayerReader:: zoom levels: 8
==========READSC BENCHMARK, zoom lvl 5 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 12
AvroBench.runLayerReader:: 454 ms
AvroBench.runLayerReader:: zoom levels: 5
COGBench.runLayerReader:: avg number of tiles: 12
COGBench.runLayerReader:: 2,207 ms
COGBench.runLayerReader:: zoom levels: 5
AvroBench.runValueReader:: 0 ms
AvroBench.runValueReader:: total time: 0 ms
AvroBench.runValueReader:: avg number of tiles: 1
AvroBench.runValueReader:: zoom levels: 5
COGBench.runValueReader:: 0 ms
COGBench.runValueReader:: total time: 0 ms
COGBench.runValueReader:: avg number of tiles: 1
COGBench.runValueReader:: zoom levels: 5
==========READSC BENCHMARK, zoom lvl 5========
AvroBench.runLayerReader:: avg number of tiles: 34
AvroBench.runLayerReader:: 1,422 ms
AvroBench.runLayerReader:: zoom levels: 5
COGBench.runLayerReader:: avg number of tiles: 34
COGBench.runLayerReader:: 676 ms
COGBench.runLayerReader:: zoom levels: 5
AvroBench.runValueReader:: 40 ms
AvroBench.runValueReader:: total time: 1403 ms
AvroBench.runValueReader:: avg number of tiles: 35
AvroBench.runValueReader:: zoom levels: 5
COGBench.runValueReader:: 60 ms
COGBench.runValueReader:: total time: 2114 ms
COGBench.runValueReader:: avg number of tiles: 35
COGBench.runValueReader:: zoom levels: 5

```
