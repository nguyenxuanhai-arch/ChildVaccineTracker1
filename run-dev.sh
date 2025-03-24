#!/bin/bash

# Script để chạy ứng dụng Spring Boot trong môi trường phát triển

echo "Đang khởi động ứng dụng quản lý tiêm chủng trong chế độ phát triển..."

# Sử dụng Maven Wrapper để chạy Spring Boot với profile dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev