variable "vpc_id" {
  description = "ID of the VPC"
  type        = string
}

variable "public_subnet_id" {
  description = "ID of the public subnet where the IPv6 test instance will be launched"
  type        = string
}

variable "key_name" {
  description = "Name of the SSH key pair"
  type        = string
}