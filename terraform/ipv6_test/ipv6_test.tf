resource "aws_instance" "ipv6_test" {
  ami                  = "ami-0e2c8caa4b6378d8c"
  instance_type        = "t2.nano"
  subnet_id            = var.public_subnet_id
  key_name             = var.key_name
  ipv6_address_count   = 1
  associate_public_ip_address = true   

  vpc_security_group_ids = [aws_security_group.ipv6_test.id]
  
  user_data = <<-EOF
              #!/bin/bash
              apt-get update
              apt-get install -y nginx curl iproute2 iputils-ping
              
              # Configure nginx to listen on IPv6
              cat > /etc/nginx/sites-available/default <<'END'
              server {
                  listen 80;
                  listen [::]:80;
                  
                  root /var/www/html;
                  index index.html;
                  
                  server_name _;
                  
                  location / {
                      try_files $uri $uri/ =404;
                  }
              }
              END
              
              # Create test page showing IP information
              cat > /var/www/html/index.html <<'END'
              <!DOCTYPE html>
              <html>
              <head><title>IPv6 Test Page</title></head>
              <body>
              <h1>Server IP Information</h1>
              <pre>
              $(hostname -I)
              $(ip -6 addr show)
              </pre>
              </body>
              </html>
              END
              
              systemctl restart nginx
              EOF
  tags = {
    Name = "ipv6-test"
  }
}

resource "aws_security_group" "ipv6_test" {
  name        = "ipv6-test-sg"
  description = "Security group for IPv6 test instance"
  vpc_id      = var.vpc_id
  
  ingress {
    from_port        = 80
    to_port          = 80
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
  
  ingress {
    from_port        = 22
    to_port          = 22
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
  
  ingress {
    from_port        = -1
    to_port          = -1
    protocol         = "icmp"
    cidr_blocks      = ["0.0.0.0/0"]
  }
  
  ingress {
    from_port        = -1
    to_port          = -1
    protocol         = "icmpv6"
    ipv6_cidr_blocks = ["::/0"]
  }
  
  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
  
  tags = {
    Name = "ipv6-test-sg"
  }
}

output "instance_ipv6" {
  value = aws_instance.ipv6_test.ipv6_addresses
}

output "instance_public_ip" {
  value = aws_instance.ipv6_test.public_ip
}

output "instance_public_dns" {
  value = aws_instance.ipv6_test.public_dns
}