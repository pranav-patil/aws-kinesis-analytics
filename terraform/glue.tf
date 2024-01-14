resource "aws_glue_catalog_database" "aws_glue_machine_metrics_database" {
  name        = "${var.stack_name}_metrics_database"
  description = "Catalog Database for cpu and network metrics from machine"

  lifecycle {
    create_before_destroy = true
    ignore_changes        = []
  }
}

resource "aws_glue_catalog_table" "machine_metric_table" {
  name          = "${var.stack_name}_machine_metrics_table"
  database_name = aws_glue_catalog_database.aws_glue_machine_metrics_database.name
  description   = "Catalog table for metrics from machine"

  table_type = "EXTERNAL_TABLE"

  parameters = {
    EXTERNAL        = "TRUE"
    classification = "parquet"
    "parquet.compression" = "SNAPPY"
    "projection.enabled" = true
    "projection.year.type" = "enum"
    "projection.year.values" = "2022"
    "projection.month.type" = "enum"
    "projection.month.values" = "01"
    "projection.day.type" = "enum"
    "projection.day.values" = "04"
    "projection.metric.type" = "enum"
    "projection.metric.values" = "cpuStats,memoryStats,interfaceStats,networkStats"
    "storage.location.template" = "s3://${module.machine_metrics_athena_bucket.this_s3_bucket_id}/metrics/metric=${"$"}{metric}/year=${"$"}{year}/month=${"$"}{month}/day=${"$"}{day}"
  }

  partition_keys {
    name = "metric"
    type = "string"
  }
  partition_keys {
    name = "year"
    type = "string"
  }
  partition_keys {
    name = "month"
    type = "string"
  }
  partition_keys {
    name = "day"
    type = "string"
  }

  storage_descriptor {
    location      = "s3://${module.machine_metrics_athena_bucket.this_s3_bucket_id}/metrics/"
    input_format  = "org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat"
    output_format = "org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat"

    ser_de_info {
      name                  = "ParquetHiveSerDe"
      serialization_library = "org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe"

      parameters = {
        "serialization.format" = 1
      }
    }

    columns {
      name    = "region"
      type    = "string"
      comment = "AWS region."
    }
    columns {
      name    = "userId"
      type    = "string"
      comment = "Unique user id."
    }
    columns {
      name    = "machineId"
      type    = "string"
      comment = "Unique ID of the Machine or Computer."
    }
    columns {
      name    = "timestamp"
      type    = "timestamp"
      comment = "Unix timestamps in milliseconds when device metric was taken."
    }
    columns {
      name    = "availableMemory"
      type    = "bigint"
      comment = "Memory available on the Machine"
    }
    columns {
      name    = "appResidentMemory"
      type    = "bigint"
      comment = "Memory consumed by applications running on the Machine"
    }
    columns {
      name    = "core"
      type    = "string"
      comment = "Name of the CPU core"
    }
    columns {
      name    = "utilization"
      type    = "int"
      comment = "Percentage utilization of CPU core"
    }
    columns {
      name    = "cyclesProcessed"
      type    = "bigint"
      comment = "Total number of cpu cycles processed by this thread"
    }
    columns {
      name    = "frequency"
      type    = "bigint"
      comment = "Total cpu frequency"
    }
    columns {
      name    = "connectionRate"
      type    = "bigint"
      comment = "Connections in one second time period"
    }
    columns {
      name    = "packetsDroppedNic"
      type    = "bigint"
      comment = "Number of packets dropped on the NIC"
    }
    columns {
      name    = "packetsTotalNic"
      type    = "bigint"
      comment = "Total Number of packets at the NIC"
    }
    columns {
      name    = "packetsDroppedFpga"
      type    = "bigint"
      comment = "Number of packets dropped at the fpga"
    }
    columns {
      name    = "packetsTotalFpga"
      type    = "bigint"
      comment = "Total Number of packets at the fpga"
    }
    columns {
      name    = "decompressedPackets"
      type    = "bigint"
      comment = "Total number of packets received on the for http decompression"
    }
    columns {
      name    = "avgLatency"
      type    = "bigint"
      comment = "Average packet latency through all threads (in nanoseconds)"
    }
    columns {
      name    = "congestionAll"
      type    = "bigint"
      comment = "Congestion across the appliance"
    }
    columns {
      name    = "congestionFpga"
      type    = "bigint"
      comment = "Congestion for FPGA"
    }
    columns {
      name    = "congestionNic"
      type    = "bigint"
      comment = "Congestion for Nic interface"
    }
    columns {
      name    = "networkInterface"
      type    = "string"
      comment = "Display name of the network interface"
    }
    columns {
      name    = "type"
      type    = "string"
      comment = "Type of network interface, i.e. ethernet, VLAN"
    }
    columns {
      name    = "rxPackets"
      type    = "bigint"
      comment = "Total number of packets received on the network interface"
    }
    columns {
      name    = "rxBytes"
      type    = "bigint"
      comment = "Total number of bytes received on the network interface"
    }
    columns {
      name    = "rxPacketsDropped"
      type    = "bigint"
      comment = "Total number of receive packets dropped on the network interface"
    }
    columns {
      name    = "txPackets"
      type    = "bigint"
      comment = "Total number of packets sent from the network interface"
    }
    columns {
      name    = "txBytes"
      type    = "bigint"
      comment = "Total number of packets sent from the network interface"
    }
    columns {
      name    = "txPacketsDropped"
      type    = "bigint"
      comment = "Total number of transmit packets dropped on the network interface"
    }
  }

  lifecycle {
    create_before_destroy = true
    ignore_changes        = []
  }

  depends_on = [
    aws_glue_catalog_database.aws_glue_machine_metrics_database
  ]
}

resource "aws_glue_catalog_table" "machine_memory_table" {
  name          = "${var.stack_name}_machine_memory_table"
  database_name = aws_glue_catalog_database.aws_glue_machine_metrics_database.name
  description   = "Memory metrics of the device"

  table_type = "EXTERNAL_TABLE"

  parameters = {
    EXTERNAL        = "TRUE"
    classification = "parquet"
    "parquet.compression" = "SNAPPY"
    "projection.enabled" = true
    "projection.year.type" = "enum"
    "projection.year.values" = "2022"
    "projection.month.type" = "enum"
    "projection.month.values" = "01,02,03,04,05,06,07,08,09,10,11,12"
    "projection.day.type" = "enum"
    "projection.day.values" = "01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31"
    "storage.location.template" = "s3://${module.machine_metrics_athena_bucket.this_s3_bucket_id}/metrics/metric=memoryStats/year=${"$"}{year}/month=${"$"}{month}/day=${"$"}{day}"
  }

  partition_keys {
    name = "year"
    type = "string"
  }
  partition_keys {
    name = "month"
    type = "string"
  }
  partition_keys {
    name = "day"
    type = "string"
  }

  storage_descriptor {
    location      = "s3://${module.machine_metrics_athena_bucket.this_s3_bucket_id}/metrics/metric=memoryStats/"
    input_format  = "org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat"
    output_format = "org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat"

    ser_de_info {
      name                  = "ParquetHiveSerDe"
      serialization_library = "org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe"

      parameters = {
        "serialization.format" = 1
      }
    }

    columns {
      name    = "region"
      type    = "string"
      comment = "AWS region."
    }
    columns {
      name    = "userId"
      type    = "string"
      comment = "Unique user id."
    }
    columns {
      name    = "machineId"
      type    = "string"
      comment = "Unique ID of the Machine or Computer."
    }
    columns {
      name    = "timestamp"
      type    = "timestamp"
      comment = "Unix timestamps in milliseconds when device metric was taken."
    }
    columns {
      name    = "availableMemory"
      type    = "bigint"
      comment = "Memory available on the Machine"
    }
    columns {
      name    = "appResidentMemory"
      type    = "bigint"
      comment = "Memory consumed by applications running on the Machine"
    }
  }

  lifecycle {
    create_before_destroy = true
    ignore_changes        = []
  }

  depends_on = [
    aws_glue_catalog_database.aws_glue_machine_metrics_database
  ]
}

resource "aws_glue_catalog_table" "machine_cpu_metric_table" {
  name          = "${var.stack_name}_machine_cpu_metrics_table"
  database_name = aws_glue_catalog_database.aws_glue_machine_metrics_database.name
  description   = "Catalog table for CPU utilization metrics from device"

  table_type = "EXTERNAL_TABLE"

  parameters = {
    EXTERNAL        = "TRUE"
    classification = "parquet"
    "parquet.compression" = "SNAPPY"
    "projection.enabled" = true
    "projection.year.type" = "enum"
    "projection.year.values" = "2022"
    "projection.month.type" = "enum"
    "projection.month.values" = "01,02,03,04,05,06,07,08,09,10,11,12"
    "projection.day.type" = "enum"
    "projection.day.values" = "01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31"
    "storage.location.template" = "s3://${module.machine_metrics_athena_bucket.this_s3_bucket_id}/metrics/metric=cpuStats/year=${"$"}{year}/month=${"$"}{month}/day=${"$"}{day}"
  }

  partition_keys {
    name = "year"
    type = "string"
  }
  partition_keys {
    name = "month"
    type = "string"
  }
  partition_keys {
    name = "day"
    type = "string"
  }

  storage_descriptor {
    location      = "s3://${module.machine_metrics_athena_bucket.this_s3_bucket_id}/metrics/metric=cpuStats/"
    input_format  = "org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat"
    output_format = "org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat"

    ser_de_info {
      name                  = "ParquetHiveSerDe"
      serialization_library = "org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe"

      parameters = {
        "serialization.format" = 1
      }
    }

    columns {
      name    = "region"
      type    = "string"
      comment = "AWS region."
    }
    columns {
      name    = "userId"
      type    = "string"
      comment = "Unique user id."
    }
    columns {
      name    = "machineId"
      type    = "string"
      comment = "Unique ID of the Machine or Computer."
    }
    columns {
      name    = "timestamp"
      type    = "timestamp"
      comment = "Unix timestamps in milliseconds when device metric was taken."
    }
    columns {
      name    = "core"
      type    = "string"
      comment = "Name of the CPU core"
    }
    columns {
      name    = "utilization"
      type    = "int"
      comment = "Percentage utilization of CPU core"
    }
    columns {
      name    = "cyclesProcessed"
      type    = "bigint"
      comment = "Total number of cpu cycles processed by this thread"
    }
    columns {
      name    = "frequency"
      type    = "bigint"
      comment = "Total cpu frequency"
    }
  }

  lifecycle {
    create_before_destroy = true
    ignore_changes        = []
  }

  depends_on = [
    aws_glue_catalog_database.aws_glue_machine_metrics_database
  ]
}

resource "aws_glue_catalog_table" "machine_network_metric_table" {
  name          = "${var.stack_name}_machine_network_metrics_table"
  database_name = aws_glue_catalog_database.aws_glue_machine_metrics_database.name
  description   = "Catalog table for network metrics from device"

  table_type = "EXTERNAL_TABLE"

  parameters = {
    EXTERNAL        = "TRUE"
    classification = "parquet"
    "parquet.compression" = "SNAPPY"
    "projection.enabled" = true
    "projection.year.type" = "enum"
    "projection.year.values" = "2022"
    "projection.month.type" = "enum"
    "projection.month.values" = "01,02,03,04,05,06,07,08,09,10,11,12"
    "projection.day.type" = "enum"
    "projection.day.values" = "01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31"
    "storage.location.template" = "s3://${module.machine_metrics_athena_bucket.this_s3_bucket_id}/metrics/metric=networkStats/year=${"$"}{year}/month=${"$"}{month}/day=${"$"}{day}"
  }

  partition_keys {
    name = "year"
    type = "string"
  }
  partition_keys {
    name = "month"
    type = "string"
  }
  partition_keys {
    name = "day"
    type = "string"
  }

  storage_descriptor {
    location      = "s3://${module.machine_metrics_athena_bucket.this_s3_bucket_id}/metrics/metric=networkStats/"
    input_format  = "org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat"
    output_format = "org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat"

    ser_de_info {
      name                  = "ParquetHiveSerDe"
      serialization_library = "org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe"

      parameters = {
        "serialization.format" = 1
      }
    }

    columns {
      name    = "region"
      type    = "string"
      comment = "AWS region."
    }
    columns {
      name    = "userId"
      type    = "string"
      comment = "Unique user id."
    }
    columns {
      name    = "machineId"
      type    = "string"
      comment = "Unique ID of the Machine or Computer."
    }
    columns {
      name    = "timestamp"
      type    = "timestamp"
      comment = "Unix timestamps in milliseconds when device metric was taken."
    }
    columns {
      name    = "connectionRate"
      type    = "bigint"
      comment = "Connections in one second time period"
    }
    columns {
      name    = "packetsDroppedNic"
      type    = "bigint"
      comment = "Number of packets dropped on the NIC"
    }
    columns {
      name    = "packetsTotalNic"
      type    = "bigint"
      comment = "Total Number of packets at the NIC"
    }
    columns {
      name    = "packetsDroppedFpga"
      type    = "bigint"
      comment = "Number of packets dropped at the fpga"
    }
    columns {
      name    = "packetsTotalFpga"
      type    = "bigint"
      comment = "Total Number of packets at the fpga"
    }
    columns {
      name    = "packetsDroppedEngineF"
      type    = "bigint"
      comment = "Number of packets dropped on the engine's F thread"
    }
    columns {
      name    = "packetsTotalEngineF"
      type    = "bigint"
      comment = "Total Number of packets sent to the engine's F thread"
    }
    columns {
      name    = "packetsDroppedEngineK"
      type    = "bigint"
      comment = "Number of packets dropped on the engine's K thread"
    }
    columns {
      name    = "packetsTotalEngineK"
      type    = "bigint"
      comment = "Total Number of packets sent to the engine's K thread"
    }
    columns {
      name    = "packetsDroppedEngineL"
      type    = "bigint"
      comment = "Number of packets dropped on the engine's L thread"
    }
    columns {
      name    = "packetsTotalEngineL"
      type    = "bigint"
      comment = "Total Number of packets sent to the engine's L thread"
    }
    columns {
      name    = "decompressedPackets"
      type    = "bigint"
      comment = "Total number of packets received on the for http decompression"
    }
    columns {
      name    = "decompressedResponses"
      type    = "bigint"
      comment = "Total number of responses or flows received for htttp decompression"
    }
    columns {
      name    = "decompressedBytes"
      type    = "bigint"
      comment = "Total number of bytes decompressed by the engine"
    }
    columns {
      name    = "compressedBytes"
      type    = "bigint"
      comment = "Total number of bytes before being sent to decompression"
    }
    columns {
      name    = "droppedDuringGzip"
      type    = "bigint"
      comment = "Total number of packets dropped while HTTP decompression is busy"
    }
    columns {
      name    = "bestEffortDuringGzip"
      type    = "bigint"
      comment = "Total number of packets allowd by Best Effort while HTTP decompression is busy"
    }
    columns {
      name    = "avgLatency"
      type    = "bigint"
      comment = "Average packet latency through all threads (in nanoseconds)"
    }
    columns {
      name    = "kLatency"
      type    = "bigint"
      comment = "Average packet latency through K-threads (in nanoseconds)"
    }
    columns {
      name    = "lLatency"
      type    = "bigint"
      comment = "Average packet latency through L-threads (in nanoseconds)"
    }
    columns {
      name    = "sslLatency"
      type    = "bigint"
      comment = "Average packet latency through SSL-threads (in nanoseconds)"
    }
    columns {
      name    = "congestionAll"
      type    = "bigint"
      comment = "Congestion across the appliance"
    }
    columns {
      name    = "congestionEngineAll"
      type    = "bigint"
      comment = "Congestion for all network engine"
    }
    columns {
      name    = "congestionEngineF"
      type    = "bigint"
      comment = "Congestion for Engine F"
    }
    columns {
      name    = "congestionEngineK"
      type    = "bigint"
      comment = "Congestion for Engine K"
    }
    columns {
      name    = "congestionEngineL"
      type    = "bigint"
      comment = "Congestion for Engine L"
    }
    columns {
      name    = "congestionFpga"
      type    = "bigint"
      comment = "Congestion for FPGA"
    }
    columns {
      name    = "congestionNic"
      type    = "bigint"
      comment = "Congestion for Nic interface"
    }
  }

  lifecycle {
    create_before_destroy = true
    ignore_changes        = []
  }

  depends_on = [
    aws_glue_catalog_database.aws_glue_machine_metrics_database
  ]
}

resource "aws_glue_catalog_table" "machine_interface_metric_table" {
  name          = "${var.stack_name}_machine_interface_metrics_table"
  database_name = aws_glue_catalog_database.aws_glue_machine_metrics_database.name
  description   = "Catalog table for interface metrics from device"

  table_type = "EXTERNAL_TABLE"

  parameters = {
    EXTERNAL        = "TRUE"
    classification = "parquet"
    "parquet.compression" = "SNAPPY"
    "projection.enabled" = true
    "projection.year.type" = "enum"
    "projection.year.values" = "2022"
    "projection.month.type" = "enum"
    "projection.month.values" = "01,02,03,04,05,06,07,08,09,10,11,12"
    "projection.day.type" = "enum"
    "projection.day.values" = "01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31"
    "storage.location.template" = "s3://${module.machine_metrics_athena_bucket.this_s3_bucket_id}/metrics/metric=interfaceStats/year=${"$"}{year}/month=${"$"}{month}/day=${"$"}{day}"
  }

  partition_keys {
    name = "year"
    type = "string"
  }
  partition_keys {
    name = "month"
    type = "string"
  }
  partition_keys {
    name = "day"
    type = "string"
  }

  storage_descriptor {
    location      = "s3://${module.machine_metrics_athena_bucket.this_s3_bucket_id}/metrics/metric=interfaceStats/"
    input_format  = "org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat"
    output_format = "org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat"

    ser_de_info {
      name                  = "ParquetHiveSerDe"
      serialization_library = "org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe"

      parameters = {
        "serialization.format" = 1
      }
    }

    columns {
      name    = "region"
      type    = "string"
      comment = "AWS region."
    }
    columns {
      name    = "userId"
      type    = "string"
      comment = "Unique user id."
    }
    columns {
      name    = "machineId"
      type    = "string"
      comment = "Unique ID of the Machine or Computer."
    }
    columns {
      name    = "timestamp"
      type    = "timestamp"
      comment = "Unix timestamps in milliseconds when device metric was taken."
    }
    columns {
      name    = "networkInterface"
      type    = "string"
      comment = "Display name of the network interface"
    }
    columns {
      name    = "type"
      type    = "string"
      comment = "Type of network interface, i.e. ethernet, VLAN"
    }
    columns {
      name    = "rxPackets"
      type    = "bigint"
      comment = "Total number of packets received on the network interface"
    }
    columns {
      name    = "rxBytes"
      type    = "bigint"
      comment = "Total number of bytes received on the network interface"
    }
    columns {
      name    = "rxPacketsDropped"
      type    = "bigint"
      comment = "Total number of receive packets dropped on the network interface"
    }
    columns {
      name    = "txPackets"
      type    = "bigint"
      comment = "Total number of packets sent from the network interface"
    }
    columns {
      name    = "txBytes"
      type    = "bigint"
      comment = "Total number of packets sent from the network interface"
    }
    columns {
      name    = "txPacketsDropped"
      type    = "bigint"
      comment = "Total number of transmit packets dropped on the network interface"
    }
  }

  lifecycle {
    create_before_destroy = true
    ignore_changes        = []
  }

  depends_on = [
    aws_glue_catalog_database.aws_glue_machine_metrics_database
  ]
}

#resource "aws_glue_partition" "example" {
#  database_name = aws_glue_catalog_database.aws_glue_machine_metrics_database.name
#  table_name    = aws_glue_catalog_table.machine_metric_table.name
#  partition_values        = ["2022","12","23","22"]
#}

#resource "aws_glue_crawler" "device-metrics-crawler" {
#  database_name = aws_glue_catalog_database.aws_glue_machine_metrics_database.name
#  name          = "${var.stack_name}-device-metrics-crawler"
#  description   = "Crawler for device metrics"
#  role          = data.aws_iam_role.machine_metrics_glue_role.arn
#  schedule      = "cron(0/20 * * * ? *)"
#  table_prefix  = "${var.stack_name}-device-metrics-"
#  recrawl_policy {
#    recrawl_behavior = "CRAWL_EVERYTHING"
#  }
#
#  s3_target {
#    path = "s3://ajay-kinesis-output-bucket"
#  }
#  schema_change_policy {
#    delete_behavior = "LOG"
#    update_behavior = "UPDATE_IN_DATABASE"
#  }
#  lineage_configuration {
#    crawler_lineage_settings = "ENABLE"
#  }
#
#  lifecycle {
#    create_before_destroy = true
#    ignore_changes        = []
#  }
#  depends_on = [
#    aws_glue_catalog_database.aws_glue_machine_metrics_database
#  ]
#}
