# AI Writing Detector

A Spring Boot REST API application that detects AI-written text and manages insurance contracts with sophisticated header-based versioning support.

## Table of Contents

- [Description](#description)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Building](#building)
- [Testing](#testing)
- [Running](#running)
- [API Endpoints](#api-endpoints)
- [Header-Based Versioning](#header-based-versioning)
- [Examples](#examples)

## Description

This challenge is to build your own AI writing detector that analyzes text and determines the likelihood it was written by an AI rather than a human.

The application also demonstrates advanced API versioning capabilities through HTTP headers. It provides contract management endpoints with support for multiple API versions (2024-01-01, 2025-01-01, 2026-01-01, 2026-04-01) with automatic request/response transformation based on the `x-version` header.

### Key Features

- **AI Text Detection**: Analyze text and determine if it was written by AI
- **Contract Management**: Create and manage insurance contracts
- **Header-Based API Versioning**: Seamlessly support multiple API versions through the `x-version` header
- **Automatic Schema Transformation**: Request and response payloads are automatically transformed between versions
- **mTLS Support**: Secure communication with mutual TLS
- **OAuth & API Key Authentication**: Multiple authentication strategies
- **Comprehensive Logging**: Request/response logging with sensitive data handling

## Features

### 1. REST API Endpoints
- **Hello World**: Simple health check endpoint
- **Remote Hello World**: Integration with external services
- **Contract Creation**: Create contracts with automatic versioning support

### 2. Version Management
- **Automatic Request Upgrade**: Incoming requests are automatically upgraded to the latest version
- **Automatic Response Downgrade**: Responses are automatically downgraded to the requested version
- **Backward Compatibility**: Support for multiple API versions simultaneously
- **Schema Evolution**: Handle changes in data structures across versions

### 3. Supported API Versions
- `2024-01-01`: Initial version with basic contract fields (name, firstName, lastName, premium)
- `2025-01-01`: Upgraded schema with `customerName` instead of `name`
- `2026-01-01`: Further schema changes
- `2026-04-01`: Latest version with additional fields

## Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.8.1 or higher
- **Git**: For cloning the repository
- **OpenSSL**: For certificate management (optional, certificates are pre-generated)

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/coding-challenge-113-AI-writing-detector.git
cd coding-challenge-113-AI-writing-detector
```

### 2. Verify Prerequisites

```bash
# Check Java version
java -version

# Check Maven version
mvn -version
```

## Configuration

### Environment Variables

The application uses a `.env` file for configuration. Key variables:

```bash
# Server Configuration
PORT=8443
KEY_STORE_PASSWORD=your-keystore-password
TRUST_STORE_PASSWORD=your-truststore-password

# OAuth Configuration (optional)
APP_HTTP_OAUTH_CLIENT_ID=your-client-id
APP_HTTP_OAUTH_CLIENT_SECRET=your-client-secret
APP_HTTP_OAUTH_TOKEN_URL=https://your-oauth-provider/token

# API Key Configuration (optional)
APP_HTTP_API_KEY=your-api-key
APP_HTTP_API_KEY_HEADER=x-api-key
```

### Application Profiles

The application supports multiple profiles:

- **production** (default): Production-ready configuration
- **local**: Development configuration with local settings

To use a specific profile:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

## Building

### Build with Maven

```bash
# Full build (compile + test)
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Build and create executable JAR
mvn clean package
```

### Build Output

The compiled JAR will be located at:
```
target/ai-writing-detector-0.0.1-SNAPSHOT.jar
```

## Testing

### Run All Tests

```bash
# Run all unit and integration tests
mvn test

# Run specific test class
mvn test -Dtest=ContractVersioningIntegrationTest

# Run tests with specific pattern
mvn test -Dtest=*Integration*
```

### Test Reports

Test reports are generated in:
```
target/surefire-reports/
```

### Key Test Classes

- `ContractVersioningIntegrationTest`: Tests header-based versioning
- `HttpClientTest`: Tests HTTP client functionality
- `ContractTest`: Tests contract model

## Running

### Run with Maven

```bash
mvn spring-boot:run
```

### Run with Java

```bash
# After building the JAR
java -jar target/ai-writing-detector-0.0.1-SNAPSHOT.jar
```

### Run with Environment Variables

```bash
# Using Linux/Mac
export PORT=8443
export KEY_STORE_PASSWORD=yourpassword
export TRUST_STORE_PASSWORD=yourpassword
java -jar target/ai-writing-detector-0.0.1-SNAPSHOT.jar

# Using Windows PowerShell
$env:PORT=8443
$env:KEY_STORE_PASSWORD="yourpassword"
$env:TRUST_STORE_PASSWORD="yourpassword"
java -jar target/ai-writing-detector-0.0.1-SNAPSHOT.jar
```

### Verify Application is Running

The application runs on `https://localhost:8443` by default.

```bash
# Test the health endpoint (should work if SSL verification is disabled in your client)
curl -k https://localhost:8443/rest/ai/detector/v1/hello-world
```

## API Endpoints

### 1. Hello World (Health Check)

**Endpoint**: `GET /rest/ai/detector/v1/hello-world`

**Description**: Simple health check endpoint that returns "Hello World!"

**Response**:
```
Hello World!
```

### 2. Remote Hello World

**Endpoint**: `GET /rest/ai/detector/v1/hello-world-remote`

**Description**: Calls a remote hello-world endpoint (configured in application.yaml)

**Response**:
```
Hello World!
```

### 3. Create Contract

**Endpoint**: `POST /rest/ai/detector/v1/contracts`

**Description**: Creates a contract and returns it with all fields populated. Supports multiple API versions through the `x-version` header.

**Headers**:
```
x-version: 2024-01-01  (or 2025-01-01, 2026-01-01, 2026-04-01)
Content-Type: application/json
```

**Request Body** (varies by version - see examples below)

**Response**: Contract object with populated fields including:
- `id`: UUID of the contract
- `name`/`customerName`: Customer identifier (depends on version)
- `firstName`, `lastName`: Customer names
- `premium`: Amount object with `amount` and `currency`

---

## Header-Based Versioning

### Overview

The API supports multiple versions through the `x-version` HTTP header. The application automatically:

1. **Upgrades** incoming request payloads to the latest internal version
2. **Transforms** the data according to version-specific rules
3. **Downgrades** response payloads back to the requested version

This allows clients to use any supported version without needing to update their integration.

### Supported Versions

| Version | Date | Status |
|---------|------|--------|
| 2024-01-01 | January 1, 2024 | Supported (Legacy) |
| 2025-01-01 | January 1, 2025 | Supported |
| 2026-01-01 | January 1, 2026 | Supported |
| 2026-04-01 | April 1, 2026 | Latest |

### Version Transformations

#### 2024-01-01 → 2025-01-01 Upgrade
- **Field Changes**:
  - `name` → `customerName`
  - `premium` (simple field) → `premiums.amount` and `premiums.currency`

#### 2025-01-01 → 2026-01-01 Upgrade
- **Field Changes**:
  - `firstName` and `lastName` are aggregated

#### 2026-01-01 → 2026-04-01 Upgrade
- **Field Changes**:
  - Additional fields and structure refinements

#### Downgrade Process
Responses are automatically downgraded in reverse order to match the requested version.

### How Versioning Works

#### Step 1: Client Sends Request with Version Header

```bash
curl -X POST https://localhost:8443/rest/ai/detector/v1/contracts \
  -H "x-version: 2024-01-01" \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe"}'
```

#### Step 2: Request Interceptor Detects Version

The `ContractRequestVersioningAdvice` intercepts the request and reads the `x-version` header.

#### Step 3: Request Transformation

- If version < latest, the request is upgraded through the transformation chain
- Example: 2024-01-01 request → upgraded to 2025-01-01 → upgraded to 2026-01-01 → upgraded to 2026-04-01

#### Step 4: Business Logic Processes Latest Version

The controller receives the contract in the latest version format and processes it.

#### Step 5: Response Downgrade

The `ContractResponseVersioningAdvice` intercepts the response and downgrades it back to the requested version.

#### Step 6: Client Receives Response in Requested Version

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "firstName": null,
  "lastName": null,
  "premium": {
    "amount": 12345.67,
    "currency": "CHF"
  }
}
```

---

## Examples

### Example 1: Create Contract (Version 2024-01-01)

**Request**:
```bash
curl -X POST https://localhost:8443/rest/ai/detector/v1/contracts \
  -H "x-version: 2024-01-01" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Lorenz Hänggi"
  }'
```

**Response** (automatically downgraded to 2024-01-01):
```json
{
  "id": "a1b2c3d4-e5f6-47g8-h9i0-j1k2l3m4n5o6",
  "name": "Lorenz Hänggi",
  "firstName": null,
  "lastName": null,
  "premium": {
    "amount": 5432.10,
    "currency": "CHF"
  }
}
```

### Example 2: Create Contract (Version 2025-01-01)

**Request**:
```bash
curl -X POST https://localhost:8443/rest/ai/detector/v1/contracts \
  -H "x-version: 2025-01-01" \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "My Contract 42"
  }'
```

**Response** (automatically downgraded to 2025-01-01):
```json
{
  "id": "b2c3d4e5-f6g7-48h9-i0j1-k2l3m4n5o6p7",
  "customerName": "My Contract 42",
  "premiums": {
    "amount": 1234.56,
    "currency": "CHF"
  }
}
```

### Example 3: Create Contract (Version 2026-01-01)

**Request**:
```bash
curl -X POST https://localhost:8443/rest/ai/detector/v1/contracts \
  -H "x-version: 2026-01-01" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Lorenz Hänggi"
  }'
```

**Response** (automatically downgraded to 2026-01-01):
```json
{
  "id": "c3d4e5f6-g7h8-49i0-j1k2-l3m4n5o6p7q8",
  "name": "Lorenz Hänggi",
  "firstName": null,
  "lastName": null,
  "premium": {
    "amount": 5432.10,
    "currency": "CHF"
  }
}
```

### Example 4: Create Contract (Version 2026-04-01 - Latest)

**Request**:
```bash
curl -X POST https://localhost:8443/rest/ai/detector/v1/contracts \
  -H "x-version: 2026-04-01" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Lorenz Hänggi"
  }'
```

**Response**:
```json
{
  "id": "d4e5f6g7-h8i9-50j1-k2l3-m4n5o6p7q8r9",
  "name": "Lorenz Hänggi",
  "firstName": null,
  "lastName": null,
  "premium": {
    "amount": 5432.10,
    "currency": "CHF"
  }
}
```

### Example 5: Hello World Endpoint

**Request**:
```bash
curl -k https://localhost:8443/rest/ai/detector/v1/hello-world
```

**Response**:
```
Hello World!
```

### Example 6: Using Bruno API Client

The project includes Bruno collection files for easy testing:

1. Open the Bruno client
2. Import the collection from `bruno/ai-writing-detector/`
3. Use pre-configured requests:
   - `hello-world`: Simple health check
   - `create contract 2024`: Create contract with 2024-01-01 schema
   - `create contract 2025`: Create contract with 2025-01-01 schema
   - `create contract 2026`: Create contract with 2026-01-01 schema
   - `create contract 2026-4`: Create contract with 2026-04-01 schema

### Example 7: Version Header Handling

**Without version header** (uses default/latest):
```bash
curl -X POST https://localhost:8443/rest/ai/detector/v1/contracts \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Contract"}'
```

**Invalid version header** (handled gracefully):
```bash
curl -X POST https://localhost:8443/rest/ai/detector/v1/contracts \
  -H "x-version: 2099-01-01" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Contract"}'
```

---

## Architecture

### Request/Response Transformation Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ x-version: 2024-01-01
       ▼
┌──────────────────────────────────┐
│ ContractRequestVersioningAdvice  │
│  (Intercepts incoming request)   │
└──────┬───────────────────────────┘
       │ Detects version
       ▼
┌──────────────────────────────────┐
│ ContractRequestUpgradeProcess    │
│ (Transforms 2024 → Latest)       │
└──────┬───────────────────────────┘
       │ Latest version data
       ▼
┌──────────────────────────────────┐
│ AiWritingDetectorController      │
│ (Business logic)                 │
└──────┬───────────────────────────┘
       │ Latest version response
       ▼
┌──────────────────────────────────┐
│ ContractResponseVersioningAdvice │
│ (Intercepts response)            │
└──────┬───────────────────────────┘
       │ Downgrades to 2024
       ▼
┌──────────────────────────────────┐
│ ContractResponseDowngradeProcess │
│ (Transforms Latest → 2024)       │
└──────┬───────────────────────────┘
       │ 2024-01-01 response
       ▼
┌─────────────┐
│   Client    │
└─────────────┘
```

---

## Troubleshooting

### SSL Certificate Issues

If you encounter SSL certificate errors:

```bash
# Disable SSL verification for testing (not recommended for production)
curl -k https://localhost:8443/rest/ai/detector/v1/hello-world

# Or use the provided certificates
export KEY_STORE_PASSWORD=your-password
java -Djavax.net.debug=ssl:handshake -jar target/ai-writing-detector-0.0.1-SNAPSHOT.jar
```

### Port Already in Use

If port 8443 is already in use:

```bash
# Change port via environment variable
export PORT=8444
mvn spring-boot:run
```

### Version Transformation Issues

If you encounter version transformation errors, check:

1. The `x-version` header is set correctly
2. All required fields are present in the request body
3. The field names match the expected version schema

---

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [REST API Best Practices](https://restfulapi.net/)
- [API Versioning Strategies](https://swagger.io/blog/api-versioning/)

---

## License

This project is part of a coding challenge and is provided as-is for educational purposes.
