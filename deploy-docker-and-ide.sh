#!/bin/bash

#  Скрипт для сборки и запуска всех сервисов в IDE, а инфры в докере
echo "Building all services..."

# Поднимаем инфру
echo "Deploying infrastructure..."
docker compose -f docker-compose-infra.yml up -d

# запускает сервис на отдельном потоке
run_service() {
    local service_dir=$1
    local service_name=$(basename $service_dir)
    echo "Building and running $service_name..."
    ./gradlew clean :$service_name:bootRun -x test &
}

# Находит все директории, содержащие build.gradle.kts
services=$(find . -name "build.gradle.kts" -exec dirname {} \; | grep -v "^.$")

# Запуск сервисов
for service in $services; do
    run_service "$service"
done

# ждемс
wait

echo "All services started!"

echo "Deployment complete!"