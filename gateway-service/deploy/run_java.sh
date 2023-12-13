#!/bin/bash
cd /opt/gateway
echo "배포"
version=$(echo *.jar | grep -oP '\d+\.\d+\.\d+')
echo $version
sudo docker build -t gateway-service:$version .
echo "build"
sudo docker run -d -p 8080:8080 --network logicarrier-network --name gateway-service gateway-service:$version
echo "successfully run!"