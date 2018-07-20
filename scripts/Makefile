VERSION ?= 0.2.0
VERSION_SUFFIX ?= -SNAPSHOT
ASSEMBLY := ../target/scala-2.11/cogs-benchmark-assembly-${VERSION}${VERSION_SUFFIX}.jar
S3_ASSEMBLY := s3://geotrellis-test/daunnc/cogs-benchmark-assembly-${VERSION}${VERSION_SUFFIX}.jar

${ASSEMBLY}: $(call rwildcard, ../src, *.scala) ../build.sbt
	cd ../../; ./sbt assembly -no-colors
	@touch -m ${ASSEMBLY}

ifndef CLUSTER_ID
CLUSTER_ID=$(shell cat terraform/terraform.tfstate | jq -r .modules[].resources[\"aws_emr_cluster.emr-spark-cluster\"].primary.id)
endif
ifndef KEY_PAIR_FILE
ifndef TF_VAR_pem_path
KEY_PAIR_FILE=$(shell cat terraform/variables.tf.json | jq -r ".variable.pem_path.default")
else
KEY_PAIR_FILE=${TF_VAR_pem_path}
endif
endif

ifndef TF_VAR_user
export TF_VAR_user=${USER}
endif

terraform-init:
	cd terraform; terraform init

create-cluster:
	cd terraform; TF_VAR_s3_notebook_bucket="blah" TF_VAR_s3_notebook_prefix="blah" terraform apply

create-jupyter-cluster:
	cd terraform ; TF_VAR_install_jupyter="true" terraform apply

destroy-cluster:
	cd terraform; TF_VAR_s3_notebook_bucket="blah" TF_VAR_s3_notebook_prefix="blah" terraform destroy

proxy:
	cd terraform; aws emr socks --cluster-id ${CLUSTER_ID} --key-pair-file ${KEY_PAIR_FILE}

ssh:
	cd terraform; aws emr ssh --cluster-id ${CLUSTER_ID} --key-pair-file ${KEY_PAIR_FILE}

cleanup-zeppelin:
	cd terraform; aws emr ssh --cluster-id ${CLUSTER_ID} --key-pair-file ${KEY_PAIR_FILE} \
	--command 'rm -r /usr/lib/zeppelin/local-repo/*/geotrellis*'

restart-zeppelin:
	cd terraform; aws emr ssh --cluster-id ${CLUSTER_ID} --key-pair-file ${KEY_PAIR_FILE} \
	--command 'sudo restart zeppelin'

stop-zeppelin:
	cd terraform; aws emr ssh --cluster-id ${CLUSTER_ID} --key-pair-file ${KEY_PAIR_FILE} \
	--command 'sudo stop zeppelin'

start-zeppelin:
	cd terraform; aws emr ssh --cluster-id ${CLUSTER_ID} --key-pair-file ${KEY_PAIR_FILE} \
	--command 'sudo start zeppelin'

upload-assembly: ${ASSEMBLY}
	cd terraform; aws emr put --cluster-id ${CLUSTER_ID} --key-pair-file ${KEY_PAIR_FILE} \
	--src ../${ASSEMBLY} --dest /tmp/cogs-benchmark-assembly-${VERSION}${VERSION_SUFFIX}.jar

upload-assembly-s3: ${ASSEMBLY}
	aws s3 cp ${ASSEMBLY} ${S3_ASSEMBLY}

benchmark:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY}\
] | cut -f2 | tee last-step-id.txt

benchmark-compression:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},compressed\
] | cut -f2 | tee last-step-id.txt


benchmark-avro-ingest:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=true,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},avro-ingest\
] | cut -f2 | tee last-step-id.txt

benchmark-avro-reads:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},avro-reads\
] | cut -f2 | tee last-step-id.txt

benchmark-avro-value-read:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},avro-value-read\
] | cut -f2 | tee last-step-id.txt

benchmark-avro-layer-read:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},avro-layer-read\
] | cut -f2 | tee last-step-id.txt

benchmark-cog-ingest:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=true,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},cog-ingest\
] | cut -f2 | tee last-step-id.txt

benchmark-cog-ingest-compression:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=true,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},cog-ingest-compression\
] | cut -f2 | tee last-step-id.txt

benchmark-cog-reads:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},cog-reads\
] | cut -f2 | tee last-step-id.txt

benchmark-cog-value-read:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},cog-value-read\
] | cut -f2 | tee last-step-id.txt

benchmark-cog-layer-read:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},cog-layer-read\
] | cut -f2 | tee last-step-id.txt

benchmark-reads:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},reads\
] | cut -f2 | tee last-step-id.txt

benchmark-reads-compressed:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},readsc\
] | cut -f2 | tee last-step-id.txt

benchmark-reads-13:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},reads-13\
] | cut -f2 | tee last-step-id.txt

benchmark-reads-9:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},reads-13\
] | cut -f2 | tee last-step-id.txt

benchmark-reads-5:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Benchmark all",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},reads-13\
] | cut -f2 | tee last-step-id.txt

benchmark-read-pixel-interleave:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Read Pixel Interleave",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},read-pixel-interleave\
] | cut -f2 | tee last-step-id.txt

benchmark-read-band-interleave:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Read Band Interleave",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},read-band-interleave\
] | cut -f2 | tee last-step-id.txt

benchmark-read-band-interleave-avro:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Read Band Interleave Avro",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},read-band-interleave-avro\
] | cut -f2 | tee last-step-id.txt

benchmark-read-band-interleave-cogs:
	aws emr add-steps --output text --cluster-id ${CLUSTER_ID} \
--steps Type=CUSTOM_JAR,Name="Read Band Interleave COGS",Jar=command-runner.jar,Args=[\
spark-submit,--master,yarn-cluster,\
--class,com.azavea.Main,\
--driver-memory,4200M,\
--driver-cores,2,\
--executor-memory,4200M,\
--executor-cores,2,\
--conf,spark.dynamicAllocation.enabled=false,\
--conf,spark.executor.instances=20,\
--conf,spark.yarn.executor.memoryOverhead=700,\
--conf,spark.yarn.driver.memoryOverhead=700,\
${S3_ASSEMBLY},read-band-interleave-cogs\
] | cut -f2 | tee last-step-id.txt
