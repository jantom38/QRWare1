import sys
import os

# Dodajemy bieżący katalog do ścieżki systemowej, aby importy działały poprawnie
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

try:
    from PyQt6 import QtWidgets, QtCore
    from PyQt6.QtWidgets import (
        QApplication, QMainWindow, QWidget, QVBoxLayout,
        QFormLayout, QLineEdit, QPushButton, QLabel, QMessageBox, QFrame
    )
    from PyQt6.QtCore import Qt
except ImportError as e:
    print("===============================================================")
    print(f"BŁĄD KRYTYCZNY: {e}")
    print("Nie udało się zaimportować biblioteki PyQt6.")
    print("")
    print("DIAGNOSTYKA ŚRODOWISKA:")
    print(f"1. Używany interpreter Pythona: {sys.executable}")
    print(f"2. Wersja Pythona: {sys.version}")
    print("3. Ścieżki poszukiwania modułów (sys.path):")
    for p in sys.path:
        print(f"   - {p}")
    print("")
    print("Jeśli jesteś pewien, że zainstalowałeś PyQt6, sprawdź czy powyższa")
    print("ścieżka interpretera (pkt 1) zgadza się z tym, gdzie instalowałeś bibliotekę.")
    print("(Częsty błąd: instalacja w venv, a uruchomienie z globalnego Pythona lub odwrotnie)")
    print("===============================================================")
    sys.exit(1)

# Importy lokalne - używamy importów bezwzględnych, ponieważ sys.path został zmodyfikowany
try:
    from api import QRWareApiClient
    from config import ConfigManager
    from dashboard import DashboardWindow
    from theme import apply_modern_style
except ImportError as e:
    print(f"Błąd importu modułów lokalnych: {e}")
    # Fallback dla uruchamiania jako pakiet (python -m qrware_pyqt_client.main)
    # Ale jeśli to zawiedzie, to już koniec
    try:
        from .api import QRWareApiClient
        from .config import ConfigManager
        from .dashboard import DashboardWindow
        from .theme import apply_modern_style
    except ImportError as e2:
        print(f"Krytyczny błąd importu: {e2}")
        sys.exit(1)


class LoginWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("QRWare - Logowanie")
        self.resize(450, 350)

        self.config = ConfigManager()

        central = QWidget()
        self.setCentralWidget(central)

        layout = QVBoxLayout()
        layout.setContentsMargins(40, 40, 40, 40)
        layout.setSpacing(20)
        
        # Tytuł
        lbl_title = QLabel("QRWare")
        lbl_title.setAlignment(Qt.AlignmentFlag.AlignCenter)
        lbl_title.setStyleSheet("font-size: 32px; font-weight: bold; color: #2c3e50;")
        layout.addWidget(lbl_title)
        
        lbl_subtitle = QLabel("System Zarządzania Magazynem")
        lbl_subtitle.setAlignment(Qt.AlignmentFlag.AlignCenter)
        lbl_subtitle.setStyleSheet("font-size: 14px; color: #7f8c8d; margin-bottom: 20px;")
        layout.addWidget(lbl_subtitle)

        # Formularz
        form_container = QWidget()
        form_layout = QVBoxLayout(form_container)
        form_layout.setContentsMargins(0, 0, 0, 0)
        form_layout.setSpacing(15)

        self.edt_server = QLineEdit(self.config.base_url)
        self.edt_server.setPlaceholderText("Adres serwera (np. http://localhost:8080)")
        
        self.edt_username = QLineEdit()
        self.edt_username.setPlaceholderText("Nazwa użytkownika lub e-mail")

        self.edt_password = QLineEdit()
        self.edt_password.setEchoMode(QLineEdit.EchoMode.Password)
        self.edt_password.setPlaceholderText("Hasło")

        form_layout.addWidget(QLabel("Serwer:"))
        form_layout.addWidget(self.edt_server)
        form_layout.addWidget(QLabel("Login:"))
        form_layout.addWidget(self.edt_username)
        form_layout.addWidget(QLabel("Hasło:"))
        form_layout.addWidget(self.edt_password)
        
        layout.addWidget(form_container)

        self.lbl_status = QLabel("")
        self.lbl_status.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.lbl_status.setStyleSheet("color: gray; font-size: 12px;")
        layout.addWidget(self.lbl_status)

        self.btn_login = QPushButton("Zaloguj się")
        self.btn_login.setCursor(Qt.CursorShape.PointingHandCursor)
        self.btn_login.setStyleSheet("""
            QPushButton {
                background-color: #3498db; 
                color: white; 
                font-weight: bold; 
                padding: 12px; 
                border-radius: 6px;
                font-size: 14px;
            }
            QPushButton:hover {
                background-color: #2980b9;
            }
        """)
        self.btn_login.clicked.connect(self.on_login)
        layout.addWidget(self.btn_login)
        
        layout.addStretch()

        central.setLayout(layout)

        self._set_status("Gotowy do logowania")

    def _set_status(self, text: str, error: bool = False):
        self.lbl_status.setText(text)
        self.lbl_status.setStyleSheet("color: #e74c3c;" if error else "color: #7f8c8d;")

    def on_login(self):
        base_url = self.edt_server.text().strip()
        username = self.edt_username.text().strip()
        password = self.edt_password.text()

        if not base_url:
            self._set_status("Podaj adres serwera", error=True)
            return
        if not username or not password:
            self._set_status("Podaj login i hasło", error=True)
            return

        self.btn_login.setEnabled(False)
        self.btn_login.setText("Logowanie...")
        self._set_status("Nawiązywanie połączenia...")
        QApplication.processEvents()

        self.config.base_url = base_url

        client = QRWareApiClient(base_url)
        ok, message, auth = client.login(username, password)

        self.btn_login.setEnabled(True)
        self.btn_login.setText("Zaloguj się")
        
        if not ok:
            self._set_status(message or "Logowanie nieudane", error=True)
            QMessageBox.critical(self, "Błąd logowania", message or "Nieznany błąd")
            return

        self.config.save_tokens(auth.accessToken, auth.refreshToken)

        self._set_status("Zalogowano pomyślnie")
        self.dashboard = DashboardWindow()
        self.dashboard.show()
        self.close()


def main():
    app = QApplication(sys.argv)
    apply_modern_style(app, dark=False)
    win = LoginWindow()
    win.show()
    sys.exit(app.exec())


if __name__ == "__main__":
    main()