#!/bin/bash

# Script để build và triển khai ứng dụng với Docker Compose

echo "=== Hệ Thống Quản Lý Tiêm Chủng - Triển khai Docker ==="
echo ""

# Kiểm tra Docker đã được cài đặt
if ! command -v docker &> /dev/null
then
    echo "Lỗi: Docker chưa được cài đặt. Vui lòng cài đặt Docker trước."
    exit 1
fi

# Kiểm tra Docker Compose đã được cài đặt
if ! command -v docker-compose &> /dev/null
then
    echo "Lỗi: Docker Compose chưa được cài đặt. Vui lòng cài đặt Docker Compose trước."
    exit 1
fi

echo "Đang xây dựng và khởi động các containers..."
docker-compose up -d --build

if [ $? -eq 0 ]; then
    echo ""
    echo "=== Triển khai thành công! ==="
    echo "Ứng dụng đang chạy tại: http://localhost:8080"
    echo "Để xem logs, sử dụng: docker-compose logs -f app"
    echo "Để dừng ứng dụng, sử dụng: docker-compose down"
else
    echo ""
    echo "=== Triển khai thất bại! Vui lòng kiểm tra lỗi ở trên. ==="
fi