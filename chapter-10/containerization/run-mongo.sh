#!/bin/bash

docker run -d \
  --name bookstore-mongo \
  -e MONGO_INITDB_ROOT_USERNAME=bookstore \
  -e MONGO_INITDB_ROOT_PASSWORD=bookstore123 \
  -e MONGO_INITDB_DATABASE=userDB \
  -p 27017:27017 \
  mongo:8
