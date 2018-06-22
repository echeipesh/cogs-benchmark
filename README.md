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
AvroBench.runLayerReader:: 165,385 ms
AvroBench.runLayerReader:: zoom levels: 13
COGBench.runLayerReader:: avg number of tiles: 444857
COGBench.runLayerReader:: 99,875 ms
COGBench.runLayerReader:: zoom levels: 13
AvroBench.runValueReader:: 61 ms
AvroBench.runValueReader:: total time: 110367 ms
AvroBench.runValueReader:: avg number of tiles: 1792
AvroBench.runValueReader:: zoom levels: 13
COGBench.runValueReader:: 109 ms
COGBench.runValueReader:: total time: 195674 ms
COGBench.runValueReader:: avg number of tiles: 1792
COGBench.runValueReader:: zoom levels: 13
==========READS BENCHMARK, zoom lvl 10 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 7098
AvroBench.runLayerReader:: 4,044 ms
AvroBench.runLayerReader:: zoom levels: 10
COGBench.runLayerReader:: avg number of tiles: 7098
COGBench.runLayerReader:: 10,555 ms
COGBench.runLayerReader:: zoom levels: 10
AvroBench.runValueReader:: 14 ms
AvroBench.runValueReader:: total time: 633 ms
AvroBench.runValueReader:: avg number of tiles: 45
AvroBench.runValueReader:: zoom levels: 10
COGBench.runValueReader:: 65 ms
COGBench.runValueReader:: total time: 2943 ms
COGBench.runValueReader:: avg number of tiles: 45
COGBench.runValueReader:: zoom levels: 10
==========READS BENCHMARK, zoom lvl 9 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 1794
AvroBench.runLayerReader:: 2,209 ms
AvroBench.runLayerReader:: zoom levels: 9
COGBench.runLayerReader:: avg number of tiles: 1794
COGBench.runLayerReader:: 8,818 ms
COGBench.runLayerReader:: zoom levels: 9
AvroBench.runValueReader:: 40 ms
AvroBench.runValueReader:: total time: 123993 ms
AvroBench.runValueReader:: avg number of tiles: 3068
AvroBench.runValueReader:: zoom levels: 9
COGBench.runValueReader:: 84 ms
COGBench.runValueReader:: total time: 259773 ms
COGBench.runValueReader:: avg number of tiles: 3068
COGBench.runValueReader:: zoom levels: 9
==========READS BENCHMARK, zoom lvl 9========
AvroBench.runLayerReader:: avg number of tiles: 5495
AvroBench.runLayerReader:: 2,755 ms
AvroBench.runLayerReader:: zoom levels: 9
COGBench.runLayerReader:: avg number of tiles: 5495
COGBench.runLayerReader:: 24,803 ms
COGBench.runLayerReader:: zoom levels: 9
==========READS BENCHMARK, zoom lvl 8 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 460
AvroBench.runLayerReader:: 914 ms
AvroBench.runLayerReader:: zoom levels: 8
COGBench.runLayerReader:: avg number of tiles: 460
COGBench.runLayerReader:: 5,091 ms
COGBench.runLayerReader:: zoom levels: 8
AvroBench.runValueReader:: 34 ms
AvroBench.runValueReader:: total time: 26773 ms
AvroBench.runValueReader:: avg number of tiles: 780
AvroBench.runValueReader:: zoom levels: 8
COGBench.runValueReader:: 78 ms
COGBench.runValueReader:: total time: 61275 ms
COGBench.runValueReader:: avg number of tiles: 780
COGBench.runValueReader:: zoom levels: 8
==========READS BENCHMARK, zoom lvl 8========
AvroBench.runLayerReader:: avg number of tiles: 1418
AvroBench.runLayerReader:: 1,508 ms
AvroBench.runLayerReader:: zoom levels: 8
COGBench.runLayerReader:: avg number of tiles: 1437
COGBench.runLayerReader:: 5,226 ms
COGBench.runLayerReader:: zoom levels: 8
==========READS BENCHMARK, zoom lvl 5 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 12
AvroBench.runLayerReader:: 633 ms
AvroBench.runLayerReader:: zoom levels: 5
COGBench.runLayerReader:: avg number of tiles: 12
COGBench.runLayerReader:: 1,224 ms
COGBench.runLayerReader:: zoom levels: 5
AvroBench.runValueReader:: 18 ms
AvroBench.runValueReader:: total time: 361 ms
AvroBench.runValueReader:: avg number of tiles: 20
AvroBench.runValueReader:: zoom levels: 5
COGBench.runValueReader:: 49 ms
COGBench.runValueReader:: total time: 994 ms
COGBench.runValueReader:: avg number of tiles: 20
COGBench.runValueReader:: zoom levels: 5
==========READS BENCHMARK, zoom lvl 5========
AvroBench.runLayerReader:: avg number of tiles: 34
AvroBench.runLayerReader:: 533 ms
AvroBench.runLayerReader:: zoom levels: 5
COGBench.runLayerReader:: avg number of tiles: 34
COGBench.runLayerReader:: 757 ms
COGBench.runLayerReader:: zoom levels: 5
AvroBench.runValueReader:: 25 ms
AvroBench.runValueReader:: total time: 907 ms
AvroBench.runValueReader:: avg number of tiles: 35
AvroBench.runValueReader:: zoom levels: 5
COGBench.runValueReader:: 72 ms
COGBench.runValueReader:: total time: 2528 ms
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
AvroBench.runLayerReader:: 169,755 ms
AvroBench.runLayerReader:: zoom levels: 13
COGBench.runLayerReader:: avg number of tiles: 444857
COGBench.runLayerReader:: 63,891 ms
COGBench.runLayerReader:: zoom levels: 13
AvroBench.runValueReader:: 60 ms
AvroBench.runValueReader:: total time: 108333 ms
AvroBench.runValueReader:: avg number of tiles: 1792
AvroBench.runValueReader:: zoom levels: 13
COGBench.runValueReader:: 77 ms
COGBench.runValueReader:: total time: 139716 ms
COGBench.runValueReader:: avg number of tiles: 1792
COGBench.runValueReader:: zoom levels: 13
==========READSC BENCHMARK, zoom lvl 10 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 7098
AvroBench.runLayerReader:: 4,059 ms
AvroBench.runLayerReader:: zoom levels: 10
COGBench.runLayerReader:: avg number of tiles: 7098
COGBench.runLayerReader:: 9,893 ms
COGBench.runLayerReader:: zoom levels: 10
AvroBench.runValueReader:: 23 ms
AvroBench.runValueReader:: total time: 1062 ms
AvroBench.runValueReader:: avg number of tiles: 45
AvroBench.runValueReader:: zoom levels: 10
COGBench.runValueReader:: 42 ms
COGBench.runValueReader:: total time: 1895 ms
COGBench.runValueReader:: avg number of tiles: 45
COGBench.runValueReader:: zoom levels: 10
==========READSC BENCHMARK, zoom lvl 10========
AvroBench.runLayerReader:: avg number of tiles: 21583
AvroBench.runLayerReader:: 7,109 ms
AvroBench.runLayerReader:: zoom levels: 10
COGBench.runLayerReader:: avg number of tiles: 21611
COGBench.runLayerReader:: 28,353 ms
COGBench.runLayerReader:: zoom levels: 10
==========READSC BENCHMARK, zoom lvl 9 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 1794
AvroBench.runLayerReader:: 1,327 ms
AvroBench.runLayerReader:: zoom levels: 9
COGBench.runLayerReader:: avg number of tiles: 1794
COGBench.runLayerReader:: 6,881 ms
COGBench.runLayerReader:: zoom levels: 9
AvroBench.runValueReader:: 43 ms
AvroBench.runValueReader:: total time: 134099 ms
AvroBench.runValueReader:: avg number of tiles: 3068
AvroBench.runValueReader:: zoom levels: 9
COGBench.runValueReader:: 55 ms
COGBench.runValueReader:: total time: 169954 ms
COGBench.runValueReader:: avg number of tiles: 3068
COGBench.runValueReader:: zoom levels: 9
==========READSC BENCHMARK, zoom lvl 9========
AvroBench.runLayerReader:: avg number of tiles: 5495
AvroBench.runLayerReader:: 2,651 ms
AvroBench.runLayerReader:: zoom levels: 9
COGBench.runLayerReader:: avg number of tiles: 5495
COGBench.runLayerReader:: 20,148 ms
COGBench.runLayerReader:: zoom levels: 9
==========READSC BENCHMARK, zoom lvl 8 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 460
AvroBench.runLayerReader:: 660 ms
AvroBench.runLayerReader:: zoom levels: 8
COGBench.runLayerReader:: avg number of tiles: 460
COGBench.runLayerReader:: 3,197 ms
COGBench.runLayerReader:: zoom levels: 8
AvroBench.runValueReader:: 35 ms
AvroBench.runValueReader:: total time: 27476 ms
AvroBench.runValueReader:: avg number of tiles: 780
AvroBench.runValueReader:: zoom levels: 8
COGBench.runValueReader:: 70 ms
COGBench.runValueReader:: total time: 55342 ms
COGBench.runValueReader:: avg number of tiles: 780
COGBench.runValueReader:: zoom levels: 8
==========READSC BENCHMARK, zoom lvl 8========
AvroBench.runLayerReader:: avg number of tiles: 1418
AvroBench.runLayerReader:: 1,751 ms
AvroBench.runLayerReader:: zoom levels: 8
COGBench.runLayerReader:: avg number of tiles: 1437
COGBench.runLayerReader:: 2,670 ms
COGBench.runLayerReader:: zoom levels: 8
==========READSC BENCHMARK, zoom lvl 5 (bounded by extent)========
AvroBench.runLayerReader:: avg number of tiles: 12
AvroBench.runLayerReader:: 1,061 ms
AvroBench.runLayerReader:: zoom levels: 5
COGBench.runLayerReader:: avg number of tiles: 12
COGBench.runLayerReader:: 518 ms
COGBench.runLayerReader:: zoom levels: 5
AvroBench.runValueReader:: 14 ms
AvroBench.runValueReader:: total time: 280 ms
AvroBench.runValueReader:: avg number of tiles: 20
AvroBench.runValueReader:: zoom levels: 5
COGBench.runValueReader:: 33 ms
COGBench.runValueReader:: total time: 666 ms
COGBench.runValueReader:: avg number of tiles: 20
COGBench.runValueReader:: zoom levels: 5
==========READSC BENCHMARK, zoom lvl 5========
AvroBench.runLayerReader:: avg number of tiles: 34
AvroBench.runLayerReader:: 756 ms
AvroBench.runLayerReader:: zoom levels: 5
COGBench.runLayerReader:: avg number of tiles: 34
COGBench.runLayerReader:: 866 ms
COGBench.runLayerReader:: zoom levels: 5
AvroBench.runValueReader:: 32 ms
AvroBench.runValueReader:: total time: 1120 ms
AvroBench.runValueReader:: avg number of tiles: 35
AvroBench.runValueReader:: zoom levels: 5
COGBench.runValueReader:: 57 ms
COGBench.runValueReader:: total time: 2005 ms
COGBench.runValueReader:: avg number of tiles: 35
COGBench.runValueReader:: zoom levels: 5

```
