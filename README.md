# Loaner Application

A Spring Boot application for managing loans and customer credit. This application provides REST APIs for loan management with JWT authentication and role-based access control.

## Features

- Customer management with credit limits
- Loan creation and management
- Installment tracking
- Role-based access control (ADMIN, USER, and CUSTOMER roles)
- JWT Authentication
- H2 Database for data storage
- User management with email verification
- Role management system

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Git

## Building the Application

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/loaner.git
   cd loaner
   ```

2. Build the application:
   ```bash
   ./mvnw clean install
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Running the Application

1. Start the application:
   ```bash
   ./mvnw spring-boot:run
   ```

2. Access the H2 Console:
   - Open your browser and navigate to:
     ```
     http://localhost:9092/h2-console
     ```

3. Use the following database configuration:
   - JDBC URL: `jdbc:h2:mem:loanerdb`
   - Username: `sa`
   - Password: `password`

## API Documentation

### Authentication APIs

1. Register New User
   ```
   POST /api/auth/register
   ```
   Example:
   ```bash
   curl -X POST 'http://localhost:9092/api/auth/register' \
   -H 'Content-Type: application/json' \
   -d '{
     "username": "newuser",
     "password": "password123",
     "email": "user@example.com",
     "fullName": "New User"
   }'
   ```

2. Login
   ```
   POST /api/auth/login
   ```
   Example:
   ```bash
   curl -X POST 'http://localhost:9092/api/auth/login' \
   -H 'Content-Type: application/json' \
   -d '{
     "username": "admin",
     "password": "admin123"
   }'
   ```

### Loan APIs

1. Create Loan
   ```
   POST /api/loans
   ```
   Request Body:
   ```json
   {
     "customerId": "long",
     "amount": "decimal",
     "interestRate": "double",
     "numberOfInstallments": "integer"
   }
   ```
   Validation Rules:
   - Number of installments must be one of: 6, 9, 12, 24
   - Interest rate must be between 0.1 and 0.5
   - Customer must have sufficient credit limit
   - Installments will be created with equal amounts
   - Due dates will be set to the first day of each month

2. Get Loans for Customer
   ```
   GET /api/loans/customer/{customerId}
   ```
   Query Parameters:
   - `isPaid` (optional): Filter by payment status
   - `numberOfInstallments` (optional): Filter by number of installments

3. Get Loan by ID
   ```
   GET /api/loans/{loanId}
   ```

4. Pay Loan
   ```
   POST /api/loans/{loanId}/pay
   ```
   Request Body:
   ```json
   {
     "amount": "decimal"
   }
   ```
   Payment Rules:
   - Installments must be paid in full
   - Earlier installments are paid first
   - Only installments within 3 months can be paid
   - Early payments receive a 0.1% discount per day
   - Late payments incur a 0.1% penalty per day

   Response:
   ```json
   {
     "paidInstallments": "integer",
     "totalSpent": "decimal",
     "loanFullyPaid": "boolean"
   }
   ```

### Loan Installment APIs

1. Get Installments by Loan ID
   ```
   GET /api/installments/{loanId}
   ```

2. Create Loan Installment
   ```
   POST /api/installments
   ```
   Request Body:
   ```json
   {
     "loanId": "long",
     "amount": "decimal",
     "dueDate": "date"
   }
   ```

## Security

The application uses JWT for authentication and role-based access control. The following roles are available:

- ROLE_ADMIN: Has full access to all APIs
- ROLE_USER: Has basic access to customer and loan APIs
- ROLE_CUSTOMER: Has limited access to customer-specific APIs

Default Users:
- Admin: username: `admin`, password: `admin123`
- User: username: `customer1`, password: `customer123`

All endpoints except `/auth/login` and `/auth/register` require authentication.

## Database Schema

The application uses the following main tables:
- `users`: Stores user information
- `roles`: Stores role definitions
- `loans`: Stores loan information
- `loan_installments`: Stores loan installment information

## Testing

Run the tests using:
```bash
./mvnw test
```

## Configuration

The application can be configured using:
- `application.properties`: General configuration
- `application-dev.properties`: Development configuration
- `application-prod.properties`: Production configuration

Key configuration properties:
- `jwt.secret`: Secret key for JWT token generation
- `jwt.expiration`: Token expiration time in milliseconds
- `spring.datasource.*`: Database configuration
- `spring.jpa.*`: JPA/Hibernate configuration
- `server.port`: Application port (default: 9092)

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request 

