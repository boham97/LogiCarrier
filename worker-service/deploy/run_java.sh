#!/bin/bash
cd /opt/cdtest
version=$(echo *.jar | grep -oP '\d+\.\d+\.\d+')
service=$(echo *.jar | cut -d'-' -f1,2)

sudo docker run -d --name "$service-1" $service:$version
echo "successfully run!"
sudo docker stop "$service-2"
sudo docker rm "$service-2"