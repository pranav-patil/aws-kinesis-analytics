
resource "aws_athena_named_query" "athena_machine_metric_query" {

  name     = "Appliance metrics query"
  database = aws_glue_catalog_database.aws_glue_machine_metrics_database.name
  query    = "SELECT * FROM ${aws_glue_catalog_database.aws_glue_machine_metrics_database.name} limit 10;"

  description = "Query for the Appliance metrics"
  workgroup   = aws_athena_workgroup.athena_machine_metric_workgroup.id
  lifecycle {
    create_before_destroy = true
    ignore_changes        = []
  }

  depends_on = [
    aws_athena_workgroup.athena_machine_metric_workgroup,
    aws_glue_catalog_database.aws_glue_machine_metrics_database
  ]
}

resource "aws_athena_workgroup" "athena_machine_metric_workgroup" {

  name = "athena_machine_metric_workgroup"

  description   = "Workgroup for Machine Metrics"
  state         = "ENABLED"
  force_destroy = true
  configuration {
    publish_cloudwatch_metrics_enabled = true
    result_configuration {
      output_location = "s3://${module.machine_metrics_athena_bucket.this_s3_bucket_id}/output/"
    }
  }
  lifecycle {
    create_before_destroy = true
    ignore_changes        = []
  }

  depends_on = []
}
