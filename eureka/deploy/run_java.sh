#!/bin/bash
cd /opt/discovery-service
echo "배포"
version=$(echo *.jar | grep -oP '\d+\.\d+\.\d+')
echo $version
sudo docker build -t discovery-service:$version .
echo "build"
sudo docker run -d -p 8761:8761 --name discovery-service discovery-service:$version
echo "successfully run!"