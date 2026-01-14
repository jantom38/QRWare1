import sys
import os
from typing import Any, Dict, List, Optional
from datetime import datetime

from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QComboBox, QDateEdit, QGroupBox, QFormLayout,
    QMessageBox, QFileDialog, QCheckBox, QTableWidget, QTableWidgetItem, QHeaderView, QSplitter
)
from PyQt6.QtCore import Qt, QDate

from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont

from config import ConfigManager
from inventory_api import InventoryApi
from movement_history_api import MovementHistoryApi
from categories_api import CategoriesApi
from zones_api import ZonesApi


class ReportsManagerWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("QRWare - Raporty i Analizy")
        self.resize(1100, 750)

        self.cfg = ConfigManager()
        self.inventory_api = InventoryApi(self.cfg)
        self.movement_api = MovementHistoryApi(self.cfg)
        self.categories_api = CategoriesApi(self.cfg)
        self.zones_api = ZonesApi(self.cfg)
        
        self.current_data = []

        central = QWidget()
        self.setCentralWidget(central)
        main_layout = QHBoxLayout(central)
        main_layout.setContentsMargins(20, 20, 20, 20)
        main_layout.setSpacing(20)

        # --- LEWA STRONA (Konfiguracja) ---
        left_layout = QVBoxLayout()
        
        lbl_config = QLabel("Konfiguracja Raportu")
        lbl_config.setStyleSheet("font-size: 18px; font-weight: bold; color: #2c3e50;")
        left_layout.addWidget(lbl_config)

        # Typ raportu
        type_group = QGroupBox("Rodzaj analizy")
        type_layout = QVBoxLayout()
        self.combo_report_type = QComboBox()
        self.combo_report_type.addItems([
            "Pozycje magazynowe (Wszystkie)",
            "Niski stan magazynowy",
            "Przepływy magazynowe",
            "Wartość zapasów",
            "Produkty przeterminowane"
        ])
        self.combo_report_type.currentIndexChanged.connect(self._on_type_changed)
        type_layout.addWidget(self.combo_report_type)
        type_group.setLayout(type_layout)
        left_layout.addWidget(type_group)

        # Parametry
        self.params_group = QGroupBox("Filtry i Zakres")
        params_layout = QFormLayout()
        params_layout.setSpacing(10)

        self.date_start = QDateEdit()
        self.date_start.setCalendarPopup(True)
        self.date_start.setDate(QDate.currentDate().addMonths(-1))

        self.date_end = QDateEdit()
        self.date_end.setCalendarPopup(True)
        self.date_end.setDate(QDate.currentDate())

        self.combo_category = QComboBox()
        self.combo_category.addItem("Wszystkie", None)
        self._load_categories()

        self.combo_zone = QComboBox()
        self.combo_zone.addItem("Wszystkie", None)
        self._load_zones()

        params_layout.addRow("Data od:", self.date_start)
        params_layout.addRow("Data do:", self.date_end)
        params_layout.addRow("Kategoria:", self.combo_category)
        params_layout.addRow("Strefa:", self.combo_zone)

        self.params_group.setLayout(params_layout)
        left_layout.addWidget(self.params_group)

        # Przyciski akcji
        btn_layout = QVBoxLayout()
        btn_layout.setSpacing(10)

        self.btn_preview = QPushButton("Podgląd Danych")
        self.btn_preview.setStyleSheet("background-color: #3498db; color: white; font-weight: bold; padding: 10px;")
        self.btn_preview.clicked.connect(self.load_preview)
        
        self.btn_generate = QPushButton("Eksportuj do PDF")
        self.btn_generate.setStyleSheet("background-color: #2ecc71; color: white; font-weight: bold; padding: 10px;")
        self.btn_generate.clicked.connect(self.generate_report)

        btn_layout.addWidget(self.btn_preview)
        btn_layout.addWidget(self.btn_generate)
        left_layout.addLayout(btn_layout)
        
        left_layout.addStretch()
        main_layout.addLayout(left_layout, 30)

        # --- PRAWA STRONA (Podgląd) ---
        right_layout = QVBoxLayout()
        
        lbl_preview = QLabel("Podgląd Wyników")
        lbl_preview.setStyleSheet("font-size: 18px; font-weight: bold; color: #2c3e50;")
        right_layout.addWidget(lbl_preview)

        self.table_preview = QTableWidget()
        self.table_preview.setAlternatingRowColors(True)
        self.table_preview.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.table_preview.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.table_preview.setStyleSheet("QTableWidget { border: 1px solid #dcdcdc; }")
        right_layout.addWidget(self.table_preview)
        
        self.lbl_summary = QLabel("Liczba rekordów: 0")
        self.lbl_summary.setStyleSheet("font-weight: bold; color: #555;")
        right_layout.addWidget(self.lbl_summary)

        main_layout.addLayout(right_layout, 70)

        self._on_type_changed()

    def _load_categories(self):
        ok, msg, cats = self.categories_api.list(only_active=True)
        if ok:
            for c in cats:
                self.combo_category.addItem(c.get("name", ""), c.get("id"))

    def _load_zones(self):
        ok, msg, zones = self.zones_api.list_active()
        if ok:
            for z in zones:
                self.combo_zone.addItem(z.get("name", ""), z.get("id"))

    def _on_type_changed(self):
        report_type = self.combo_report_type.currentText()
        is_movement = "Przepływy" in report_type
        self.date_start.setEnabled(is_movement)
        self.date_end.setEnabled(is_movement)
        self.combo_category.setEnabled(True)
        self.combo_zone.setEnabled(True)
        
        # Wyczyść podgląd przy zmianie typu
        self.table_preview.setRowCount(0)
        self.table_preview.setColumnCount(0)
        self.lbl_summary.setText("Liczba rekordów: 0")
        self.current_data = []

    def load_preview(self):
        report_type = self.combo_report_type.currentText()
        self.setCursor(Qt.CursorShape.WaitCursor)
        try:
            data = self._fetch_data(report_type)
            self.current_data = data
            self._populate_table(report_type, data)
            
            summary_text = f"Liczba rekordów: {len(data)}"
            if report_type == "Wartość zapasów":
                total_val = self._calculate_total_value(data)
                summary_text += f" | Całkowita wartość: {total_val:.2f} PLN"
                
            self.lbl_summary.setText(summary_text)
        except Exception as e:
            QMessageBox.critical(self, "Błąd", f"Nie udało się pobrać danych: {str(e)}")
        finally:
            self.setCursor(Qt.CursorShape.ArrowCursor)

    def _populate_table(self, report_type, data):
        self.table_preview.setRowCount(0)
        
        if "Przepływy" in report_type:
            headers = ["Data", "Typ", "Produkt", "Ilość", "Użytkownik", "Od", "Do"]
            self.table_preview.setColumnCount(len(headers))
            self.table_preview.setHorizontalHeaderLabels(headers)
            self.table_preview.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
            
            for i, item in enumerate(data):
                self.table_preview.insertRow(i)
                date_str = item.get("movementDate", "")[:16].replace("T", " ")
                mtype = self._translate_movement_type(item.get("movementType", ""))
                
                prod_name = ""
                if "inventoryItem" in item:
                     prod = item["inventoryItem"].get("product", {})
                     prod_name = prod.get("name", "")
                
                self.table_preview.setItem(i, 0, QTableWidgetItem(date_str))
                self.table_preview.setItem(i, 1, QTableWidgetItem(mtype))
                self.table_preview.setItem(i, 2, QTableWidgetItem(prod_name))
                self.table_preview.setItem(i, 3, QTableWidgetItem(str(item.get("quantityChanged", ""))))
                self.table_preview.setItem(i, 4, QTableWidgetItem(item.get("userName", "")))
                
                fl = item.get("fromLocation") or {}
                tl = item.get("toLocation") or {}
                self.table_preview.setItem(i, 5, QTableWidgetItem(fl.get("code") or ""))
                self.table_preview.setItem(i, 6, QTableWidgetItem(tl.get("code") or ""))

        elif "Wartość" in report_type:
            headers = ["Produkt", "SKU", "Ilość", "Cena jedn.", "Wartość"]
            self.table_preview.setColumnCount(len(headers))
            self.table_preview.setHorizontalHeaderLabels(headers)
            self.table_preview.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
            
            for i, item in enumerate(data):
                self.table_preview.insertRow(i)
                prod = item.get("product", {})
                name = prod.get("name", "")
                sku = prod.get("sku", "")
                qty = item.get("quantity", 0)
                cost = self._get_cost(item)
                val = qty * cost
                
                self.table_preview.setItem(i, 0, QTableWidgetItem(name))
                self.table_preview.setItem(i, 1, QTableWidgetItem(sku))
                self.table_preview.setItem(i, 2, QTableWidgetItem(str(qty)))
                self.table_preview.setItem(i, 3, QTableWidgetItem(f"{cost:.2f}"))
                self.table_preview.setItem(i, 4, QTableWidgetItem(f"{val:.2f}"))

        elif "przeterminowane" in report_type:
            headers = ["Produkt", "SKU", "Wiadomość", "Waga"]
            self.table_preview.setColumnCount(len(headers))
            self.table_preview.setHorizontalHeaderLabels(headers)
            self.table_preview.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
            
            for i, item in enumerate(data):
                self.table_preview.insertRow(i)
                self.table_preview.setItem(i, 0, QTableWidgetItem(str(item.get("productName", ""))))
                self.table_preview.setItem(i, 1, QTableWidgetItem(str(item.get("sku", ""))))
                self.table_preview.setItem(i, 2, QTableWidgetItem(str(item.get("message", ""))))
                self.table_preview.setItem(i, 3, QTableWidgetItem(str(item.get("severity", ""))))

        else: # Domyślny (Inventory)
            headers = ["ID", "Produkt", "Lokalizacja", "Ilość", "Status"]
            self.table_preview.setColumnCount(len(headers))
            self.table_preview.setHorizontalHeaderLabels(headers)
            self.table_preview.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
            
            for i, item in enumerate(data):
                self.table_preview.insertRow(i)
                prod = item.get("product") or {}
                loc = item.get("location") or {}
                
                self.table_preview.setItem(i, 0, QTableWidgetItem(str(item.get("id"))))
                self.table_preview.setItem(i, 1, QTableWidgetItem(prod.get("name") or prod.get("sku") or ""))
                self.table_preview.setItem(i, 2, QTableWidgetItem(loc.get("name") or loc.get("code") or ""))
                self.table_preview.setItem(i, 3, QTableWidgetItem(str(item.get("quantity"))))
                self.table_preview.setItem(i, 4, QTableWidgetItem(item.get("status") or ""))

    def generate_report(self):
        report_type = self.combo_report_type.currentText()
        
        # Jeśli dane nie były załadowane, pobierz je teraz
        if not self.current_data:
            try:
                self.current_data = self._fetch_data(report_type)
            except Exception as e:
                QMessageBox.critical(self, "Błąd", f"Błąd pobierania danych: {str(e)}")
                return

        if not self.current_data:
            QMessageBox.information(self, "Info", "Brak danych do wygenerowania raportu.")
            return

        file_path, _ = QFileDialog.getSaveFileName(
            self, "Zapisz raport PDF",
            f"raport_{datetime.now().strftime('%Y%m%d_%H%M')}.pdf",
            "PDF Files (*.pdf)"
        )

        if not file_path:
            return

        try:
            self._create_pdf(file_path, report_type, self.current_data)
            QMessageBox.information(self, "Sukces", f"Raport zapisany w: {file_path}")
        except Exception as e:
            QMessageBox.critical(self, "Błąd", f"Błąd generowania PDF: {str(e)}")

    def _fetch_data(self, report_type: str) -> List[Dict[str, Any]]:
        data = []

        if report_type == "Pozycje magazynowe (Wszystkie)":
            ok, msg, items, _ = self.inventory_api.page(0, 1000)
            if ok: data = items

        elif report_type == "Niski stan magazynowy":
            ok, msg, items = self.inventory_api.get_low_stock()
            if ok: data = items

        elif report_type == "Przepływy magazynowe":
            start = self.date_start.date().toString("yyyy-MM-dd") + "T00:00:00"
            end = self.date_end.date().toString("yyyy-MM-dd") + "T23:59:59"
            ok, msg, items = self.movement_api.by_date_range(start, end)
            if ok: data = items

        elif report_type == "Wartość zapasów":
            ok, msg, items, _ = self.inventory_api.page(0, 1000)
            if ok: data = items

        elif report_type == "Produkty przeterminowane":
            ok, msg, alerts = self.inventory_api.get_alerts()
            if ok:
                data = [a for a in alerts if a.get("type") == "EXPIRED"]

        # Filtrowanie po stronie klienta
        filtered_data = []
        cat_id = self.combo_category.currentData()
        zone_id = self.combo_zone.currentData()

        for item in data:
            match = True
            item_zone_id = None

            if "location" in item and isinstance(item["location"], dict):
                loc = item["location"]
                if "zoneId" in loc:
                    item_zone_id = loc["zoneId"]
                elif "zone" in loc and isinstance(loc["zone"], dict):
                     item_zone_id = loc["zone"].get("id")

            if zone_id is not None:
                if item_zone_id != zone_id:
                    if "location" in item:
                         match = False
                    elif "toLocation" in item or "fromLocation" in item:
                        to_z = self._get_zone_id_from_loc(item.get("toLocation"))
                        from_z = self._get_zone_id_from_loc(item.get("fromLocation"))
                        if to_z != zone_id and from_z != zone_id:
                            match = False

            if match:
                filtered_data.append(item)

        return filtered_data

    def _get_zone_id_from_loc(self, loc):
        if not loc: return None
        if "zoneId" in loc: return loc["zoneId"]
        if "zone" in loc and isinstance(loc["zone"], dict): return loc["zone"].get("id")
        return None
        
    def _get_cost(self, item):
        cost = item.get("unitCost")
        if cost is None and "product" in item:
            cost = item["product"].get("price")
        try:
            return float(cost) if cost is not None else 0.0
        except ValueError:
            return 0.0
            
    def _calculate_total_value(self, data):
        total = 0.0
        for item in data:
            qty = item.get("quantity", 0)
            cost = self._get_cost(item)
            total += (qty * cost)
        return total

    def _create_pdf(self, path, title, data):
        c = canvas.Canvas(path, pagesize=A4)
        width, height = A4

        font_path = "C:\\Windows\\Fonts\\arial.ttf"
        font_name = "Arial"
        try:
            pdfmetrics.registerFont(TTFont(font_name, font_path))
            c.setFont(font_name, 10)
        except:
            font_name = "Helvetica"
            c.setFont("Helvetica", 10)

        c.setFont(font_name, 16)
        c.drawString(20 * mm, height - 20 * mm, f"Raport: {title}")

        c.setFont(font_name, 10)
        c.drawString(20 * mm, height - 30 * mm, f"Data wygenerowania: {datetime.now().strftime('%Y-%m-%d %H:%M')}")

        y = height - 40 * mm
        c.drawString(20 * mm, y, f"Zakres dat: {self.date_start.text()} - {self.date_end.text()}")
        c.drawString(100 * mm, y, f"Kategoria: {self.combo_category.currentText()}")
        c.drawString(150 * mm, y, f"Strefa: {self.combo_zone.currentText()}")

        y -= 15 * mm
        self._draw_table_header(c, y, title, font_name)
        y -= 5 * mm
        c.line(20 * mm, y + 2 * mm, 190 * mm, y + 2 * mm)
        y -= 5 * mm

        total_value = 0.0

        for item in data:
            if y < 20 * mm:
                c.showPage()
                y = height - 20 * mm
                c.setFont(font_name, 10)
                self._draw_table_header(c, y, title, font_name)
                y -= 10 * mm

            self._draw_row(c, y, title, item, font_name)

            if title == "Wartość zapasów":
                qty = item.get("quantity", 0)
                cost = self._get_cost(item)
                total_value += (qty * cost)

            y -= 5 * mm

        if title == "Wartość zapasów":
            y -= 5 * mm
            c.setFont(font_name, 12)
            c.drawString(120 * mm, y, f"Całkowita wartość: {total_value:.2f} PLN")

        c.save()

    def _draw_table_header(self, c, y, report_type, font_name):
        c.setFont(font_name, 9)
        if "Przepływy" in report_type:
            c.drawString(20 * mm, y, "Data")
            c.drawString(50 * mm, y, "Typ")
            c.drawString(80 * mm, y, "Produkt")
            c.drawString(130 * mm, y, "Ilość")
            c.drawString(150 * mm, y, "Użytkownik")
        elif "Wartość" in report_type:
            c.drawString(20 * mm, y, "Produkt")
            c.drawString(80 * mm, y, "Ilość")
            c.drawString(110 * mm, y, "Cena jedn.")
            c.drawString(140 * mm, y, "Wartość")
        elif "przeterminowane" in report_type:
             c.drawString(20 * mm, y, "Produkt")
             c.drawString(80 * mm, y, "SKU")
             c.drawString(110 * mm, y, "Wiadomość")
        else:
            c.drawString(20 * mm, y, "ID")
            c.drawString(40 * mm, y, "Produkt")
            c.drawString(90 * mm, y, "Lokalizacja")
            c.drawString(130 * mm, y, "Ilość")
            c.drawString(150 * mm, y, "Status")

    def _translate_movement_type(self, mtype: str) -> str:
        translations = {
            "RECEIPT": "Przyjęcie",
            "ISSUE": "Wydanie",
            "TRANSFER": "Przesunięcie",
            "MOVE": "Przeniesienie",
            "ADJUSTMENT": "Korekta",
            "CYCLE_COUNT": "Inwentaryzacja cykliczna",
            "PHYSICAL_COUNT": "Inwentaryzacja fizyczna",
            "RESERVE": "Rezerwacja",
            "UNRESERVE": "Anulowanie rezerwacji",
            "PICK": "Kompletacja",
            "PACK": "Pakowanie",
            "SHIP": "Wysyłka",
            "RETURN": "Zwrot",
            "PUTAWAY": "Odłożenie",
            "REPLENISHMENT": "Uzupełnienie",
            "ALLOCATION": "Alokacja",
            "DEALLOCATION": "Dealokacja",
            "QUARANTINE": "Kwarantanna",
            "RELEASE": "Zwolnienie",
            "HOLD": "Wstrzymanie",
            "UNHOLD": "Wznowienie",
            "DAMAGE": "Uszkodzenie",
            "DISPOSAL": "Utylizacja",
            "LOSS": "Strata",
            "FOUND": "Znalezienie",
            "EXPIRY": "Przeterminowanie",
            "RECALL": "Wycofanie",
            "STAGING": "Buforowanie",
            "CROSSDOCK": "Cross-docking",
            "CONSOLIDATION": "Konsolidacja",
            "SPLIT": "Podział",
            "MERGE": "Scalenie",
            "CONVERSION": "Konwersja",
            "PRODUCTION": "Produkcja",
            "CONSUMPTION": "Zużycie",
            "SCRAP": "Złomowanie",
            "REWORK": "Naprawa",
            "SAMPLE": "Próbka",
            "LOAN": "Wypożyczenie",
            "LOAN_RETURN": "Zwrot wypożyczenia",
            "ORDER_RECEIPT": "Przyjęcie zamówienia",
            "ORDER_ISSUE": "Wydanie zamówienia",
            "ORDER_PICK": "Kompletacja zamówienia",
            "ORDER_PACK": "Pakowanie zamówienia",
            "ORDER_CANCEL": "Anulowanie zamówienia",
            "ORDER_RETURN": "Zwrot zamówienia",
            "ORDER_ADJUSTMENT": "Korekta zamówienia"
        }
        return translations.get(mtype, mtype)

    def _draw_row(self, c, y, report_type, item, font_name):
        c.setFont(font_name, 8)
        if "Przepływy" in report_type:
            date_str = item.get("movementDate", "")[:16].replace("T", " ")
            c.drawString(20 * mm, y, date_str)

            mtype = item.get("movementType", "")
            c.drawString(50 * mm, y, self._translate_movement_type(mtype))

            prod_name = ""
            if "inventoryItem" in item:
                 prod = item["inventoryItem"].get("product", {})
                 prod_name = prod.get("name", "")
            c.drawString(80 * mm, y, prod_name[:25])

            c.drawString(130 * mm, y, str(item.get("quantityChanged", "")))
            c.drawString(150 * mm, y, item.get("userName", ""))

        elif "Wartość" in report_type:
            prod = item.get("product", {})
            name = prod.get("name", "") or prod.get("sku", "")
            c.drawString(20 * mm, y, name[:35])

            qty = item.get("quantity", 0)
            cost = self._get_cost(item)
            val = qty * cost

            c.drawString(80 * mm, y, str(qty))
            c.drawString(110 * mm, y, f"{cost:.2f}")
            c.drawString(140 * mm, y, f"{val:.2f}")

        elif "przeterminowane" in report_type:
             c.drawString(20 * mm, y, str(item.get("productName", ""))[:30])
             c.drawString(80 * mm, y, str(item.get("sku", "")))
             c.drawString(110 * mm, y, str(item.get("message", ""))[:50])

        else:
            prod = item.get("product") or {}
            loc = item.get("location") or {}

            prod_name = prod.get("name") or prod.get("sku") or ""
            loc_name = loc.get("name") or loc.get("code") or ""

            c.drawString(20 * mm, y, str(item.get("id")))
            c.drawString(40 * mm, y, prod_name[:25])
            c.drawString(90 * mm, y, loc_name[:20])
            c.drawString(130 * mm, y, str(item.get("quantity")))
            c.drawString(150 * mm, y, item.get("status") or "")


if __name__ == "__main__":
    from theme import apply_modern_style
    app = QApplication(sys.argv)
    apply_modern_style(app, dark=False)
    w = ReportsManagerWindow()
    w.show()
    sys.exit(app.exec())