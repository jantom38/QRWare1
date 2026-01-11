import sys
from typing import Any, Dict, List, Optional

from PyQt6.QtCore import Qt
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QLineEdit, QComboBox, QTableWidget, QTableWidgetItem,
    QSpinBox, QDialog, QFormLayout, QMessageBox, QCheckBox
)

from config import ConfigManager
from zones_api import ZonesApi


class ZoneFormDialog(QDialog):
    def __init__(self, item: Optional[Dict[str, Any]] = None, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Strefa")
        self.resize(560, 520)
        self.item = item or {}

        form = QFormLayout(self)

        self.edt_code = QLineEdit(self.item.get("code", ""))
        self.edt_name = QLineEdit(self.item.get("name", ""))
        self.edt_desc = QLineEdit(self.item.get("description", ""))
        self.cmb_type = QComboBox();
        self._fill_zone_types()
        self.chk_active = QCheckBox(); self.chk_active.setChecked(bool(self.item.get("active", True)))
        self.spn_sec = QSpinBox(); self.spn_sec.setRange(1, 10); self.spn_sec.setValue(int(self.item.get("securityLevel") or 1))
        self.spn_pick = QSpinBox(); self.spn_pick.setRange(1, 10); self.spn_pick.setValue(int(self.item.get("pickingPriority") or 5))
        self.edt_manager = QLineEdit(self.item.get("manager", ""))
        self.edt_contact = QLineEdit(self.item.get("contactInfo", ""))
        self.edt_color = QLineEdit(self.item.get("color", ""))

        form.addRow("Kod:", self.edt_code)
        form.addRow("Nazwa:", self.edt_name)
        form.addRow("Opis:", self.edt_desc)
        form.addRow("Typ:", self.cmb_type)
        form.addRow("Aktywna:", self.chk_active)
        form.addRow("Poziom bezpieczeństwa:", self.spn_sec)
        form.addRow("Priorytet pickingu:", self.spn_pick)
        form.addRow("Manager:", self.edt_manager)
        form.addRow("Kontakt:", self.edt_contact)
        form.addRow("Kolor (#RRGGBB):", self.edt_color)

        btns = QHBoxLayout()
        self.btn_ok = QPushButton("Zapisz")
        self.btn_cancel = QPushButton("Anuluj")
        btns.addWidget(self.btn_ok)
        btns.addWidget(self.btn_cancel)
        form.addRow(btns)

        self.btn_ok.clicked.connect(self.accept)
        self.btn_cancel.clicked.connect(self.reject)

    def _fill_zone_types(self):
        # Lista zgodna z ZoneType.java
        types = [
            "STORAGE","RECEIVING","SHIPPING","PICKING","PACKING","STAGING","CROSSDOCK","QUARANTINE",
            "COLD_STORAGE","FREEZER","HAZMAT","HIGH_SECURITY","BULK","FAST_MOVING","SLOW_MOVING",
            "RETURNS","DAMAGED","MAINTENANCE","OFFICE","PRODUCTION","QUALITY_CONTROL","OVERFLOW",
            "SEASONAL","HIGH_VALUE","AUTOMATED"
        ]
        self.cmb_type.addItems(types)
        if self.item.get("type"):
            idx = self.cmb_type.findText(str(self.item["type"]).strip(), Qt.MatchFlag.MatchFixedString)
            if idx >= 0:
                self.cmb_type.setCurrentIndex(idx)

    def build_create_payload(self) -> Dict[str, Any]:
        return {
            "code": self.edt_code.text().strip(),
            "name": self.edt_name.text().strip(),
            "description": self.edt_desc.text().strip(),
            "type": self.cmb_type.currentText() or None,
            "active": bool(self.chk_active.isChecked()),
            "securityLevel": int(self.spn_sec.value()),
            "pickingPriority": int(self.spn_pick.value()),
            "manager": self.edt_manager.text().strip(),
            "contactInfo": self.edt_contact.text().strip(),
            "color": self.edt_color.text().strip(),
        }

    def build_update_payload(self) -> Dict[str, Any]:
        payload: Dict[str, Any] = {}
        for k, v in {
            "code": self.edt_code.text().strip(),
            "name": self.edt_name.text().strip(),
            "description": self.edt_desc.text().strip(),
            "type": self.cmb_type.currentText() or None,
            "active": bool(self.chk_active.isChecked()),
            "securityLevel": int(self.spn_sec.value()),
            "pickingPriority": int(self.spn_pick.value()),
            "manager": self.edt_manager.text().strip(),
            "contactInfo": self.edt_contact.text().strip(),
            "color": self.edt_color.text().strip(),
        }.items():
            if (isinstance(v, str) and v != "") or (not isinstance(v, str)):
                payload[k] = v
        return payload


class ZonesManagerWindow(QMainWindow):
    def _fill_zone_types(self):
        # Lista zgodna z ZoneType.java
        types = [
            "STORAGE","RECEIVING","SHIPPING","PICKING","PACKING","STAGING","CROSSDOCK","QUARANTINE",
            "COLD_STORAGE","FREEZER","HAZMAT","HIGH_SECURITY","BULK","FAST_MOVING","SLOW_MOVING",
            "RETURNS","DAMAGED","MAINTENANCE","OFFICE","PRODUCTION","QUALITY_CONTROL","OVERFLOW",
            "SEASONAL","HIGH_VALUE","AUTOMATED"
        ]
        self.cmb_type.addItems(types)
        if self.item.get("type"):
            idx = self.cmb_type.findText(str(self.item["type"]).strip(), Qt.MatchFlag.MatchFixedString)
            if idx >= 0:
                self.cmb_type.setCurrentIndex(idx)


    def __init__(self):
        super().__init__()
        self.setWindowTitle("QRWare - Zones")
        self.resize(1000, 620)
        self.cfg = ConfigManager()
        self.api = ZonesApi(self.cfg)

        central = QWidget(); self.setCentralWidget(central)
        root = QVBoxLayout(); central.setLayout(root)
        root.setContentsMargins(16, 16, 16, 16)
        root.setSpacing(12)

        top = QHBoxLayout()
        self.edt_server = QLineEdit(self.cfg.base_url)
        btn_save_server = QPushButton("Zapisz serwer"); btn_save_server.clicked.connect(self._save_server)
        self.edt_search = QLineEdit(); self.edt_search.setPlaceholderText("Szukaj (code/name/desc)…")
        btn_search = QPushButton("Szukaj"); btn_search.clicked.connect(self._do_search)
        btn_refresh = QPushButton("Odśwież"); btn_refresh.clicked.connect(self._load_page)
        top.addWidget(QLabel("Serwer:")); top.addWidget(self.edt_server, 2)
        top.addWidget(btn_save_server)
        top.addStretch(1)
        top.addWidget(self.edt_search, 2)
        top.addWidget(btn_search)
        top.addWidget(btn_refresh)
        root.addLayout(top)

        self.tbl = QTableWidget(0, 8)
        self.tbl.setHorizontalHeaderLabels(["ID","Kod","Nazwa","Typ","Aktywna","Poziom bezp.","Priorytet","Manager"])
        self.tbl.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.tbl.horizontalHeader().setStretchLastSection(True)
        root.addWidget(self.tbl, 1)

        actions = QHBoxLayout()
        btn_add = QPushButton("Dodaj…"); btn_add.clicked.connect(self._add)
        btn_edit = QPushButton("Edytuj…"); btn_edit.clicked.connect(self._edit)
        btn_toggle = QPushButton("Aktywuj/Dezaktywuj"); btn_toggle.clicked.connect(self._toggle)
        btn_delete = QPushButton("Usuń"); btn_delete.clicked.connect(self._delete)
        actions.addWidget(btn_add); actions.addWidget(btn_edit); actions.addWidget(btn_toggle); actions.addWidget(btn_delete)
        root.addLayout(actions)

        self._page=0; self._size=100; self._last_search=None
        self._load_page()

    def _save_server(self):
        self.cfg.base_url = self.edt_server.text().strip()
        QMessageBox.information(self, "Zapisano", "Adres serwera zapisany.")

    def _load_page(self):
        if self._last_search:
            ok, msg, items = self.api.search(self._last_search)
        else:
            ok, msg, items, page_info = self.api.page(self._page, self._size)
        if not ok:
            QMessageBox.warning(self, "Strefy", msg)
            return
        self._populate(items)

    def _populate(self, items: List[Dict[str, Any]]):
        self.tbl.setRowCount(len(items))
        for r, it in enumerate(items):
            def setc(c: int, text: str):
                self.tbl.setItem(r, c, QTableWidgetItem(text))
            setc(0, str(it.get("id", "")))
            setc(1, it.get("code") or "")
            setc(2, it.get("name") or "")
            setc(3, it.get("type") or "")
            setc(4, "TAK" if it.get("active") else "NIE")
            setc(5, str(it.get("securityLevel", "") or ""))
            setc(6, str(it.get("pickingPriority", "") or ""))
            setc(7, it.get("manager") or "")
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
            "code": self.tbl.item(row, 1).text(),
            "name": self.tbl.item(row, 2).text(),
        }

    def _do_search(self):
        q = self.edt_search.text().strip()
        self._last_search = q if q else None
        self._load_page()

    def _add(self):
        dlg = ZoneFormDialog(parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_create_payload()
            if not payload.get("code") or not payload.get("name") or not payload.get("type"):
                QMessageBox.warning(self, "Walidacja", "Wymagane: kod, nazwa, typ.")
                return
            ok, msg, _ = self.api.create(payload)
            if not ok:
                QMessageBox.critical(self, "Utworzenie", msg)
            else:
                self._load_page()

    def _edit(self):
        base = self._current_row_item()
        if not base:
            QMessageBox.information(self, "Edycja", "Wybierz strefę.")
            return
        dlg = ZoneFormDialog(item=base, parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_update_payload()
            ok, msg, _ = self.api.update(base["id"], payload)
            if not ok:
                QMessageBox.critical(self, "Aktualizacja", msg)
            else:
                self._load_page()

    def _toggle(self):
        zid = self._selected_id()
        if zid is None:
            QMessageBox.information(self, "Status", "Wybierz strefę.")
            return
        ok, msg, _ = self.api.toggle_active(zid)
        if not ok:
            QMessageBox.critical(self, "Status", msg)
        else:
            self._load_page()

    def _delete(self):
        zid = self._selected_id()
        if zid is None:
            QMessageBox.information(self, "Usuń", "Wybierz strefę.")
            return
        if QMessageBox.question(self, "Potwierdzenie", "Czy na pewno usunąć (soft) strefę?") != QMessageBox.StandardButton.Yes:
            return
        ok, msg = self.api.delete(zid)
        if not ok:
            QMessageBox.critical(self, "Usuwanie", msg)
        else:
            self._load_page()


if __name__ == "__main__":
    from theme import apply_modern_style
    app = QApplication(sys.argv)
    apply_modern_style(app, dark=False)
    w = ZonesManagerWindow()
    w.show()
    sys.exit(app.exec())
