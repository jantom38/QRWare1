import sys
from typing import Any, Dict, List, Optional

from PyQt6 import QtCore
from PyQt6.QtCore import Qt
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLineEdit, QPushButton, QLabel, QComboBox, QTableWidget, QTableWidgetItem,
    QSpinBox, QMessageBox, QDialog, QFormLayout, QCheckBox, QMenu, QFrame, QGridLayout, QSplitter
)

from config import ConfigManager
from products_api import ProductsApi
from categories_api import CategoriesApi
from validators import RequiredField, validate_required


class ProductFormDialog(QDialog):
    def __init__(self, categories: List[Dict[str, Any]], product: Optional[Dict[str, Any]] = None, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Produkt")
        self.resize(800, 600)
        self.categories = categories
        self.product = product or {}

        layout = QVBoxLayout(self)
        layout.setContentsMargins(20, 20, 20, 20)
        layout.setSpacing(15)

        lbl_title = QLabel("Edycja Produktu" if product else "Nowy Produkt")
        lbl_title.setStyleSheet("font-size: 18px; font-weight: bold; color: #2c3e50; margin-bottom: 10px;")
        layout.addWidget(lbl_title)

        grid = QGridLayout()
        grid.setSpacing(10)

        self.edt_sku = QLineEdit(self.product.get("sku", ""))
        self.edt_name = QLineEdit(self.product.get("name", ""))
        self.edt_desc = QLineEdit(self.product.get("description", ""))
        self.edt_price = QLineEdit(str(self.product.get("price", "") or ""))
        self.edt_cost = QLineEdit(str(self.product.get("cost", "") or ""))
        self.edt_unit = QLineEdit(self.product.get("unitOfMeasure", "PIECE"))
        self.edt_weight = QLineEdit(str(self.product.get("weight", "") or ""))
        self.edt_len = QLineEdit(str(self.product.get("dimensionsLength", "") or ""))
        self.edt_wid = QLineEdit(str(self.product.get("dimensionsWidth", "") or ""))
        self.edt_hei = QLineEdit(str(self.product.get("dimensionsHeight", "") or ""))
        self.spn_min = QSpinBox();
        self.spn_min.setMaximum(10 ** 9);
        self.spn_min.setValue(int(self.product.get("minimumStock") or 0))
        self.spn_max = QSpinBox();
        self.spn_max.setMaximum(10 ** 9);
        self.spn_max.setValue(int(self.product.get("maximumStock") or 0))
        self.spn_reorder = QSpinBox();
        self.spn_reorder.setMaximum(10 ** 9);
        self.spn_reorder.setValue(int(self.product.get("reorderPoint") or 0))

        self.chk_active = QCheckBox("Aktywny");
        self.chk_active.setChecked(bool(self.product.get("active", True)))
        self.chk_perishable = QCheckBox("Szybko psujący");
        self.chk_perishable.setChecked(bool(self.product.get("perishable", False)))
        self.chk_hazardous = QCheckBox("Niebezpieczny");
        self.chk_hazardous.setChecked(bool(self.product.get("hazardous", False)))
        self.chk_fragile = QCheckBox("Kruchy");
        self.chk_fragile.setChecked(bool(self.product.get("fragile", False)))

        self.edt_manufacturer = QLineEdit(self.product.get("manufacturer", ""))
        self.edt_supplier = QLineEdit(self.product.get("supplier", ""))
        self.edt_storage = QLineEdit(self.product.get("storageConditions", ""))
        self.edt_barcode = QLineEdit(self.product.get("barcode", ""))

        self.cmb_category = QComboBox()
        self.cmb_category.addItem("-- brak --", None)
        for c in categories:
            self.cmb_category.addItem(c.get("name") or f"ID {c.get('id')}", c.get("id"))
        if self.product.get("category") and self.product["category"]:
            cat_id = self.product["category"].get("id")
            idx = self.cmb_category.findData(cat_id)
            if idx >= 0:
                self.cmb_category.setCurrentIndex(idx)

        grid.addWidget(QLabel("SKU:"), 0, 0); grid.addWidget(self.edt_sku, 0, 1)
        grid.addWidget(QLabel("Nazwa:"), 1, 0); grid.addWidget(self.edt_name, 1, 1)
        grid.addWidget(QLabel("Opis:"), 2, 0); grid.addWidget(self.edt_desc, 2, 1)
        grid.addWidget(QLabel("Kategoria:"), 3, 0); grid.addWidget(self.cmb_category, 3, 1)
        grid.addWidget(QLabel("Cena:"), 4, 0); grid.addWidget(self.edt_price, 4, 1)
        grid.addWidget(QLabel("Koszt:"), 5, 0); grid.addWidget(self.edt_cost, 5, 1)
        grid.addWidget(QLabel("Jednostka:"), 6, 0); grid.addWidget(self.edt_unit, 6, 1)
        grid.addWidget(QLabel("Kod kreskowy:"), 7, 0); grid.addWidget(self.edt_barcode, 7, 1)
        grid.addWidget(QLabel("Producent:"), 8, 0); grid.addWidget(self.edt_manufacturer, 8, 1)
        grid.addWidget(QLabel("Dostawca:"), 9, 0); grid.addWidget(self.edt_supplier, 9, 1)

        grid.addWidget(QLabel("Waga:"), 0, 2); grid.addWidget(self.edt_weight, 0, 3)
        grid.addWidget(QLabel("Długość:"), 1, 2); grid.addWidget(self.edt_len, 1, 3)
        grid.addWidget(QLabel("Szerokość:"), 2, 2); grid.addWidget(self.edt_wid, 2, 3)
        grid.addWidget(QLabel("Wysokość:"), 3, 2); grid.addWidget(self.edt_hei, 3, 3)
        grid.addWidget(QLabel("Min. stan:"), 4, 2); grid.addWidget(self.spn_min, 4, 3)
        grid.addWidget(QLabel("Max. stan:"), 5, 2); grid.addWidget(self.spn_max, 5, 3)
        grid.addWidget(QLabel("Punkt zam.:"), 6, 2); grid.addWidget(self.spn_reorder, 6, 3)
        grid.addWidget(QLabel("Warunki skł.:"), 7, 2); grid.addWidget(self.edt_storage, 7, 3)

        chk_layout = QVBoxLayout()
        chk_layout.addWidget(self.chk_active)
        chk_layout.addWidget(self.chk_perishable)
        chk_layout.addWidget(self.chk_hazardous)
        chk_layout.addWidget(self.chk_fragile)

        grid.addLayout(chk_layout, 8, 2, 2, 2)

        layout.addLayout(grid)
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

    def build_create_payload(self) -> Dict[str, Any]:
        payload: Dict[str, Any] = {
            "sku": self.edt_sku.text().strip(),
            "name": self.edt_name.text().strip(),
            "description": self.edt_desc.text().strip(),
            "price": self._num(self.edt_price.text()),
            "cost": self._num(self.edt_cost.text()),
            "unit": self.edt_unit.text().strip() or "PIECE",
            "weight": self._num(self.edt_weight.text()),
            "length": self._num(self.edt_len.text()),
            "width": self._num(self.edt_wid.text()),
            "height": self._num(self.edt_hei.text()),
            "minimumStock": int(self.spn_min.value()),
            "maximumStock": int(self.spn_max.value()),
            "reorderPoint": int(self.spn_reorder.value()),
            "active": bool(self.chk_active.isChecked()),
            "perishable": bool(self.chk_perishable.isChecked()),
            "hazardous": bool(self.chk_hazardous.isChecked()),
            "fragile": bool(self.chk_fragile.isChecked()),
            "manufacturer": self.edt_manufacturer.text().strip(),
            "supplier": self.edt_supplier.text().strip(),
            "storageConditions": self.edt_storage.text().strip(),
            "barcode": self.edt_barcode.text().strip(),
        }
        cat_id = self.cmb_category.currentData()
        if cat_id is not None:
            payload["categoryId"] = int(cat_id)
        return payload

    def build_update_payload(self) -> Dict[str, Any]:
        payload: Dict[str, Any] = {}
        for k, v in {
            "name": self.edt_name.text().strip(),
            "description": self.edt_desc.text().strip(),
            "price": self._num(self.edt_price.text()),
            "cost": self._num(self.edt_cost.text()),
            "unit": self.edt_unit.text().strip(),
            "weight": self._num(self.edt_weight.text()),
            "length": self._num(self.edt_len.text()),
            "width": self._num(self.edt_wid.text()),
            "height": self._num(self.edt_hei.text()),
            "minimumStock": int(self.spn_min.value()),
            "maximumStock": int(self.spn_max.value()),
            "reorderPoint": int(self.spn_reorder.value()),
            "active": bool(self.chk_active.isChecked()),
            "perishable": bool(self.chk_perishable.isChecked()),
            "hazardous": bool(self.chk_hazardous.isChecked()),
            "fragile": bool(self.chk_fragile.isChecked()),
            "manufacturer": self.edt_manufacturer.text().strip(),
            "supplier": self.edt_supplier.text().strip(),
            "storageConditions": self.edt_storage.text().strip(),
            "barcode": self.edt_barcode.text().strip(),
        }.items():
            if (isinstance(v, str) and v != "") or (not isinstance(v, str)):
                payload[k] = v
        cat_id = self.cmb_category.currentData()
        if cat_id is not None:
            payload["categoryId"] = int(cat_id)
        return payload

    def accept(self):
        def valid_price() -> tuple[bool, str]:
            if (self.edt_price.text() or "").strip() == "":
                return False, "pole wymagane"
            if self._num(self.edt_price.text()) is None:
                return False, "musi być liczbą"
            return True, ""

        ok = validate_required(
            self,
            [
                RequiredField("SKU", self.edt_sku),
                RequiredField("Nazwa", self.edt_name),
                RequiredField("Kategoria", self.cmb_category),
                RequiredField("Cena", self.edt_price, validator=valid_price),
            ],
            title="Brak wymaganych danych produktu",
        )
        if not ok:
            return

        super().accept()

    @staticmethod
    def _num(text: str) -> Optional[float]:
        t = (text or "").replace(",", ".").strip()
        if t == "":
            return None
        try:
            return float(t)
        except ValueError:
            return None


class ProductManagerWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("QRWare - Produkty")
        self.resize(1100, 700)
        self.cfg = ConfigManager()
        self.products_api = ProductsApi(self.cfg)
        self.categories_api = CategoriesApi(self.cfg)

        central = QWidget();
        self.setCentralWidget(central)
        root = QVBoxLayout();
        central.setLayout(root)
        root.setContentsMargins(20, 20, 20, 20)
        root.setSpacing(15)

        header = QHBoxLayout()
        
        title_layout = QVBoxLayout()
        lbl_title = QLabel("Zarządzanie Produktami")
        lbl_title.setStyleSheet("font-size: 24px; font-weight: bold; color: #2c3e50;")
        lbl_subtitle = QLabel("Przeglądaj, dodawaj i edytuj asortyment")
        lbl_subtitle.setStyleSheet("font-size: 14px; color: #7f8c8d;")
        title_layout.addWidget(lbl_title)
        title_layout.addWidget(lbl_subtitle)
        header.addLayout(title_layout)
        
        header.addStretch()
        
        root.addLayout(header)

        toolbar = QHBoxLayout()
        toolbar.setSpacing(10)
        
        self.edt_search = QLineEdit();
        self.edt_search.setPlaceholderText("Szukaj po nazwie, SKU…")
        self.edt_search.setMinimumWidth(250)
        toolbar.addWidget(self.edt_search)
        
        btn_search = QPushButton("Szukaj");
        btn_search.clicked.connect(self._do_search)
        toolbar.addWidget(btn_search)
        
        self.chk_only_active = QCheckBox("Tylko aktywne")
        self.chk_only_active.setChecked(True)
        self.chk_only_active.stateChanged.connect(self._load_products)
        toolbar.addWidget(self.chk_only_active)
        
        toolbar.addStretch()
        
        btn_load_cats = QPushButton("Odśwież kategorie");
        btn_load_cats.clicked.connect(self._load_categories)
        toolbar.addWidget(btn_load_cats)
        
        btn_refresh = QPushButton("Odśwież listę");
        btn_refresh.clicked.connect(self._load_products)
        toolbar.addWidget(btn_refresh)
        
        root.addLayout(toolbar)

        self.splitter = QSplitter(Qt.Orientation.Horizontal)
        root.addWidget(self.splitter, 1)

        left = QWidget()
        left_layout = QVBoxLayout(left)
        left_layout.setContentsMargins(0, 0, 0, 0)
        left_layout.setSpacing(10)

        self.tbl = QTableWidget(0, 8)
        self.tbl.setHorizontalHeaderLabels(
            ["ID", "SKU", "Nazwa", "Cena", "Aktywny", "Kategoria", "Producent", "Kod kreskowy"])
        self.tbl.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.tbl.horizontalHeader().setStretchLastSection(True)
        self.tbl.setAlternatingRowColors(True)
        self.tbl.setStyleSheet("QTableWidget { border: 1px solid #dcdcdc; }")

        self.tbl.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu)
        self.tbl.customContextMenuRequested.connect(self._show_context_menu)

        self.tbl.itemSelectionChanged.connect(self._on_row_selected)

        left_layout.addWidget(self.tbl, 1)

        actions = QHBoxLayout()
        
        btn_add = QPushButton("Dodaj Produkt");
        btn_add.setStyleSheet("background-color: #2ecc71; color: white; font-weight: bold; padding: 8px 16px;")
        btn_add.clicked.connect(self._add_product)
        actions.addWidget(btn_add)
        
        btn_edit = QPushButton("Edytuj");
        btn_edit.clicked.connect(self._edit_product)
        actions.addWidget(btn_edit)
        
        btn_toggle = QPushButton("Aktywuj/Dezaktywuj");
        btn_toggle.clicked.connect(self._toggle_active)
        actions.addWidget(btn_toggle)
        
        actions.addStretch()
        
        btn_delete = QPushButton("Usuń");
        btn_delete.setStyleSheet("background-color: #e74c3c; color: white;")
        btn_delete.clicked.connect(self._delete_product)
        actions.addWidget(btn_delete)
        
        left_layout.addLayout(actions)

        right = QWidget()
        right_layout = QVBoxLayout(right)
        right_layout.setContentsMargins(6, 0, 0, 0)
        right_layout.setSpacing(6)

        lbl_details_title = QLabel("Szczegóły produktu")
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
        label_css = base_font_css + "font-weight: 600; color: #495057;"
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
        header_grid.addWidget(QLabel("Aktywny:"), 0, 2)
        header_grid.addWidget(self.txt_active, 0, 3)

        self.txt_id = mk_value()
        self.txt_sku = mk_value()
        self.txt_category = mk_value()
        self.txt_price = mk_value()
        self.txt_cost = mk_value()
        self.txt_unit = mk_value()
        self.txt_barcode = mk_value()
        self.txt_manufacturer = mk_value()
        self.txt_supplier = mk_value()
        self.txt_storage = mk_value()
        self.txt_desc = mk_value()

        header_grid.addWidget(self.txt_id, 1, 0, 1, 2)
        header_grid.addWidget(self.txt_sku, 1, 2, 1, 2)

        header_grid.addWidget(self.txt_category, 2, 0, 1, 2)
        header_grid.addWidget(self.txt_unit, 2, 2, 1, 2)

        header_grid.addWidget(self.txt_price, 3, 0, 1, 2)
        header_grid.addWidget(self.txt_cost, 3, 2, 1, 2)

        header_grid.addWidget(self.txt_barcode, 4, 0, 1, 2)
        header_grid.addWidget(self.txt_manufacturer, 4, 2, 1, 2)

        header_grid.addWidget(self.txt_supplier, 5, 0, 1, 2)
        header_grid.addWidget(self.txt_storage, 5, 2, 1, 2)

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

        self._categories: List[Dict[str, Any]] = []
        self._current_items: List[Dict[str, Any]] = []
        self._page: int = 0
        self._size: int = 100
        self._last_search: Optional[str] = None

        self._load_categories()
        self._load_products()

    def _show_context_menu(self, pos):
        if not self.tbl.selectionModel().selectedRows():
            return

        menu = QMenu()
        gen_qr_action = menu.addAction("Generuj kod QR")
        create_inv_action = menu.addAction("Utwórz pozycję magazynową")

        action = menu.exec(self.tbl.mapToGlobal(pos))

        if action == gen_qr_action:
            self._open_qr_generator()
        elif action == create_inv_action:
            self._create_inventory_from_product()

    def _open_qr_generator(self):
        row = self.tbl.currentRow()
        if row < 0:
            return

        pid_item = self.tbl.item(row, 0)
        sku_item = self.tbl.item(row, 1)

        if not pid_item or not sku_item:
            return

        try:
            pid = int(pid_item.text())
            sku = sku_item.text()

            from qr_manager import QRManagerWindow

            self.qr_window = QRManagerWindow()
            self.qr_window.set_form_data(
                data=sku,
                qr_type="TEXT",
                entity_type="product",
                entity_id=pid
            )
            self.qr_window.show()

        except Exception as e:
            QMessageBox.critical(self, "Błąd", f"Nie udało się otworzyć generatora QR: {str(e)}")

    def _create_inventory_from_product(self):
        row = self.tbl.currentRow()
        if row < 0:
            return

        pid_item = self.tbl.item(row, 0)
        if not pid_item:
            return

        try:
            pid = int(pid_item.text())

            from inventory_manager import InventoryFormDialog, InventoryManagerWindow
            from locations_api import LocationsApi

            loc_api = LocationsApi(self.cfg)
            ok, msg, locations = loc_api.list_active()
            if not ok:
                QMessageBox.warning(self, "Błąd", f"Nie udało się pobrać lokalizacji: {msg}")
                return

            ok_prod, msg_prod, products = self.products_api.get_active()
            if not ok_prod:
                QMessageBox.warning(self, "Błąd", f"Nie udało się pobrać produktów: {msg_prod}")
                return

            target_product = next((p for p in products if p['id'] == pid), None)

            initial_data = {
                "product": target_product,
                "quantity": 0,
                "status": "AVAILABLE"
            }

            dlg = InventoryFormDialog(products, locations, item=initial_data, parent=self)

            idx = dlg.cmb_product.findData(pid)
            if idx >= 0:
                dlg.cmb_product.setCurrentIndex(idx)
                dlg.cmb_product.setEnabled(False)

            if dlg.exec() == QDialog.DialogCode.Accepted:
                from inventory_api import InventoryApi
                inv_api = InventoryApi(self.cfg)

                payload = dlg.build_create_payload()
                if not payload.get("productId") or not payload.get("locationId"):
                    QMessageBox.warning(self, "Walidacja", "Wybierz lokalizację.")
                    return

                ok_create, msg_create, _ = inv_api.create(payload)
                if ok_create:
                    QMessageBox.information(self, "Sukces", "Utworzono nową pozycję magazynową.")
                else:
                    QMessageBox.critical(self, "Błąd", f"Nie udało się utworzyć pozycji: {msg_create}")

        except Exception as e:
            QMessageBox.critical(self, "Błąd", f"Wystąpił błąd: {str(e)}")

    def _save_server(self):
        self.cfg.base_url = self.edt_server.text().strip()
        QMessageBox.information(self, "Zapisano", "Adres serwera zapisany.")

    def _load_categories(self):
        ok, msg, cats = self.categories_api.list(only_active=False)
        if not ok:
            QMessageBox.warning(self, "Kategorie", msg)
            self._categories = []
        else:
            self._categories = cats

    def _on_row_selected(self):
        row = self.tbl.currentRow()
        if row < 0 or row >= len(self._current_items):
            for lbl in [self.txt_id, self.txt_sku, self.txt_name, self.txt_desc, self.txt_category, self.txt_price,
                        self.txt_cost, self.txt_unit, self.txt_active, self.txt_barcode, self.txt_manufacturer,
                        self.txt_supplier, self.txt_storage]:
                lbl.setText("-")
            return

        p = self._current_items[row]
        cat = p.get('category') or {}

        self.txt_name.setText(p.get('name') or "")
        self.txt_active.setText("TAK" if p.get('active') else "NIE")

        self.txt_id.setText(self._fmt_row("ID", str(p.get('id', ''))))
        self.txt_sku.setText(self._fmt_row("SKU", p.get('sku') or ""))
        self.txt_category.setText(self._fmt_row("Kategoria", cat.get('name') or ""))
        self.txt_unit.setText(self._fmt_row("Jednostka", p.get('unitOfMeasure') or p.get('unit') or ""))
        self.txt_price.setText(self._fmt_row("Cena", str(p.get('price') or "")))
        self.txt_cost.setText(self._fmt_row("Koszt", str(p.get('cost') or "")))
        self.txt_barcode.setText(self._fmt_row("Kod kreskowy", p.get('barcode') or ""))
        self.txt_manufacturer.setText(self._fmt_row("Producent", p.get('manufacturer') or ""))
        self.txt_supplier.setText(self._fmt_row("Dostawca", p.get('supplier') or ""))
        self.txt_storage.setText(self._fmt_row("Warunki skł.", p.get('storageConditions') or ""))
        self.txt_desc.setText(self._fmt_row("Opis", p.get('description') or ""))

    def _load_products(self):
        active = True if self.chk_only_active.isChecked() else None

        if self._last_search:
            ok, msg, items = self.products_api.search(self._last_search)
        else:
            ok, msg, items, page_info = self.products_api.list(page=self._page, size=self._size, active=active)
        if not ok:
            QMessageBox.warning(self, "Produkty", msg)
            return
        self._populate_table(items)

    def _populate_table(self, items: List[Dict[str, Any]]):
        self._current_items = items or []
        self.tbl.setRowCount(len(items))
        for r, p in enumerate(items):
            def setc(c: int, text: str):
                self.tbl.setItem(r, c, QTableWidgetItem(text))

            setc(0, str(p.get("id", "")))
            setc(1, p.get("sku") or "")
            setc(2, p.get("name") or "")
            setc(3, str(p.get("price", "") or ""))
            setc(4, "TAK" if p.get("active") else "NIE")
            cat = p.get("category") or {}
            setc(5, cat.get("name") or "")
            setc(6, p.get("manufacturer") or "")
            setc(7, p.get("barcode") or "")
        self.tbl.resizeColumnsToContents()
        self._on_row_selected()

    def _selected_product_id(self) -> Optional[int]:
        rows = self.tbl.selectionModel().selectedRows()
        if not rows:
            return None
        try:
            return int(self.tbl.item(rows[0].row(), 0).text())
        except Exception:
            return None

    def _do_search(self):
        q = self.edt_search.text().strip()
        self._last_search = q if q else None
        self._load_products()

    def _pick_product_row(self) -> Optional[Dict[str, Any]]:
        rid = self._selected_product_id()
        if rid is None:
            return None
        row = self.tbl.currentRow()
        return {
            "id": rid,
            "sku": self.tbl.item(row, 1).text(),
            "name": self.tbl.item(row, 2).text(),
            "price": self.tbl.item(row, 3).text(),
            "active": self.tbl.item(row, 4).text() == "TAK",
        }

    def _add_product(self):
        dlg = ProductFormDialog(self._categories, parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_create_payload()
            if not payload.get("sku") or not payload.get("name"):
                QMessageBox.warning(self, "Walidacja", "SKU i Nazwa są wymagane.")
                return
            ok, msg, _ = self.products_api.create(payload)
            if not ok:
                QMessageBox.critical(self, "Utworzenie", msg)
            else:
                self._load_products()

    def _edit_product(self):
        base = self._pick_product_row()
        if not base:
            QMessageBox.information(self, "Edycja", "Wybierz produkt w tabeli.")
            return
        dlg = ProductFormDialog(self._categories, product=base, parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_update_payload()
            ok, msg, _ = self.products_api.update(base["id"], payload)
            if not ok:
                QMessageBox.critical(self, "Aktualizacja", msg)
            else:
                self._load_products()

    def _delete_product(self):
        pid = self._selected_product_id()
        if pid is None:
            QMessageBox.information(self, "Usuń", "Wybierz produkt.")
            return
        if QMessageBox.question(self, "Potwierdzenie",
                                "Czy na pewno chcesz (soft) usunąć produkt?") != QMessageBox.StandardButton.Yes:
            return
        ok, msg = self.products_api.delete(pid)
        if not ok:
            QMessageBox.critical(self, "Usuwanie", msg)
        else:
            self._load_products()

    def _toggle_active(self):
        pid = self._selected_product_id()
        if pid is None:
            QMessageBox.information(self, "Status", "Wybierz produkt.")
            return
        ok, msg, _ = self.products_api.toggle_active(pid)
        if not ok:
            QMessageBox.critical(self, "Status", msg)
        else:
            self._load_products()


def main():
    app = QApplication(sys.argv)
    w = ProductManagerWindow()
    w.show()
    sys.exit(app.exec())


if __name__ == "__main__":
    main()