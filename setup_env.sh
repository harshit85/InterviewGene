#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 Checking existing components...${NC}"

check_cmd() {
    if command -v "$1" &> /dev/null; then
        echo -e "✅ $2 is already installed: $(which "$1")"
        return 0
    else
        echo -e "❌ $2 is NOT installed."
        return 1
    fi
}

# 1. Check Homebrew
check_cmd "brew" "Homebrew"
HAS_BREW=$?

# 2. Check Java
check_cmd "java" "Java"
HAS_JAVA=$?

# 3. Check Maven
check_cmd "mvn" "Maven"
HAS_MVN=$?

# 4. Check Docker
check_cmd "docker" "Docker"
HAS_DOCKER=$?

# 5. Check Docker Compose
if command -v docker-compose &> /dev/null || docker compose version &> /dev/null; then
    echo -e "✅ Docker Compose is already installed."
    HAS_DOCKER_COMPOSE=0
else
    echo -e "❌ Docker Compose is NOT installed."
    HAS_DOCKER_COMPOSE=1
fi

# 6. Check Docker Daemon
if docker info &> /dev/null; then
    echo -e "✅ Docker daemon is running."
    DOCKER_RUNNING=0
else
    echo -e "❌ Docker daemon is NOT running."
    DOCKER_RUNNING=1
fi

# 7. Check Kafka (cli tools)
check_cmd "kafka-topics" "Kafka CLI"
HAS_KAFKA=$?

echo -e "\n${BLUE}🛠️ Starting installation for missing components...${NC}"

# Install Homebrew if missing
if [ $HAS_BREW -ne 0 ]; then
    echo "Installing Homebrew..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    eval "$(/opt/homebrew/bin/brew shellenv)"
fi

# Install Java 21 if missing
if [ $HAS_JAVA -ne 0 ]; then
    echo "Installing Java 21 via Homebrew..."
    brew install openjdk@21
    sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk
fi

# Install Maven if missing
if [ $HAS_MVN -ne 0 ]; then
    echo "Installing Maven via Homebrew..."
    brew install maven
fi

# Install Docker Desktop if missing
if [ $HAS_DOCKER -ne 0 ] || [ $HAS_DOCKER_COMPOSE -ne 0 ]; then
    if [ $HAS_DOCKER -ne 0 ]; then
        echo "Installing Docker Desktop via Homebrew Cask..."
        brew install --cask docker
    fi
    
    # If docker-compose specifically is missing and didn't come with Docker Desktop
    if [ $HAS_DOCKER_COMPOSE -ne 0 ]; then
        echo "Installing Docker Compose via Homebrew..."
        brew install docker-compose
    fi
    
    echo -e "${GREEN}⚠️ Please open Docker from your Applications folder to finish the setup.${NC}"
fi

# Try to start Docker on MacOS if it is installed but not running
if [ $HAS_DOCKER -eq 0 ] && [ $DOCKER_RUNNING -ne 0 ]; then
    echo "Attempting to start Docker Desktop..."
    open -a Docker
    echo "Waiting for Docker to start..."
    COUNT=0
    until docker info &> /dev/null || [ $COUNT -eq 20 ]; do
        echo -n "."
        sleep 3
        ((COUNT++))
    done
    echo ""
    if docker info &> /dev/null; then
        echo -e "${GREEN}✅ Docker started successfully.${NC}"
    else
        echo -e "${RED}❌ Docker failed to start automatically. Please start it manually.${NC}"
    fi
fi

# Install Kafka (CLI tools) if missing
if [ $HAS_KAFKA -ne 0 ]; then
    echo "Installing Kafka via Homebrew..."
    brew install kafka
fi

echo -e "\n${GREEN}✨ Environment check and setup completed!${NC}"
echo "If you just installed Docker, make sure it is RUNNING before starting the services."
echo "You can now run ./run_local.sh to start the project."
