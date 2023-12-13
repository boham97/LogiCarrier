#!/bin/bash
cd /opt/login
echo "배포"
version=$(echo *.jar | grep -oP '\d+\.\d+\.\d+')
echo $version
sudo docker build -t login-service:$version .
echo "build"
sudo docker run -d -p 8080:8080 --network logicarrier-network --name login-service login-service:$version -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" -e "spring.datasource.url=jdbc:mysql://mysql:3306/logiCarrier"  logicarrier/login-service:1.0.0
echo "successfully run!"