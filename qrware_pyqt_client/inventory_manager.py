import sys
import os
from typing import Any, Dict, List, Optional

from PyQt6.QtCore import Qt
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QLineEdit, QComboBox, QTableWidget, QTableWidgetItem,
    QSpinBox, QDialog, QFormLayout, QMessageBox, QCheckBox, QDateEdit, QMenu, QFileDialog
)
from PyQt6.QtGui import QColor, QBrush
from PyQt6.QtCore import QDate

from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas
from reportlab.lib.units import mm

from config import ConfigManager
from inventory_api import InventoryApi
from products_api import ProductsApi
from locations_api import LocationsApi


class InventoryFormDialog(QDialog):
    def __init__(self, products: List[Dict[str, Any]], locations: List[Dict[str, Any]],
                 item: Optional[Dict[str, Any]] = None, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Pozycja magazynu")
        self.resize(560, 600)
        self.products = products
        self.locations = locations
        self.item = item or {}

        form = QFormLayout(self)

        self.cmb_product = QComboBox();
        self._fill_products()
        self.cmb_location = QComboBox();
        self._fill_locations()

        self.spn_qty = QSpinBox();
        self.spn_qty.setMaximum(10 ** 9);
        self.spn_qty.setValue(int(self.item.get("quantity") or 0))
        self.spn_res = QSpinBox();
        self.spn_res.setMaximum(10 ** 9);
        self.spn_res.setValue(int(self.item.get("reservedQuantity") or 0))
        self.cmb_status = QComboBox();
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

        self.dt_received = QDateEdit();
        self.dt_received.setCalendarPopup(True)
        self.dt_expiry = QDateEdit();
        self.dt_expiry.setCalendarPopup(True)
        self.dt_manu = QDateEdit();
        self.dt_manu.setCalendarPopup(True)
        self._set_date(self.dt_received, self.item.get("receivedDate"))
        self._set_date(self.dt_expiry, self.item.get("expiryDate"))
        self._set_date(self.dt_manu, self.item.get("manufactureDate"))

        self.edt_unit_cost = QLineEdit(str(self.item.get("unitCost", "") or ""))
        self.edt_supplier = QLineEdit(self.item.get("supplierReference", ""))
        self.edt_po = QLineEdit(self.item.get("purchaseOrderNumber", ""))
        self.edt_notes = QLineEdit(self.item.get("notes", ""))

        self.spn_temp = QSpinBox();
        self.spn_temp.setRange(-100, 200);
        self.spn_temp.setValue(int(self.item.get("temperature") or 0))
        self.spn_hum = QSpinBox();
        self.spn_hum.setRange(0, 100);
        self.spn_hum.setValue(int(self.item.get("humidity") or 0))
        self.spn_cond = QSpinBox();
        self.spn_cond.setRange(0, 100);
        self.spn_cond.setValue(int(self.item.get("conditionRating") or 10))
        self.chk_quar = QCheckBox();
        self.chk_quar.setChecked(bool(self.item.get("quarantine", False)))
        self.edt_quar_reason = QLineEdit(self.item.get("quarantineReason", ""))
        self.chk_hold = QCheckBox();
        self.chk_hold.setChecked(bool(self.item.get("hold", False)))
        self.edt_hold_reason = QLineEdit(self.item.get("holdReason", ""))

        form.addRow("Produkt:", self.cmb_product)
        form.addRow("Lokalizacja:", self.cmb_location)
        form.addRow("Ilość:", self.spn_qty)
        form.addRow("Zarezerwowane:", self.spn_res)
        form.addRow("Status:", self.cmb_status)
        form.addRow("QR:", self.edt_qr)
        form.addRow("Partia (lot):", self.edt_lot)
        form.addRow("Batch:", self.edt_batch)
        form.addRow("Serial:", self.edt_serial)
        form.addRow("Przyjęto:", self.dt_received)
        form.addRow("Wygasa:", self.dt_expiry)
        form.addRow("Wyprodukowano:", self.dt_manu)
        form.addRow("Koszt jednostkowy:", self.edt_unit_cost)
        form.addRow("Ref. dostawcy:", self.edt_supplier)
        form.addRow("Nr zamówienia:", self.edt_po)
        form.addRow("Notatki:", self.edt_notes)
        form.addRow("Temp [C]:", self.spn_temp)
        form.addRow("Wilgotność [%]:", self.spn_hum)
        form.addRow("Ocena stanu:", self.spn_cond)
        form.addRow("Kwarantanna:", self.chk_quar)
        form.addRow("Powód kwarantanny:", self.edt_quar_reason)
        form.addRow("Wstrzymany:", self.chk_hold)
        form.addRow("Powód wstrzymania:", self.edt_hold_reason)

        btns = QHBoxLayout()
        self.btn_ok = QPushButton("Zapisz")
        self.btn_cancel = QPushButton("Anuluj")
        btns.addWidget(self.btn_ok)
        btns.addWidget(self.btn_cancel)
        form.addRow(btns)

        self.btn_ok.clicked.connect(self.accept)
        self.btn_cancel.clicked.connect(self.reject)

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
        self.setWindowTitle("Alerty magazynowe")
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
        self.resize(1100, 680)
        self.cfg = ConfigManager()
        self.api = InventoryApi(self.cfg)
        self.products_api = ProductsApi(self.cfg)
        self.locations_api = LocationsApi(self.cfg)

        central = QWidget();
        self.setCentralWidget(central)
        root = QVBoxLayout();
        central.setLayout(root)
        root.setContentsMargins(16, 16, 16, 16)
        root.setSpacing(12)

        top = QHBoxLayout()
        self.edt_server = QLineEdit(self.cfg.base_url)
        btn_save_server = QPushButton("Zapisz serwer");
        btn_save_server.clicked.connect(self._save_server)
        self.edt_search = QLineEdit();
        self.edt_search.setPlaceholderText("Szukaj (produkt/QR/serial/lot)…")
        btn_search = QPushButton("Szukaj");
        btn_search.clicked.connect(self._do_search)
        btn_refresh = QPushButton("Odśwież");
        btn_refresh.clicked.connect(self._load_page)
        btn_alerts = QPushButton("Alerty");
        btn_alerts.clicked.connect(self._show_alerts)
        btn_report = QPushButton("Raport PDF");
        btn_report.clicked.connect(self._generate_report)

        top.addWidget(QLabel("Serwer:"));
        top.addWidget(self.edt_server, 2)
        top.addWidget(btn_save_server)
        top.addStretch(1)
        top.addWidget(self.edt_search, 2)
        top.addWidget(btn_search)
        top.addWidget(btn_refresh)
        top.addWidget(btn_alerts)
        top.addWidget(btn_report)
        root.addLayout(top)

        self.tbl = QTableWidget(0, 10)
        self.tbl.setHorizontalHeaderLabels([
            "ID", "Produkt", "Lokalizacja", "Ilość", "Zarezerw.", "Dostępne", "Status", "QR", "Lot", "Serial"
        ])
        self.tbl.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.tbl.horizontalHeader().setStretchLastSection(True)

        # Konfiguracja menu kontekstowego
        self.tbl.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu)
        self.tbl.customContextMenuRequested.connect(self._show_context_menu)

        root.addWidget(self.tbl, 1)

        actions = QHBoxLayout()
        btn_add = QPushButton("Dodaj…");
        btn_add.clicked.connect(self._add)
        btn_edit = QPushButton("Edytuj…");
        btn_edit.clicked.connect(self._edit)
        btn_receive = QPushButton("Przyjęcie…");
        btn_receive.clicked.connect(self._receive)
        btn_issue = QPushButton("Wydanie…");
        btn_issue.clicked.connect(self._issue)
        btn_delete = QPushButton("Usuń");
        btn_delete.clicked.connect(self._delete)
        actions.addWidget(btn_add);
        actions.addWidget(btn_edit);
        actions.addWidget(btn_receive);
        actions.addWidget(btn_issue);
        actions.addWidget(btn_delete)
        root.addLayout(actions)

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
        self._current_items = items
        self._populate(items)

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