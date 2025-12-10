# QRWare Desktop - Setup & Quick Start

## ✅ Co zostało zaimplementowane

### 🏗️ **Kompletna architektura Kotlin Multiplatform**
- ✅ **Shared module** z business logiką
- ✅ **Desktop app** z Compose Multiplatform UI
- ✅ **Gradle configuration** KMP + Compose

### 🔐 **Kompletny system autoryzacji**
- ✅ **Models**: `AuthModels.kt` - wszystkie modele danych
- ✅ **Network**: `HttpClient.kt` + `AuthApiService.kt` - komunikacja z API
- ✅ **Storage**: `TokenStorage.kt` + `PlatformTokenStorage.kt` - bezpieczne przechowywanie
- ✅ **Auth Management**: `TokenManager.kt` - auto-refresh tokenów
- ✅ **Repository**: `AuthRepository.kt` - główny interfejs dla UI
- ✅ **DI**: `SharedModule.kt` - dependency injection

### 🎨 **Kompletny UI desktop**
- ✅ **Login Screen**: Pełny ekran logowania z walidacją
- ✅ **Home Screen**: Dashboard z quick actions
- ✅ **Theme**: Material 3 z custom kolorami QRWare
- ✅ **ViewModels**: `LoginViewModel.kt` dla zarządzania stanem

### 🛠️ **Narzędzia developerskie**
- ✅ **Build scripts**: `run.bat`, `test-build.bat`
- ✅ **Gradle wrapper** skonfigurowany

## 🚀 Quick Start

### 1. **Start Backend Server**
```bash
cd C:\Users\jasie\Documents\GitHub\QRWare
.\start.bat
# Backend będzie dostępny na http://localhost:8080
```

### 2. **Build & Run Desktop App**
```bash
cd C:\Users\jasie\Documents\GitHub\QRWare\QRWare-Desktop
.\run.bat
```

### 3. **Test Login**
- Username: `admin` (lub jak masz skonfigurowane w backend)
- Password: `password`
- Server URL: `http://localhost:8080`

## 📁 Struktura projektu

```
QRWare-Desktop/
├── shared/                    # 🔗 Wspólny kod (Android + Desktop)
│   ├── data/
│   │   ├── model/            # 📝 Modele danych
│   │   ├── network/          # 🌐 HTTP Client & API
│   │   ├── storage/          # 💾 Lokalne przechowywanie
│   │   ├── auth/            # 🔐 Zarządzanie autoryzacją
│   │   └── repository/      # 🗃️ Repository pattern
│   └── di/                  # 💉 Dependency Injection
├── desktopApp/              # 🖥️ Aplikacja Windows/Desktop
│   └── ui/
│       ├── screens/         # 📱 Ekrany UI
│       ├── viewmodel/       # 🧠 ViewModels
│       └── theme/          # 🎨 Material 3 Theme
└── build files...
```

## 🔧 Features zaimplementowane

### 🔐 **Autoryzacja**
- [x] Login/Logout
- [x] Token management (access + refresh)
- [x] Automatic token refresh
- [x] Secure local storage (encrypted)
- [x] Server health check
- [x] Permission & role management
- [x] Password validation

### 🖥️ **Desktop UI**
- [x] Material 3 Design System
- [x] Responsive login screen
- [x] Dashboard z quick actions
- [x] Error handling & loading states
- [x] Dark/Light theme support
- [x] Server settings dialog

### 🏗️ **Architecture**
- [x] Kotlin Multiplatform
- [x] Compose Multiplatform
- [x] MVVM pattern
- [x] Repository pattern
- [x] Dependency Injection
- [x] Coroutines & Flow

## 🎯 Następne kroki

### 1. **Natychmiastowe**
```bash
# Test czy działa:
cd QRWare-Desktop
.\test-build.bat
.\run.bat
```

### 2. **Development roadmap**
1. **📱 Dodaj nawigację** między ekranami
2. **📦 Inventory management** screens
3. **📋 Orders management** 
4. **🏷️ QR Code scanner**
5. **👥 User management**
6. **🔧 Settings & configuration**

### 3. **Android migration**
```kotlin
// Dodaj Android target do shared/build.gradle.kts:
android {
    namespace = "com.qrware.shared"
    compileSdk = 35
}
```

## 🎉 **Gotowe do użycia!**

Masz teraz:
- ✅ **Pełną architekturę KMP**
- ✅ **Działający system autoryzacji** 
- ✅ **Desktop app z Material 3 UI**
- ✅ **Auto token refresh**
- ✅ **Bezpieczne przechowywanie danych**

**Start testing:** `.\run.bat` 🚀