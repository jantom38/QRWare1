import sys
from typing import Any, Dict, List, Optional

from PyQt6.QtCore import Qt
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QLineEdit, QComboBox, QTableWidget, QTableWidgetItem,
    QSpinBox, QDialog, QFormLayout, QMessageBox, QCheckBox, QFrame, QSplitter, QGridLayout
)

from config import ConfigManager
from zones_api import ZonesApi
from validators import RequiredField, validate_required


class ZoneFormDialog(QDialog):
    def __init__(self, item: Optional[Dict[str, Any]] = None, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Strefa")
        self.resize(560, 550)
        self.item = item or {}

        layout = QVBoxLayout(self)
        layout.setContentsMargins(20, 20, 20, 20)
        layout.setSpacing(15)

        lbl_title = QLabel("Edycja Strefy" if item else "Nowa Strefa")
        lbl_title.setStyleSheet("font-size: 18px; font-weight: bold; color: #2c3e50; margin-bottom: 10px;")
        layout.addWidget(lbl_title)

        form = QFormLayout()
        form.setSpacing(10)

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

        layout.addLayout(form)
        layout.addStretch()

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

    def _fill_zone_types(self):
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

    def accept(self):
        ok = validate_required(
            self,
            [
                RequiredField("Kod", self.edt_code),
                RequiredField("Nazwa", self.edt_name),
                RequiredField("Typ", self.cmb_type),
            ],
            title="Brak wymaganych danych strefy",
        )
        if not ok:
            return
        super().accept()

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
        self.resize(1100, 700)
        self.cfg = ConfigManager()
        self.api = ZonesApi(self.cfg)

        central = QWidget(); self.setCentralWidget(central)
        root = QVBoxLayout(); central.setLayout(root)
        root.setContentsMargins(20, 20, 20, 20)
        root.setSpacing(15)

        header = QHBoxLayout()
        
        title_layout = QVBoxLayout()
        lbl_title = QLabel("Zarządzanie Strefami")
        lbl_title.setStyleSheet("font-size: 24px; font-weight: bold; color: #2c3e50;")
        lbl_subtitle = QLabel("Definiuj strefy magazynowe i ich parametry")
        lbl_subtitle.setStyleSheet("font-size: 14px; color: #7f8c8d;")
        title_layout.addWidget(lbl_title)
        title_layout.addWidget(lbl_subtitle)
        header.addLayout(title_layout)
        
        header.addStretch()
        
        root.addLayout(header)

        toolbar = QHBoxLayout()
        toolbar.setSpacing(10)
        
        self.edt_search = QLineEdit(); 
        self.edt_search.setPlaceholderText("Szukaj (code/name/desc)…")
        self.edt_search.setMinimumWidth(250)
        toolbar.addWidget(self.edt_search)
        
        btn_search = QPushButton("Szukaj"); 
        btn_search.clicked.connect(self._do_search)
        toolbar.addWidget(btn_search)

        self.chk_only_active = QCheckBox("Tylko aktywne")
        self.chk_only_active.setChecked(True)
        self.chk_only_active.stateChanged.connect(self._load_page)
        toolbar.addWidget(self.chk_only_active)

        toolbar.addStretch()
        
        btn_refresh = QPushButton("Odśwież"); 
        btn_refresh.clicked.connect(self._load_page)
        toolbar.addWidget(btn_refresh)
        
        root.addLayout(toolbar)

        self.splitter = QSplitter(Qt.Orientation.Horizontal)
        root.addWidget(self.splitter, 1)

        left = QWidget()
        left_layout = QVBoxLayout(left)
        left_layout.setContentsMargins(0, 0, 0, 0)
        left_layout.setSpacing(10)

        self.tbl = QTableWidget(0, 8)
        self.tbl.setHorizontalHeaderLabels(["ID","Kod","Nazwa","Typ","Aktywna","Poziom bezp.","Priorytet","Manager"])
        self.tbl.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.tbl.horizontalHeader().setStretchLastSection(True)
        self.tbl.setAlternatingRowColors(True)
        self.tbl.setStyleSheet("QTableWidget { border: 1px solid #dcdcdc; }")
        self.tbl.itemSelectionChanged.connect(self._on_row_selected)
        left_layout.addWidget(self.tbl, 1)

        actions = QHBoxLayout()
        
        btn_add = QPushButton("Dodaj Strefę"); 
        btn_add.setStyleSheet("background-color: #2ecc71; color: white; font-weight: bold; padding: 8px 16px;")
        btn_add.clicked.connect(self._add)
        actions.addWidget(btn_add)
        
        btn_edit = QPushButton("Edytuj"); 
        btn_edit.clicked.connect(self._edit)
        actions.addWidget(btn_edit)
        
        btn_toggle = QPushButton("Aktywuj/Dezaktywuj"); 
        btn_toggle.clicked.connect(self._toggle)
        actions.addWidget(btn_toggle)
        
        actions.addStretch()
        
        btn_delete = QPushButton("Usuń"); 
        btn_delete.setStyleSheet("background-color: #e74c3c; color: white;")
        btn_delete.clicked.connect(self._delete)
        actions.addWidget(btn_delete)
        
        left_layout.addLayout(actions)

        right = QWidget()
        right_layout = QVBoxLayout(right)
        right_layout.setContentsMargins(6, 0, 0, 0)
        right_layout.setSpacing(6)

        lbl_details_title = QLabel("Szczegóły strefy")
        lbl_details_title.setStyleSheet("font-size: 16px; font-weight: bold; color: #2c3e50; margin: 0px;")
        right_layout.addWidget(lbl_details_title)

        sep = QFrame()
        sep.setFrameShape(QFrame.Shape.HLine)
        sep.setStyleSheet("color: #d0d0d0;")
        right_layout.addWidget(sep)

        self.details_group = QFrame()
        self.details_group.setStyleSheet("background: transparent;")
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

        self.txt_name = mk_value("-")
        self.txt_name.setStyleSheet("font-size: 16px; font-family: Segoe UI, Arial; font-weight: 700; color: #3498db; border-bottom: 1px solid #e8eaed;")
        header_grid.addWidget(QLabel("Nazwa:"), 0, 0)
        header_grid.addWidget(self.txt_name, 0, 1)

        self.txt_active = mk_value("-")
        self.txt_active.setStyleSheet("font-size: 12px; font-family: Segoe UI, Arial; font-weight: 700; border-bottom: 1px solid #e8eaed;")
        header_grid.addWidget(QLabel("Aktywna:"), 0, 2)
        header_grid.addWidget(self.txt_active, 0, 3)

        self.txt_id = mk_value()
        self.txt_code = mk_value()
        self.txt_type = mk_value()
        self.txt_sec = mk_value()
        self.txt_pick = mk_value()
        self.txt_manager = mk_value()
        self.txt_contact = mk_value()
        self.txt_color = mk_value()
        self.txt_desc = mk_value()

        header_grid.addWidget(self.txt_id, 1, 0, 1, 2)
        header_grid.addWidget(self.txt_code, 1, 2, 1, 2)

        header_grid.addWidget(self.txt_type, 2, 0, 1, 4)

        header_grid.addWidget(self.txt_sec, 3, 0, 1, 2)
        header_grid.addWidget(self.txt_pick, 3, 2, 1, 2)

        header_grid.addWidget(self.txt_manager, 4, 0, 1, 2)
        header_grid.addWidget(self.txt_contact, 4, 2, 1, 2)

        header_grid.addWidget(self.txt_color, 5, 0, 1, 4)
        header_grid.addWidget(self.txt_desc, 6, 0, 1, 4)

        details_outer.addWidget(header_widget)

        sep2 = QFrame()
        sep2.setFrameShape(QFrame.Shape.HLine)
        sep2.setStyleSheet("color: #e0e0e0;")
        details_outer.addWidget(sep2)

        right_layout.addWidget(self.details_group, 1)
        right_layout.addStretch(0)

        self.splitter.addWidget(left)
        self.splitter.addWidget(right)
        self.splitter.setStretchFactor(0, 3)
        self.splitter.setStretchFactor(1, 2)

        self._page=0; self._size=100; self._last_search=None
        self._current_items = []
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
        self._current_items = items or []
        self._populate(items)

    def _on_row_selected(self):
        row = self.tbl.currentRow()
        if row < 0 or row >= len(self._current_items):
            for lbl in [self.txt_id, self.txt_code, self.txt_name, self.txt_type, self.txt_active, self.txt_sec,
                        self.txt_pick, self.txt_manager, self.txt_contact, self.txt_color, self.txt_desc]:
                lbl.setText("-")
            return

        item = self._current_items[row]

        self.txt_name.setText(item.get('name') or "")
        self.txt_active.setText("TAK" if item.get('active') else "NIE")

        self.txt_id.setText(self._fmt_row("ID", str(item.get('id', ''))))
        self.txt_code.setText(self._fmt_row("Kod", item.get('code') or ""))
        self.txt_type.setText(self._fmt_row("Typ", item.get('type') or ""))
        self.txt_sec.setText(self._fmt_row("Poziom bezp.", str(item.get('securityLevel') or '')))
        self.txt_pick.setText(self._fmt_row("Priorytet", str(item.get('pickingPriority') or '')))
        self.txt_manager.setText(self._fmt_row("Manager", item.get('manager') or ""))
        self.txt_contact.setText(self._fmt_row("Kontakt", item.get('contactInfo') or ""))
        self.txt_color.setText(self._fmt_row("Kolor", item.get('color') or ""))
        self.txt_desc.setText(self._fmt_row("Opis", item.get('description') or ""))

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
        if row < len(self._current_items):
             item = self._current_items[row]
             if item.get('id') == rid:
                 return item
        
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
