import sys
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, QGridLayout,
    QLabel, QPushButton, QMessageBox, QFrame, QScrollArea
)
from PyQt6.QtCore import Qt, QSize
from PyQt6.QtGui import QIcon, QFont, QColor, QPalette

from config import ConfigManager
from product_manager import ProductManagerWindow
from theme import apply_modern_style
from order_manager import OrderManagerWindow


class DashboardButton(QPushButton):
    def __init__(self, title, subtitle, icon_name=None, color="#3498db"):
        super().__init__()
        self.setMinimumSize(220, 140)
        self.setCursor(Qt.CursorShape.PointingHandCursor)
        
        # Przechowujemy dane
        self.title_text = title
        self.subtitle_text = subtitle
        self.base_color = color
        
        # Stylizacja
        self.setStyleSheet(f"""
            QPushButton {{
                background-color: white;
                border: 1px solid #e0e0e0;
                border-radius: 12px;
                text-align: left;
                padding: 15px;
            }}
            QPushButton:hover {{
                background-color: #f8f9fa;
                border: 1px solid {color};
                border-left: 5px solid {color};
            }}
            QPushButton:pressed {{
                background-color: #f0f0f0;
            }}
        """)
        
        # Layout wewnętrzny
        layout = QVBoxLayout(self)
        layout.setContentsMargins(10, 10, 10, 10)
        
        # Tytuł
        lbl_title = QLabel(title)
        lbl_title.setStyleSheet(f"font-size: 18px; font-weight: bold; color: #333; border: none; background: transparent;")
        layout.addWidget(lbl_title)
        
        # Podtytuł
        lbl_subtitle = QLabel(subtitle)
        lbl_subtitle.setStyleSheet("font-size: 13px; color: #777; border: none; background: transparent;")
        lbl_subtitle.setWordWrap(True)
        layout.addWidget(lbl_subtitle)
        
        layout.addStretch()
        
        # Pasek dolny (ozdobny)
        lbl_action = QLabel("Otwórz →")
        lbl_action.setStyleSheet(f"font-size: 12px; font-weight: bold; color: {color}; border: none; background: transparent;")
        lbl_action.setAlignment(Qt.AlignmentFlag.AlignRight)
        layout.addWidget(lbl_action)


class DashboardWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("QRWare - Dashboard")
        self.resize(1200, 800)
        self.cfg = ConfigManager()

        # Główny widget z tłem
        central = QWidget()
        central.setStyleSheet("background-color: #f4f6f9;")
        self.setCentralWidget(central)
        
        main_layout = QVBoxLayout(central)
        main_layout.setContentsMargins(0, 0, 0, 0)
        main_layout.setSpacing(0)

        # --- HEADER ---
        header = QWidget()
        header.setStyleSheet("background-color: white; border-bottom: 1px solid #ddd;")
        header.setFixedHeight(80)
        header_layout = QHBoxLayout(header)
        header_layout.setContentsMargins(30, 0, 30, 0)
        
        title_layout = QVBoxLayout()
        lbl_app_name = QLabel("QRWare")
        lbl_app_name.setStyleSheet("font-size: 24px; font-weight: bold; color: #2c3e50;")
        lbl_desc = QLabel("System Zarządzania Magazynem")
        lbl_desc.setStyleSheet("font-size: 14px; color: #7f8c8d;")
        title_layout.addWidget(lbl_app_name)
        title_layout.addWidget(lbl_desc)
        header_layout.addLayout(title_layout)
        
        header_layout.addStretch()
        
        user_info = QLabel(f"Serwer: {self.cfg.base_url}")
        user_info.setStyleSheet("color: #7f8c8d; margin-right: 15px;")
        header_layout.addWidget(user_info)
        
        btn_logout = QPushButton("Wyloguj")
        btn_logout.setCursor(Qt.CursorShape.PointingHandCursor)
        btn_logout.setStyleSheet("""
            QPushButton {
                background-color: #e74c3c; 
                color: white; 
                border-radius: 6px; 
                padding: 8px 16px; 
                font-weight: bold;
            }
            QPushButton:hover {
                background-color: #c0392b;
            }
        """)
        btn_logout.clicked.connect(self.logout)
        header_layout.addWidget(btn_logout)
        
        main_layout.addWidget(header)

        # --- CONTENT AREA (Scrollable) ---
        scroll = QScrollArea()
        scroll.setWidgetResizable(True)
        scroll.setFrameShape(QFrame.Shape.NoFrame)
        scroll.setStyleSheet("background-color: transparent;")
        
        content_widget = QWidget()
        content_widget.setStyleSheet("background-color: transparent;")
        content_layout = QVBoxLayout(content_widget)
        content_layout.setContentsMargins(40, 40, 40, 40)
        content_layout.setSpacing(30)
        
        # Sekcja 1: Zarządzanie Magazynem
        self.add_section_header(content_layout, "Magazyn i Produkty")
        grid1 = QGridLayout()
        grid1.setSpacing(20)
        
        btn_inv = DashboardButton("Stan Magazynowy", "Przeglądaj stany, przyjmuj i wydawaj towary", color="#2ecc71")
        btn_inv.clicked.connect(self.open_inventory)
        grid1.addWidget(btn_inv, 0, 0)
        
        btn_prod = DashboardButton("Produkty", "Katalog produktów, SKU, ceny i opisy", color="#3498db")
        btn_prod.clicked.connect(self.open_products)
        grid1.addWidget(btn_prod, 0, 1)
        
        btn_cats = DashboardButton("Kategorie", "Zarządzaj drzewem kategorii produktów", color="#9b59b6")
        btn_cats.clicked.connect(self.open_categories)
        grid1.addWidget(btn_cats, 0, 2)
        
        btn_qr = DashboardButton("Kody QR", "Generator i zarządzanie kodami QR", color="#34495e")
        btn_qr.clicked.connect(self.open_qr_manager)
        grid1.addWidget(btn_qr, 0, 3)
        
        content_layout.addLayout(grid1)
        
        # Sekcja 2: Logistyka i Zamówienia
        self.add_section_header(content_layout, "Logistyka i Operacje")
        grid2 = QGridLayout()
        grid2.setSpacing(20)
        
        btn_orders = DashboardButton("Zamówienia", "Zarządzanie zamówieniami klientów i dostawców", color="#e67e22")
        btn_orders.clicked.connect(self.open_orders)
        grid2.addWidget(btn_orders, 0, 0)
        
        btn_locs = DashboardButton("Lokalizacje", "Zarządzanie regałami, półkami i miejscami", color="#1abc9c")
        btn_locs.clicked.connect(self.open_locations)
        grid2.addWidget(btn_locs, 0, 1)
        
        btn_zones = DashboardButton("Strefy", "Definiowanie stref magazynowych", color="#16a085")
        btn_zones.clicked.connect(self.open_zones)
        grid2.addWidget(btn_zones, 0, 2)
        
        btn_hist = DashboardButton("Historia Ruchów", "Pełny log operacji magazynowych", color="#7f8c8d")
        btn_hist.clicked.connect(self.open_movement_history)
        grid2.addWidget(btn_hist, 0, 3)
        
        content_layout.addLayout(grid2)
        
        # Sekcja 3: Administracja
        self.add_section_header(content_layout, "Administracja i Raporty")
        grid3 = QGridLayout()
        grid3.setSpacing(20)
        
        btn_users = DashboardButton("Użytkownicy", "Zarządzanie dostępem i kontami pracowników", color="#e74c3c")
        btn_users.clicked.connect(self.open_user_management)
        grid3.addWidget(btn_users, 0, 0)
        
        btn_reports = DashboardButton("Raporty", "Analizy, statystyki i zestawienia", color="#8e44ad")
        btn_reports.clicked.connect(self.open_reports)
        grid3.addWidget(btn_reports, 0, 1)
        
        # Puste widgety dla wyrównania siatki
        grid3.addWidget(QWidget(), 0, 2)
        grid3.addWidget(QWidget(), 0, 3)
        
        content_layout.addLayout(grid3)
        
        content_layout.addStretch()
        
        scroll.setWidget(content_widget)
        main_layout.addWidget(scroll)

    def add_section_header(self, layout, text):
        lbl = QLabel(text)
        lbl.setStyleSheet("font-size: 20px; font-weight: bold; color: #2c3e50; margin-top: 10px; margin-bottom: 5px;")
        layout.addWidget(lbl)
        
        line = QFrame()
        line.setFrameShape(QFrame.Shape.HLine)
        line.setFrameShadow(QFrame.Shadow.Plain)
        line.setStyleSheet("background-color: #e0e0e0;")
        line.setFixedHeight(1)
        layout.addWidget(line)

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