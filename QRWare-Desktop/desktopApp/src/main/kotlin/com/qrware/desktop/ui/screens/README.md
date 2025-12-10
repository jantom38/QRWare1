# Desktop Application Screens

## Status systemu autoryzacji

✅ **Gotowe:**
- ✅ Modele danych autoryzacji (AuthModels.kt)
- ✅ HTTP Client z obsługą tokenów (HttpClient.kt)
- ✅ API Service dla autoryzacji (AuthApiService.kt)
- ✅ System przechowywania tokenów (TokenStorage + PlatformTokenStorage)
- ✅ Manager tokenów z auto-refresh (TokenManager.kt)
- ✅ Repository autoryzacji (AuthRepository.kt)
- ✅ Dependency Injection (SharedModule.kt)
- ✅ Główna aplikacja desktop (Main.kt)
- ✅ Ekran logowania z pełnym UI (LoginScreen.kt)
- ✅ ViewModel dla logowania (LoginViewModel.kt)
- ✅ Podstawowy ekran główny (HomeScreen.kt)
- ✅ Motyw aplikacji (QRWareTheme.kt + Typography.kt)

## Następne kroki:

1. **Test połączenia z backendem**
2. **Dodanie nawigacji między ekranami**
3. **Implementacja pozostałych ekranów (Inventory, Orders, etc.)**
4. **Obsługa błędów i offline mode**
5. **Konfiguracja serwera przez UI**

## Struktura aplikacji:

```
QRWare-Desktop/
├── shared/
│   ├── data/
│   │   ├── model/ (AuthModels.kt, InventoryModels.kt, LocationModels.kt)
│   │   ├── network/ (HttpClient.kt, AuthApiService.kt)
│   │   ├── storage/ (TokenStorage.kt)
│   │   ├── auth/ (TokenManager.kt)
│   │   └── repository/ (AuthRepository.kt)
│   └── di/ (SharedModule.kt)
└── desktopApp/
    ├── ui/
    │   ├── screens/ (LoginScreen.kt, HomeScreen.kt)
    │   ├── viewmodel/ (LoginViewModel.kt)
    │   └── theme/ (QRWareTheme.kt, Typography.kt)
    └── Main.kt
```

## Features implementowane:

- **🔐 Kompletna autoryzacja:** Login, logout, token refresh
- **💾 Bezpieczne przechowywanie tokenów:** Szyfrowane lokalnie
- **🌐 HTTP Client:** Z automatycznym dodawaniem tokenów
- **🎨 Material 3 UI:** Responsive design
- **⚡ Kotlin Multiplatform:** Gotowe do rozszerzenia na Android
- **🔄 Auto token refresh:** Automatyczne odświeżanie przed wygaśnięciem