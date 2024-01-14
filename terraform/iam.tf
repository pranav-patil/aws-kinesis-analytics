
data "aws_iam_policy_document" "kinesis_firehose_stream_assume_role" {

  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["firehose.amazonaws.com"]
    }

    condition {
      test = "StringEquals"

      values = [
        data.aws_caller_identity.current.account_id
      ]

      variable = "sts:ExternalId"
    }
  }
}

resource "aws_iam_role" "kinesis_firehose_stream_role" {
  name        = "${var.stack_name}MachineMetricsFirehose@${var.region}"
  description = "Assumed by informational metrics firehose delivery stream"
  assume_role_policy = data.aws_iam_policy_document.kinesis_firehose_stream_assume_role.json
}

module "machine_metrics_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "1.22.0"

  bucket                  = "${var.stack_name}-${var.region}-device-metrics-raw"
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
    Name        = "${var.stack_name}-device-metrics-raw"
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


data "aws_iam_policy_document" "machine_metrics_bucket_assume_policy" {
  statement {
    effect = "Allow"

    actions = [
      "s3:PutObject",
      "s3:GetObject",
      "s3:ListBucketMultipartUploads",
      "s3:AbortMultipartUpload",
      "s3:ListBucket",
      "s3:GetBucketLocation"
    ]

    resources = [
      module.machine_metrics_bucket.this_s3_bucket_arn,
      "${module.machine_metrics_bucket.this_s3_bucket_arn}/*"
    ]
  }
}

resource "aws_iam_policy" "machine_metrics_bucket_policy" {
  name   = "${var.stack_name}MachineMetricsS3Policy@${var.region}"
  path   = "/"
  policy = data.aws_iam_policy_document.machine_metrics_bucket_assume_policy.json
}

resource "aws_iam_role" "machine_metrics_bucket_role" {
  name        = "${var.stack_name}MachineMetricsS3Role@${var.region}"
  description = "Assumed by Device Metrics S3 bucket"
  assume_role_policy = data.aws_iam_policy_document.machine_metrics_bucket_assume_policy.json
}

resource "aws_iam_role_policy_attachment" "machine_metrics_bucket_policy_attachment" {
  role       = aws_iam_role.machine_metrics_bucket_role.name
  policy_arn = aws_iam_policy.machine_metrics_bucket_policy.arn
}

data "aws_iam_policy_document" "kinesis_metric_stream_policy" {
  statement {
    effect = "Allow"
    actions = [
      "kinesis:DescribeStream",
      "kinesis:GetShardIterator",
      "kinesis:GetRecords",
      "kinesis:ListShards",
      "kinesis:ListStreams",
      "kinesis:PutRecord",
      "kinesis:PutRecords"
    ]
    resources = [aws_kinesis_stream.machine_metrics_kinesis_stream.arn]
  }
}

data "aws_iam_policy_document" "analytic_assumerole_policy" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["kinesisanalytics.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "analytic_app_role" {
  name               = "analytic-App_Role"
  assume_role_policy = data.aws_iam_policy_document.analytic_assumerole_policy.json
}

data "aws_iam_policy_document" "analytic_app_loggroup" {
  statement {
    actions = [
      "logs:CreateLogGroup",
      "logs:DescribeLogStreams",
      "logs:DescribeLogGroups",
      "logs:CreateLogStream",
      "logs:PutDestination",
      "logs:PutLogEvents"
    ]
    resources = ["*"]
    effect    = "Allow"
  }
}

resource "aws_iam_role_policy" "analytic_app_policy_loggroup" {
  name   = "analytic_app_policy_loggroup"
  role   = aws_iam_role.analytic_app_role.name
  policy = data.aws_iam_policy_document.analytic_app_loggroup.json
}


data "aws_iam_policy_document" "machine_metrics_firehose_delivery_policy" {
  statement {
    actions = [
      "glue:GetTableVersions"
    ]
    resources = [
      "arn:aws:glue:${var.region}:${data.aws_caller_identity.current.account_id}:table/${var.stack_name}_metrics_database/*",
      "arn:aws:glue:${var.region}:${data.aws_caller_identity.current.account_id}:database/${var.stack_name}_metrics_database",
      "arn:aws:glue:${var.region}:${data.aws_caller_identity.current.account_id}:catalog"
    ]
    effect    = "Allow"
  }
  statement {
    actions = [
      "s3:AbortMultipartUpload",
      "s3:GetBucketLocation",
      "s3:GetObject",
      "s3:ListBucket",
      "s3:ListBucketMultipartUploads",
      "s3:PutObject"
    ]
    resources = [
      "arn:aws:s3:::${var.stack_name}*"
    ]
    effect    = "Allow"
  }
  statement {
    actions = [
      "lambda:InvokeFunction"
    ]
    resources = [
      "arn:aws:lambda:${var.region}:${data.aws_caller_identity.current.account_id}:function:*"
    ]
    effect    = "Allow"
  }
  statement {
    actions = [
      "logs:PutLogEvents"
    ]
    resources = [
      "arn:aws:logs:${var.region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/kinesisfirehose/${var.stack_name}*:log-stream:*"
    ]
    effect    = "Allow"
  }
  statement {
    actions = [
      "kinesis:DescribeStream",
      "kinesis:GetShardIterator",
      "kinesis:GetRecords"
    ]
    resources = [
      "arn:aws:kinesis:${var.region}:${data.aws_caller_identity.current.account_id}:stream/${var.stack_name}*"
    ]
    effect    = "Allow"
  }
}

resource "aws_iam_role" "machine_metrics_firehose_delivery_role" {
  name               = "machine_metrics_firehose_delivery_role"
  assume_role_policy = data.aws_iam_policy_document.machine_metrics_firehose_delivery_policy.json
}

data "aws_iam_policy_document" "machine_metrics_kinesis_analytics_policy" {
  statement {
    actions = [
      "s3:PutObject",
      "s3:GetObject",
      "s3:GetObjectRetention",
      "s3:DeleteObjectVersion",
      "s3:ListBucketVersions",
      "s3:ListBucket",
      "s3:GetBucketVersioning",
      "s3:DeleteObject",
      "s3:GetBucketLocation",
      "s3:GetObjectVersion"
    ]
    resources = [
      "arn:aws:s3:::${var.stack_name}*",
      "arn:aws:s3:::*/*"
    ]
    effect    = "Allow"
  }
  statement {
    actions = [
      "logs:PutMetricFilter",
      "logs:DescribeLogGroups",
      "logs:DescribeLogStreams",
      "logs:GetLogEvents",
      "logs:PutLogEvents"
    ]
    resources = [
      "arn:aws:logs:*:${data.aws_caller_identity.current.account_id}:log-group:*:log-stream:*",
      "arn:aws:logs:${var.region}:${data.aws_caller_identity.current.account_id}:log-group:${var.stack_name}*"
    ]
    effect    = "Allow"
  }
  statement {
    actions = [
      "logs:GetLogRecord",
      "logs:GetQueryResults",
      "logs:PutDestination",
      "logs:GetLogDelivery",
      "logs:DescribeDestinations",
      "logs:ListLogDeliveries"
    ]
    resources = [
      "*"
    ]
    effect    = "Allow"
  }
  statement {
    actions = [
      "kinesis:PutRecord",
      "kinesis:PutRecords",
      "kinesis:GetShardIterator",
      "kinesis:GetRecords",
      "kinesis:DescribeStream",
      "kinesis:ListStreams",
      "kinesis:ListShards"
    ]
    resources = [
      "arn:aws:kinesis:${var.region}:${data.aws_caller_identity.current.account_id}:stream/${var.stack_name}*-stream"
    ]
    effect    = "Allow"
  }
  statement {
    actions = [
      "firehose:DescribeDeliveryStream",
      "firehose:PutRecord",
      "firehose:PutRecordBatch"
    ]
    resources = [
      "arn:aws:firehose:${var.region}:${data.aws_caller_identity.current.account_id}:deliverystream/${var.stack_name}*-stream"
    ]
    effect    = "Allow"
  }
}

resource "aws_iam_role" "machine_metrics_kinesis_analytics_role" {
  name               = "machine_metrics_kinesis_analytics_role"
  assume_role_policy = data.aws_iam_policy_document.machine_metrics_kinesis_analytics_policy.json
}

