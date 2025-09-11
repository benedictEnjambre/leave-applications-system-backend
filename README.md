# Leave Management System

## Overview
The Leave Applications System is a web application that allows employees to file their leave requests, and managers to review and approve/reject them. An HR administrator manages employees, assigns managers, and sets leave credits for employees.

## Roles & Use Cases
### HR Admin
- Create employee and/or manager profiles.
- Assign managers to employees.
- Set total number of annual leave credits for employees.
- View and approve/reject leave applications for all employees.

### Employees
- Apply for leaves.
- View submitted leave applications.

 ### Managers
- Apply for leaves.
- View own leave applications.
- View and approve/reject leave requests of employees reporting to them.

## Note:
- Authentication and login are out of scope. Instead, a dropdown allows switching between HR, Manager, and Employee views.

### Tech Stack
 - Backend
 - Spring Boot
#### Database
- PostgreSQL



### Dependencies
#### Backend
- Java 17+
- Gradle
- Spring Boot (Web, Data JPA, H2 Database)

### Prerequisites
- JDK 17+
- Node.js 18+ and Angular CLI for the frontend
- PostgreSQL 14+
### Project layout (backend)
- Code: `src/main/java/com/synacy/trainee/leavemanagementsystem`
- Config: `src/main/resources/application.properties`
- Build: `build.gradle`
- Entry point: `LeavesmanagementApplication.java`
### Database setup (PostgreSQL)
Create  database

```bash
sudo psql -u postgres psql
CREATE DATABASE leaves_management;
\q
```
Configure the backend to use PostgreSQL.
Spring datasource and JPA settings.
```properties

spring.application.name=leavesmanagement
spring.datasource.url=jdbc:postgresql://localhost:5432/leaves_management
spring.datasource.username=postgres
spring.datasource.password=root
spring.jpa.show-sql=true
# Hibernate ddl auto (create, create-drop, validate, update)
spring.jpa.hibernate.ddl-auto=create
```
## Run the backend
Explanation: Build and run with Gradle wrapper on Linux.
```bash
chmod +x ./gradlew
./gradlew clean build
./gradlew bootRun
```
## API overview
Note: Paths may differ slightly if controllers define a different base path. Default shown as `/api/...`.
### Users
  - `GET /api/v1/user?max={max}&page={page}&manager={managerId}&totalCredits={credits}&remainingCredits={credits}` \- Fetch  paginated list of users.

  - `GET /api/user/{id}` \- get user
  - `POST /api/v1/user` \ -  create user
  - `PUT /api/user/{id}` \- update user

### Leave Applications
  - `POST /api/v1/leave-application/{userId}` \-create a leave application
  - Body: `userId`, `startDate`, `endDate`, `reason`
  - `GET /api/v1/leave-application/{userId}/me?page=1&max=5` \- Fetch My Leave
  - `GET /api/v1/leave-application/{userId}/team?page=1&max=5` \-Fetch Team Leaves (Manager Only)
  - `GET /api/v1/leave-application/{userId}/all?page=1&max=5` \-Fetch All Leaves (HR Only)
  - `PATCH /api/v1/leave-application/{userId}/{leaveId}/cancel`  \-Cancel Leave (Employee Only)
  - `/api/v1/leave-application/{userId}/{leaveId}/approve` \-Approve Leave (Manager or HR Only)
  - `PATCH /api/v1/leave-application/{userId}/{leaveId}/reject` \-Fetch All Users





## Testing
Explanation: Run backend tests.
```bash
./gradlew test or ./gradlew build
```
## Link the Angular frontend to the backend
Set the API base URL in your Angular environment.

