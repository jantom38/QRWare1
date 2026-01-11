import sys
import os

# Dodajemy bieżący katalog do ścieżki systemowej, aby importy działały poprawnie
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

try:
    from PyQt6 import QtWidgets, QtCore
    from PyQt6.QtWidgets import (
        QApplication, QMainWindow, QWidget, QVBoxLayout,
        QFormLayout, QLineEdit, QPushButton, QLabel, QMessageBox
    )
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
        self.resize(420, 240)

        self.config = ConfigManager()

        central = QWidget()
        self.setCentralWidget(central)

        layout = QVBoxLayout()
        form = QFormLayout()

        self.edt_server = QLineEdit(self.config.base_url)
        self.edt_server.setPlaceholderText("http://localhost:8080")

        self.edt_username = QLineEdit()
        self.edt_username.setPlaceholderText("nazwa użytkownika lub e-mail")

        self.edt_password = QLineEdit()
        self.edt_password.setEchoMode(QLineEdit.EchoMode.Password)
        self.edt_password.setPlaceholderText("hasło")

        form.addRow("Adres serwera:", self.edt_server)
        form.addRow("Login/E-mail:", self.edt_username)
        form.addRow("Hasło:", self.edt_password)

        layout.addLayout(form)

        self.lbl_status = QLabel()
        self.lbl_status.setStyleSheet("color: gray;")
        layout.addWidget(self.lbl_status)

        self.btn_login = QPushButton("Zaloguj")
        self.btn_login.clicked.connect(self.on_login)
        layout.addWidget(self.btn_login)

        central.setLayout(layout)

        self._set_status("Gotowy")

    def _set_status(self, text: str, error: bool = False):
        self.lbl_status.setText(text)
        self.lbl_status.setStyleSheet("color: red;" if error else "color: gray;")

    def on_login(self):
        base_url = self.edt_server.text().strip()
        username = self.edt_username.text().strip()
        password = self.edt_password.text()

        if not base_url:
            QMessageBox.warning(self, "Błąd", "Podaj adres serwera.")
            return
        if not username or not password:
            QMessageBox.warning(self, "Błąd", "Podaj login/e-mail i hasło.")
            return

        self.btn_login.setEnabled(False)
        self._set_status("Logowanie…")
        QApplication.processEvents()

        self.config.base_url = base_url

        client = QRWareApiClient(base_url)
        ok, message, auth = client.login(username, password)

        self.btn_login.setEnabled(True)
        if not ok:
            self._set_status(message or "Logowanie nieudane", error=True)
            QMessageBox.critical(self, "Logowanie nieudane", message or "Błąd")
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