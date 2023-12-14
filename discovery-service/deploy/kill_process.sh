#!/bin/bash
cd /opt/discovery-service
version=$(echo *.jar | grep -oP '\d+\.\d+\.\d+')
service=$(echo *.jar | cut -d'-' -f1,2)

sudo docker build -t $service:$version .
echo "build"
sudo docker run -d --name "$service-2" -p 8761:8761 --network logicarrier-network $service:$version
echo "successfully run!"
sudo docker stop "$service-1"
sudo docker rm "$service-1"
sudo docker image prune