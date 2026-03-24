#!/bin/bash

docker run -d \
  --name bookstore-zipkin \
  -p 9411:9411 \
  openzipkin/zipkin
