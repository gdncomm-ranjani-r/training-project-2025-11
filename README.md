# Online Marketplace Platform - Microservices Architecture

A comprehensive microservices-based online marketplace platform built with Java and Spring Boot, featuring authentication, product search, and shopping cart functionality.

## Architecture Overview

The platform consists of 4 microservices:

1. **API Gateway** (Port 8080) - Handles routing, authentication, and authorization
2. **Member Service** (Port 8081) - User registration, login, and JWT token management
3. **Product Service** (Port 8082) - Product catalog, search, and listing
4. **Cart Service** (Port 8083) - Shopping cart management

## Technology Stack

- **Framework**: Spring Boot 3.4.12
- **Language**: Java 21
- **Databases**:
  - PostgreSQL (Member & Cart services)
  - MongoDB (Product service)
  - Redis (Caching & Token blacklist)
- **Security**: Spring Security with JWT (JWS)
- **API Gateway**: Spring Cloud Gateway
- **Build Tool**: Maven

## Features

### Member Service
- ✅ User registration with email validation
- ✅ Password hashing using BCrypt (Spring Security)
- ✅ Password validation using Spring Validation
- ✅ Login with JWT token generation
- ✅ JWT token with symmetric key (HS256)

### Product Service
- ✅ Product search with wildcard support
- ✅ Paginated product listing
- ✅ Product detail view
- ✅ Category-based filtering
- ✅ Redis caching for product details

### Cart Service
- ✅ Add products to cart (requires authentication)
- ✅ View shopping cart (requires authentication)
- ✅ Delete items from cart (requires authentication)
- ✅ Redis caching for cart data
- ✅ Automatic quantity updates for duplicate products

### API Gateway
- ✅ JWT-based authentication and authorization
- ✅ Token validation from header or cookie
- ✅ Token blacklist for logout functionality
- ✅ Route forwarding to appropriate microservices
- ✅ Public endpoints (register, login, product search)
- ✅ Protected endpoints (cart operations)

## Prerequisites

- Java 21
- Maven 3.6+
- PostgreSQL 12+
- MongoDB 4.4+
- Redis 6+

## Setup Instructions

### 1. Database Setup

Use Docker Compose to start all databases:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- MongoDB on port 27017
- Redis on port 6379

### 2. Database Configuration

The services are configured to connect to:
- **PostgreSQL**: `localhost:5432/marketplace_db` (username: postgres, password: postgres)
- **MongoDB**: `localhost:27017/marketplace_db`
- **Redis**: `localhost:6379`

### 3. Build and Run Services

#### Option 1: Run all services individually

```bash
# Terminal 1 - API Gateway
cd api-gateway
mvn spring-boot:run

# Terminal 2 - Member Service
cd member
mvn spring-boot:run

# Terminal 3 - Product Service
cd product
mvn spring-boot:run

# Terminal 4 - Cart Service
cd cart
mvn spring-boot:run
```

#### Option 2: Build all services

```bash
# Build all services
mvn clean install -f api-gateway/pom.xml
mvn clean install -f member/pom.xml
mvn clean install -f product/pom.xml
mvn clean install -f cart/pom.xml
```

### 4. Data Seeding

The services automatically seed data on startup:
- **Member Service**: Creates 5,000+ members
- **Product Service**: Creates 50,000+ products

Wait for the seeding to complete (check console logs).

## API Endpoints

### Public Endpoints (No Authentication Required)

#### Member Service
- `POST /api/members/register` - Register a new user
  ```json
  {
    "email": "user@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }
  ```

- `POST /api/members/login` - Login and get JWT token
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```

#### Product Service
- `GET /api/products` - Get all products (paginated)
  - Query params: `page` (default: 0), `size` (default: 20)

- `GET /api/products/search?q=keyword` - Search products
  - Query params: `q` (search term), `page`, `size`
  - Supports wildcard: `q=*phone*`

- `GET /api/products/category/{category}` - Get products by category
  - Query params: `page`, `size`

- `GET /api/products/{id}` - Get product details

### Protected Endpoints (Authentication Required)

#### Cart Service
All cart endpoints require JWT token in `Authorization: Bearer <token>` header or `jwt` cookie.

- `POST /api/cart` - Add item to cart
  - Header: `X-Member-Id: <memberId>` (automatically set by gateway)
  ```json
  {
    "productId": "product123",
    "productName": "Product Name",
    "price": 99.99,
    "quantity": 2
  }
  ```

- `GET /api/cart` - Get cart contents
  - Header: `X-Member-Id: <memberId>`

- `DELETE /api/cart/items/{productId}` - Remove item from cart
  - Header: `X-Member-Id: <memberId>`

- `DELETE /api/cart` - Clear entire cart
  - Header: `X-Member-Id: <memberId>`

#### Auth Service (API Gateway)
- `POST /api/auth/logout` - Logout and invalidate token
  - Header: `Authorization: Bearer <token>` or Cookie: `jwt=<token>`

## Authentication Flow

1. **Register/Login**: User registers or logs in through Member Service
2. **Token Generation**: Member Service generates JWT token
3. **Token Usage**: Client includes token in `Authorization: Bearer <token>` header or as `jwt` cookie
4. **Gateway Validation**: API Gateway validates token and extracts member ID
5. **Request Forwarding**: Gateway forwards request with `X-Member-Id` header to downstream services
6. **Logout**: Token is blacklisted in Redis

## Testing

### Run Unit Tests

```bash
# Member Service
cd member && mvn test

# Product Service
cd product && mvn test

# Cart Service
cd cart && mvn test
```

### Integration Testing

Use tools like Postman or curl to test the APIs:

```bash
# Register
curl -X POST http://localhost:8080/api/members/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","firstName":"John","lastName":"Doe"}'

# Login
curl -X POST http://localhost:8080/api/members/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Search Products
curl http://localhost:8080/api/products/search?q=*phone*

# Add to Cart (use token from login)
curl -X POST http://localhost:8080/api/cart \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{"productId":"1","productName":"Test Product","price":99.99,"quantity":2}'
```

## Project Structure

```
training-project-2025-11/
├── api-gateway/          # API Gateway service
│   ├── src/
│   │   ├── main/java/com/demo/api_gateway/
│   │   │   ├── config/    # Gateway and JWT configuration
│   │   │   ├── filter/    # JWT authentication filter
│   │   │   └── controller/# Auth controller (logout)
│   └── pom.xml
├── member/               # Member service
│   ├── src/
│   │   ├── main/java/com/demo/member/
│   │   │   ├── config/    # Security and data seeding
│   │   │   ├── controller/# REST controllers
│   │   │   ├── dto/       # Data transfer objects
│   │   │   ├── entity/    # JPA entities
│   │   │   ├── repository/# Data repositories
│   │   │   └── service/   # Business logic
│   └── pom.xml
├── product/              # Product service
│   ├── src/
│   │   ├── main/java/com/demo/product/
│   │   │   ├── config/    # Cache configuration and data seeding
│   │   │   ├── controller/# REST controllers
│   │   │   ├── dto/       # Data transfer objects
│   │   │   ├── entity/    # MongoDB documents
│   │   │   ├── repository/# MongoDB repositories
│   │   │   └── service/   # Business logic
│   └── pom.xml
├── cart/                 # Cart service
│   ├── src/
│   │   ├── main/java/com/demo/cart/
│   │   │   ├── config/    # Cache configuration
│   │   │   ├── controller/# REST controllers
│   │   │   ├── dto/       # Data transfer objects
│   │   │   ├── entity/    # JPA entities
│   │   │   ├── repository/# Data repositories
│   │   │   └── service/   # Business logic
│   └── pom.xml
└── docker-compose.yml    # Database setup
```

## Security Features

- ✅ Password hashing with BCrypt (Spring Security)
- ✅ Password validation with Spring Validation
- ✅ JWT token-based authentication
- ✅ Token blacklisting for logout
- ✅ Secure token extraction from header or cookie
- ✅ Member ID injection for downstream services

## Performance Optimizations

- ✅ Redis caching for product details
- ✅ Redis caching for cart data
- ✅ Pagination for all list endpoints
- ✅ Efficient database queries with indexes

## Database Design

### PostgreSQL (Member & Cart)
- **members**: User accounts with hashed passwords
- **cart_items**: Shopping cart items linked to members

### MongoDB (Product)
- **products**: Product catalog with text indexes for search

### Redis
- Product detail cache (10 minutes TTL)
- Cart cache (5 minutes TTL)
- Token blacklist (24 hours TTL)

## Future Enhancements (Optional)

- [ ] Docker containerization for all services
- [ ] Kubernetes deployment configurations
- [ ] Elasticsearch integration for advanced search
- [ ] gRPC implementation
- [ ] Additional design patterns implementation
- [ ] API documentation with Swagger/OpenAPI
- [ ] Monitoring and observability (Prometheus, Grafana)
- [ ] Distributed tracing (Zipkin, Jaeger)

## License

This project is for training purposes only.

## Author

Training Project - 2025 QA to BE Conversion Program
