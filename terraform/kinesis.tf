
module "machine_metrics_athena_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "1.22.0"

  bucket                  = "${var.stack_name}-${var.region}-machine-metrics-athena"
  acl                     = "private"
#  block_public_acls       = true
#  block_public_policy     = true
#  ignore_public_acls      = true
#  restrict_public_buckets = true
  force_destroy           = true

  lifecycle_rule = [
    {
      enabled = true
      expiration = {
        days = 30
      }
    }
  ]

  tags = {
    Name        = "${var.stack_name}-machine-metrics-raw"
  }

#  server_side_encryption_configuration = {
#    rule = {
#      apply_server_side_encryption_by_default = {
#        sse_algorithm     = "aws:kms"
#        kms_master_key_id = module.es_snapshot_kms.arn
#      }
#    }
#  }
}

#data "aws_lambda_function" "metrics_converter_lambda" {
#  function_name = "${var.stack_name}-machine-metrics-converter"
#}

resource "aws_kinesis_firehose_delivery_stream" "machine_metrics_firehose_stream" {
  name        = "${var.stack_name}-firehose-metrics-stream"
  destination = "extended_s3"

  extended_s3_configuration {
    role_arn        = aws_iam_role.machine_metrics_firehose_delivery_role.arn
    bucket_arn      = module.machine_metrics_athena_bucket.this_s3_bucket_arn
    buffer_size     = 64
    buffer_interval = 60
    s3_backup_mode      = "Disabled"
    prefix              = "metrics/metric=!{partitionKeyFromQuery:metric}/year=!{timestamp:yyyy}/month=!{timestamp:MM}/day=!{timestamp:dd}/"
    error_output_prefix = "firehose-errors/year=!{timestamp:yyyy}/month=!{timestamp:MM}/day=!{timestamp:dd}/!{firehose:error-output-type}/hour=!{timestamp:HH}/!{firehose:error-output-type}"
    compression_format  = "UNCOMPRESSED"

    dynamic_partitioning_configuration {
      enabled = true
    }

    processing_configuration {
      enabled = "true"

      processors {
        type = "MetadataExtraction"

        parameters {
          parameter_name  = "MetadataExtractionQuery"
          parameter_value = "{metric:.metric}"
        }

        parameters {
          parameter_name  = "JsonParsingEngine"
          parameter_value = "JQ-1.6"
        }
      }

      processors {
        type = "RecordDeAggregation"

        parameters {
          parameter_name  = "SubRecordType"
          parameter_value = "JSON"
        }
      }
      # record delimited unification
      processors {
        type = "AppendDelimiterToRecord"
      }
    }

#    s3_backup_configuration {
#      role_arn   = aws_iam_role.kinesis_firehose_stream_role.arn
#      bucket_arn = aws_s3_bucket.backup_bucket.arn
#      prefix     = "backup"
#
#      cloudwatch_logging_options {
#        enabled         = true
#        log_group_name  = aws_cloudwatch_log_group.machine_metrics_firehose_stream_logging_group.name
#        log_stream_name = aws_cloudwatch_log_stream.machine_metrics_firehose_stream_logging_stream.name
#      }
#    }

    cloudwatch_logging_options {
      enabled         = "true"
      log_group_name  = aws_cloudwatch_log_group.machine_metrics_firehose_stream_logging_group.name
      log_stream_name = aws_cloudwatch_log_stream.machine_metrics_firehose_stream_logging_stream.name
    }

    #parquet creation based on glue tables
    data_format_conversion_configuration {
      enabled = "true"

      input_format_configuration {
        deserializer {
          open_x_json_ser_de {}
#          hive_json_ser_de {}
        }
      }

      output_format_configuration {
        serializer {
          parquet_ser_de {}
        }
      }

      schema_configuration {
        database_name = aws_glue_catalog_database.aws_glue_machine_metrics_database.name
        table_name    = aws_glue_catalog_table.machine_metric_table.name
        role_arn      = aws_iam_role.machine_metrics_firehose_delivery_role.arn
        region        = var.region
      }
    }
  }
}

resource "aws_cloudwatch_log_group" "machine_metrics_firehose_stream_logging_group" {
  name = "/aws/kinesisfirehose/${var.stack_name}-metrics-kinesis-firehose-s3-stream"
}

resource "aws_cloudwatch_log_stream" "machine_metrics_firehose_stream_logging_stream" {
  log_group_name = aws_cloudwatch_log_group.machine_metrics_firehose_stream_logging_group.name
  name           = "DeviceMetricsFirehoseDelivery"
}

resource "aws_kinesis_stream" "machine_metrics_kinesis_stream" {
  name             = "${var.stack_name}-kinesis-metrics-stream"
  shard_count      = 1
  retention_period = 24    # Hours
  #TODO encryption

  shard_level_metrics = [
    "IncomingRecords",
    "OutgoingRecords"
  ]

  tags = {
    Environment = "dev"
  }
}

resource "aws_s3_bucket" "machine_metrics_kinesis_analytics_code" {
  bucket        = "${var.stack_name}-metrics-analytics-code"
  acl           = "private"
  force_destroy = true

  versioning {
    enabled = true
  }

  tags = {
    Name        = "machine-metrics-analytics-code"
    Environment = "test"
  }
}

resource "aws_s3_bucket_object" "analytic_app_jar" {
  bucket = aws_s3_bucket.machine_metrics_kinesis_analytics_code.bucket
  key    = "kinesis-data-analytics.jar"
  source = "../kinesis-data-analytics.jar"
  force_destroy = true
}

# PropertyGroup are in the application_properties.json
resource "aws_kinesisanalyticsv2_application" "metrics_analytics_app" {
  name                   = "${var.stack_name}-metrics-flink-application"
  description            = " Flink application to parse and act on the metrics records"
  runtime_environment    = "FLINK-1_13"
  service_execution_role = aws_iam_role.machine_metrics_kinesis_analytics_role.arn
  start_application = true

  application_configuration {
    application_code_configuration {
      code_content {
        s3_content_location {
          bucket_arn = aws_s3_bucket.machine_metrics_kinesis_analytics_code.arn
          file_key   = aws_s3_bucket_object.analytic_app_jar.key
          object_version = aws_s3_bucket_object.analytic_app_jar.version_id
        }
      }
      code_content_type = "ZIPFILE"
    }

    environment_properties {
      property_group {
        property_group_id = "consumer.config.0"
        property_map = {
          "flink.parallelism" = "2"
          "aws.region" = var.region
          "input.stream.name" = aws_kinesis_stream.machine_metrics_kinesis_stream.name
          "output.stream.name" = aws_kinesis_firehose_delivery_stream.machine_metrics_firehose_stream.name
          "flink.stream.initpos" = "LATEST"
        }
      }
    }
    application_snapshot_configuration {
      snapshots_enabled = false
    }
    flink_application_configuration {
      checkpoint_configuration {
        configuration_type = "DEFAULT"
      }

      monitoring_configuration {
        configuration_type = "CUSTOM"
        log_level          = "DEBUG"
#        metrics_level      = "TASK"
        metrics_level      = "APPLICATION"
      }

      parallelism_configuration {
        auto_scaling_enabled = false
        configuration_type   = "CUSTOM"
        parallelism          = 2
        parallelism_per_kpu  = 4
      }
    }
  }

 cloudwatch_logging_options {
    log_stream_arn = aws_cloudwatch_log_stream.analytics_app_log_stream.arn
  }

  tags = {
    Environment = "demo"
  }
}

resource "aws_cloudwatch_log_group" "analytics_app_log_group" {
  name = "${var.stack_name}-metrics-analytics-application"
}

resource "aws_cloudwatch_log_stream" "analytics_app_log_stream" {
  name           = "MetricsAnalyticsApplication"
  log_group_name = aws_cloudwatch_log_group.analytics_app_log_group.name
}
