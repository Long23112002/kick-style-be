#!/bin/bash

# Script để chạy migration cho từng service
# Cách sử dụng: ./migrate.sh <service_name> dev
# Hoặc: ./migrate.sh all dev để chạy tất cả

if [[ "$1" == "all" ]]; then
  echo "Đang chạy migration cho tất cả services..."
  for d in services/*; do
    if [ -d "$d" ]; then
      service_name=$(basename "$d")
      echo "Migration cho service: $service_name"
      mvn flyway:migrate@$service_name -q
      if [ $? -eq 0 ]; then
        echo "✅ Migration thành công cho $service_name"
      else
        echo "❌ Migration thất bại cho $service_name"
        exit 1
      fi
    fi
  done
  echo "🎉 Hoàn thành migration cho tất cả services!"
else
  echo "Đang chạy migration cho service: $1"
  mvn flyway:migrate@$1 -q
  if [ $? -eq 0 ]; then
    echo "✅ Migration thành công cho $1"
  else
    echo "❌ Migration thất bại cho $1"
    exit 1
  fi
fi