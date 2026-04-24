# Branch Darpan

Branch Darpan is a full-stack enterprise workflow application for SBI covering hierarchical user management, survey submission and approval, reversal and exemption flows, and audit/history tracking.

## Workspace Layout

- `docs/`: source requirement documents
- `branch-darpan-backend/`: Spring Boot 3 / Java 21 API
- `branch-darpan-frontend/`: Angular standalone frontend
- `docker-compose.yml`: local MySQL 8 bootstrap

## Local Setup

1. Start MySQL:

   ```bash
   docker compose up -d
   ```

2. Backend:

   ```bash
   cd branch-darpan-backend
   mvn spring-boot:run
   ```

3. Frontend:

   ```bash
   cd branch-darpan-frontend
   npm install
   npm start
   ```

## Design Notes

- The backend follows a layered Spring Boot structure with security, controllers, services, repositories, and Flyway migrations.
- The frontend uses standalone Angular components, signal-driven auth state, and role-based navigation.
- Shared workflows such as questionnaire rendering and approval wrappers are designed for reuse across feature areas.
