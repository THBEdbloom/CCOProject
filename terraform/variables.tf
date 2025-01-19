
# AWS Credentials
variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
}

# EC2 Configuration
variable "instance_type" {
  description = "EC2 instance type for ec2 servers"
  type        = string
}

variable "ami_id" {
  description = "AMI ID for servers"
  type        = string
}

variable "asg_min_size" {
  description = "Minimum size for the Auto Scaling Group"
  type        = number
}

variable "asg_max_size" {
  description = "Maximum size for the Auto Scaling Group"
  type        = number
}

variable "asg_desired_capacity" {
  description = "Desired capacity for the Auto Scaling Group"
  type        = number
}

# RDS Configuration
variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
}

variable "db_allocated_storage" {
  description = "Storage for RDS instance (in GB)"
  type        = number
}

variable "db_engine_version" {
  description = "MySQL engine version"
  type        = string
}

variable "db_name" {
  description = "Name of the database to create"
  type        = string
}

# Resource Tags
variable "tags" {
  description = "Common tags"
  type        = map(string)
}