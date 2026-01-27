import sys
from typing import Any, Dict, List, Optional

from PyQt6.QtCore import Qt
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QLineEdit, QComboBox, QTableWidget, QTableWidgetItem,
    QSpinBox, QDialog, QFormLayout, QMessageBox, QCheckBox, QDoubleSpinBox, QGridLayout, QProgressBar, QFrame
)

from config import ConfigManager
from locations_api import LocationsApi
from zones_api import ZonesApi
from validators import RequiredField, validate_required


class LocationFormDialog(QDialog):
    def __init__(self, zones: List[Dict[str, Any]], item: Optional[Dict[str, Any]] = None, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Lokalizacja")
        self.resize(800, 650)
        self.zones = zones
        self.item = item or {}

        main_layout = QVBoxLayout(self)
        main_layout.setContentsMargins(20, 20, 20, 20)
        main_layout.setSpacing(15)

        lbl_title = QLabel("Edycja Lokalizacji" if item else "Nowa Lokalizacja")
        lbl_title.setStyleSheet("font-size: 18px; font-weight: bold; color: #2c3e50; margin-bottom: 10px;")
        main_layout.addWidget(lbl_title)

        grid = QGridLayout()
        grid.setSpacing(10)
        main_layout.addLayout(grid)

        self.edt_code = QLineEdit(self.item.get("code", ""))
        self.edt_name = QLineEdit(self.item.get("name", ""))
        self.edt_desc = QLineEdit(self.item.get("description", ""))
        self.cmb_zone = QComboBox(); self._fill_zones()
        self.cmb_type = QComboBox(); self._fill_location_types()

        self.edt_aisle = QLineEdit(self.item.get("aisle", ""))
        self.edt_rack = QLineEdit(self.item.get("rack", ""))
        self.edt_shelf = QLineEdit(self.item.get("shelf", ""))
        self.edt_bin = QLineEdit(self.item.get("bin", ""))

        self.edt_barcode = QLineEdit(self.item.get("barcode", ""))
        self.edt_qr = QLineEdit(self.item.get("qrCode", ""))

        self.spn_capacity_items = QSpinBox(); self.spn_capacity_items.setMaximum(10**9); self.spn_capacity_items.setValue(int(self.item.get("capacityItems") or 0))
        self.spn_capacity_vol = QDoubleSpinBox(); self.spn_capacity_vol.setMaximum(1e9); self.spn_capacity_vol.setDecimals(3)
        self.spn_capacity_wt = QDoubleSpinBox(); self.spn_capacity_wt.setMaximum(1e9); self.spn_capacity_wt.setDecimals(3)

        self.spn_x = QDoubleSpinBox(); self.spn_x.setMaximum(1e9); self.spn_x.setDecimals(3)
        self.spn_y = QDoubleSpinBox(); self.spn_y.setMaximum(1e9); self.spn_y.setDecimals(3)
        self.spn_z = QDoubleSpinBox(); self.spn_z.setMaximum(1e9); self.spn_z.setDecimals(3)

        if self.item.get("capacityVolume") is not None:
            try: self.spn_capacity_vol.setValue(float(self.item.get("capacityVolume") or 0))
            except Exception: pass
        if self.item.get("capacityWeight") is not None:
            try: self.spn_capacity_wt.setValue(float(self.item.get("capacityWeight") or 0))
            except Exception: pass
        if self.item.get("xCoordinate") is not None:
            try: self.spn_x.setValue(float(self.item.get("xCoordinate") or 0))
            except Exception: pass
        if self.item.get("yCoordinate") is not None:
            try: self.spn_y.setValue(float(self.item.get("yCoordinate") or 0))
            except Exception: pass
        if self.item.get("zCoordinate") is not None:
            try: self.spn_z.setValue(float(self.item.get("zCoordinate") or 0))
            except Exception: pass

        self.chk_temp_ctrl = QCheckBox("Kontrola temperatury"); self.chk_temp_ctrl.setChecked(bool(self.item.get("temperatureControlled", False)))
        self.spn_temp_min = QSpinBox(); self.spn_temp_min.setRange(-1000, 1000); self.spn_temp_min.setValue(int(self.item.get("temperatureMin") or 0))
        self.spn_temp_max = QSpinBox(); self.spn_temp_max.setRange(-1000, 1000); self.spn_temp_max.setValue(int(self.item.get("temperatureMax") or 0))

        self.chk_hum_ctrl = QCheckBox("Kontrola wilgotności"); self.chk_hum_ctrl.setChecked(bool(self.item.get("humidityControlled", False)))
        self.spn_hum_min = QSpinBox(); self.spn_hum_min.setRange(0, 1000); self.spn_hum_min.setValue(int(self.item.get("humidityMin") or 0))
        self.spn_hum_max = QSpinBox(); self.spn_hum_max.setRange(0, 1000); self.spn_hum_max.setValue(int(self.item.get("humidityMax") or 0))

        self.chk_haz = QCheckBox("Materiały niebezpieczne"); self.chk_haz.setChecked(bool(self.item.get("hazardousMaterials", False)))
        self.chk_fragile = QCheckBox("Kruche"); self.chk_fragile.setChecked(bool(self.item.get("fragileItems", False)))
        self.spn_sec_level = QSpinBox(); self.spn_sec_level.setRange(0, 10); self.spn_sec_level.setValue(int(self.item.get("securityLevel") or 1))

        self.chk_active = QCheckBox("Aktywna"); self.chk_active.setChecked(bool(self.item.get("active", True)))
        self.chk_pickable = QCheckBox("Pickable"); self.chk_pickable.setChecked(bool(self.item.get("pickable", True)))
        self.chk_receivable = QCheckBox("Receivable"); self.chk_receivable.setChecked(bool(self.item.get("receivable", True)))

        grid.addWidget(QLabel("Kod:"), 0, 0); grid.addWidget(self.edt_code, 0, 1)
        grid.addWidget(QLabel("Nazwa:"), 1, 0); grid.addWidget(self.edt_name, 1, 1)
        grid.addWidget(QLabel("Opis:"), 2, 0); grid.addWidget(self.edt_desc, 2, 1)
        grid.addWidget(QLabel("Strefa:"), 3, 0); grid.addWidget(self.cmb_zone, 3, 1)
        grid.addWidget(QLabel("Typ:"), 4, 0); grid.addWidget(self.cmb_type, 4, 1)

        grid.addWidget(QLabel("Aisle:"), 5, 0); grid.addWidget(self.edt_aisle, 5, 1)
        grid.addWidget(QLabel("Rack:"), 6, 0); grid.addWidget(self.edt_rack, 6, 1)
        grid.addWidget(QLabel("Shelf:"), 7, 0); grid.addWidget(self.edt_shelf, 7, 1)
        grid.addWidget(QLabel("Bin:"), 8, 0); grid.addWidget(self.edt_bin, 8, 1)

        grid.addWidget(QLabel("Barcode:"), 9, 0); grid.addWidget(self.edt_barcode, 9, 1)
        grid.addWidget(QLabel("QR:"), 10, 0); grid.addWidget(self.edt_qr, 10, 1)

        grid.addWidget(QLabel("Pojemność [szt.]:"), 11, 0); grid.addWidget(self.spn_capacity_items, 11, 1)
        grid.addWidget(QLabel("Poj. obj. [m3]:"), 12, 0); grid.addWidget(self.spn_capacity_vol, 12, 1)
        grid.addWidget(QLabel("Poj. masowa [kg]:"), 13, 0); grid.addWidget(self.spn_capacity_wt, 13, 1)

        grid.addWidget(QLabel("X:"), 0, 2); grid.addWidget(self.spn_x, 0, 3)
        grid.addWidget(QLabel("Y:"), 1, 2); grid.addWidget(self.spn_y, 1, 3)
        grid.addWidget(QLabel("Z:"), 2, 2); grid.addWidget(self.spn_z, 2, 3)

        grid.addWidget(self.chk_temp_ctrl, 3, 2, 1, 2)
        grid.addWidget(QLabel("Temp. min:"), 4, 2); grid.addWidget(self.spn_temp_min, 4, 3)
        grid.addWidget(QLabel("Temp. max:"), 5, 2); grid.addWidget(self.spn_temp_max, 5, 3)

        grid.addWidget(self.chk_hum_ctrl, 6, 2, 1, 2)
        grid.addWidget(QLabel("Wilg. min:"), 7, 2); grid.addWidget(self.spn_hum_min, 7, 3)
        grid.addWidget(QLabel("Wilg. max:"), 8, 2); grid.addWidget(self.spn_hum_max, 8, 3)

        grid.addWidget(self.chk_haz, 9, 2, 1, 2)
        grid.addWidget(self.chk_fragile, 10, 2, 1, 2)

        grid.addWidget(QLabel("Poz. bezp.:"), 11, 2); grid.addWidget(self.spn_sec_level, 11, 3)

        status_layout = QHBoxLayout()
        status_layout.addWidget(self.chk_active)
        status_layout.addWidget(self.chk_pickable)
        status_layout.addWidget(self.chk_receivable)
        grid.addLayout(status_layout, 12, 2, 1, 2)

        main_layout.addStretch()

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
        
        main_layout.addLayout(btns)

    def _fill_location_types(self):
        types = [
            "SHELF","RACK","FLOOR","PALLET","BIN","CAGE","COLD_STORAGE","FREEZER","HAZMAT",
            "RECEIVING","SHIPPING","STAGING","QUARANTINE","DAMAGED","RETURNS","PICKING","PACKING",
            "CROSSDOCK","BULK","OVERFLOW","MAINTENANCE","OFFICE","VIRTUAL"
        ]
        self.cmb_type.addItems(types)
        if self.item.get("type"):
            idx = self.cmb_type.findText(str(self.item["type"]).strip(), Qt.MatchFlag.MatchFixedString)
            if idx >= 0:
                self.cmb_type.setCurrentIndex(idx)

    def _fill_zones(self):
        self.cmb_zone.addItem("-- wybierz --", None)
        for z in self.zones:
            self.cmb_zone.addItem(z.get("name") or z.get("code") or f"ID {z.get('id')}", z.get("id"))
        if self.item.get("zoneId") or (self.item.get("zone") and self.item["zone"].get("id")):
            zid = self.item.get("zoneId") or self.item["zone"].get("id")
            idx = self.cmb_zone.findData(zid)
            if idx >= 0:
                self.cmb_zone.setCurrentIndex(idx)

    def build_create_payload(self) -> Dict[str, Any]:
        payload = {
            "code": self.edt_code.text().strip(),
            "name": self.edt_name.text().strip(),
            "description": self.edt_desc.text().strip(),
            "zoneId": self.cmb_zone.currentData(),
            "type": self.cmb_type.currentText() or None,
            "aisle": self.edt_aisle.text().strip(),
            "rack": self.edt_rack.text().strip(),
            "shelf": self.edt_shelf.text().strip(),
            "bin": self.edt_bin.text().strip(),
            "barcode": self.edt_barcode.text().strip(),
            "qrCode": self.edt_qr.text().strip(),
            "capacityItems": int(self.spn_capacity_items.value()),
            "capacityVolume": float(self.spn_capacity_vol.value()),
            "capacityWeight": float(self.spn_capacity_wt.value()),
            "xCoordinate": float(self.spn_x.value()),
            "yCoordinate": float(self.spn_y.value()),
            "zCoordinate": float(self.spn_z.value()),
            "temperatureControlled": bool(self.chk_temp_ctrl.isChecked()),
            "temperatureMin": int(self.spn_temp_min.value()),
            "temperatureMax": int(self.spn_temp_max.value()),
            "humidityControlled": bool(self.chk_hum_ctrl.isChecked()),
            "humidityMin": int(self.spn_hum_min.value()),
            "humidityMax": int(self.spn_hum_max.value()),
            "hazardousMaterials": bool(self.chk_haz.isChecked()),
            "fragileItems": bool(self.chk_fragile.isChecked()),
            "securityLevel": int(self.spn_sec_level.value()),
            "active": bool(self.chk_active.isChecked()),
            "pickable": bool(self.chk_pickable.isChecked()),
            "receivable": bool(self.chk_receivable.isChecked()),
        }
        return payload

    def accept(self):
        ok = validate_required(
            self,
            [
                RequiredField("Kod", self.edt_code),
                RequiredField("Nazwa", self.edt_name),
                RequiredField("Strefa", self.cmb_zone),
                RequiredField("Typ", self.cmb_type),
            ],
            title="Brak wymaganych danych lokalizacji",
        )
        if not ok:
            return
        super().accept()

    def build_update_payload(self) -> Dict[str, Any]:
        payload: Dict[str, Any] = {}
        for k, v in {
            "name": self.edt_name.text().strip(),
            "description": self.edt_desc.text().strip(),
            "type": self.cmb_type.currentText() or None,
            "aisle": self.edt_aisle.text().strip(),
            "rack": self.edt_rack.text().strip(),
            "shelf": self.edt_shelf.text().strip(),
            "bin": self.edt_bin.text().strip(),
            "barcode": self.edt_barcode.text().strip(),
            "qrCode": self.edt_qr.text().strip(),
            "capacityItems": int(self.spn_capacity_items.value()),
            "capacityVolume": float(self.spn_capacity_vol.value()),
            "capacityWeight": float(self.spn_capacity_wt.value()),
            "xCoordinate": float(self.spn_x.value()),
            "yCoordinate": float(self.spn_y.value()),
            "zCoordinate": float(self.spn_z.value()),
            "temperatureControlled": bool(self.chk_temp_ctrl.isChecked()),
            "temperatureMin": int(self.spn_temp_min.value()),
            "temperatureMax": int(self.spn_temp_max.value()),
            "humidityControlled": bool(self.chk_hum_ctrl.isChecked()),
            "humidityMin": int(self.spn_hum_min.value()),
            "humidityMax": int(self.spn_hum_max.value()),
            "hazardousMaterials": bool(self.chk_haz.isChecked()),
            "fragileItems": bool(self.chk_fragile.isChecked()),
            "securityLevel": int(self.spn_sec_level.value()),
            "active": bool(self.chk_active.isChecked()),
            "pickable": bool(self.chk_pickable.isChecked()),
            "receivable": bool(self.chk_receivable.isChecked()),
        }.items():
            if (isinstance(v, str) and v != "") or (not isinstance(v, str)):
                payload[k] = v
        zid = self.cmb_zone.currentData()
        if zid:
            payload["zoneId"] = int(zid)
        return payload


class LocationsManagerWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("QRWare - Locations")
        self.resize(1200, 750)
        self.cfg = ConfigManager()
        self.api = LocationsApi(self.cfg)
        self.zones_api = ZonesApi(self.cfg)

        central = QWidget(); self.setCentralWidget(central)
        root = QVBoxLayout(); central.setLayout(root)
        root.setContentsMargins(20, 20, 20, 20)
        root.setSpacing(15)

        header = QHBoxLayout()
        
        title_layout = QVBoxLayout()
        lbl_title = QLabel("Zarządzanie Lokalizacjami")
        lbl_title.setStyleSheet("font-size: 24px; font-weight: bold; color: #2c3e50;")
        lbl_subtitle = QLabel("Definiuj regały, półki i miejsca składowania")
        lbl_subtitle.setStyleSheet("font-size: 14px; color: #7f8c8d;")
        title_layout.addWidget(lbl_title)
        title_layout.addWidget(lbl_subtitle)
        header.addLayout(title_layout)
        
        header.addStretch()
        
        root.addLayout(header)

        toolbar = QHBoxLayout()
        toolbar.setSpacing(10)
        
        self.edt_search = QLineEdit(); 
        self.edt_search.setPlaceholderText("Szukaj (kod/nazwa/opis)…")
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

        self.tbl = QTableWidget(0, 12)
        self.tbl.setHorizontalHeaderLabels([
            "ID", "Kod", "Nazwa", "Strefa", "Typ",
            "Poj. (szt)", "Poj. (m3)", "Poj. (kg)",
            "Zajętość",
            "Aktywna", "Pickable", "Receivable"
        ])
        self.tbl.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.tbl.horizontalHeader().setStretchLastSection(True)
        self.tbl.setAlternatingRowColors(True)
        self.tbl.setStyleSheet("QTableWidget { border: 1px solid #dcdcdc; }")
        root.addWidget(self.tbl, 1)

        actions = QHBoxLayout()
        
        btn_add = QPushButton("Dodaj Lokalizację"); 
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
        
        root.addLayout(actions)

        self._page=0; self._size=100; self._last_search=None
        self._zones: List[Dict[str, Any]] = []
        self._load_zones()
        self._load_page()

    def _save_server(self):
        self.cfg.base_url = self.edt_server.text().strip()
        QMessageBox.information(self, "Zapisano", "Adres serwera zapisany.")

    def _load_zones(self):
        ok, msg, zones = self.zones_api.list_active()
        if not ok:
            QMessageBox.warning(self, "Strefy", msg)
            self._zones = []
        else:
            self._zones = zones

    def _load_page(self):
        if self._last_search:
            ok, msg, items = self.api.search(self._last_search)
            active = None
        else:
            active = True if self.chk_only_active.isChecked() else None
            ok, msg, items, page_info = self.api.page(self._page, self._size, active=active)
        if not ok:
            QMessageBox.warning(self, "Lokalizacje", msg)
            return
        self._populate(items)

    def _populate(self, items: List[Dict[str, Any]]):
        self.tbl.setRowCount(len(items))
        for r, it in enumerate(items):
            def setc(c: int, text: str):
                self.tbl.setItem(r, c, QTableWidgetItem(text))
            z = it.get("zone") or {}
            setc(0, str(it.get("id", "")))
            setc(1, it.get("code") or "")
            setc(2, it.get("name") or "")
            setc(3, z.get("name") or z.get("code") or "")
            setc(4, it.get("type") or "")

            cap_items = it.get("capacityItems")
            cap_vol = it.get("capacityVolume")
            cap_wt = it.get("capacityWeight")

            setc(5, str(cap_items) if cap_items is not None else "-")
            setc(6, f"{cap_vol:.3f}" if cap_vol is not None else "-")
            setc(7, f"{cap_wt:.3f}" if cap_wt is not None else "-")

            current_items = it.get("currentItems", 0)

            progress = QProgressBar()
            progress.setRange(0, 100)
            progress.setTextVisible(True)
            progress.setStyleSheet("QProgressBar { border: 1px solid #bbb; border-radius: 4px; text-align: center; }")

            if cap_items and cap_items > 0:
                percent = int((current_items / cap_items) * 100)
                progress.setValue(min(percent, 100))
                progress.setFormat(f"{percent}% ({current_items}/{cap_items})")

                if percent > 90:
                    progress.setStyleSheet("QProgressBar::chunk { background-color: #e74c3c; } QProgressBar { border: 1px solid #bbb; border-radius: 4px; text-align: center; }") # Czerwony
                elif percent > 70:
                    progress.setStyleSheet("QProgressBar::chunk { background-color: #f39c12; } QProgressBar { border: 1px solid #bbb; border-radius: 4px; text-align: center; }") # Pomarańczowy
                else:
                    progress.setStyleSheet("QProgressBar::chunk { background-color: #2ecc71; } QProgressBar { border: 1px solid #bbb; border-radius: 4px; text-align: center; }") # Zielony
            else:
                progress.setValue(0)
                progress.setFormat("Brak limitu")

            self.tbl.setCellWidget(r, 8, progress)

            setc(9, "TAK" if it.get("active") else "NIE")
            setc(10, "TAK" if it.get("pickable") else "NIE")
            setc(11, "TAK" if it.get("receivable") else "NIE")
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
        dlg = LocationFormDialog(self._zones, parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_create_payload()
            if not payload.get("code") or not payload.get("name") or not payload.get("zoneId"):
                QMessageBox.warning(self, "Walidacja", "Wymagane: kod, nazwa, strefa.")
                return
            ok, msg, _ = self.api.create(payload)
            if not ok:
                QMessageBox.critical(self, "Utworzenie", msg)
            else:
                self._load_page()

    def _edit(self):
        base = self._current_row_item()
        if not base:
            QMessageBox.information(self, "Edycja", "Wybierz lokalizację.")
            return
        dlg = LocationFormDialog(self._zones, item=base, parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_update_payload()
            ok, msg, _ = self.api.update(base["id"], payload)
            if not ok:
                QMessageBox.critical(self, "Aktualizacja", msg)
            else:
                self._load_page()

    def _toggle(self):
        lid = self._selected_id()
        if lid is None:
            QMessageBox.information(self, "Status", "Wybierz lokalizację.")
            return
        ok, msg, _ = self.api.toggle_active(lid)
        if not ok:
            QMessageBox.critical(self, "Status", msg)
        else:
            self._load_page()

    def _delete(self):
        lid = self._selected_id()
        if lid is None:
            QMessageBox.information(self, "Usuń", "Wybierz lokalizację.")
            return
        if QMessageBox.question(self, "Potwierdzenie", "Czy na pewno usunąć (soft) lokalizację?") != QMessageBox.StandardButton.Yes:
            return
        ok, msg = self.api.delete(lid)
        if not ok:
            QMessageBox.critical(self, "Usuwanie", msg)
        else:
            self._load_page()


if __name__ == "__main__":
    from theme import apply_modern_style
    app = QApplication(sys.argv)
    apply_modern_style(app, dark=False)
    w = LocationsManagerWindow()
    w.show()
    sys.exit(app.exec())
