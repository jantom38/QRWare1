# Rozdział: Plan Testów i Weryfikacja Systemu QRWare

## 1. Wstęp
Celem niniejszego rozdziału jest przedstawienie strategii testowania systemu QRWare, mającej na celu weryfikację poprawności działania aplikacji, jej stabilności oraz zgodności z wymaganiami funkcjonalnymi i niefunkcjonalnymi. Proces testowania obejmuje zarówno warstwę mobilną (aplikacja Android), jak i warstwę serwerową (API), zapewniając kompleksową kontrolę jakości.

## 2. Środowisko Testowe
Testy przeprowadzane są w środowisku zbliżonym do produkcyjnego, aby zapewnić wiarygodność wyników.
*   **Urządzenia mobilne:** Emulator Android (Pixel 4 API 30) oraz fizyczne urządzenia testowe z systemem Android 10+.
*   **Serwer aplikacji:** Lokalna instancja serwera uruchomiona na środowisku deweloperskim (localhost:8080).
*   **Baza danych:** Testowa instancja bazy danych (np. H2 lub PostgreSQL) z przygotowanym zestawem danych testowych.

## 3. Narzędzia Testowe
W procesie weryfikacji wykorzystano następujące narzędzia:
*   **JUnit 4/5:** Do testów jednostkowych logiki biznesowej w aplikacji mobilnej.
*   **Android Instrumentation Tests (Espresso):** Do testów interfejsu użytkownika (UI) i integracji z systemem Android.
*   **Postman / IntelliJ HTTP Client:** Do testowania punktów końcowych REST API, weryfikacji kodów odpowiedzi HTTP oraz struktury danych JSON.
*   **Retrofit:** Biblioteka używana w aplikacji, której interfejsy (np. `ApiService`) stanowią podstawę do definicji przypadków testowych API.

## 4. Strategia Testów

### 4.1. Testy Jednostkowe (Unit Tests)
Skupiają się na weryfikacji pojedynczych komponentów systemu w izolacji.
*   **Zakres:** ViewModele, mapery danych, funkcje pomocnicze (utils).
*   **Przykład:** Weryfikacja poprawności obliczeń stanów magazynowych lub walidacji danych wejściowych formularzy.

### 4.2. Testy Integracyjne API (Postman)
Mają na celu sprawdzenie komunikacji między klientem a serwerem. Wykorzystano plik `test-api.http` jako bazę do stworzenia kolekcji testów w Postmanie.
*   **Zakres:** Logowanie, rejestracja, operacje CRUD na produktach i magazynie.
*   **Metodyka:** Wysyłanie żądań HTTP (GET, POST, PUT, DELETE) i asercja otrzymanych odpowiedzi (status 200 OK, 201 Created, 401 Unauthorized itp.).

### 4.3. Testy Scenariuszowe (End-to-End)
Symulują rzeczywiste zachowanie użytkownika przechodzącego przez kompletny proces biznesowy.

## 5. Scenariusze Użycia i Przypadki Testowe

Poniżej przedstawiono kluczowe scenariusze testowe opracowane na podstawie analizy kodu (`ApiService.kt`) oraz wymagań systemu.

### Scenariusz 1: Zarządzanie Dostępem i Użytkownikami
**Cel:** Weryfikacja bezpieczeństwa i poprawności procesu autoryzacji.

| ID | Krok Testowy | Oczekiwany Rezultat |
|----|--------------|---------------------|
| TC-01 | Rejestracja nowego użytkownika z poprawnymi danymi | Konto utworzone, status 200/201, otrzymanie potwierdzenia. |
| TC-02 | Próba rejestracji na istniejący email | Błąd walidacji, komunikat o istniejącym użytkowniku. |
| TC-03 | Logowanie poprawne | Otrzymanie tokena JWT, dostęp do zasobów chronionych. |
| TC-04 | Logowanie błędne hasło | Odmowa dostępu, status 401. |
| TC-05 | Pobranie danych profilu (`/api/auth/me`) | Zwrócenie poprawnych danych zalogowanego użytkownika. |

### Scenariusz 2: Obsługa Magazynu (Inventory Management)
**Cel:** Sprawdzenie poprawności operacji na stanach magazynowych.

| ID | Krok Testowy | Oczekiwany Rezultat |
|----|--------------|---------------------|
| TC-06 | Dodanie nowego produktu do magazynu | Produkt widoczny na liście, stan początkowy zgodny z wprowadzonym. |
| TC-07 | Przyjęcie towaru (`receiveStock`) | Zwiększenie ilości dostępnej, aktualizacja historii operacji. |
| TC-08 | Wydanie towaru (`issueStock`) | Zmniejszenie ilości, blokada przy próbie wydania więcej niż stan. |
| TC-09 | Wyszukiwanie produktu po nazwie | Lista wyników zawiera szukany produkt. |
| TC-10 | Pobranie alertów magazynowych (`getInventoryAlerts`) | Wyświetlenie produktów z niskim stanem. |

### Scenariusz 3: Obsługa Kodów QR
**Cel:** Weryfikacja integracji z systemem kodów QR.

| ID | Krok Testowy | Oczekiwany Rezultat |
|----|--------------|---------------------|
| TC-11 | Generowanie kodu QR dla produktu | Otrzymanie unikalnego ciągu/obrazu kodu QR. |
| TC-12 | Skanowanie istniejącego kodu QR (`scanQRCode`) | Zwrócenie szczegółów produktu przypisanego do kodu. |
| TC-13 | Skanowanie nieznanego kodu | Komunikat o błędzie "Kod nieznany". |

### Scenariusz 4: Zarządzanie Lokalizacjami i Strefami
**Cel:** Sprawdzenie struktury magazynu.

| ID | Krok Testowy | Oczekiwany Rezultat |
|----|--------------|---------------------|
| TC-14 | Dodanie nowej strefy magazynowej | Strefa dodana do listy aktywnych stref. |
| TC-15 | Przypisanie lokalizacji do strefy | Lokalizacja poprawnie powiązana, widoczna w szczegółach strefy. |

## 6. Automatyzacja Testów API (Postman)
Zaleca się wykorzystanie narzędzia Postman do automatyzacji testów regresji. Na podstawie pliku `test-api.http` można wygenerować kolekcję "QRWare API Collection", która zawiera:
1.  **Folder Auth:** Testy logowania i odświeżania tokenów.
2.  **Folder Inventory:** Testy CRUD dla magazynu.
3.  **Folder Users:** Testy zarządzania użytkownikami (dla Admina).

Skrypty testowe w Postmanie (JavaScript) mogą automatycznie weryfikować:
*   Czas odpowiedzi serwera (< 500ms).
*   Obecność wymaganych pól w JSON.
*   Poprawność typów danych.

## 7. Podsumowanie
Przedstawiony plan testów pokrywa krytyczne ścieżki działania aplikacji QRWare. Połączenie testów jednostkowych, integracyjnych oraz manualnych scenariuszy użycia zapewnia wysoki poziom pewności co do jakości oprogramowania przed wdrożeniem produkcyjnym.
