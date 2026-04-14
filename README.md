# Peer-to-Peer Book Rental Web Application

Java Spring Boot MVC web application developed for the OOAD mini-project. The system supports peer-to-peer book rental, rental lifecycle management, waitlist handling, reviews, chat, reporting, moderation, and admin analytics.

## Stack

- Java 17
- Spring Boot 3
- Spring MVC
- Thymeleaf
- Spring Data JPA
- H2 database
- Maven

## Implemented Features

- User registration, login, logout, and profile management
- Book listing for rent
- Browse and search books by title, author, and genre
- Rental request flow
- Lender approval and rejection of requests
- Mock advance payment
- Rental dashboard for borrowed and lent books
- Rental extension request and approval/rejection
- Return confirmation and extra charge handling
- Automatic overdue charge generation
- Waitlist for unavailable books
- In-app notifications when waitlisted books become available
- One-to-one chat tied to a rental
- Ratings and reviews after completed rentals
- User issue reporting
- Moderator dashboard for report handling
- Admin dashboard with platform analytics

## Architecture

The project follows MVC architecture.

- `Model`: domain entities such as `User`, `Book`, `RentalRequest`, `RentalTransaction`, `Payment`, `Review`, `Report`, `WaitlistEntry`, `Notification`
- `View`: Thymeleaf templates under `src/main/resources/templates`
- `Controller`: Spring MVC controllers grouped by module ownership

## Design Patterns Used

- `Singleton`: Spring-managed controller and service beans
- `Factory`: centralized object creation in `BookRentalFactory`
- `Facade`: `AdminDashboardFacade` for admin dashboard data access
- `Command`: report moderation actions using command objects

## Project Structure

### Shared Core

- `src/main/java/com/pesu/bookrental/config`
- `src/main/java/com/pesu/bookrental/domain/model`
- `src/main/java/com/pesu/bookrental/domain/enums`
- `src/main/java/com/pesu/bookrental/repository`
- `src/main/java/com/pesu/bookrental/factory`

### Teammate Ownership Packages

- `src/main/java/com/pesu/bookrental/tanisha`
  - authentication, profile, notifications
- `src/main/java/com/pesu/bookrental/tanya`
  - books, browse/search, waitlist
- `src/main/java/com/pesu/bookrental/vedika`
  - rental requests, payments, rental lifecycle
- `src/main/java/com/pesu/bookrental/vennela`
  - home, chat, reviews, reports, moderation, admin

### Templates

- `src/main/resources/templates/auth`
- `src/main/resources/templates/books`
- `src/main/resources/templates/rentals`
- `src/main/resources/templates/waitlist`
- `src/main/resources/templates/notifications`
- `src/main/resources/templates/reports`
- `src/main/resources/templates/staff`
- `src/main/resources/templates/user`

## Run Locally

1. Install Java 17 and Maven.
2. Open the project root.
3. Run:

```bash
/opt/homebrew/bin/mvn -Dmaven.repo.local=.m2/repository spring-boot:run
```

4. Open:

- `http://localhost:8080`
- H2 console: `http://localhost:8080/h2-console`

## Database Configuration

The project uses file-based H2 persistence, so data remains available across restarts.

Configured in:
- `src/main/resources/application.properties`

Current database URL:

```text
jdbc:h2:file:./data/bookrentaldb
```

H2 console values:

- JDBC URL: `jdbc:h2:file:./data/bookrentaldb`
- Username: `sa`
- Password: leave blank

## Demo Credentials

The application seeds moderator and admin accounts automatically.

- Moderator:
  - Email: `moderator@bookrental.local`
  - Password: `mod123`

- Administrator:
  - Email: `admin@bookrental.local`
  - Password: `admin123`

Regular users can be created through the registration page.

## Team Ownership

- Tanisha: Authentication, Login/Logout, Registration, Profile Management, Notifications
- Tanya: Book Listing, Browse/Search Books, Waitlist Management
- Vedika: Rental Requests, Approve/Reject Requests, Mock Advance Payment, Rental Lifecycle, Extension Requests, Returns, Extra Charges, Overdue Handling
- Vennela: Home Dashboard, Chat, Reviews and Ratings, Issue Reporting, Moderation, Admin Dashboard, Analytics


## Build Verification

Run:

```bash
/opt/homebrew/bin/mvn -Dmaven.repo.local=.m2/repository test
```

This verifies that the Spring Boot application context loads successfully.

## Notes

- Passwords are currently stored in plain text for classroom/demo purposes.
- The payment flow is mock/simulated and does not use a real payment gateway.
- The project is structured for OOAD submission, UML mapping, and team-based module ownership.
