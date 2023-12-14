#!/bin/bash
cd /opt/gateway-service
version=$(echo *.jar | grep -oP '\d+\.\d+\.\d+')
service=$(echo *.jar | cut -d'-' -f1,2)

sudo docker run -d --name "$service-1" -p 8000:8000 --network logicarrier-network -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" $service:$version
echo "successfully run!"
sudo docker stop "$service-2"
sudo docker rm "$service-2"