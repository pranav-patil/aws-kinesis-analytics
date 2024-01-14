
terraform {
  backend "s3" {
    encrypt = true
  }
}

# Provider configuration
provider "aws" {
  region = var.region

  assume_role {
    # TODO Support accounts other than c1-network-rnd (801659726931)
    role_arn = "arn:aws:iam::801659726931:role/C1NetworkRoleForRndStackDeploy"
  }

  ignore_tags {
    keys = ["Date"]
  }
}

locals {
  # These tags are static for the lifetime of a stack.
  tags = {
    StackName = var.stack_name
    CreatedBy = var.user
    Date      = timestamp()
    Purpose   = "rd"
  }

  # The ECS cluster tags includes the following non-standard, mutable tags
  # Updated   Timestamp of last time deploy.py -a was run on stack.
  #           Used by the rdstacks-cleanup job to calculate if lifetime has expired.
  # Lifetime  Integer setting lifetime of stack in days.
  # Revision  Version number of infra-rnd used to deploy stack.
  cluster_tags = {
    Updated  = timestamp()
    Lifetime = var.lifetime
  }

  # These ports are used in common Security Groups and so are fixed
  db_port    = 7455
  cache_port = 6379

  event_key_id = "alias/KMS-event-endpoint"
#  rnd_zone     = "rnd.network.us-1.dev-cloudone.trendmicro.com"
}

data "aws_caller_identity" "current" {}

# Find the common VPC
data "aws_vpc" "common" {
  tags = {
    Name = "rdstacks"
  }
}


data "aws_ssm_parameter" "app_subnet_ids" {
  name = "/application/vpc_subnets"
}

data "aws_iam_role" "lambda" {
  name = "${var.stack_name}LambdaExecution@${var.region}"
}

data "aws_kms_key" "lambda" {
  key_id = "alias/KMS-lambda"
}

data "aws_kms_key" "s3" {
  key_id = "alias/KMS-S3"
}

data "aws_kms_key" "sqs_account_lifecycle" {
  key_id = "alias/KMS-SQS"
}

data "aws_iam_role" "lambda_execution" {
  name = "${var.stack_name}LambdaExecution@${var.region}"
}

data "aws_kms_key" "cloudwatch" {
  key_id = "alias/KMS-CW"
}
