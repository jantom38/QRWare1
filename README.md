# QRWare - Warehouse QR Management System

Zaawansowany system magazynowy oparty na kodach QR z kontrolą dostępu i bezpieczeństwem.

## 🚀 Szybki Start

### Wymagania
- Java 17+
- Maven 3.6+
- PostgreSQL 12+ (opcjonalnie, domyślnie używa H2)

### Uruchomienie

1. **Klonowanie repozytorium:**
```bash
git clone <repository-url>
cd QRWare
```

2. **Kompilacja:**
```bash
mvn clean compile
```

3. **Uruchomienie (profil dev z H2):**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

4. **Uruchomienie (profil prod z PostgreSQL):**
```bash
# Najpierw skonfiguruj PostgreSQL w application.yml
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 🔗 Dostępne URL

- **Aplikacja:** http://localhost:8080
- **H2 Console:** http://localhost:8080/h2-console (tylko dev)
- **Health Check:** http://localhost:8080/api/health
- **Swagger UI:** http://localhost:8080/swagger-ui.html (TODO)
- **Actuator:** http://localhost:8080/actuator/health

## 🧪 Testowanie API

### 1. Health Check
```bash
curl http://localhost:8080/api/health
```

### 2. Status API
```bash
curl http://localhost:8080/api/status
```

### 3. Rejestracja użytkownika
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPass123!",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### 4. Logowanie
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "TestPass123!"
  }'
```

### 5. Sprawdzenie profilu (wymagany token)
```bash
# Użyj tokena z logowania
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 6. Test endpointów (wymagany token)
```bash
# Test podstawowych endpointów
curl -X GET http://localhost:8080/api/test/public
curl -X GET http://localhost:8080/api/test/protected \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
curl -X GET http://localhost:8080/api/test/admin \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🔧 Konfiguracja

### Profile
- **dev** - H2 database, debug logging
- **prod** - PostgreSQL, production settings

### Environment Variables
```bash
# Database (prod)
export DB_URL=jdbc:postgresql://localhost:5432/qrware_db
export DB_USERNAME=qrware_user
export DB_PASSWORD=qrware_password

# JWT
export JWT_SECRET=your-super-secret-key-min-512-bits
export JWT_EXPIRATION=86400000

# CORS
export CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
```

## 📊 Monitorowanie

### H2 Console (dev)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (puste)


## 🛠️ Development

### Dostępne Maven Goals
```bash
# Kompilacja
mvn compile

# Uruchomienie testów
mvn test

# Pakowanie
mvn package

# Uruchomienie z profilem
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Clean + restart
mvn clean spring-boot:run
```

### Struktura Projektu
```
src/main/java/com/qrware/
├── domain/          # Modele domenowe
├── repository/      # Repozytoria JPA
├── security/        # Konfiguracja bezpieczeństwa
├── controller/      # REST Controllers
└── WarehouseQRSystemApplication.java
```

## 🔒 Security

### Domyślne Role
- **USER** - podstawowy dostęp
- **WAREHOUSE_WORKER** - operacje magazynowe
- **WAREHOUSE_MANAGER** - zarządzanie magazynem
- **ADMIN** - pełny dostęp

### JWT Tokens
- **Access Token** - 24h ważności
- **Refresh Token** - 7 dni ważności
- **Algorithm** - HS512

## 📝 TODO
- [ ] Flyway migrations
- [ ] Sample data loading
- [ ] Swagger documentation
- [ ] Docker configuration
- [ ] Integration tests
- [ ] QR Code generation service
- [ ] Inventory management endpoints
- [ ] Report generation

## 🐛 Troubleshooting

### Częste problemy

1. **Port 8080 zajęty:**
```bash
# Zmień port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

2. **CORS errors:**
```bash
# Sprawdź konfigurację w application.yml
# Upewnij się że frontend URL jest w allowed-origins
```

3. **JWT errors:**
```bash
# Sprawdź czy JWT_SECRET ma co najmniej 64 znaki
# Sprawdź ważność tokena
```

4. **Database connection:**
```bash
# Dev: H2 powinno działać automatycznie
# Prod: Sprawdź czy PostgreSQL jest uruchomiony
```

## 📞 Support

W przypadku problemów sprawdź:
1. Logi aplikacji
2. Actuator health endpoint
3. H2 console (dev)
4. Network tab w browser dev tools (CORS)
