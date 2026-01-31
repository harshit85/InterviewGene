#!/bin/bash

# Configuration
SERVICES=(
    "discovery-server"
    "api-gateway"
    "access-service"
    "user-service"
    "interview-service"
    "resume-service"
    "evaluation-service"
    "question-service"
    "analytics-service"
)

# Check for docker-compose or docker compose
if command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE="docker-compose"
elif docker compose version &> /dev/null; then
    DOCKER_COMPOSE="docker compose"
else
    echo "❌ docker-compose or docker compose is not installed."
    exit 1
fi

echo "🚀 Starting Infrastructure (MySQL, Redis, Kafka, Minio)..."
$DOCKER_COMPOSE up -d

# Check if docker-compose up was successful
if [ $? -ne 0 ]; then
    echo "❌ Failed to start infrastructure. Make sure Docker is running."
    exit 1
fi

echo "⏳ Waiting for MySQL to be ready..."
# Use docker-compose exec which is more reliable for service names
MAX_RETRIES=30
COUNT=0
until $DOCKER_COMPOSE exec -T mysql mysqladmin ping -h"localhost" --silent || [ $COUNT -eq $MAX_RETRIES ]; do
    echo "Waiting for MySQL... ($COUNT/$MAX_RETRIES)"
    sleep 2
    ((COUNT++))
done

if [ $COUNT -eq $MAX_RETRIES ]; then
    echo "❌ MySQL failed to start in time. Check 'docker ps' or logs."
    exit 1
fi

echo "📦 Building all services..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the logs above."
    exit 1
fi

# Function to run a service
run_service() {
    local service=$1
    echo "▶️ Starting $service..."
    # Create service-specific logs
    (cd "deployables/$service" && mvn spring-boot:run -Dspring-boot.run.arguments="--logging.file.name=../../logs/$service.log") > "logs/$service-mvn.log" 2>&1 &
}

# Create logs directory
mkdir -p logs

# Clean up previous logs if they exist
rm -f logs/*.log

echo "📡 Starting Discovery Server first..."
run_service "discovery-server"

echo "⏳ Waiting for Discovery Server to initialize (15s)..."
sleep 15

# Start remaining services in parallel
for service in "${SERVICES[@]:1}"; do
    run_service "$service"
    sleep 2 # Small delay to avoid resource spikes
done

echo "✅ All services are starting in the background."
echo "🔗 Useful Links:"
echo "- Eureka Dashboard: http://localhost:8761"
echo "- API Gateway: http://localhost:8080"
echo "- Minio Console: http://localhost:9001"
echo ""
echo "📝 Run 'jobs' to see active processes or 'kill %1 %2 ...' to stop them."
echo "💡 To stop everything including infrastructure, run: $DOCKER_COMPOSE down"
wait
