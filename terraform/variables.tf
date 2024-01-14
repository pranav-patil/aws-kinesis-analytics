#
# Terraform Variables required for RND Deployment
#
variable "stack_name" {
  description = "Stack Name"
  type        = string
}

variable "stage" {
  description = "Stage"
  default     = "rnd"
}

variable "user" {
  description = "User making infrastructure changes"
  type        = string
}

variable "lifetime" {
  description = "Lifetime of cluster in days"
  type        = number
  default     = 1
}

# Regions
variable "region" {
  description = "Deployment Region"
  type        = string
}

# Firehose
variable "firehose_stream" {
  description = "AWS Firehose log message delivery stream"
  type        = string
  default     = ""
}

variable "firehose_region" {
  description = "AWS Firehose log message delivery stream region"
  type        = string
  default     = ""
}

variable "enable_xray" {
  description = "Enable AWS X-Ray"
  type        = bool
  default     = false
}
