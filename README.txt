================================================================================
                System optymalizujący procesy magazynowe
                z wykorzystaniem technologii kodów QR
================================================================================
Autor: Jan Tomasik

ZAWARTOŚĆ ARCHIWUM:
-------------------
1. Backend       - Kod źródłowy serwera (Java/Spring Boot).
2. MobileApp     - Kod źródłowy aplikacji mobilnej (Android/Kotlin).
3. DesktopClient - Kod źródłowy klienta desktopowego (Python/PyQt).
4. README.txt    - Instrukcja uruchomienia.

WYMAGANIA SYSTEMOWE:
--------------------
- Java JDK 17 lub nowsza
- Maven 3.8+ (lub użycie wbudowanego w IDE)
- Python 3.10+
- Android Studio (do uruchomienia aplikacji mobilnej)
- Baza danych PostgreSQL (opcjonalnie - domyślnie system używa H2 w pamięci dla profilu dev)

================================================================================
                        INSTRUKCJA URUCHOMIENIA
================================================================================

KROK 1: URUCHOMIENIE SERWERA (BACKEND)
--------------------------------------
System posiada wbudowany mechanizm inicjalizacji danych (DataInitializer).
Przy pierwszym uruchomieniu automatycznie utworzy strukturę bazy oraz
przykładowych użytkowników i produkty.

1. Przejdź do katalogu 'Backend'.
2. Otwórz projekt w IntelliJ IDEA (lub innym IDE).
3. Upewnij się, że w pliku 'src/main/resources/application.yml' ustawiony jest
   profil 'dev' (korzysta z bazy H2) lub skonfiguruj połączenie do własnej
   bazy PostgreSQL w sekcji 'prod'.
4. Uruchom klasę główną: 'com.qrware.WarehouseQrSystemApplication'.
5. Serwer wystartuje na porcie 8080.

   Dostępne konta testowe (Login / Hasło):
   - Administrator: admin   / password
   - Manager:       manager / password
   - Magazynier:    worker  / password


KROK 2: URUCHOMIENIE KLIENTA DESKTOPOWEGO (PYTHON)
--------------------------------------------------
1. Przejdź do katalogu 'DesktopClient'.
2. Zainstaluj wymagane biblioteki:
   pip install -r requirements.txt
3. Uruchom aplikację:
   python main.py
4. W oknie logowania wpisz adres serwera (domyślnie: http://localhost:8080)
   oraz dane logowania (np. manager / password).

KROK 3: URUCHOMIENIE APLIKACJI MOBILNEJ (ANDROID)
-------------------------------------------------
1. Otwórz katalog 'MobileApp' w Android Studio.
2. Poczekaj na synchronizację projektu Gradle.
3. W pliku konfiguracyjnym
   upewnij się, że adres IP serwera jest poprawny.
   UWAGA: Emulator Androida widzi localhost komputera jako 10.0.2.2.
   Jeśli używasz fizycznego telefonu, komputer i telefon muszą być w tej samej
   sieci Wi-Fi, a w aplikacji należy podać adres IP komputera (np. 192.168.x.x).
4. Uruchom aplikację na emulatorze lub urządzeniu fizycznym.

================================================================================
                        DODATKOWE INFORMACJE
================================================================================
- Baza danych jest resetowana przy każdym restarcie w profilu 'dev' (H2 in-memory).
  Aby zachować dane, należy przełączyć się na profil 'prod' i PostgreSQL.
- Wygenerowane kody QR są zapisywane w katalogu 'uploads' w folderze Backend.
