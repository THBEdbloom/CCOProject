provider "aws" {
  region     = var.aws_region
  # Zugangsdaten können über Umgebungsvariablen oder AWS CLI-Konfiguration bereitgestellt werden
  # access_key = ""
  # secret_key = ""
  # token = ""
}

# Benutzerdefiniertedaten-Template für EC2-Instanzen
# Wird verwendet, um Konfigurationsdaten an die Instanzen zu übergeben
data "template_file" "user_data" {
  template = file("script.tpl")
  vars = {
    db_endpoint = aws_db_instance.database.endpoint
    aws_s3_bucket = aws_s3_bucket.backend_uploads.id
    db_username = var.db_username
    db_password = var.db_password
  }
}

# Generierung eines SSH-Schlüsselpaares für EC2-Instanzen
resource "tls_private_key" "pk" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "backend" {
  key_name   = "backend-key"  
  public_key = tls_private_key.pk.public_key_openssh
}

# Speichert den privaten Schlüssel lokal für SSH-Zugriff
resource "local_file" "ssh_key" {
  filename = "${aws_key_pair.backend.key_name}.pem"
  content = tls_private_key.pk.private_key_pem
}

# Ermittelt verfügbare Availability Zones für die Region
data "aws_availability_zones" "available" {
  state = "available"
  
}

# Hauptkonfiguration des VPC mit IPv4- und IPv6-Unterstützung
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  assign_generated_ipv6_cidr_block = true

  tags = {
    Name = "main-vpc"
  }
}

# Erstellung öffentlicher Subnetze in verschiedenen AZs für hohe Verfügbarkeit
resource "aws_subnet" "public" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index + 1}.0/24"
  map_public_ip_on_launch = true

  # IPv6-Konfiguration
  ipv6_cidr_block   = cidrsubnet(aws_vpc.main.ipv6_cidr_block, 8, count.index)
  enable_resource_name_dns_aaaa_record_on_launch = true
  assign_ipv6_address_on_creation = true

  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = "public-subnet-${count.index + 1}"
  }
}

# Erstellung privater Subnetze für Backend-Dienste und Datenbank
resource "aws_subnet" "private" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index + 10}.0/24"

  ipv6_cidr_block   = cidrsubnet(aws_vpc.main.ipv6_cidr_block, 8, count.index + 10)
  enable_resource_name_dns_aaaa_record_on_launch = true
  assign_ipv6_address_on_creation = true

  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = "private-subnet-${count.index + 1}"
  }
}

# Internet Gateway für Internetzugriff aus öffentlichen Subnetzen
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "main-igw"
  }
}

# Egress-only Internet Gateway für IPv6 in privaten Subnetzen
# Ermöglicht ausgehenden IPv6-Verkehr aus privaten Subnetzen
resource "aws_egress_only_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "main-eigw"
  }
}
resource "aws_eip" "nat" {
  domain = "vpc"

  depends_on = [aws_internet_gateway.main]
}

# NAT Gateway Setup für Internetzugriff aus privaten Subnetzen
resource "aws_nat_gateway" "main" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public[0].id

  tags = {
    Name = "main-nat-gateway"
  }

  depends_on = [aws_internet_gateway.main]
}

# Routentabellen für öffentliche und private Subnetze
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  # Route für ausgehenden IPv4-Verkehr über Internet Gateway
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  # Route für ausgehenden IPv6-Verkehr
  route {
    ipv6_cidr_block = "::/0"
    gateway_id      = aws_internet_gateway.main.id
  }

  tags = {
    Name = "public-rt"
  }
}

# Routentabelle für private Subnetze mit NAT Gateway
resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id

  # IPv4-Verkehr über NAT Gateway
  route {
    cidr_block = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.main.id
  }

  # IPv6-Verkehr über Egress-only Gateway
  route {
    ipv6_cidr_block = "::/0"
    egress_only_gateway_id = aws_egress_only_internet_gateway.main.id
  }

  tags = {
    Name = "private-rt"
  }
}

resource "aws_route_table_association" "public" {
  count          = length(aws_subnet.public)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "private" {
  count          = length(aws_subnet.private)
  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private.id
}
# Network ACLs als zusätzliche Sicherheitsebene
resource "aws_network_acl" "public" {
  vpc_id     = aws_vpc.main.id
  subnet_ids = aws_subnet.public[*].id

  # Erlaubt eingehenden HTTP-Verkehr für den ALB (IPv4)
  ingress {
    protocol   = "tcp"
    rule_no    = 100
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 80
    to_port    = 80
  }

  # Erlaubt eingehenden HTTP-Verkehr für den ALB
  ingress {
    protocol        = "tcp"
    rule_no         = 101
    action          = "allow"
    ipv6_cidr_block = "::/0"
    from_port       = 80
    to_port         = 80
  }

  ingress {
    protocol   = "tcp"
    rule_no    = 200
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 1024
    to_port    = 65535
  }

  ingress {
    protocol        = "tcp"
    rule_no         = 201
    action          = "allow"
    ipv6_cidr_block = "::/0"
    from_port       = 1024
    to_port         = 65535
  }

  # Für debugging, SSH Verbindung öffentlich erlaubt 
  /*
  ingress {
    protocol        = "tcp"
    rule_no         = 300
    action          = "allow"
    cidr_block      = "0.0.0.0/0"
    from_port       = 22
    to_port         = 22
  }
  */ 

  # Erlaubt ausgehenden Verkehr (IPv4)
  egress {
    protocol   = -1
    rule_no    = 100
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }

  # Erlaubt ausgehenden Verkehr (IPv6)
  egress {
    protocol        = -1
    rule_no         = 101
    action          = "allow"
    ipv6_cidr_block = "::/0"
    from_port       = 0
    to_port         = 0
  }

  tags = {
    Name = "public-nacl"
  }
}

# Private Subnet NACL
resource "aws_network_acl" "private" {
  vpc_id     = aws_vpc.main.id
  subnet_ids = aws_subnet.private[*].id

  # Erlaubt eingehenden Verkehr vom ALB (port 8080, alle Public Subnets)
  ingress {
      protocol   = "tcp"
      rule_no    = 100
      action     = "allow"
      cidr_block = "10.0.1.0/24" 
      from_port  = 8080
      to_port    = 8080
  }

  ingress {
      protocol   = "tcp"
      rule_no    = 101
      action     = "allow"
      cidr_block = "10.0.2.0/24"  
      from_port  = 8080
      to_port    = 8080
  }

  # Allow inbound return traffic for outbound connections
  ingress {
    protocol   = "tcp"
    rule_no    = 200
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 1024
    to_port    = 65535
  }

  ingress {
    protocol        = "tcp"
    rule_no         = 201
    action          = "allow"
    ipv6_cidr_block = "::/0"
    from_port       = 1024
    to_port         = 65535
  }

  # Allow outbound traffic (IPv4)
  egress {
    protocol   = -1
    rule_no    = 100
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }

  # Allow outbound traffic (IPv6)
  egress {
    protocol        = -1
    rule_no         = 101
    action          = "allow"
    ipv6_cidr_block = "::/0"
    from_port       = 0
    to_port         = 0
  }

  # Allow outbound to RDS
  egress {
    protocol   = "tcp"
    rule_no    = 200
    action     = "allow"
    cidr_block = "10.0.0.0/16" 
    from_port  = 3306
    to_port    = 3306
  }

  tags = {
    Name = "private-nacl"
  }
}

# S3-Bucket für Anwendungs-Uploads mit Verschlüsselung und Blockierung öffentlicher Zugriffe
resource "aws_s3_bucket" "backend_uploads" {
  bucket_prefix = "backend-uploads-"  
  force_destroy = true               

  tags = merge(var.tags, {
    Name = "backend-uploads"
  })
}


resource "aws_s3_bucket_server_side_encryption_configuration" "backend_uploads" {
  bucket = aws_s3_bucket.backend_uploads.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "backend_uploads" {
  bucket = aws_s3_bucket.backend_uploads.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Output the bucket name
output "s3_bucket_name" {
  value = aws_s3_bucket.backend_uploads.id
  description = "Name of the created S3 bucket"
}

output "s3_bucket_arn" {
  value = aws_s3_bucket.backend_uploads.arn
  description = "ARN of the created S3 bucket"
}


resource "aws_lb" "backend" {
  name               = "backend-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id
  ip_address_type    = "dualstack"

}

resource "aws_lb_listener" "backend" {
  load_balancer_arn = aws_lb.backend.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.backend.arn
  }
}

resource "aws_lb_target_group" "backend" {
  name        = "backend-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "instance"

  health_check {
    path                = "/actuator/health"
    healthy_threshold   = 2
    unhealthy_threshold = 3  # default value
    timeout             = 6  # HTTP default
    interval            = 30  # default
  }
  deregistration_delay = 180 # default is 300, but for testing its easier to reduce the time
}

resource "aws_launch_template" "backend" {
  name_prefix   = "backend-template"
  image_id      = var.ami_id
  instance_type = var.instance_type
  key_name      = aws_key_pair.backend.key_name

  user_data = base64encode(data.template_file.user_data.rendered)

  lifecycle {
    create_before_destroy = true
  }

  network_interfaces {
    associate_public_ip_address = false
    security_groups            = [aws_security_group.backend.id]
    ipv6_address_count        = 1
  }

  tag_specifications {
    resource_type = "instance"
    tags = merge(var.tags, {
      Name = "backend-server"
    })
  }

  description = "Launch template for backend - Version ${timestamp()}"
  # dient nur dazu, dass bei jedem terraform apply hier ein Update der Instanzen stattfindet
  # (falls aws refresh methoden verwendet werden, kann dies verändert werden oder durch Versionzahlen geändert werden)
}

resource "aws_autoscaling_policy" "target_tracking_cpu" {
  name                   = "target-tracking-cpu"
  autoscaling_group_name = aws_autoscaling_group.backend.name
  policy_type           = "TargetTrackingScaling"

  target_tracking_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ASGAverageCPUUtilization"
    }
    target_value = 60.0
    disable_scale_in = false
  }

}

resource "aws_autoscaling_policy" "predictive_scaling" {
  name                   = "predictive-scaling-policy"
  autoscaling_group_name = aws_autoscaling_group.backend.name
  policy_type           = "PredictiveScaling"

  predictive_scaling_configuration {
    metric_specification {
      target_value = 60
      predefined_scaling_metric_specification {
        predefined_metric_type = "ASGAverageCPUUtilization"
        resource_label        = "backend-predictive-scaling"
      }
      predefined_load_metric_specification {
        predefined_metric_type = "ASGTotalCPUUtilization"
        resource_label        = "backend-predictive-scaling"
      }
    }
    mode                          = "ForecastAndScale"
    scheduling_buffer_time        = 300
    max_capacity_breach_behavior  = "IncreaseMaxCapacity"
    max_capacity_buffer          = 10
  }
}
/*
Schedule-based, für spezielle Events / wenn bekannt ist, dass beispielsweise die Aktivität Nachts immer geringer ist.

# East Coast Schedule
resource "aws_autoscaling_schedule" "us_east_early_morning" {
  scheduled_action_name   = "us-east-early-morning"
  min_size               = 2
  max_size               = 4
  desired_capacity       = 2
  recurrence            = "0 5 * * *"
  time_zone             = "Etc/GMT-6"
  autoscaling_group_name = aws_autoscaling_group.backend.name
}

resource "aws_autoscaling_schedule" "us_east_scale_down_night" {
  scheduled_action_name   = "us-east-scale-down-night"
  min_size               = 1
  max_size               = 4
  desired_capacity       = 1
  recurrence            = "0 1 * * *"
  time_zone             = "Etc/GMT-6"
  autoscaling_group_name = aws_autoscaling_group.backend.name
}
*/
resource "aws_autoscaling_group" "backend" {
  name               = "autoscaling_backend"
  desired_capacity   = var.asg_desired_capacity
  max_size          = var.asg_max_size
  min_size          = var.asg_min_size
  target_group_arns = [aws_lb_target_group.backend.arn]
  vpc_zone_identifier = aws_subnet.private[*].id
  health_check_type = "ELB"
  health_check_grace_period = 300

  launch_template {
    id      = aws_launch_template.backend.id
    version = "$Latest"
  }

  dynamic "tag" {
    for_each = merge(var.tags, {
      Version = "v1.0.${timestamp()}"
        # dient nur dazu, dass bei jedem terraform apply hier ein Update der Instanzen stattfindet
        # (falls aws refresh methoden verwendet werden, kann dies verändert werden oder durch Versionzahlen geändert werden)
    })
    content {
      key                 = tag.key
      value               = tag.value
      propagate_at_launch = true
    }
  }

  instance_refresh {
    strategy = "Rolling"
    preferences {
      min_healthy_percentage = 50
      instance_warmup = 120
    }
    triggers = ["tag"]
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ALB Security Group
resource "aws_security_group" "alb" {
  name        = "alb-sg"
  description = "Security group for ALB"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    ipv6_cidr_blocks = ["::/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    ipv6_cidr_blocks = ["::/0"]
  }

   tags = merge(var.tags, {
    Name = "alb-sg"
  })
}

# Database subnet group
resource "aws_db_subnet_group" "main" {
  name       = "main"
  subnet_ids = aws_subnet.private[*].id
}

# RDS Instance
resource "aws_db_instance" "database" {
  identifier           = "database"
  allocated_storage    = var.db_allocated_storage
  storage_type        = "gp3"
  engine              = "mysql"
  engine_version      = var.db_engine_version
  instance_class      = var.db_instance_class
  username            = var.db_username
  password            = var.db_password
  db_name             = var.db_name

  skip_final_snapshot = true
  performance_insights_enabled = false
  multi_az                    = false

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.database.id]


}

# Security Groups
resource "aws_security_group" "database" {
  name        = "database-sg"
  description = "Security group for database"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.backend.id]
  }

  tags = merge(var.tags, {
    Name = "backend-sg"
  })
}

resource "aws_security_group" "backend" {
    name        = "backend-sg"
    description = "Security group for backend EC2 instances"
    vpc_id      = aws_vpc.main.id
  
    ingress {
      from_port       = 8080
      to_port         = 8080
      protocol        = "tcp"
      security_groups = [aws_security_group.alb.id]
    }

    # only for debugging - not production 
    ingress {
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"] 
    }

    egress {
      from_port   = 0
      to_port     = 0
      protocol    = "-1"
      cidr_blocks = ["0.0.0.0/0"]
    }

    egress {
      from_port        = 0
      to_port          = 0
      protocol         = "-1"
      ipv6_cidr_blocks = ["::/0"]
    }
     tags = merge(var.tags, {
    Name = "database-sg"
  })
}

/*
Testing the IPv6 Connection using public subnet  / public IPs allowed 
module "ipv6_test" {
  source = "./ipv6_test" 
  
  vpc_id           = aws_vpc.main.id
  public_subnet_id = aws_subnet.public[0].id
  key_name         = aws_key_pair.backend.key_name
}

output "ipv6_test_ipv6" {
  value = module.ipv6_test.instance_ipv6
}

output "ipv6_test_public_ip" {
  value = module.ipv6_test.instance_public_ip
}
*/

output "backend_url" {
  value = "http://${aws_lb.backend.dns_name}"
}


