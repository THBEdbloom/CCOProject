#!/bin/bash
sudo apt-get update

DB_ENDPOINT="${db_endpoint}"
DB_USERNAME="${db_username}"
DB_PASSWORD="${db_password}"
AWS_S3_BUCKET="${aws_s3_bucket}"

AWS_ACCESS_KEY="${aws_access_key}"
AWS_SECRET_KEY="${aws_secret_key}"
AWS_SESSION_TOKEN="${aws_session_token}"

sudo apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    software-properties-common

# official GPG key
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# Docker repository
echo \
"deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
$(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update

sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

sudo systemctl start docker
sudo systemctl enable docker

sudo usermod -aG docker ubuntu
sudo docker pull thbedbloom/ccoproject

sudo docker network create app-network

sudo docker run -d \
    --name spring-boot-app \
    --network app-network \
    -p 8080:8080 \
    --restart unless-stopped \
    -e SPRING_DATASOURCE_URL=jdbc:mysql://$DB_ENDPOINT/videothek \
    -e SPRING_DATASOURCE_USERNAME=$DB_USERNAME \
    -e SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD \
    -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY \
    -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_KEY \
    -e AWS_SESSION_TOKEN=$AWS_SESSION_TOKEN \
    -e AWS_S3_BUCKET=$AWS_S3_BUCKET \
    thbedbloom/ccoproject:app