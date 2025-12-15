# SportsLink

<p align="center">
  <strong>A digital marketplace connecting field owners with sports enthusiasts</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.7-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
</p>

---

## Project Abstract

**SportsLink** is a digital marketplace platform that connects field owners with sports enthusiasts who want to rent sports fields and related equipment on-demand. The application addresses a critical inefficiency in sports facility utilization: field owners have idle capacity during off-peak hours while potential users lack convenient access to affordable sports facilities.

### Key Features

| Stakeholder | Features |
|-------------|----------|
| **Field Owners** | Monetize underutilized sports facilities, manage bookings and equipment inventory, track maintenance schedules, monitor occupancy rates |
| **Renters** | Discover nearby sports fields filtered by sport type, location, and availability; easily reserve fields for specific time slots;|
| **Administrators** | Monitor platform health, analyze usage metrics, manage user partnerships, ensure compliance and safety standards |

### Domain
Sports facilities and equipment rental platform, initially focused on collective sports (padel, futsal, tennis, badminton, basketball, volleyball, etc.)

### Tech Stack Highlights
- **Backend:** Spring Boot 3.5.7 with Spring Security (JWT Authentication)
- **Database:** PostgreSQL with H2 for testing
- **Payments:** Stripe Integration
- **Storage:** MinIO (S3-compatible)
- **Testing:** JUnit, Cucumber, Selenium, RestAssured, Testcontainers
- **Observability:** Prometheus, Micrometer, Zipkin
- **API Documentation:** Swagger/OpenAPI

---

## Project Team

This project was developed for the **TQS (Testes e Qualidade de Software)** course, applying Software Quality Assurance (SQA) and DevOps practices.

| Name | NMec | Roles |
|------|------|-------|
| Diogo Duarte | 120482 | DevOps Master |
| Paulo Cunha | 118741 | Team Coordinator |
| Rafael Ferreira | 118803 | QA Engineer |
| Tomás Hilário |  119896 | Product Owner |

---

## Project Bookmarks

### Core Resources

| Resource | Link |
|----------|------|
| **Repository** | [GitHub - TQS-Project-25-26/SportsLink](https://github.com/TQS-Project-25-26/SportsLink) |
| **Project Backlog** | [Jira](https://sportslink.atlassian.net/jira/software/projects/SL/boards/1/backlog) |
| **API Documentation** | [Swagger UI](http://192.168.160.31:8080/swagger-ui/index.html) |
| **Static Analysis Dashboard** | [SonarCloud](https://sonarcloud.io/project/overview?id=TQS-Project-25-26_SportsLink) |


### Documentation

| Document | Description |
|----------|-------------|
| [TQS_Product.pdf](docs/TQS_Product.pdf) | Product documentation and specifications |
| [TQS_QA.pdf](docs/TQS_QA.pdf) | Quality Assurance documentation |

### DevOps & Observability

| Resource | Description |
|----------|-------------|
| **Prometheus** | Metrics collection |
| **Grafana** | Metrics visualization dashboard |
| **Zipkin** | Distributed tracing |

---



