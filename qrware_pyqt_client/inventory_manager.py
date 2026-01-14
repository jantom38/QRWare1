import sys
import os
from typing import Any, Dict, List, Optional

from PyQt6.QtCore import Qt, QDate
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QLineEdit, QComboBox, QTableWidget, QTableWidgetItem,
    QSpinBox, QDialog, QFormLayout, QMessageBox, QCheckBox, QDateEdit, QMenu, QFileDialog, QFrame,
    QSplitter, QGridLayout, QScrollArea
)
from PyQt6.QtGui import QColor, QBrush

from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas
from reportlab.lib.units import mm

from config import ConfigManager
from inventory_api import InventoryApi
from products_api import ProductsApi
from locations_api import LocationsApi
from validators import RequiredField, validate_required


class InventoryFormDialog(QDialog):
    def __init__(self, products: List[Dict[str, Any]], locations: List[Dict[str, Any]],
                 item: Optional[Dict[str, Any]] = None, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Pozycja magazynu")
        self.resize(900, 650)  # Zwiększona szerokość dla dwóch kolumn
        self.products = products
        self.locations = locations
        self.item = item or {}

        # Główny layout
        layout = QVBoxLayout(self)
        layout.setContentsMargins(20, 20, 20, 20)
        layout.setSpacing(15)

        lbl_title = QLabel("Edycja Pozycji" if item else "Nowa Pozycja Magazynowa")
        lbl_title.setStyleSheet("font-size: 18px; font-weight: bold; color: #2c3e50; margin-bottom: 10px;")
        layout.addWidget(lbl_title)

        # Grid layout dla formularza (2 kolumny)
        grid = QGridLayout()
        grid.setSpacing(10)
        grid.setColumnStretch(1, 1)
        grid.setColumnStretch(3, 1)

        # --- Inicjalizacja kontrolek ---

        # Lewa kolumna (Podstawowe + Identyfikatory)
        self.cmb_product = QComboBox()
        self._fill_products()
        self.cmb_location = QComboBox()
        self._fill_locations()

        self.spn_qty = QSpinBox()
        self.spn_qty.setMaximum(10 ** 9)
        self.spn_qty.setValue(int(self.item.get("quantity") or 0))
        
        self.spn_res = QSpinBox()
        self.spn_res.setMaximum(10 ** 9)
        self.spn_res.setValue(int(self.item.get("reservedQuantity") or 0))
        
        self.cmb_status = QComboBox()
        self.cmb_status.addItems([
            "AVAILABLE", "RESERVED", "ON_HOLD", "QUARANTINE", "DAMAGED", "EXPIRED", "RECALLED", "IN_TRANSIT", "PICKED",
            "SHIPPED", "RETURNED", "DISPOSED", "LOST", "COUNTED", "ALLOCATED", "BACKORDERED", "RECEIVING", "INSPECTING",
            "STAGING"
        ])
        if self.item.get("status"):
            idx = self.cmb_status.findText(self.item["status"], Qt.MatchFlag.MatchFixedString)
            if idx >= 0:
                self.cmb_status.setCurrentIndex(idx)

        self.edt_qr = QLineEdit(self.item.get("qrCode", ""))
        self.edt_lot = QLineEdit(self.item.get("lotNumber", ""))
        self.edt_batch = QLineEdit(self.item.get("batchNumber", ""))
        self.edt_serial = QLineEdit(self.item.get("serialNumber", ""))

        # Prawa kolumna (Daty, Finanse, Warunki, Flagi)
        self.dt_received = QDateEdit()
        self.dt_received.setCalendarPopup(True)
        self.dt_expiry = QDateEdit()
        self.dt_expiry.setCalendarPopup(True)
        self.dt_manu = QDateEdit()
        self.dt_manu.setCalendarPopup(True)
        self._set_date(self.dt_received, self.item.get("receivedDate"))
        self._set_date(self.dt_expiry, self.item.get("expiryDate"))
        self._set_date(self.dt_manu, self.item.get("manufactureDate"))

        self.edt_unit_cost = QLineEdit(str(self.item.get("unitCost", "") or ""))
        self.edt_supplier = QLineEdit(self.item.get("supplierReference", ""))
        self.edt_manufacturer = QLineEdit(self.item.get("manufacturer", ""))
        self.edt_po = QLineEdit(self.item.get("purchaseOrderNumber", ""))
        self.edt_notes = QLineEdit(self.item.get("notes", ""))

        self.spn_temp = QSpinBox()
        self.spn_temp.setRange(-100, 200)
        self.spn_temp.setValue(int(self.item.get("temperature") or 0))
        self.spn_temp.setSuffix(" °C")
        
        self.spn_hum = QSpinBox()
        self.spn_hum.setRange(0, 100)
        self.spn_hum.setValue(int(self.item.get("humidity") or 0))
        self.spn_hum.setSuffix(" %")
        
        self.spn_cond = QSpinBox()
        self.spn_cond.setRange(0, 100)
        self.spn_cond.setValue(int(self.item.get("conditionRating") or 10))

        # Checkboxy i powody
        self.chk_quar = QCheckBox("Kwarantanna")
        self.chk_quar.setChecked(bool(self.item.get("quarantine", False)))
        self.edt_quar_reason = QLineEdit(self.item.get("quarantineReason", ""))
        self.edt_quar_reason.setPlaceholderText("Powód kwarantanny")
        
        self.chk_hold = QCheckBox("Wstrzymany (Hold)")
        self.chk_hold.setChecked(bool(self.item.get("hold", False)))
        self.edt_hold_reason = QLineEdit(self.item.get("holdReason", ""))
        self.edt_hold_reason.setPlaceholderText("Powód wstrzymania")

        # --- Układanie w siatce ---
        
        # Kolumna 1 (Etykiety) i 2 (Wartości) - Lewa strona
        grid.addWidget(QLabel("Produkt:"), 0, 0)
        grid.addWidget(self.cmb_product, 0, 1)
        
        grid.addWidget(QLabel("Lokalizacja:"), 1, 0)
        grid.addWidget(self.cmb_location, 1, 1)
        
        grid.addWidget(QLabel("Ilość:"), 2, 0)
        grid.addWidget(self.spn_qty, 2, 1)
        
        grid.addWidget(QLabel("Zarezerwowane:"), 3, 0)
        grid.addWidget(self.spn_res, 3, 1)
        
        grid.addWidget(QLabel("Status:"), 4, 0)
        grid.addWidget(self.cmb_status, 4, 1)
        
        grid.addWidget(QLabel("Kod QR:"), 5, 0)
        grid.addWidget(self.edt_qr, 5, 1)
        
        grid.addWidget(QLabel("Partia (Lot):"), 6, 0)
        grid.addWidget(self.edt_lot, 6, 1)
        
        grid.addWidget(QLabel("Batch:"), 7, 0)
        grid.addWidget(self.edt_batch, 7, 1)
        
        grid.addWidget(QLabel("Serial No:"), 8, 0)
        grid.addWidget(self.edt_serial, 8, 1)

        # Kolumna 3 (Etykiety) i 4 (Wartości) - Prawa strona
        grid.addWidget(QLabel("Data przyjęcia:"), 0, 2)
        grid.addWidget(self.dt_received, 0, 3)
        
        grid.addWidget(QLabel("Data ważności:"), 1, 2)
        grid.addWidget(self.dt_expiry, 1, 3)
        
        grid.addWidget(QLabel("Data produkcji:"), 2, 2)
        grid.addWidget(self.dt_manu, 2, 3)
        
        grid.addWidget(QLabel("Koszt jedn.:"), 3, 2)
        grid.addWidget(self.edt_unit_cost, 3, 3)
        
        grid.addWidget(QLabel("Ref. dostawcy:"), 4, 2)
        grid.addWidget(self.edt_supplier, 4, 3)

        grid.addWidget(QLabel("Producent:"), 5, 2)
        grid.addWidget(self.edt_manufacturer, 5, 3)

        grid.addWidget(QLabel("Nr zamówienia (PO):"), 6, 2)
        grid.addWidget(self.edt_po, 6, 3)
        
        grid.addWidget(QLabel("Temperatura:"), 7, 2)
        grid.addWidget(self.spn_temp, 7, 3)
        
        grid.addWidget(QLabel("Wilgotność:"), 8, 2)
        grid.addWidget(self.spn_hum, 8, 3)
        
        grid.addWidget(QLabel("Ocena stanu (0-10):"), 9, 2)
        grid.addWidget(self.spn_cond, 9, 3)

        # Notatki na całą szerokość (pod głównymi polami)
        grid.addWidget(QLabel("Notatki:"), 10, 0)
        grid.addWidget(self.edt_notes, 10, 1, 1, 3)

        # Sekcja flag (Kwarantanna / Hold) - na dole
        flags_layout = QGridLayout()
        flags_layout.addWidget(self.chk_quar, 0, 0)
        flags_layout.addWidget(self.edt_quar_reason, 0, 1)
        flags_layout.addWidget(self.chk_hold, 1, 0)
        flags_layout.addWidget(self.edt_hold_reason, 1, 1)
        
        grid.addLayout(flags_layout, 11, 0, 1, 4)

        layout.addLayout(grid)
        layout.addStretch()

        # Przyciski
        btns = QHBoxLayout()
        btns.addStretch()
        self.btn_cancel = QPushButton("Anuluj")
        self.btn_cancel.setStyleSheet("background-color: #95a5a6; color: white;")
        self.btn_cancel.clicked.connect(self.reject)
        btns.addWidget(self.btn_cancel)

        self.btn_ok = QPushButton("Zapisz")
        self.btn_ok.setStyleSheet("background-color: #2ecc71; color: white; font-weight: bold;")
        self.btn_ok.clicked.connect(self.accept)
        btns.addWidget(self.btn_ok)
        
        layout.addLayout(btns)

    def _fill_products(self):
        self.cmb_product.addItem("-- wybierz --", None)
        for p in self.products:
            self.cmb_product.addItem(p.get("name") or p.get("sku") or f"ID {p.get('id')}", p.get("id"))
        if self.item.get("product"):
            pid = self.item["product"].get("id")
            idx = self.cmb_product.findData(pid)
            if idx >= 0:
                self.cmb_product.setCurrentIndex(idx)

    def _fill_locations(self):
        self.cmb_location.addItem("-- wybierz --", None)
        for l in self.locations:
            display = l.get("name") or l.get("code") or f"ID {l.get('id')}"
            self.cmb_location.addItem(display, l.get("id"))
        if self.item.get("location"):
            lid = self.item["location"].get("id")
            idx = self.cmb_location.findData(lid)
            if idx >= 0:
                self.cmb_location.setCurrentIndex(idx)

    def _set_date(self, ctrl: QDateEdit, iso_date: Optional[str]):
        if not iso_date:
            ctrl.setDate(QDate.currentDate())
            return
        try:
            y, m, d = [int(x) for x in iso_date.split("-")]
            ctrl.setDate(QDate(y, m, d))
        except Exception:
            ctrl.setDate(QDate.currentDate())

    def accept(self):
        def qty_ok() -> tuple[bool, str]:
            if int(self.spn_qty.value()) <= 0:
                return False, "musi być > 0"
            return True, ""

        ok = validate_required(
            self,
            [
                RequiredField("Produkt", self.cmb_product),
                RequiredField("Lokalizacja", self.cmb_location),
                RequiredField("Ilość", self.spn_qty, validator=qty_ok),
                # backend ma NOT NULL na inventory_items.qr_code
                RequiredField("Kod QR", self.edt_qr),
            ],
            title="Brak wymaganych danych pozycji magazynowej",
        )
        if not ok:
            return
        super().accept()

    def build_create_payload(self) -> Dict[str, Any]:
        payload: Dict[str, Any] = {
            "productId": self.cmb_product.currentData(),
            "locationId": self.cmb_location.currentData(),
            "quantity": int(self.spn_qty.value()),
            "reservedQuantity": int(self.spn_res.value()),
            "status": self.cmb_status.currentText(),
            "qrCode": self.edt_qr.text().strip(),
            "lotNumber": self.edt_lot.text().strip(),
            "batchNumber": self.edt_batch.text().strip(),
            "serialNumber": self.edt_serial.text().strip(),
            "receivedDate": self.dt_received.date().toString("yyyy-MM-dd"),
            "expiryDate": self.dt_expiry.date().toString("yyyy-MM-dd"),
            "manufactureDate": self.dt_manu.date().toString("yyyy-MM-dd"),
            "unitCost": float(self.edt_unit_cost.text()) if self.edt_unit_cost.text().strip() else None,
            "supplierReference": self.edt_supplier.text().strip(),
            "manufacturer": self.edt_manufacturer.text().strip(),
            "purchaseOrderNumber": self.edt_po.text().strip(),
            "notes": self.edt_notes.text().strip(),
            "temperature": int(self.spn_temp.value()),
            "humidity": int(self.spn_hum.value()),
            "conditionRating": int(self.spn_cond.value()),
            "quarantine": bool(self.chk_quar.isChecked()),
            "quarantineReason": self.edt_quar_reason.text().strip(),
            "hold": bool(self.chk_hold.isChecked()),
            "holdReason": self.edt_hold_reason.text().strip(),
        }
        return payload

    def build_update_payload(self) -> Dict[str, Any]:
        payload: Dict[str, Any] = {
            "quantity": int(self.spn_qty.value()),
            "reservedQuantity": int(self.spn_res.value()),
            "status": self.cmb_status.currentText(),
            "lotNumber": self.edt_lot.text().strip(),
            "batchNumber": self.edt_batch.text().strip(),
            "serialNumber": self.edt_serial.text().strip(),
            "receivedDate": self.dt_received.date().toString("yyyy-MM-dd"),
            "expiryDate": self.dt_expiry.date().toString("yyyy-MM-dd"),
            "manufactureDate": self.dt_manu.date().toString("yyyy-MM-dd"),
            "unitCost": float(self.edt_unit_cost.text()) if self.edt_unit_cost.text().strip() else None,
            "supplierReference": self.edt_supplier.text().strip(),
            "manufacturer": self.edt_manufacturer.text().strip(),
            "purchaseOrderNumber": self.edt_po.text().strip(),
            "notes": self.edt_notes.text().strip(),
            "temperature": int(self.spn_temp.value()),
            "humidity": int(self.spn_hum.value()),
            "conditionRating": int(self.spn_cond.value()),
            "quarantine": bool(self.chk_quar.isChecked()),
            "quarantineReason": self.edt_quar_reason.text().strip(),
            "hold": bool(self.chk_hold.isChecked()),
            "holdReason": self.edt_hold_reason.text().strip(),
        }
        loc_id = self.cmb_location.currentData()
        if loc_id:
            payload["locationId"] = int(loc_id)
        return payload


class AlertsDialog(QDialog):
    def __init__(self, alerts: List[Dict[str, Any]], parent=None):
        super().__init__(parent)
        self.setWindowTitle("Alarmy magazynowe")
        self.resize(800, 400)
        layout = QVBoxLayout(self)

        self.tbl = QTableWidget(0, 5)
        self.tbl.setHorizontalHeaderLabels(["Typ", "Waga", "Produkt", "SKU", "Wiadomość"])
        self.tbl.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl.horizontalHeader().setStretchLastSection(True)
        layout.addWidget(self.tbl)

        btn_close = QPushButton("Zamknij")
        btn_close.clicked.connect(self.accept)
        layout.addWidget(btn_close)

        self._populate(alerts)

    def _populate(self, alerts: List[Dict[str, Any]]):
        self.tbl.setRowCount(len(alerts))
        for r, alert in enumerate(alerts):
            severity = alert.get("severity", "INFO")
            
            bg_color = None
            if severity == "CRITICAL":
                bg_color = QColor(255, 200, 200)  # Jasny czerwony
            elif severity == "WARNING":
                bg_color = QColor(255, 235, 150)  # Jasny pomarańczowy/żółty

            def setc(c: int, text: str):
                item = QTableWidgetItem(text)
                if bg_color:
                    item.setBackground(QBrush(bg_color))
                self.tbl.setItem(r, c, item)

            setc(0, alert.get("type", ""))
            setc(1, severity)
            setc(2, alert.get("productName", ""))
            setc(3, alert.get("sku", ""))
            setc(4, alert.get("message", ""))
        
        self.tbl.resizeColumnsToContents()


class InventoryManagerWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("QRWare - Inventory")
        self.resize(1200, 750)
        self.cfg = ConfigManager()
        self.api = InventoryApi(self.cfg)
        self.products_api = ProductsApi(self.cfg)
        self.locations_api = LocationsApi(self.cfg)

        central = QWidget();
        self.setCentralWidget(central)
        root = QVBoxLayout();
        central.setLayout(root)
        root.setContentsMargins(20, 20, 20, 20)
        root.setSpacing(15)

        # --- HEADER ---
        header = QHBoxLayout()
        
        title_layout = QVBoxLayout()
        lbl_title = QLabel("Zarządzanie Magazynem")
        lbl_title.setStyleSheet("font-size: 24px; font-weight: bold; color: #2c3e50;")
        lbl_subtitle = QLabel("Monitoruj stany, przyjmuj i wydawaj towary")
        lbl_subtitle.setStyleSheet("font-size: 14px; color: #7f8c8d;")
        title_layout.addWidget(lbl_title)
        title_layout.addWidget(lbl_subtitle)
        header.addLayout(title_layout)
        
        header.addStretch()
        
        # Usunięto panel serwera
        
        root.addLayout(header)

        # --- TOOLBAR ---
        toolbar = QHBoxLayout()
        toolbar.setSpacing(10)
        
        self.edt_search = QLineEdit();
        self.edt_search.setPlaceholderText("Szukaj (produkt/QR/serial/lot)…")
        self.edt_search.setMinimumWidth(250)
        toolbar.addWidget(self.edt_search)
        
        btn_search = QPushButton("Szukaj");
        btn_search.clicked.connect(self._do_search)
        toolbar.addWidget(btn_search)

        # Zmiana: Checkbox
        self.chk_hide_zero = QCheckBox("Ukryj zerowe stany")
        self.chk_hide_zero.setChecked(True)
        self.chk_hide_zero.stateChanged.connect(self._load_page)
        toolbar.addWidget(self.chk_hide_zero)

        toolbar.addStretch()
        
        btn_alerts = QPushButton("Alarmy");
        btn_alerts.setStyleSheet("background-color: #e67e22; color: white;")
        btn_alerts.clicked.connect(self._show_alerts)
        toolbar.addWidget(btn_alerts)
        
        btn_report = QPushButton("Raport PDF");
        btn_report.clicked.connect(self._generate_report)
        toolbar.addWidget(btn_report)
        
        btn_refresh = QPushButton("Odśwież");
        btn_refresh.clicked.connect(self._load_page)
        toolbar.addWidget(btn_refresh)
        
        root.addLayout(toolbar)

        # --- MAIN SPLIT (TABLE + DETAILS) ---
        self.splitter = QSplitter(Qt.Orientation.Horizontal)
        root.addWidget(self.splitter, 1)

        left = QWidget()
        left_layout = QVBoxLayout(left)
        left_layout.setContentsMargins(0, 0, 0, 0)
        left_layout.setSpacing(10)

        # --- TABLE ---
        self.tbl = QTableWidget(0, 10)
        self.tbl.setHorizontalHeaderLabels([
            "ID", "Produkt", "Lokalizacja", "Ilość", "Zarezerw.", "Dostępne", "Status", "QR", "Lot", "Serial"
        ])
        self.tbl.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.tbl.horizontalHeader().setStretchLastSection(True)
        self.tbl.setAlternatingRowColors(True)
        self.tbl.setStyleSheet("QTableWidget { border: 1px solid #dcdcdc; }")

        # Konfiguracja menu kontekstowego
        self.tbl.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu)
        self.tbl.customContextMenuRequested.connect(self._show_context_menu)
        self.tbl.itemSelectionChanged.connect(self._on_row_selected)

        left_layout.addWidget(self.tbl, 1)

        # --- ACTIONS ---
        actions = QHBoxLayout()
        
        btn_add = QPushButton("Dodaj Pozycję");
        btn_add.setStyleSheet("background-color: #2ecc71; color: white; font-weight: bold; padding: 8px 16px;")
        btn_add.clicked.connect(self._add)
        actions.addWidget(btn_add)
        
        btn_edit = QPushButton("Edytuj");
        btn_edit.clicked.connect(self._edit)
        actions.addWidget(btn_edit)
        
        actions.addSpacing(20)
        
        btn_receive = QPushButton("Przyjęcie");
        btn_receive.setStyleSheet("background-color: #3498db; color: white;")
        btn_receive.clicked.connect(self._receive)
        actions.addWidget(btn_receive)
        
        btn_issue = QPushButton("Wydanie");
        btn_issue.setStyleSheet("background-color: #9b59b6; color: white;")
        btn_issue.clicked.connect(self._issue)
        actions.addWidget(btn_issue)
        
        actions.addStretch()
        
        btn_delete = QPushButton("Usuń");
        btn_delete.setStyleSheet("background-color: #e74c3c; color: white;")
        btn_delete.clicked.connect(self._delete)
        actions.addWidget(btn_delete)
        
        left_layout.addLayout(actions)

        # Right: details panel
        right = QWidget()
        right_layout = QVBoxLayout(right)
        right_layout.setContentsMargins(6, 0, 0, 0)
        right_layout.setSpacing(6)

        lbl_details_title = QLabel("Szczegóły pozycji")
        lbl_details_title.setStyleSheet("font-size: 16px; font-weight: bold; color: #2c3e50; margin: 0px;")
        right_layout.addWidget(lbl_details_title)

        sep = QFrame()
        sep.setFrameShape(QFrame.Shape.HLine)
        sep.setStyleSheet("color: #d0d0d0;")
        right_layout.addWidget(sep)

        # Scroll area dla szczegółów, bo może być ich dużo
        scroll = QScrollArea()
        scroll.setWidgetResizable(True)
        scroll.setFrameShape(QFrame.Shape.NoFrame)
        scroll.setStyleSheet("background: transparent;")
        
        self.details_group = QWidget()
        self.details_group.setStyleSheet("background: transparent;")
        scroll.setWidget(self.details_group)
        
        details_outer = QVBoxLayout(self.details_group)
        details_outer.setSpacing(8)
        details_outer.setContentsMargins(0, 0, 0, 0)

        header_widget = QWidget()
        header_widget.setStyleSheet(
            "background-color: #f8f9fa;"
            "border-radius: 6px;"
            "padding: 10px;"
            "border: 1px solid #e1e4e8;"
        )
        header_grid = QGridLayout(header_widget)
        header_grid.setSpacing(6)

        base_font_css = "font-size: 12px; font-family: Segoe UI, Arial; color: #2c3e50;"
        value_css = base_font_css + "padding: 2px 0px; border-bottom: 1px solid #e8eaed;"

        def mk_value(text: str = "-") -> QLabel:
            v = QLabel(text)
            v.setStyleSheet(value_css)
            v.setWordWrap(True)
            v.setTextFormat(Qt.TextFormat.RichText)
            return v

        def fmt_row(key: str, value: str) -> str:
            value = value if value not in [None, ""] else "-"
            return f"<b style='color:#495057'>{key}:</b> {value}"

        self._fmt_row = fmt_row

        self.txt_prod_name = mk_value("-")
        self.txt_prod_name.setStyleSheet("font-size: 16px; font-family: Segoe UI, Arial; font-weight: 700; color: #3498db; border-bottom: 1px solid #e8eaed;")
        header_grid.addWidget(QLabel("Produkt:"), 0, 0)
        header_grid.addWidget(self.txt_prod_name, 0, 1)

        self.txt_status = mk_value("-")
        self.txt_status.setStyleSheet("font-size: 12px; font-family: Segoe UI, Arial; font-weight: 700; border-bottom: 1px solid #e8eaed;")
        header_grid.addWidget(QLabel("Status:"), 0, 2)
        header_grid.addWidget(self.txt_status, 0, 3)

        # Inicjalizacja etykiet szczegółów
        self.txt_id = mk_value()
        self.txt_sku = mk_value()
        self.txt_location = mk_value()
        self.txt_qty = mk_value()
        self.txt_reserved = mk_value()
        self.txt_available = mk_value()
        self.txt_qr = mk_value()
        self.txt_lot = mk_value()
        self.txt_batch = mk_value()
        self.txt_serial = mk_value()
        self.txt_received = mk_value()
        self.txt_expiry = mk_value()
        self.txt_manu = mk_value()
        self.txt_unit_cost = mk_value()
        self.txt_supplier = mk_value()
        self.txt_po = mk_value()
        self.txt_notes = mk_value()
        self.txt_temp_hum = mk_value()
        self.txt_condition = mk_value()
        self.txt_quarantine = mk_value()
        self.txt_hold = mk_value()

        # Układanie w siatce szczegółów
        header_grid.addWidget(self.txt_id, 1, 0, 1, 2)
        header_grid.addWidget(self.txt_sku, 1, 2, 1, 2)

        header_grid.addWidget(self.txt_location, 2, 0, 1, 4)

        header_grid.addWidget(self.txt_qty, 3, 0, 1, 2)
        header_grid.addWidget(self.txt_reserved, 3, 2, 1, 2)
        
        header_grid.addWidget(self.txt_available, 4, 0, 1, 2)
        header_grid.addWidget(self.txt_qr, 4, 2, 1, 2)

        header_grid.addWidget(self.txt_lot, 5, 0, 1, 2)
        header_grid.addWidget(self.txt_batch, 5, 2, 1, 2)

        header_grid.addWidget(self.txt_serial, 6, 0, 1, 2)
        header_grid.addWidget(self.txt_received, 6, 2, 1, 2)
        
        header_grid.addWidget(self.txt_expiry, 7, 0, 1, 2)
        header_grid.addWidget(self.txt_manu, 7, 2, 1, 2)

        header_grid.addWidget(self.txt_unit_cost, 8, 0, 1, 2)
        header_grid.addWidget(self.txt_po, 8, 2, 1, 2)

        header_grid.addWidget(self.txt_supplier, 9, 0, 1, 4)
        
        header_grid.addWidget(self.txt_temp_hum, 10, 0, 1, 2)
        header_grid.addWidget(self.txt_condition, 10, 2, 1, 2)

        header_grid.addWidget(self.txt_quarantine, 11, 0, 1, 4)
        header_grid.addWidget(self.txt_hold, 12, 0, 1, 4)
        
        header_grid.addWidget(self.txt_notes, 13, 0, 1, 4)

        details_outer.addWidget(header_widget)
        
        sep2 = QFrame()
        sep2.setFrameShape(QFrame.Shape.HLine)
        sep2.setStyleSheet("color: #e0e0e0;")
        details_outer.addWidget(sep2)

        right_layout.addWidget(scroll, 1)
        
        self.splitter.addWidget(left)
        self.splitter.addWidget(right)
        self.splitter.setStretchFactor(0, 3)
        self.splitter.setStretchFactor(1, 2)

        self._page = 0
        self._size = 100
        self._last_search: Optional[str] = None
        self._products: List[Dict[str, Any]] = []
        self._locations: List[Dict[str, Any]] = []
        self._current_items: List[Dict[str, Any]] = []

        self._load_refs()
        self._load_page()

    # --- NOWE METODY QR ---
    def _show_context_menu(self, pos):
        if not self.tbl.selectionModel().selectedRows():
            return

        menu = QMenu()
        gen_qr_action = menu.addAction("Generuj kod QR")
        action = menu.exec(self.tbl.mapToGlobal(pos))

        if action == gen_qr_action:
            self._open_qr_generator()

    def _open_qr_generator(self):
        row = self.tbl.currentRow()
        if row < 0:
            return

        # Pobieranie danych z wiersza
        id_item = self.tbl.item(row, 0)
        qr_item = self.tbl.item(row, 7)  # Kolumna QR

        if not id_item:
            return

        try:
            inv_id = int(id_item.text())
            current_qr = qr_item.text() if qr_item else ""

            # Jeśli item nie ma jeszcze kodu QR, proponujemy "INV-{id}" lub coś podobnego
            qr_data = current_qr if current_qr else f"INV-{inv_id}"

            from qr_manager import QRManagerWindow

            self.qr_window = QRManagerWindow()
            # Ustawiamy dane: data=qr_data, type=INVENTORY, entity_type=inventory_item, entity_id=inv_id
            self.qr_window.set_form_data(
                data=qr_data,
                qr_type="INVENTORY",
                entity_type="inventory_item",
                entity_id=inv_id
            )
            self.qr_window.show()

        except Exception as e:
            QMessageBox.critical(self, "Błąd", f"Nie udało się otworzyć generatora QR: {str(e)}")

    # ----------------------

    def _save_server(self):
        self.cfg.base_url = self.edt_server.text().strip()
        QMessageBox.information(self, "Zapisano", "Adres serwera zapisany.")

    def _load_refs(self):
        ok, msg, prods = self.products_api.get_active()
        if not ok:
            QMessageBox.warning(self, "Produkty", msg)
            self._products = []
        else:
            self._products = prods
        ok, msg, locs = self.locations_api.list_active()
        if not ok:
            QMessageBox.warning(self, "Lokalizacje", msg)
            self._locations = []
        else:
            self._locations = locs

    def _load_page(self):
        if self._last_search:
            ok, msg, items = self.api.search(self._last_search)
        else:
            ok, msg, items, page_info = self.api.page(self._page, self._size)
        if not ok:
            QMessageBox.warning(self, "Inventory", msg)
            return

        # Client-side filtering for zero stock
        if self.chk_hide_zero.isChecked():
            items = [i for i in items if (i.get("quantity") or 0) > 0]

        self._current_items = items
        self._populate(items)

    def _on_row_selected(self):
        row = self.tbl.currentRow()
        if row < 0 or row >= len(self._current_items):
            for lbl in [self.txt_id, self.txt_sku, self.txt_prod_name, self.txt_status, self.txt_location,
                        self.txt_qty, self.txt_reserved, self.txt_available, self.txt_qr, self.txt_lot,
                        self.txt_batch, self.txt_serial, self.txt_received, self.txt_expiry, self.txt_manu,
                        self.txt_unit_cost, self.txt_supplier, self.txt_po, self.txt_notes, self.txt_temp_hum,
                        self.txt_condition, self.txt_quarantine, self.txt_hold]:
                lbl.setText("-")
            return

        item = self._current_items[row]
        prod = item.get('product') or {}
        loc = item.get('location') or {}

        self.txt_prod_name.setText(prod.get('name') or prod.get('sku') or "")
        self.txt_status.setText(item.get('status') or "")

        self.txt_id.setText(self._fmt_row("ID", str(item.get('id', ''))))
        self.txt_sku.setText(self._fmt_row("SKU", prod.get('sku') or ""))
        self.txt_location.setText(self._fmt_row("Lokalizacja", loc.get('name') or loc.get('code') or ""))
        
        self.txt_qty.setText(self._fmt_row("Ilość", str(item.get('quantity') or 0)))
        self.txt_reserved.setText(self._fmt_row("Zarezerw.", str(item.get('reservedQuantity') or 0)))
        self.txt_available.setText(self._fmt_row("Dostępne", str(item.get('availableQuantity') or 0)))
        
        self.txt_qr.setText(self._fmt_row("QR", item.get('qrCode') or ""))
        self.txt_lot.setText(self._fmt_row("Lot", item.get('lotNumber') or ""))
        self.txt_batch.setText(self._fmt_row("Batch", item.get('batchNumber') or ""))
        self.txt_serial.setText(self._fmt_row("Serial", item.get('serialNumber') or ""))
        
        self.txt_received.setText(self._fmt_row("Przyjęto", item.get('receivedDate') or ""))
        self.txt_expiry.setText(self._fmt_row("Przydat.", item.get('expiryDate') or ""))
        self.txt_manu.setText(self._fmt_row("Prod.", item.get('manufactureDate') or ""))
        
        cost = item.get('unitCost')
        self.txt_unit_cost.setText(self._fmt_row("Koszt", f"{cost:.2f}" if cost is not None else ""))
        self.txt_supplier.setText(self._fmt_row("Dostawca", item.get('supplierReference') or ""))
        self.txt_po.setText(self._fmt_row("PO", item.get('purchaseOrderNumber') or ""))
        
        temp = item.get('temperature')
        hum = item.get('humidity')
        env_str = f"{temp}°C / {hum}%" if temp is not None else ""
        self.txt_temp_hum.setText(self._fmt_row("Warunki", env_str))
        self.txt_condition.setText(self._fmt_row("Stan", str(item.get('conditionRating') or "")))
        
        quar = item.get('quarantine')
        quar_reason = item.get('quarantineReason') or ""
        self.txt_quarantine.setText(self._fmt_row("Kwarantanna", f"TAK ({quar_reason})" if quar else "NIE"))
        
        hold = item.get('hold')
        hold_reason = item.get('holdReason') or ""
        self.txt_hold.setText(self._fmt_row("Wstrzymany", f"TAK ({hold_reason})" if hold else "NIE"))
        
        self.txt_notes.setText(self._fmt_row("Notatki", item.get('notes') or ""))

    def _populate(self, items: List[Dict[str, Any]]):
        self.tbl.setRowCount(len(items))
        for r, it in enumerate(items):
            def setc(c: int, text: str):
                self.tbl.setItem(r, c, QTableWidgetItem(text))

            prod = it.get("product") or {}
            loc = it.get("location") or {}
            setc(0, str(it.get("id", "")))
            setc(1, prod.get("name") or prod.get("sku") or "")
            setc(2, loc.get("name") or loc.get("code") or "")
            setc(3, str(it.get("quantity", "") or ""))
            setc(4, str(it.get("reservedQuantity", "") or ""))
            setc(5, str(it.get("availableQuantity", "") or ""))
            setc(6, it.get("status") or "")
            setc(7, it.get("qrCode") or "")
            setc(8, it.get("lotNumber") or "")
            setc(9, it.get("serialNumber") or "")
        self.tbl.resizeColumnsToContents()
        self._on_row_selected()

    def _selected_id(self) -> Optional[int]:
        rows = self.tbl.selectionModel().selectedRows()
        if not rows:
            return None
        try:
            return int(self.tbl.item(rows[0].row(), 0).text())
        except Exception:
            return None

    def _current_row_item(self) -> Optional[Dict[str, Any]]:
        rid = self._selected_id()
        if rid is None:
            return None
        row = self.tbl.currentRow()
        # Find item in _current_items by ID to be safe, or just use index if sorted
        # Using index is safer if table matches _current_items
        if row < len(self._current_items):
             item = self._current_items[row]
             if item.get('id') == rid:
                 return item
        
        # Fallback
        return {
            "id": rid,
            "quantity": self.tbl.item(row, 3).text(),
            "reservedQuantity": self.tbl.item(row, 4).text(),
            "status": self.tbl.item(row, 6).text(),
        }

    def _do_search(self):
        q = self.edt_search.text().strip()
        self._last_search = q if q else None
        self._load_page()

    def _add(self):
        dlg = InventoryFormDialog(self._products, self._locations, parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_create_payload()
            if not payload.get("productId") or not payload.get("locationId"):
                QMessageBox.warning(self, "Walidacja", "Wybierz produkt i lokalizację.")
                return
            ok, msg, _ = self.api.create(payload)
            if not ok:
                QMessageBox.critical(self, "Utworzenie", msg)
            else:
                self._load_page()

    def _edit(self):
        base = self._current_row_item()
        if not base:
            QMessageBox.information(self, "Edycja", "Wybierz pozycję.")
            return
        dlg = InventoryFormDialog(self._products, self._locations, item=base, parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_update_payload()
            ok, msg, _ = self.api.update(base["id"], payload)
            if not ok:
                QMessageBox.critical(self, "Aktualizacja", msg)
            else:
                self._load_page()

    def _delete(self):
        iid = self._selected_id()
        if iid is None:
            QMessageBox.information(self, "Usuń", "Wybierz pozycję.")
            return
        if QMessageBox.question(self, "Potwierdzenie",
                                "Czy na pewno usunąć pozycję?") != QMessageBox.StandardButton.Yes:
            return
        ok, msg = self.api.delete(iid)
        if not ok:
            QMessageBox.critical(self, "Usuwanie", msg)
        else:
            self._load_page()

    def _receive(self):
        iid = self._selected_id()
        if iid is None:
            QMessageBox.information(self, "Przyjęcie", "Wybierz pozycję.")
            return
        qty, okp = self._ask_int("Przyjęcie", "Ilość do przyjęcia:")
        if not okp:
            return
        ok, msg, _ = self.api.receive(iid, qty)
        if not ok:
            QMessageBox.critical(self, "Przyjęcie", msg)
        else:
            self._load_page()

    def _issue(self):
        iid = self._selected_id()
        if iid is None:
            QMessageBox.information(self, "Wydanie", "Wybierz pozycję.")
            return
        qty, okp = self._ask_int("Wydanie", "Ilość do wydania:")
        if not okp:
            return
        ok, msg, _ = self.api.issue(iid, qty)
        if not ok:
            QMessageBox.critical(self, "Wydanie", msg)
        else:
            self._load_page()

    def _ask_int(self, title: str, label: str) -> tuple[int, bool]:
        from PyQt6.QtWidgets import QInputDialog
        val, ok = QInputDialog.getInt(self, title, label, 1, 0, 10 ** 9, 1)
        return val, ok

    def _show_alerts(self):
        ok, msg, alerts = self.api.get_alerts()
        if not ok:
            QMessageBox.warning(self, "Błąd", f"Nie udało się pobrać alertów: {msg}")
            return
        
        if not alerts:
            QMessageBox.information(self, "Alerty", "Brak alertów magazynowych.")
            return

        dlg = AlertsDialog(alerts, parent=self)
        dlg.exec()

    def _generate_report(self):
        if not self._current_items:
            QMessageBox.information(self, "Info", "Brak danych do wygenerowania raportu.")
            return

        file_path, _ = QFileDialog.getSaveFileName(self, "Zapisz raport PDF", "inventory_report.pdf", "PDF Files (*.pdf)")

        if not file_path:
            return

        try:
            self._create_pdf_report(file_path, self._current_items)
            QMessageBox.information(self, "Sukces", f"Raport zapisany w: {file_path}")
        except Exception as e:
            QMessageBox.critical(self, "Błąd", f"Błąd podczas generowania raportu: {str(e)}")

    def _create_pdf_report(self, path, items):
        c = canvas.Canvas(path, pagesize=A4)
        width, height = A4

        # Title
        c.setFont("Helvetica-Bold", 16)
        c.drawString(20 * mm, height - 20 * mm, "Raport Stanów Magazynowych")

        c.setFont("Helvetica", 10)
        c.drawString(20 * mm, height - 30 * mm, f"Data wygenerowania: {QDate.currentDate().toString('yyyy-MM-dd')}")

        # Headers
        y = height - 50 * mm
        c.setFont("Helvetica-Bold", 10)
        c.drawString(20 * mm, y, "ID")
        c.drawString(40 * mm, y, "Produkt")
        c.drawString(90 * mm, y, "Lokalizacja")
        c.drawString(130 * mm, y, "Ilość")
        c.drawString(150 * mm, y, "Status")

        y -= 5 * mm
        c.line(20 * mm, y + 2 * mm, 190 * mm, y + 2 * mm)

        c.setFont("Helvetica", 9)

        for item in items:
            if y < 20 * mm:
                c.showPage()
                y = height - 20 * mm
                c.setFont("Helvetica", 9)

            prod = item.get("product") or {}
            loc = item.get("location") or {}

            prod_name = prod.get("name") or prod.get("sku") or ""
            if len(prod_name) > 30:
                prod_name = prod_name[:27] + "..."

            loc_name = loc.get("name") or loc.get("code") or ""

            c.drawString(20 * mm, y, str(item.get("id")))
            c.drawString(40 * mm, y, prod_name)
            c.drawString(90 * mm, y, loc_name)
            c.drawString(130 * mm, y, str(item.get("quantity")))
            c.drawString(150 * mm, y, item.get("status") or "")

            y -= 5 * mm

        c.save()


if __name__ == "__main__":
    from theme import apply_modern_style

    app = QApplication(sys.argv)
    apply_modern_style(app, dark=False)
    w = InventoryManagerWindow()
    w.show()
    sys.exit(app.exec())
