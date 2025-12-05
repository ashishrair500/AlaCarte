# PostgreSQL Database Usage Guide

This guide shows you how to use PostgreSQL with Spring Data JPA in your MenuAgent project.

## Table of Contents
- [Overview](#overview)
- [Entity Classes](#entity-classes)
- [Repository Pattern](#repository-pattern)
- [Service Layer](#service-layer)
- [Common Operations](#common-operations)
- [Query Methods](#query-methods)
- [Best Practices](#best-practices)

## Overview

The project uses:
- **PostgreSQL** as the database
- **Spring Data JPA** for ORM (Object-Relational Mapping)
- **Hibernate** as the JPA implementation
- **Lombok** to reduce boilerplate code

## Entity Classes

Entity classes represent database tables. Here's the example `MenuItem` entity:

### Example Entity

```java
@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private BigDecimal price;
    
    // ... other fields
}
```

### Key Annotations

- `@Entity` - Marks class as a JPA entity
- `@Table(name = "menu_items")` - Specifies the table name
- `@Id` - Marks the primary key field
- `@GeneratedValue` - Auto-generates the ID
- `@Column` - Customizes column properties
- `@Data` - Lombok annotation for getters/setters
- `@NoArgsConstructor` / `@AllArgsConstructor` - Lombok constructors

## Repository Pattern

Repositories provide database access methods. Spring Data JPA automatically implements them.

### Example Repository

```java
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    // Spring Data JPA generates these methods automatically
    List<MenuItem> findByCategory(String category);
    List<MenuItem> findByIsVegetarianTrue();
    List<MenuItem> findByPriceBetween(BigDecimal min, BigDecimal max);
    
    // Custom JPQL query
    @Query("SELECT m FROM MenuItem m WHERE m.category = :cat")
    List<MenuItem> customQuery(@Param("cat") String category);
}
```

### Built-in Methods

All repositories extending `JpaRepository` get these methods for free:

```java
// Create/Update
MenuItem save(MenuItem item);
List<MenuItem> saveAll(Iterable<MenuItem> items);

// Read
Optional<MenuItem> findById(Long id);
List<MenuItem> findAll();
List<MenuItem> findAllById(Iterable<Long> ids);
long count();
boolean existsById(Long id);

// Delete
void deleteById(Long id);
void delete(MenuItem item);
void deleteAll();
```

## Service Layer

Services contain business logic and use repositories:

```java
@Service
public class MenuItemService {
    
    @Autowired
    private MenuItemRepository repository;
    
    public MenuItem createMenuItem(MenuItem item) {
        return repository.save(item);
    }
    
    public List<MenuItem> getAllMenuItems() {
        return repository.findAll();
    }
    
    public Optional<MenuItem> getMenuItemById(Long id) {
        return repository.findById(id);
    }
    
    public List<MenuItem> getVegetarianItems() {
        return repository.findByIsVegetarianTrue();
    }
    
    public void deleteMenuItem(Long id) {
        repository.deleteById(id);
    }
}
```

## Common Operations

### Creating New Records

```java
MenuItem item = new MenuItem();
item.setName("Margherita Pizza");
item.setDescription("Classic Italian pizza");
item.setPrice(new BigDecimal("12.99"));
item.setCategory("Pizza");
item.setIsVegetarian(true);

MenuItem saved = menuItemRepository.save(item);
```

### Reading Records

```java
// Find by ID
Optional<MenuItem> item = menuItemRepository.findById(1L);
if (item.isPresent()) {
    System.out.println(item.get().getName());
}

// Find all
List<MenuItem> allItems = menuItemRepository.findAll();

// Count
long count = menuItemRepository.count();
```

### Updating Records

```java
Optional<MenuItem> item = menuItemRepository.findById(1L);
if (item.isPresent()) {
    MenuItem menuItem = item.get();
    menuItem.setPrice(new BigDecimal("14.99"));
    menuItemRepository.save(menuItem); // Updates existing record
}
```

### Deleting Records

```java
// Delete by ID
menuItemRepository.deleteById(1L);

// Delete by entity
MenuItem item = menuItemRepository.findById(1L).orElseThrow();
menuItemRepository.delete(item);

// Delete all
menuItemRepository.deleteAll();
```

## Query Methods

Spring Data JPA can generate queries from method names:

### Method Name Patterns

```java
// Find by single field
List<MenuItem> findByName(String name);
List<MenuItem> findByCategory(String category);

// Find with conditions
List<MenuItem> findByPriceGreaterThan(BigDecimal price);
List<MenuItem> findByPriceLessThan(BigDecimal price);
List<MenuItem> findByPriceBetween(BigDecimal min, BigDecimal max);

// Find with boolean
List<MenuItem> findByIsVegetarianTrue();
List<MenuItem> findByIsVeganFalse();

// String matching
List<MenuItem> findByNameContaining(String keyword);
List<MenuItem> findByNameStartingWith(String prefix);
List<MenuItem> findByNameIgnoreCase(String name);

// Combining conditions with AND/OR
List<MenuItem> findByCategoryAndIsVegetarianTrue(String category);
List<MenuItem> findByIsVegetarianTrueOrIsVeganTrue();

// Sorting
List<MenuItem> findByCategoryOrderByPriceAsc(String category);
List<MenuItem> findByIsVegetarianTrueOrderByNameDesc();

// Limiting results
MenuItem findFirstByCategory(String category);
List<MenuItem> findTop5ByOrderByPriceDesc();
```

### Custom JPQL Queries

```java
@Query("SELECT m FROM MenuItem m WHERE m.category = :category AND m.price < :maxPrice")
List<MenuItem> findByCategoryUnderPrice(
    @Param("category") String category,
    @Param("maxPrice") BigDecimal maxPrice
);
```

### Native SQL Queries

```java
@Query(value = "SELECT * FROM menu_items WHERE category = ?1 LIMIT 10", nativeQuery = true)
List<MenuItem> findTop10ByCategory(String category);
```

## Best Practices

### 1. Use Constructor Injection

```java
@Service
public class MenuItemService {
    private final MenuItemRepository repository;
    
    public MenuItemService(MenuItemRepository repository) {
        this.repository = repository;
    }
}
```

### 2. Handle Optional Values

```java
public MenuItem getMenuItem(Long id) {
    return menuItemRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
}
```

### 3. Use Transactions for Multiple Operations

```java
@Transactional
public void updateMenuItems(List<MenuItem> items) {
    items.forEach(item -> {
        // Multiple operations in one transaction
        menuItemRepository.save(item);
    });
}
```

### 4. Use DTOs for API Responses

Don't expose entities directly in your API:

```java
@Data
public class MenuItemDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    
    public static MenuItemDTO from(MenuItem entity) {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPrice(entity.getPrice());
        return dto;
    }
}
```

### 5. Validate Input

```java
@Entity
public class MenuItem {
    @NotNull
    @Size(min = 1, max = 200)
    private String name;
    
    @Min(0)
    private BigDecimal price;
}
```

### 6. Use Pagination for Large Results

```java
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Page<MenuItem> findByCategory(String category, Pageable pageable);
}

// Usage
Pageable pageable = PageRequest.of(0, 10); // page 0, size 10
Page<MenuItem> page = repository.findByCategory("Pizza", pageable);
```

## Configuration Options

### application.properties

```properties
# Show SQL queries in logs (useful for debugging)
spring.jpa.show-sql=true

# Format SQL for readability
spring.jpa.properties.hibernate.format_sql=true

# DDL Auto modes:
# - create: Drop and recreate tables (CAREFUL!)
# - create-drop: Create on start, drop on shutdown
# - update: Update schema automatically (recommended for development)
# - validate: Validate schema, don't change it
# - none: Do nothing
spring.jpa.hibernate.ddl-auto=update

# Database dialect
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## Testing with Database

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MenuItemRepositoryTest {
    
    @Autowired
    private MenuItemRepository repository;
    
    @Test
    void testSaveMenuItem() {
        MenuItem item = new MenuItem();
        item.setName("Test Item");
        
        MenuItem saved = repository.save(item);
        
        assertNotNull(saved.getId());
        assertEquals("Test Item", saved.getName());
    }
}
```

## Next Steps

1. Create your own entity classes based on `MenuItem` example
2. Create corresponding repository interfaces
3. Implement service layer with business logic
4. Create REST controllers to expose your data
5. Add validation and error handling
6. Write tests for your repositories and services

For more information:
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)










