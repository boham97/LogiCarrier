#!/bin/bash
sudo docker stop gateway-service
sudo docker rm gateway-service
sudo docker image prune