#!/bin/bash

docker run -d \
  --name bookstore-postgres \
  -e POSTGRES_USER=bookstore \
  -e POSTGRES_PASSWORD=bookstore123 \
  -e POSTGRES_DB=inventory \
  -p 5432:5432 \
  postgres:17
