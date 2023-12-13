#!/bin/bash
sudo docker stop discovery-service
sudo docker rm discovery-service
sudo docker image prune