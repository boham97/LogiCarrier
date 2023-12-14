#!/bin/bash
cd /opt/cdtest
version=$(echo *.jar | grep -oP '\d+\.\d+\.\d+')
service=$(echo *.jar | cut -d'-' -f1,2)

sudo docker build -t $service:$version .
echo "build"
sudo docker run -d --name "$service-2" $service:$version
echo "successfully run!"
sudo docker stop "$service-1"
sudo docker rm "$service-1"
sudo docker image prune