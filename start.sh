#!/bin/bash

# QRWare Startup Script
# Usage: ./start.sh [profile] [port]
# Example: ./start.sh dev 8080

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
PROFILE=${1:-dev}
PORT=${2:-8080}
MAVEN_OPTS="-Xmx1024m -Xms512m"

echo -e "${BLUE}🚀 Starting QRWare Warehouse Management System${NC}"
echo -e "${BLUE}================================================${NC}"

# Function to print colored output
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if Java is installed
if ! command -v java &> /dev/null; then
    print_error "Java is not installed or not in PATH"
    print_info "Please install Java 17 or higher"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt "17" ]; then
    print_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

print_success "Java version check passed"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed or not in PATH"
    print_info "Please install Maven 3.6 or higher"
    exit 1
fi

print_success "Maven found"

# Check if port is available
if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null ; then
    print_error "Port $PORT is already in use"
    print_info "Please choose a different port or stop the process using port $PORT"
    exit 1
fi

print_success "Port $PORT is available"

# Display startup information
echo ""
print_info "Startup Configuration:"
echo "  Profile: $PROFILE"
echo "  Port: $PORT"
echo "  Maven Options: $MAVEN_OPTS"
echo ""

# Set environment variables
export MAVEN_OPTS="$MAVEN_OPTS"

# Profile specific settings
case $PROFILE in
    "dev")
        print_info "Development profile selected"
        print_info "Using H2 in-memory database"
        print_info "H2 Console will be available at: http://localhost:$PORT/h2-console"
        ;;
    "prod")
        print_info "Production profile selected"
        print_warning "Make sure PostgreSQL is running and configured"
        ;;
    *)
        print_warning "Unknown profile: $PROFILE"
        print_info "Available profiles: dev, prod"
        ;;
esac

echo ""
print_info "Starting application..."
echo ""

# Function to handle cleanup on exit
cleanup() {
    echo ""
    print_warning "Shutting down QRWare..."
    exit 0
}

# Trap signals for cleanup
trap cleanup SIGINT SIGTERM

# Start the application with Maven
mvn spring-boot:run \
    -Dspring-boot.run.profiles="$PROFILE" \
    -Dspring-boot.run.arguments="--server.port=$PORT" \
    -q

# If we reach here, the application has stopped
print_info "Application stopped"