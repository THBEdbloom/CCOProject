# General Settings
aws_region  = "us-east-1"

# EC2 Configuration
instance_type       = "t2.micro"
ami_id             = "ami-0e2c8caa4b6378d8c"
asg_min_size       = 2
asg_max_size       = 4
asg_desired_capacity = 2

# RDS Configuration
db_instance_class    = "db.t4g.micro"
db_allocated_storage = 20
db_engine_version    = "8.0"
db_name             = "videothek"

# Resource Tags
tags = {
  Environment = "production"
  Project     = "backend-infrastructure"
  Terraform   = "true"
}