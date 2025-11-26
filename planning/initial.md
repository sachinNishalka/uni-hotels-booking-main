# Hotel Booking Management System - MVP & Staged Implementation Plan

## Architecture Review & Recommendations

### Current Architecture Strengths

- Clear separation of concerns (main app for routing, hotel-specific services for processing)
- Dual database approach (MySQL for auth, MongoDB for bookings) is appropriate
- Azure AD integration for enterprise authentication

### Identified Gaps & Improvements

**Critical Missing Components:**

1. **API Gateway/Rate Limiting**: No protection against spam from Uni Hotels Platform
2. **Dead Letter Queue**: No handling for failed booking requests or Opera PMS failures
3. **Audit Logging**: No tracking of who confirmed/rejected bookings and when
4. **Idempotency**: Uni Hotels might send duplicate requests - need duplicate detection
5. **Monitoring/Alerting**: No visibility into system health or booking processing delays

**Architecture Modifications Recommended:**

1. Add Redis/MongoDB for idempotency keys (prevent duplicate bookings)
2. Add request/response logging with correlation IDs for traceability
3. Implement circuit breaker for Opera PMS integration (when Opera is down)
4. Add booking status workflow: PENDING → EMPLOYEE_REVIEW → CONFIRMED → SYNCED_TO_OPERA
5. Consider message queue (RabbitMQ/AWS SQS) between main app and hotel services for better resilience

## MVP Scope (Phase 1)

**Goal**: Single hotel end-to-end flow with manual confirmation

### Backend Components

**1. Main Application (Current Spring Boot Project)**

- REST endpoint to receive bookings from Uni Hotels Platform
- JWT-based authentication for employee login (MySQL)
- Simple routing logic to forward requests to first hotel microservice
- Basic validation and idempotency check

**2. First Hotel Microservice (e.g., HAR - Hotel Aruba)**

- Separate Spring Boot application
- Store bookings in MongoDB with status tracking
- REST API for CRUD operations on bookings
- Opera Cloud PMS integration (UAT environment)
- Employee confirmation workflow endpoints

**3. React Frontend**

- Azure AD login
- Dashboard showing pending/confirmed bookings
- Booking detail view with confirm/reject actions
- Basic filtering and search

**Key Files to Create:**

- Main App: `BookingController.java`, `BookingRequestDto.java`, `HotelRoutingService.java`, `IdempotencyService.java`
- Hotel Service: `BookingEntity.java`, `BookingRepository.java`, `OperaPmsClient.java`, `BookingService.java`
- Config: `application.yml` with MySQL, Azure AD, hotel service URLs

## Phase 2: Production Readiness

1. **Monitoring & Observability**

- Add Spring Boot Actuator with Prometheus metrics
- Implement structured logging (JSON format with correlation IDs)
- Add health checks for databases and external services

2. **Error Handling & Resilience**

- Implement Resilience4j circuit breaker for Opera PMS
- Add retry mechanism with exponential backoff
- Create dead letter queue in MongoDB for failed bookings

3. **Security Hardening**

- Add API key authentication for Uni Hotels Platform webhook
- Implement rate limiting with Bucket4j
- Add request/response encryption for sensitive data

## Phase 3: Multi-Hotel Scaling

1. **Hotel Service Template**

- Create reusable hotel service template
- Deploy HRF and ASM microservices
- Update main app routing to support multiple hotels

2. **Centralized Configuration**

- Move hotel configs to Spring Cloud Config Server or database
- Dynamic hotel registration and discovery

3. **Advanced Features**

- Real-time dashboard updates with WebSockets
- Booking analytics and reporting
- Automated booking rules (auto-confirm based on criteria)

## Phase 4: Optimization & Scale

1. **Performance**

- Add Redis caching for frequently accessed data
- Implement database indexing strategy
- Add connection pooling optimization

2. **Message Queue Integration**

- Replace synchronous HTTP calls with async messaging (RabbitMQ/Kafka)
- Enable better fault tolerance and load distribution

3. **DevOps**

- Containerize with Docker
- Set up CI/CD pipeline
- Deploy to cloud (Azure/AWS) with auto-scaling

## Technology Stack Summary

**Backend:**

- Spring Boot 3.5.8, Java 21
- Spring Data JPA (MySQL) + Spring Data MongoDB
- Spring Security with Azure AD OAuth2
- Resilience4j for circuit breaker
- RestTemplate/WebClient for HTTP calls
- Lombok for boilerplate reduction

**Frontend:**

- React 18+
- MSAL (Microsoft Authentication Library) for Azure AD
- Axios for API calls
- Material-UI or Ant Design for UI components
- React Query for data fetching

**Databases:**

- MySQL 8.0+ (user authentication)
- MongoDB 6.0+ (booking records)
- Redis (optional, for caching and idempotency)

**External Integrations:**

- Opera Cloud REST API
- Azure Active Directory
- Uni Hotels Platform Webhook
