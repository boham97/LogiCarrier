#!/bin/bash
cd /opt/robot-service
version=$(echo *.jar | grep -oP '\d+\.\d+\.\d+')
service=$(echo *.jar | cut -d'-' -f1,2)

sudo docker build -t $service:$version .
echo "build"
sudo docker run -d --name "$service-2" --network logicarrier-network -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \
 -e "spring.datasource.url=jdbc:mysql://mysql:3306/robot" \
 -e "spring_redis_host: redis" \
 -e "spring_kafka_host: kafka1" \
 -e "spring_kafka_port: 19092" \
  $service:$version
echo "successfully run!"
sudo docker stop "$service-1"
sudo docker rm "$service-1"
sudo docker image prune