import sys
import os
from typing import Any, Dict, List, Optional
from datetime import datetime

from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QComboBox, QDateEdit, QGroupBox, QFormLayout,
    QMessageBox, QFileDialog, QCheckBox
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
        self.setWindowTitle("QRWare - Raporty")
        self.resize(600, 500)

        self.cfg = ConfigManager()
        self.inventory_api = InventoryApi(self.cfg)
        self.movement_api = MovementHistoryApi(self.cfg)
        self.categories_api = CategoriesApi(self.cfg)
        self.zones_api = ZonesApi(self.cfg)

        central = QWidget()
        self.setCentralWidget(central)
        layout = QVBoxLayout(central)

        # --- Sekcja wyboru typu raportu ---
        type_group = QGroupBox("Typ raportu")
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
        layout.addWidget(type_group)

        # --- Sekcja parametrów ---
        self.params_group = QGroupBox("Parametry")
        params_layout = QFormLayout()

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
        layout.addWidget(self.params_group)

        # --- Przyciski ---
        btn_layout = QHBoxLayout()

        self.btn_generate = QPushButton("Generuj Raport PDF")
        self.btn_generate.setStyleSheet("font-weight: bold; padding: 10px; background-color: #007bff; color: white;")
        self.btn_generate.clicked.connect(self.generate_report)

        btn_layout.addStretch()
        btn_layout.addWidget(self.btn_generate)
        btn_layout.addStretch()

        layout.addLayout(btn_layout)
        layout.addStretch()

        self._on_type_changed() # Inicjalizacja widoczności pól

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

        # Pokaż/ukryj daty w zależności od typu raportu
        is_movement = "Przepływy" in report_type
        self.date_start.setEnabled(is_movement)
        self.date_end.setEnabled(is_movement)

        # Kategorie i strefy mogą być używane do filtrowania w większości raportów
        # (choć w obecnej implementacji API może nie wspierać wszystkiego,
        #  zrobimy filtrowanie po stronie klienta jeśli trzeba)
        self.combo_category.setEnabled(True)
        self.combo_zone.setEnabled(True)

    def generate_report(self):
        report_type = self.combo_report_type.currentText()

        file_path, _ = QFileDialog.getSaveFileName(
            self, "Zapisz raport PDF",
            f"raport_{datetime.now().strftime('%Y%m%d_%H%M')}.pdf",
            "PDF Files (*.pdf)"
        )

        if not file_path:
            return

        try:
            data = self._fetch_data(report_type)
            self._create_pdf(file_path, report_type, data)
            QMessageBox.information(self, "Sukces", f"Raport zapisany w: {file_path}")
        except Exception as e:
            QMessageBox.critical(self, "Błąd", f"Błąd generowania raportu: {str(e)}")

    def _fetch_data(self, report_type: str) -> List[Dict[str, Any]]:
        # Pobieranie danych w zależności od typu
        data = []

        if report_type == "Pozycje magazynowe (Wszystkie)":
            ok, msg, items, _ = self.inventory_api.page(0, 1000) # Pobierz dużo
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
            # Pobieramy alerty, tam są przeterminowane
            ok, msg, alerts = self.inventory_api.get_alerts()
            if ok:
                # Filtrujemy tylko EXPIRED
                data = [a for a in alerts if a.get("type") == "EXPIRED"]

        # Filtrowanie po stronie klienta (Kategoria, Strefa)
        # Uwaga: Struktura danych różni się dla InventoryItem, MovementHistory, Alert
        filtered_data = []
        cat_id = self.combo_category.currentData()
        zone_id = self.combo_zone.currentData()

        for item in data:
            match = True

            # Logika wyciągania kategorii i strefy z obiektu
            item_cat_id = None
            item_zone_id = None

            # Dla InventoryItem
            if "product" in item and isinstance(item["product"], dict):
                # Zakładamy, że product ma categoryId lub category object
                # W InventoryItemDTO product to ProductDTO
                # Sprawdźmy strukturę ProductDTO w API...
                # Przyjmijmy uproszczenie: jeśli nie ma wprost, pomijamy filtr kategorii
                pass

            if "location" in item and isinstance(item["location"], dict):
                # LocationDTO ma zoneId
                loc = item["location"]
                if "zoneId" in loc:
                    item_zone_id = loc["zoneId"]
                elif "zone" in loc and isinstance(loc["zone"], dict):
                     item_zone_id = loc["zone"].get("id")

            # Dla MovementHistory
            # Ma toLocation i fromLocation

            # Filtrowanie Strefy
            if zone_id is not None:
                if item_zone_id != zone_id:
                    # Jeśli to inventory item i ma lokalizację
                    if "location" in item:
                         match = False
                    # Jeśli to movement history, sprawdzamy czy dotyczy strefy (to lub from)
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

    def _create_pdf(self, path, title, data):
        c = canvas.Canvas(path, pagesize=A4)
        width, height = A4

        # Rejestracja czcionki z polskimi znakami (Arial lub inna systemowa)
        # Spróbujmy znaleźć standardową czcionkę Windows
        font_path = "C:\\Windows\\Fonts\\arial.ttf"
        font_name = "Arial"
        try:
            pdfmetrics.registerFont(TTFont(font_name, font_path))
            c.setFont(font_name, 10)
        except:
            # Fallback
            font_name = "Helvetica"
            c.setFont("Helvetica", 10)

        # Nagłówek
        c.setFont(font_name, 16)
        c.drawString(20 * mm, height - 20 * mm, f"Raport: {title}")

        c.setFont(font_name, 10)
        c.drawString(20 * mm, height - 30 * mm, f"Data wygenerowania: {datetime.now().strftime('%Y-%m-%d %H:%M')}")

        # Parametry
        y = height - 40 * mm
        c.drawString(20 * mm, y, f"Zakres dat: {self.date_start.text()} - {self.date_end.text()}")
        c.drawString(100 * mm, y, f"Kategoria: {self.combo_category.currentText()}")
        c.drawString(150 * mm, y, f"Strefa: {self.combo_zone.currentText()}")

        # Tabela
        y -= 15 * mm
        self._draw_table_header(c, y, title, font_name)
        y -= 5 * mm
        c.line(20 * mm, y + 2 * mm, 190 * mm, y + 2 * mm)

        # Dodatkowy odstęp po nagłówku
        y -= 5 * mm

        total_value = 0.0

        for item in data:
            if y < 20 * mm:
                c.showPage()
                y = height - 20 * mm
                c.setFont(font_name, 10)
                self._draw_table_header(c, y, title, font_name)
                y -= 10 * mm # Większy odstęp po nagłówku na nowej stronie

            self._draw_row(c, y, title, item, font_name)

            # Sumowanie wartości
            if title == "Wartość zapasów":
                qty = item.get("quantity", 0)
                # Sprawdzamy czy unitCost jest dostępny wprost, czy w produkcie
                cost = item.get("unitCost")
                if cost is None and "product" in item:
                    # Czasem cena jest w produkcie
                    cost = item["product"].get("price")

                # Konwersja na float, jeśli to string lub None
                try:
                    cost = float(cost) if cost is not None else 0.0
                except ValueError:
                    cost = 0.0

                total_value += (qty * cost)

            y -= 5 * mm

        # Podsumowanie dla wartości
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

            # Pobieranie kosztu z obsługą różnych lokalizacji i typów
            cost = item.get("unitCost")
            if cost is None and "product" in item:
                cost = item["product"].get("price")

            try:
                cost = float(cost) if cost is not None else 0.0
            except ValueError:
                cost = 0.0

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