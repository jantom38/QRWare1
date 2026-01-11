import sys
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, QGridLayout,
    QLabel, QPushButton, QMessageBox, QFrame
)
from PyQt6.QtCore import Qt

from config import ConfigManager
from product_manager import ProductManagerWindow
from theme import apply_modern_style
from order_manager import OrderManagerWindow


class DashboardWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("QRWare - Dashboard")
        self.resize(1000, 600)
        self.cfg = ConfigManager()

        central = QWidget()
        self.setCentralWidget(central)
        
        main_layout = QVBoxLayout()
        central.setLayout(main_layout)

        header_layout = QHBoxLayout()
        
        title = QLabel("Panel Główny")
        title.setStyleSheet("font-size: 28px; font-weight: bold; color: #333;")
        header_layout.addWidget(title)
        
        header_layout.addStretch()
        
        lbl_server = QLabel(f"Serwer: {self.cfg.base_url}")
        lbl_server.setStyleSheet("color: #666;")
        header_layout.addWidget(lbl_server)
        
        main_layout.addLayout(header_layout)
        
        line = QFrame()
        line.setFrameShape(QFrame.Shape.HLine)
        line.setFrameShadow(QFrame.Shadow.Sunken)
        main_layout.addWidget(line)

        main_layout.addSpacing(20)

        grid_layout = QGridLayout()
        grid_layout.setSpacing(15)

        buttons_config = [
            ("Produkty", self.open_products, 0, 0),
            ("Kategorie", self.open_categories, 0, 1),
            ("Magazyn", self.open_inventory, 0, 2),
            
            ("Lokalizacje", self.open_locations, 1, 0),
            ("Strefy", self.open_zones, 1, 1),
            ("Kody QR", self.open_qr_manager, 1, 2),
            
            ("Zamówienia", self.open_orders, 2, 0),
            ("Historia Ruchów", self.open_movement_history, 2, 1),
            ("Użytkownicy", self.open_user_management, 2, 2),

            ("Raporty", self.open_reports, 3, 0),
        ]

        for text, handler, r, c in buttons_config:
            btn = QPushButton(text)
            btn.setMinimumHeight(60)
            btn.setStyleSheet("font-size: 16px;")
            btn.clicked.connect(handler)
            grid_layout.addWidget(btn, r, c)

        main_layout.addLayout(grid_layout)
        main_layout.addStretch()

        footer_layout = QHBoxLayout()
        
        info = QLabel("Witaj w systemie QRWare.")
        info.setStyleSheet("color: #555; font-style: italic;")
        footer_layout.addWidget(info)
        
        footer_layout.addStretch()
        
        btn_logout = QPushButton("Wyloguj")
        btn_logout.setStyleSheet("background-color: #d9534f; color: white; font-weight: bold;")
        btn_logout.clicked.connect(self.logout)
        footer_layout.addWidget(btn_logout)

        main_layout.addLayout(footer_layout)

    def open_products(self):
        self.pm = ProductManagerWindow()
        self.pm.show()

    def open_inventory(self):
        from inventory_manager import InventoryManagerWindow
        self.im = InventoryManagerWindow()
        self.im.show()

    def open_locations(self):
        from locations_manager import LocationsManagerWindow
        self.lm = LocationsManagerWindow()
        self.lm.show()

    def open_zones(self):
        from zones_manager import ZonesManagerWindow
        self.zm = ZonesManagerWindow()
        self.zm.show()

    def open_qr_manager(self):
        from qr_manager import QRManagerWindow
        self.qm = QRManagerWindow()
        self.qm.show()

    def open_movement_history(self):
        from movement_history_manager import MovementHistoryWindow
        self.mh = MovementHistoryWindow()
        self.mh.show()

    def open_categories(self):
        from categories_manager import CategoriesManagerWindow
        self.cm = CategoriesManagerWindow()
        self.cm.show()

    def open_orders(self):
        self.om = OrderManagerWindow()
        self.om.show()

    def open_user_management(self):
        from user_management_manager import UserManagementWindow
        self.um = UserManagementWindow()
        self.um.show()

    def open_reports(self):
        from reports_manager import ReportsManagerWindow
        self.rm = ReportsManagerWindow()
        self.rm.show()

    def logout(self):
        self.cfg.save_tokens("", "")
        QMessageBox.information(self, "Wylogowano", "Pomyślnie wylogowano.")
        self.close()


if __name__ == "__main__":
    app = QApplication(sys.argv)
    apply_modern_style(app, dark=False)
    w = DashboardWindow()
    w.show()
    sys.exit(app.exec())