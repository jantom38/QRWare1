import sys
from typing import Any, Dict, List, Optional

from PyQt6 import QtCore
from PyQt6.QtCore import Qt
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLineEdit, QPushButton, QLabel, QComboBox, QTableWidget, QTableWidgetItem,
    QSpinBox, QMessageBox, QDialog, QFormLayout, QCheckBox, QMenu
)

from config import ConfigManager
from products_api import ProductsApi
from categories_api import CategoriesApi


class ProductFormDialog(QDialog):
    def __init__(self, categories: List[Dict[str, Any]], product: Optional[Dict[str, Any]] = None, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Produkt")
        self.resize(520, 520)
        self.categories = categories
        self.product = product or {}

        form = QFormLayout(self)

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
        self.chk_active = QCheckBox();
        self.chk_active.setChecked(bool(self.product.get("active", True)))
        self.chk_perishable = QCheckBox();
        self.chk_perishable.setChecked(bool(self.product.get("perishable", False)))
        self.chk_hazardous = QCheckBox();
        self.chk_hazardous.setChecked(bool(self.product.get("hazardous", False)))
        self.chk_fragile = QCheckBox();
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

        form.addRow("SKU:", self.edt_sku)
        form.addRow("Nazwa:", self.edt_name)
        form.addRow("Opis:", self.edt_desc)
        form.addRow("Cena:", self.edt_price)
        form.addRow("Koszt:", self.edt_cost)
        form.addRow("Jednostka:", self.edt_unit)
        form.addRow("Waga:", self.edt_weight)
        form.addRow("Długość:", self.edt_len)
        form.addRow("Szerokość:", self.edt_wid)
        form.addRow("Wysokość:", self.edt_hei)
        form.addRow("Min. stan:", self.spn_min)
        form.addRow("Max. stan:", self.spn_max)
        form.addRow("Punkt zam.:", self.spn_reorder)
        form.addRow("Aktywny:", self.chk_active)
        form.addRow("Szybko psujący:", self.chk_perishable)
        form.addRow("Niebezpieczny:", self.chk_hazardous)
        form.addRow("Kruchy:", self.chk_fragile)
        form.addRow("Producent:", self.edt_manufacturer)
        form.addRow("Dostawca:", self.edt_supplier)
        form.addRow("Warunki skł.:", self.edt_storage)
        form.addRow("Kod kreskowy:", self.edt_barcode)
        form.addRow("Kategoria:", self.cmb_category)

        btns = QHBoxLayout()
        self.btn_ok = QPushButton("Zapisz")
        self.btn_cancel = QPushButton("Anuluj")
        btns.addWidget(self.btn_ok)
        btns.addWidget(self.btn_cancel)
        form.addRow(btns)

        self.btn_ok.clicked.connect(self.accept)
        self.btn_cancel.clicked.connect(self.reject)

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
        self.resize(1000, 640)
        self.cfg = ConfigManager()
        self.products_api = ProductsApi(self.cfg)
        self.categories_api = CategoriesApi(self.cfg)

        central = QWidget();
        self.setCentralWidget(central)
        root = QVBoxLayout();
        central.setLayout(root)
        root.setContentsMargins(16, 16, 16, 16)
        root.setSpacing(12)

        top = QHBoxLayout()
        self.edt_server = QLineEdit(self.cfg.base_url);
        self.edt_server.setPlaceholderText("http://localhost:8080")
        btn_save_server = QPushButton("Zapisz serwer")
        btn_save_server.clicked.connect(self._save_server)
        self.edt_search = QLineEdit();
        self.edt_search.setPlaceholderText("Szukaj po nazwie…")
        btn_search = QPushButton("Szukaj");
        btn_search.clicked.connect(self._do_search)
        self.cmb_filter = QComboBox();
        self.cmb_filter.addItems(["Wszystkie", "Aktywne", "Nieaktywne"])
        btn_refresh = QPushButton("Odśwież");
        btn_refresh.clicked.connect(self._load_products)
        btn_load_cats = QPushButton("Załaduj kategorie");
        btn_load_cats.clicked.connect(self._load_categories)

        top.addWidget(QLabel("Serwer:"));
        top.addWidget(self.edt_server, 2)
        top.addWidget(btn_save_server)
        top.addWidget(self.cmb_filter)
        top.addWidget(self.edt_search, 1)
        top.addWidget(btn_search)
        top.addWidget(btn_load_cats)
        top.addWidget(btn_refresh)
        root.addLayout(top)

        self.tbl = QTableWidget(0, 8)
        self.tbl.setHorizontalHeaderLabels(
            ["ID", "SKU", "Nazwa", "Cena", "Aktywny", "Kategoria", "Producent", "Kod kreskowy"])
        self.tbl.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.tbl.horizontalHeader().setStretchLastSection(True)

        self.tbl.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu)
        self.tbl.customContextMenuRequested.connect(self._show_context_menu)

        root.addWidget(self.tbl, 1)

        actions = QHBoxLayout()
        btn_add = QPushButton("Dodaj…");
        btn_add.clicked.connect(self._add_product)
        btn_edit = QPushButton("Edytuj…");
        btn_edit.clicked.connect(self._edit_product)
        btn_toggle = QPushButton("Aktywuj/Dezaktywuj");
        btn_toggle.clicked.connect(self._toggle_active)
        btn_delete = QPushButton("Usuń (soft)");
        btn_delete.clicked.connect(self._delete_product)
        actions.addWidget(btn_add);
        actions.addWidget(btn_edit);
        actions.addWidget(btn_toggle);
        actions.addWidget(btn_delete)
        root.addLayout(actions)

        self._categories: List[Dict[str, Any]] = []
        self._page: int = 0
        self._size: int = 100
        self._last_search: Optional[str] = None

        self._load_categories()
        self._load_products()

    # --- NOWE METODY DO OBSŁUGI QR ---
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
        pid_item = self.tbl.item(row, 0)
        sku_item = self.tbl.item(row, 1)

        if not pid_item or not sku_item:
            return

        try:
            pid = int(pid_item.text())
            sku = sku_item.text()

            # Import dynamiczny aby uniknąć cyklicznych zależności na starcie
            from qr_manager import QRManagerWindow

            self.qr_window = QRManagerWindow()
            # Ustawiamy dane: data=sku, type=TEXT, entity_type=product, entity_id=pid
            self.qr_window.set_form_data(
                data=sku,
                qr_type="TEXT",
                entity_type="product",
                entity_id=pid
            )
            self.qr_window.show()

        except Exception as e:
            QMessageBox.critical(self, "Błąd", f"Nie udało się otworzyć generatora QR: {str(e)}")

    # ---------------------------------

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

    def _load_products(self):
        filter_idx = self.cmb_filter.currentIndex()
        active: Optional[bool] = None
        if filter_idx == 1:
            active = True
        elif filter_idx == 2:
            active = False

        if self._last_search:
            ok, msg, items = self.products_api.search(self._last_search)
        else:
            ok, msg, items, page_info = self.products_api.list(page=self._page, size=self._size, active=active)
        if not ok:
            QMessageBox.warning(self, "Produkty", msg)
            return
        self._populate_table(items)

    def _populate_table(self, items: List[Dict[str, Any]]):
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