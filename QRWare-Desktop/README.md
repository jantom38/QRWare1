# QRWare Desktop - System Zarządzania Magazynem

Aplikacja desktopowa dla systemu QRWare zbudowana w Kotlin Multiplatform z Jetpack Compose Desktop.

## 🚀 Funkcjonalności

### Zaimplementowane w pierwszej wersji:
- ✅ **Logowanie użytkowników** - bezpieczna autentykacja z tokenami JWT
- ✅ **Dashboard** - przegląd stanu systemu z kluczowymi metrykami
- ✅ **Zarządzanie Produktami** - przeglądanie, dodawanie, edycja produktów
- ✅ **Kategorie Produktów** - organizacja produktów w kategorie
- ✅ **Stan Magazynowy** - monitorowanie poziomu zapasów
- ✅ **Lokalizacje** - zarządzanie lokalizacjami w magazynie
- ✅ **Strefy** - organizacja magazynu w strefy funkcjonalne
- ✅ **Status Systemu** - monitoring zdrowia backendu

### Planowane funkcjonalności:
- 🔄 **QR Code Scanner** - skanowanie kodów przez kamerę (Windows Hello Camera)
- 🔄 **Generowanie QR** - tworzenie kodów QR dla produktów
- 🔄 **Historia Ruchów** - śledzenie wszystkich operacji magazynowych
- 🔄 **Zarządzanie Zamówieniami** - obsługa zamówień i realizacji
- 🔄 **Raporty** - generowanie raportów PDF/Excel
- 🔄 **Powiadomienia** - alerty o niskim stanie, terminach
- 🔄 **Tryb Offline** - podstawowa funkcjonalność bez połączenia

## 📋 Wymagania

### Środowisko programistyczne:
- **JDK 11+** (zalecane JDK 17)
- **Kotlin 2.0.21+**
- **Gradle 8.9+**
- **IntelliJ IDEA** lub **Android Studio**

### System operacyjny:
- **Windows 10/11** (x64)
- **8GB RAM** (minimum)
- **1GB** wolnego miejsca na dysku
- **Połączenie internetowe** do backendu

### Backend:
- Działający serwer **QRWare Backend** (Spring Boot)
- Domyślnie: `http://localhost:8080`

## 🛠️ Instalacja i Uruchomienie

### 1. Klonowanie repozytorium
```bash
cd C:/Users/jasie/Documents/GitHub/QRWare/QRWare-Desktop
```

### 2. Sprawdzenie środowiska
```bash
java -version  # Powinna być wersja 11+
gradle -version
```

### 3. Budowanie projektu
```bash
# Budowanie shared module
./gradlew :shared:build

# Budowanie aplikacji desktop
./gradlew :desktopApp:build
```

### 4. Uruchomienie w trybie deweloperskim
```bash
./gradlew :desktopApp:run
```

### 5. Tworzenie dystrybucji
```bash
# Windows MSI installer
./gradlew :desktopApp:createDistributable
./gradlew :desktopApp:packageMsi

# Portable version
./gradlew :desktopApp:createRuntimeImage
```

## 🏗️ Architektura

### Struktura projektu:
```
QRWare-Desktop/
├── shared/                     # Wspólny kod KMP
│   ├── src/commonMain/kotlin/
│   │   ├── data/
│   │   │   ├── model/         # Modele danych
│   │   │   ├── network/       # API Client (Ktor)
│   │   │   └── repository/    # Repozytoria danych
│   │   └── di/                # Dependency Injection
│   └── build.gradle.kts
├── desktopApp/                # Aplikacja Windows
│   ├── src/main/kotlin/
│   │   ├── ui/
│   │   │   ├── screens/       # Ekrany aplikacji
│   │   │   ├── navigation/    # Nawigacja
│   │   │   └── theme/         # Material Design 3
│   │   └── Main.kt           # Entry point
│   └── build.gradle.kts
└── build.gradle.kts           # Root build script
```

### Stack technologiczny:
- **Kotlin Multiplatform** - współdzielenie kodu
- **Jetpack Compose Desktop** - nowoczesny UI
- **Material Design 3** - system projektowania
- **Ktor Client** - komunikacja HTTP z backendem
- **Kotlinx Serialization** - serializacja JSON
- **Coroutines & Flow** - programowanie asynchroniczne

## 🔧 Konfiguracja

### Zmiana URL serwera:
Edytuj `shared/src/commonMain/kotlin/com/qrware/shared/di/AppModule.kt`:
```kotlin
private val apiClient by lazy { 
    ApiClient(
        baseUrl = "http://twoj-serwer:8080", // Zmień tutaj
        tokenProvider = { authRepository.authToken.value }
    )
}
```

### Dostosowanie UI:
- Kolory: `desktopApp/src/main/kotlin/ui/theme/Theme.kt`
- Ikony: używamy Material Icons Extended
- Typografia: Material Design 3 Typography

## 🐛 Rozwiązywanie problemów

### Błędy budowania:

#### "Could not resolve dependencies"
```bash
# Wyczyść cache Gradle
./gradlew clean
./gradlew --refresh-dependencies
```

#### "Java version compatibility"
```bash
# Sprawdź wersję Java
java -version
# Ustaw JAVA_HOME jeśli potrzeba
export JAVA_HOME=/path/to/jdk-17
```

### Błędy uruchomienia:

#### "Connection refused"
- Sprawdź czy backend działa na localhost:8080
- Uruchom backend: `mvn spring-boot:run` w katalogu głównym QRWare

#### "Authentication failed"
- Sprawdź credentials w ekranie logowania
- Domyślne konto admin (jeśli dostępne w backendzie)

### Problemy z wydajnością:
- Zwiększ pamięć JVM: dodaj `-Xmx4g` w gradle.properties
- Wyłącz animacje w trybie debug

## 🤝 Rozwój

### Dodawanie nowych funkcji:

1. **Model danych**: Dodaj w `shared/src/commonMain/kotlin/data/model/`
2. **API calls**: Rozszerz `ApiClient.kt`
3. **Repository**: Dodaj logikę biznesową w repository
4. **UI Screen**: Stwórz nowy ekran w `desktopApp/src/main/kotlin/ui/screens/`
5. **Navigation**: Dodaj do `AppNavigation.kt`

### Debugging:
- Włącz logowanie Ktor: ustaw level na `LogLevel.ALL`
- Użyj IntelliJ IDEA debugger
- Sprawdzaj logi w konsoli

### Testing:
```bash
# Uruchom testy
./gradlew test

# Testy tylko shared module
./gradlew :shared:test
```

## 📦 Dystrybucja

### Windows MSI:
```bash
./gradlew :desktopApp:packageMsi
# Wyjście: desktopApp/build/compose/binaries/main/msi/
```

### Portable exe:
```bash
./gradlew :desktopApp:createRuntimeImage
# Wyjście: desktopApp/build/compose/binaries/main/app/
```

## 📄 Licencja

Ten projekt jest częścią systemu QRWare. Wszystkie prawa zastrzeżone.

---

## 🎯 Roadmap

### Wersja 1.1 (Q2 2024):
- Pełna funkcjonalność QR kodów
- Skanowanie kodów przez kamerę
- Tryb offline z synchronizacją

### Wersja 1.2 (Q3 2024):
- Zaawansowane raporty
- Eksport do Excel/PDF
- Powiadomienia systemowe

### Wersja 2.0 (Q4 2024):
- Pełna paryteta z aplikacją mobilną
- Zaawansowana analityka
- Integracje z systemami zewnętrznymi

## 💡 Wsparcie

Jeśli masz pytania lub problemy:
1. Sprawdź sekcję "Rozwiązywanie problemów"
2. Przeszukaj issues w repozytorium
3. Stwórz nowy issue z szczegółowym opisem

**Miłego korzystania z QRWare Desktop!** 🚀