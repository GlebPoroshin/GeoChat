#!/bin/bash

#  Скрипт для сборки и запуска всех сервисов
echo "Building all services..."

# Находит все директории, содержащие build.gradle.kts
for service in $(find . -name "build.gradle.kts" -exec dirname {} \; | grep -v "^.$"); do
    service_name=$(basename $service)
    echo "Building $service_name..."
    ./gradlew clean :$service_name:build -x test
done

# Поднимаем все сервисы
echo "Deploying services..."
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml up -d

echo "Deployment complete!" 