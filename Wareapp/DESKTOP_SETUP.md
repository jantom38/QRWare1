# 🖥️ QRWare Desktop App - Setup Guide

## ✅ **Status: GOTOWE DO URUCHOMIENIA!**

Desktop aplikacja została skompletnie skonfigurowana i zbudowana pomyślnie.

## 🚀 **Jak uruchomić w IntelliJ IDEA:**

### **Opcja 1: Gradle Task (NAJSZYBSZA)**

1. **Otwórz Terminal w IntelliJ** (`View → Tool Windows → Terminal`)
2. **Uruchom desktop app:**
   ```bash
   .\gradlew.bat :desktopApp:run
   ```
3. **Aplikacja się uruchomi!** 🎉

### **Opcja 2: Run Configuration**

1. **Konfiguracja Run:**
   - `Run → Edit Configurations...`
   - `Add New (+) → Gradle`
   - **Name:** `QRWare Desktop`
   - **Gradle project:** `Wareapp`
   - **Tasks:** `:desktopApp:run`
   - **Arguments:** `--console=plain`

2. **Uruchomienie:**
   - Kliknij zielony przycisk ▶️ obok "QRWare Desktop"

### **Opcja 3: Gradle Panel**

1. **Otwórz Gradle panel** (`View → Tool Windows → Gradle`)
2. **Rozwiń:** `Wareapp → desktopApp → Tasks → application`
3. **Kliknij dwukrotnie:** `run`

---

## 🎯 **Co otrzymasz:**

### **🔐 Login Screen**
- **Username:** `admin`, `user`, `test` (lub cokolwiek)
- **Password:** minimum 3 znaki (np. `password`)
- **Server Settings:** możliwość konfiguracji URL backendu
- **Responsive UI** z Material 3 design

### **🏠 Dashboard**
- **9 Quick Actions:** QR Scanner, Inventory, Orders, Locations, Products, Users, Reports, History, Settings
- **Profesjonalny layout** z kartami i grid view
- **Logout funkcjonalność**

---

## 🛠️ **Funkcje Desktop App:**

### **✅ Działające:**
- ✅ **Login system** (symulowany)
- ✅ **Material 3 Theme** (light/dark)
- ✅ **Responsive layout** (1200x800px)
- ✅ **Navigation** (Login ↔ Dashboard)
- ✅ **Server configuration** dialog
- ✅ **Cross-platform build** (Windows, macOS, Linux)

### **🔄 W przygotowaniu:**
- 🔄 **Real API integration** (Network Layer)
- 🔄 **QR Code scanning**
- 🔄 **Data tables** dla inventory/orders
- 🔄 **Navigation** między modułami

---

## 📁 **Struktura Desktop App:**

```
desktopApp/
├── src/desktopMain/kotlin/
│   └── com/qrware/desktop/
│       ├── Main.kt                    # Entry point
│       └── ui/
│           ├── QRWareDesktopApp.kt    # Main app container
│           ├── screens/
│           │   ├── LoginScreen.kt     # Login UI
│           │   └── HomeScreen.kt      # Dashboard UI
│           └── theme/
│               └── QRWareDesktopTheme.kt # Material 3 theme
└── build.gradle.kts                   # Desktop build config
```

---

## 🎉 **Ready to Test!**

**Uruchom aplikację jedną z powyższych metod i przetestuj:**

1. **Login flow** - wpisz dowolne dane i zaloguj się
2. **Dashboard** - przejrzyj quick actions
3. **Server Settings** - sprawdź dialog konfiguracji
4. **Logout** - wyloguj się i wróć do login

## 🚀 **Następne kroki:**

1. **Przetestować aplikację** na Windows
2. **Dodać Network Layer** (Ktor integration)
3. **Implementować konkretne moduły** (Inventory, Orders, etc.)
4. **Połączyć z backend API**

**Gotowe do testowania!** 🎯