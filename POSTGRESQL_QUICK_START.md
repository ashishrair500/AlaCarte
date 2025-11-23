# PostgreSQL Quick Start Guide

## 🚀 Quick Setup (5 minutes)

### 1. Install PostgreSQL

**macOS:**
```bash
brew install postgresql@16
brew services start postgresql@16
```

**Linux:**
```bash
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

### 2. Create Database

```bash
# Access PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE menuagent;

# Exit
\q
```

### 3. Update Configuration

The database settings are already configured in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/menuagent
spring.datasource.username=postgres
spring.datasource.password=postgres
```

**Change these if your PostgreSQL uses different credentials!**

### 4. Build and Run

```bash
# Build the project (downloads PostgreSQL driver and dependencies)
./gradlew build

# Run the application
./gradlew bootRun
```

## ✅ What's Been Set Up

### Dependencies Added to `build.gradle`:
- ✅ `spring-boot-starter-data-jpa` - JPA/Hibernate support
- ✅ `postgresql:42.7.1` - PostgreSQL JDBC driver

### Configuration in `application.properties`:
- ✅ Database connection URL
- ✅ Username and password
- ✅ JPA/Hibernate settings
- ✅ Auto-schema generation (creates tables automatically)

### Example Files Created:
- ✅ `MenuItem.java` - Example entity (database table)
- ✅ `MenuItemRepository.java` - Example repository (database operations)
- ✅ `MenuItemService.java` - Example service (business logic)

## 📝 Quick Database Operations

### Create a Record

```java
@Autowired
private MenuItemService menuItemService;

MenuItem pizza = new MenuItem();
pizza.setName("Margherita Pizza");
pizza.setDescription("Classic Italian pizza with tomato and mozzarella");
pizza.setPrice(new BigDecimal("12.99"));
pizza.setCategory("Pizza");
pizza.setIsVegetarian(true);
pizza.setSpiceLevel(0);

menuItemService.createMenuItem(pizza);
```

### Read Records

```java
// Get all items
List<MenuItem> allItems = menuItemService.getAllMenuItems();

// Get by ID
Optional<MenuItem> item = menuItemService.getMenuItemById(1L);

// Get vegetarian items
List<MenuItem> vegItems = menuItemService.getVegetarianItems();

// Search by name
List<MenuItem> pizzas = menuItemService.searchMenuItemsByName("pizza");
```

### Update a Record

```java
MenuItem updatedItem = new MenuItem();
updatedItem.setName("Updated Name");
updatedItem.setPrice(new BigDecimal("15.99"));
// ... set other fields

menuItemService.updateMenuItem(1L, updatedItem);
```

### Delete a Record

```java
menuItemService.deleteMenuItem(1L);
```

## 🎯 Testing Your Database

### Using psql Command Line

```bash
# Connect to database
psql -U postgres -d menuagent

# List tables
\dt

# View table structure
\d menu_items

# Query data
SELECT * FROM menu_items;

# Exit
\q
```

### Using Code (Create a Test Endpoint)

Add this to your controller:

```java
@RestController
@RequestMapping("/api/menu")
public class MenuItemController {
    
    private final MenuItemService menuItemService;
    
    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }
    
    @GetMapping
    public List<MenuItem> getAllItems() {
        return menuItemService.getAllMenuItems();
    }
    
    @PostMapping
    public MenuItem createItem(@RequestBody MenuItem item) {
        return menuItemService.createMenuItem(item);
    }
}
```

Then test with curl:

```bash
# Create an item
curl -X POST http://localhost:8080/api/menu \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Pizza",
    "description": "Delicious test pizza",
    "price": 10.99,
    "category": "Pizza",
    "isVegetarian": true,
    "spiceLevel": 1
  }'

# Get all items
curl http://localhost:8080/api/menu
```

## 🔧 Common Issues & Solutions

### Issue: Connection refused

**Solution:** PostgreSQL is not running
```bash
# macOS
brew services start postgresql@16

# Linux
sudo systemctl start postgresql
```

### Issue: Authentication failed

**Solution:** Wrong username/password in `application.properties`

Check your PostgreSQL credentials:
```bash
psql -U postgres
```

If that doesn't work, your username might be different (like your system username).

### Issue: Database "menuagent" does not exist

**Solution:** Create the database
```bash
psql -U postgres -c "CREATE DATABASE menuagent;"
```

### Issue: Port 5432 already in use

**Solution:** Another PostgreSQL is running, or change the port in `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/menuagent
```

## 📚 Next Steps

1. ✅ Follow this quick start to set up PostgreSQL
2. ✅ Run your application and verify it connects
3. 📖 Read `DATABASE_USAGE.md` for detailed usage guide
4. 📖 Read `SETUP.md` for complete setup instructions
5. 🎨 Create your own entities based on the `MenuItem` example
6. 🚀 Build your features with database persistence

## 🆘 Need Help?

Check these files:
- `DATABASE_USAGE.md` - Comprehensive database usage guide
- `SETUP.md` - Full project setup guide
- Example files in `src/main/java/com/foodbymood/`:
  - `entity/MenuItem.java`
  - `repository/MenuItemRepository.java`
  - `service/MenuItemService.java`

## 🔍 Verify Setup is Working

Run the application and check the logs. You should see:

```
Hibernate: create table menu_items (...)
HikariPool-1 - Start completed.
```

This means:
- ✅ Database connection successful
- ✅ Tables created automatically
- ✅ Ready to use!

---

**🎉 You're all set! Happy coding!**


