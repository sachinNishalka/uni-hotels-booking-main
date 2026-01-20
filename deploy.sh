#!/bin/bash
docker stop uni-hotels-booking-main || true
docker rm uni-hotels-booking-main || true
docker build -t uni-hotels-booking-main:latest .
docker run -d --name uni-hotels-booking-main --network uni-hotels-network -p 8059:8059 uni-hotels-booking-main:latest