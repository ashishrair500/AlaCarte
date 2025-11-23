# Quick Setup Guide

## Step 1: Set Up PostgreSQL Database

### Install PostgreSQL (if not already installed)

**On macOS:**
```bash
brew install postgresql@16
brew services start postgresql@16
```

**On Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

**On Windows:**
Download and install from: https://www.postgresql.org/download/windows/

### Create Database

```bash
# Login to PostgreSQL
psql -U postgres

# Create the database
CREATE DATABASE menuagent;

# Exit
\q
```

**Default credentials** (update in `application.properties` if different):
- **Username**: postgres
- **Password**: postgres
- **Database**: menuagent
- **Port**: 5432

### Update Database Configuration

Edit `src/main/resources/application.properties` and update these values if needed:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/menuagent
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## Step 2: Set Your Gemini API Key

You need a Google Gemini API key. Get one from: https://makersuite.google.com/app/apikey

**Option 1: Set as environment variable (Recommended)**
```bash
export GEMINI_API_KEY=your-api-key-here
```

**Option 2: Edit application.properties**
Edit `src/main/resources/application.properties` and replace `your-api-key-here` with your actual key.

## Step 3: Run the Application

### Easy Way (using the script):
```bash
./run.sh
```

### Manual Way:
```bash
# Set Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Build and run
./gradlew bootRun
```

## Step 4: Test the API

Once the application is running, test it with:

```bash
curl -X POST http://localhost:8080/api/food/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "mood": "happy",
    "preferences": "vegetarian, spicy"
  }'
```

Or check health:
```bash
curl http://localhost:8080/api/food/health
```

## Database Features

The application uses **Spring Data JPA** with **PostgreSQL**:

- **Automatic table creation**: Tables are created automatically based on your entity classes
- **ORM mapping**: Use JPA annotations to map Java objects to database tables
- **Repository pattern**: Simple CRUD operations with JpaRepository
- **Query generation**: Automatic query generation from method names

## Troubleshooting

### General Issues
- **Java version issues**: The script automatically uses Java 21. Make sure you have it installed.
- **Port 8080 already in use**: Change the port in `application.properties`: `server.port=8081`
- **Menu PDF not found**: Make sure the PDF file is at `src/main/resources/P Town Bar Drinks Menu_design2.pdf` or update the path in `application.properties`

### Database Issues
- **Connection refused**: Make sure PostgreSQL is running (`brew services start postgresql@16` on macOS)
- **Authentication failed**: Check username/password in `application.properties`
- **Database does not exist**: Create the database using `CREATE DATABASE menuagent;`
- **Port 5432 in use**: Another PostgreSQL instance might be running. Change the port or stop other instances.

