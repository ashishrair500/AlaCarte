# PostgreSQL Database Integration - Complete Guide

## 📦 What Has Been Set Up

Your MenuAgent project now has full PostgreSQL database support! Here's what was added:

### ✅ Dependencies (build.gradle)
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
runtimeOnly 'org.postgresql:postgresql:42.7.1'
```

### ✅ Database Configuration (application.properties)
```properties
# PostgreSQL Connection
spring.datasource.url=jdbc:postgresql://localhost:5432/menuagent
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Settings
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### ✅ Example Code Files

| File | Purpose | Location |
|------|---------|----------|
| `MenuItem.java` | Example entity (table) | `src/main/java/com/foodbymood/entity/` |
| `MenuItemRepository.java` | Database operations | `src/main/java/com/foodbymood/repository/` |
| `MenuItemService.java` | Business logic | `src/main/java/com/foodbymood/service/` |

### ✅ Documentation Files

| File | What It Contains |
|------|------------------|
| `POSTGRESQL_QUICK_START.md` | 5-minute setup guide (START HERE!) |
| `DATABASE_USAGE.md` | Comprehensive database usage guide |
| `SETUP.md` | Updated with PostgreSQL setup instructions |

## 🚀 Get Started in 3 Steps

### Step 1: Install PostgreSQL

Choose your platform:

**macOS:**
```bash
brew install postgresql@16
brew services start postgresql@16
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

**Windows:**
- Download from [postgresql.org](https://www.postgresql.org/download/windows/)

### Step 2: Create Database

```bash
# Access PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE menuagent;

# Verify
\l

# Exit
\q
```

### Step 3: Build and Run

```bash
# Build project (downloads dependencies)
./gradlew build

# Run application
./gradlew bootRun
```

**That's it!** Your application now has PostgreSQL database support.

## 📖 Documentation Guide

Choose based on your needs:

### 🏃‍♂️ I want to get started quickly (5 minutes)
→ Read `POSTGRESQL_QUICK_START.md`

### 📚 I want to learn how to use the database
→ Read `DATABASE_USAGE.md`

### 🔧 I want to configure everything properly
→ Read `SETUP.md`

### 👀 I want to see example code
→ Look at:
- `src/main/java/com/foodbymood/entity/MenuItem.java`
- `src/main/java/com/foodbymood/repository/MenuItemRepository.java`
- `src/main/java/com/foodbymood/service/MenuItemService.java`

## 🎯 What You Can Do Now

### 1. Create Database Tables (Entities)

```java
@Entity
@Table(name = "your_table")
@Data
public class YourEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    // ... other fields
}
```

### 2. Perform Database Operations (Repositories)

```java
@Repository
public interface YourRepository extends JpaRepository<YourEntity, Long> {
    List<YourEntity> findByName(String name);
    // Spring Data JPA generates the implementation!
}
```

### 3. Add Business Logic (Services)

```java
@Service
public class YourService {
    private final YourRepository repository;
    
    public YourService(YourRepository repository) {
        this.repository = repository;
    }
    
    public List<YourEntity> getAll() {
        return repository.findAll();
    }
}
```

## 💡 Key Features

### Automatic Table Creation
- Tables are created automatically from your entity classes
- No need to write SQL CREATE TABLE statements
- Schema updates automatically when you change entities

### Built-in CRUD Operations
```java
repository.save(entity);           // Create/Update
repository.findById(id);           // Read
repository.findAll();              // Read all
repository.deleteById(id);         // Delete
```

### Query Generation from Method Names
```java
// Spring generates SQL automatically!
List<MenuItem> findByCategory(String category);
List<MenuItem> findByPriceGreaterThan(BigDecimal price);
List<MenuItem> findByIsVegetarianTrue();
```

### Custom Queries
```java
@Query("SELECT m FROM MenuItem m WHERE m.category = :cat")
List<MenuItem> customQuery(@Param("cat") String category);
```

## 🔍 Verify Everything Works

### 1. Check Database Connection

Run the application and look for these logs:

```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Hibernate: create table menu_items (...)
```

✅ **Success!** Database is connected and tables are created.

### 2. Check Tables in PostgreSQL

```bash
psql -U postgres -d menuagent

\dt                    # List tables
\d menu_items         # Show table structure
```

### 3. Test with a Simple Endpoint

Create a test controller:

```java
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private MenuItemService service;
    
    @GetMapping("/count")
    public long count() {
        return service.countMenuItems();
    }
}
```

Test it:
```bash
curl http://localhost:8080/api/test/count
# Should return: 0
```

## 🛠️ Common Tasks

### Change Database Name
Edit `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database_name
```

### Change Database Credentials
Edit `application.properties`:
```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Use Different Port
Edit `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/menuagent
```

### Disable Auto Schema Creation
Edit `application.properties`:
```properties
spring.jpa.hibernate.ddl-auto=none
```

### Hide SQL Logs
Edit `application.properties`:
```properties
spring.jpa.show-sql=false
```

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| Connection refused | Start PostgreSQL: `brew services start postgresql@16` |
| Authentication failed | Check username/password in `application.properties` |
| Database doesn't exist | Create it: `psql -U postgres -c "CREATE DATABASE menuagent;"` |
| Port already in use | Change port in `application.properties` |
| Lombok errors | Run `./gradlew build` to download dependencies |

## 📚 Learn More

### Spring Data JPA
- [Official Documentation](https://spring.io/projects/spring-data-jpa)
- [Query Methods](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods)

### PostgreSQL
- [Official Documentation](https://www.postgresql.org/docs/)
- [Data Types](https://www.postgresql.org/docs/current/datatype.html)

### Hibernate (JPA Implementation)
- [Documentation](https://hibernate.org/orm/documentation/)
- [Annotations Reference](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#annotations)

## 🎨 Architecture Overview

```
┌─────────────────────────────────────────┐
│          REST Controllers               │
│  (Handle HTTP requests/responses)       │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│           Service Layer                 │
│     (Business logic & transactions)     │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│        Repository Layer                 │
│   (Database operations - auto-generated)│
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Entity Classes                  │
│     (Java objects = Database tables)    │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         PostgreSQL Database             │
│      (Data persistence storage)         │
└─────────────────────────────────────────┘
```

## ✨ Next Steps

1. ✅ **Set up PostgreSQL** - Follow `POSTGRESQL_QUICK_START.md`
2. ✅ **Build and run** - Test the connection
3. 📖 **Learn the basics** - Read `DATABASE_USAGE.md`
4. 🎨 **Create your entities** - Model your data
5. 🚀 **Build features** - Use the database in your app

## 📞 Quick Reference

| Task | Command |
|------|---------|
| Start PostgreSQL | `brew services start postgresql@16` (macOS) |
| Stop PostgreSQL | `brew services stop postgresql@16` (macOS) |
| Access database | `psql -U postgres -d menuagent` |
| List tables | `\dt` (in psql) |
| View table | `\d table_name` (in psql) |
| Run queries | `SELECT * FROM table_name;` (in psql) |
| Build project | `./gradlew build` |
| Run app | `./gradlew bootRun` |

---

**🎉 You're all set with PostgreSQL! Start building amazing features!**

Questions? Check the documentation files or the example code.





