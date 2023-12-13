#!/bin/bash
sudo docker stop login-service
sudo docker rm login-service
sudo docker image prune