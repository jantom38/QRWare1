from PyQt6.QtWidgets import (
    QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, QTableWidget,
    QTableWidgetItem, QPushButton, QLabel, QHeaderView,
    QMessageBox, QGroupBox, QDialog, QFormLayout,
    QComboBox, QLineEdit, QTextEdit, QInputDialog,
    QTabWidget, QDateEdit, QSpinBox, QDoubleSpinBox, QGridLayout,
    QCompleter, QCheckBox, QListWidget, QListWidgetItem, QFrame, QMenu
)
from PyQt6.QtCore import Qt, QDate, QDateTime
from datetime import datetime
from order_api import OrderService
from inventory_api import InventoryApi

PL_MAP = {
    "INBOUND": "PRZYJĘCIE",
    "OUTBOUND": "WYDANIE",
    "TRANSFER": "PRZESUNIĘCIE",
    "INTERNAL": "WEWNĘTRZNE",
    "NORMAL": "NORMALNY",
    "HIGH": "WYSOKI",
    "CRITICAL": "KRYTYCZNY",
    "LOW": "NISKI",
    "CREATED": "UTWORZONE",
    "ASSIGNED": "PRZYPISANE",
    "IN_PROGRESS": "W TOKU",
    "ON_HOLD": "WSTRZYMANE",
    "PARTIALLY_COMPLETED": "CZĘŚCIOWO ZREALIZOWANE",
    "COMPLETED": "ZAKOŃCZONE",
    "CANCELLED": "ANULOWANE",
    "FAILED": "NIEUDANE",
    "PENDING": "OCZEKUJE",
    "PICKED": "SKOMPLETOWANE",
    "PACKED": "SPAKOWANE",
    "SHIPPED": "WYSŁANE",
    "DELIVERED": "DOSTARCZONE"
}


def tr(key):
    return PL_MAP.get(str(key), str(key))


class InventorySelectionDialog(QDialog):
    def __init__(
        self,
        api_service: OrderService,
        inventory_api: InventoryApi,
        order_type: str = None,
        fixed_source_location_id: int | None = None,
        fixed_destination_location_id: int | None = None,
        parent=None,
    ):
        super().__init__(parent)
        self.api = api_service
        self.inv_api = inventory_api
        self.order_type = order_type
        self.fixed_source_location_id = fixed_source_location_id
        self.fixed_destination_location_id = fixed_destination_location_id

        self.setWindowTitle("Dodaj pozycję do zlecenia")
        self.resize(900, 600)

        self._result_item = None

        layout = QVBoxLayout(self)
        layout.setContentsMargins(20, 20, 20, 20)
        layout.setSpacing(15)

        lbl_prod = QLabel("1. Wybierz Produkt")
        lbl_prod.setStyleSheet("font-weight: bold; font-size: 14px; color: #2c3e50;")
        layout.addWidget(lbl_prod)

        self.product_search = QLineEdit()
        self.product_search.setPlaceholderText("Wpisz nazwę lub SKU, aby wyszukać...")
        self.product_search.textChanged.connect(self.filter_products)
        layout.addWidget(self.product_search)

        self.product_list = QListWidget()
        self.product_list.setMinimumHeight(160)
        self.product_list.currentItemChanged.connect(lambda *_: self.on_product_changed())
        layout.addWidget(self.product_list)

        qty_row = QHBoxLayout()
        qty_row.addWidget(QLabel("Ilość:"))
        self.qty_spin = QSpinBox()
        self.qty_spin.setRange(1, 999999)
        self.qty_spin.setValue(1)
        self.qty_spin.setSuffix(" szt.")
        qty_row.addWidget(self.qty_spin)
        qty_row.addStretch()
        layout.addLayout(qty_row)

        lbl_loc = QLabel("2. Opcje Lokalizacji")
        lbl_loc.setStyleSheet("font-weight: bold; font-size: 14px; color: #2c3e50; margin-top: 10px;")
        layout.addWidget(lbl_loc)

        self.exact_checkbox = QCheckBox("Wymagaj dokładnej lokalizacji (pobierz z konkretnego miejsca)")
        self.exact_checkbox.setChecked(True)
        self.exact_checkbox.setEnabled(False)
        self.exact_checkbox.stateChanged.connect(self.on_exact_changed)
        layout.addWidget(self.exact_checkbox)

        self.hint_label = QLabel("Wybierz produkt, aby zobaczyć dostępne stany magazynowe.")
        self.fixed_loc_label = QLabel("")
        self.fixed_loc_label.setStyleSheet("color: #555; font-size: 11px;")
        layout.addWidget(self.fixed_loc_label)
        self.hint_label.setStyleSheet("color: gray; font-style: italic; font-size: 11px;")
        layout.addWidget(self.hint_label)

        self.table = QTableWidget()
        self.table.setColumnCount(6)
        self.table.setHorizontalHeaderLabels([
            "Inv ID", "Produkt", "SKU", "Dostępne", "Lokalizacja", "Status"
        ])
        self.table.horizontalHeader().setSectionResizeMode(1, QHeaderView.ResizeMode.Stretch)
        self.table.horizontalHeader().setSectionResizeMode(4, QHeaderView.ResizeMode.Stretch)
        self.table.setVisible(False)
        self.table.setStyleSheet("QTableWidget { border: 1px solid #dcdcdc; }")
        layout.addWidget(self.table)

        btns = QHBoxLayout()
        self.btn_refresh = QPushButton("Odśwież")
        self.btn_refresh.clicked.connect(self.refresh_inventory_list)
        self.btn_refresh.setEnabled(False)

        self.btn_cancel = QPushButton("Anuluj")
        self.btn_cancel.setStyleSheet("background-color: #95a5a6; color: white;")
        self.btn_cancel.clicked.connect(self.reject)

        self.btn_select = QPushButton("Dodaj pozycję")
        self.btn_select.setStyleSheet("background-color: #2ecc71; color: white; font-weight: bold;")
        self.btn_select.clicked.connect(self.accept_selection)
        self.btn_select.setEnabled(False)

        btns.addStretch()
        btns.addWidget(self.btn_refresh)
        btns.addWidget(self.btn_cancel)
        btns.addWidget(self.btn_select)
        layout.addLayout(btns)

        self._all_products = []
        self._load_products()

    def _load_products(self):
        self._all_products = list(self.api.get_simple_products() or [])
        self.filter_products("")

    def filter_products(self, text: str):
        text = (text or "").strip().lower()
        self.product_list.blockSignals(True)
        self.product_list.clear()

        for pid, name, sku in self._all_products:
            hay = f"{name} {sku or ''}".lower()
            if text and text not in hay:
                continue
            label = f"{name} ({sku})" if sku else name
            item = QListWidgetItem(label)
            item.setData(Qt.ItemDataRole.UserRole, pid)
            item.setData(Qt.ItemDataRole.UserRole + 1, name)
            item.setData(Qt.ItemDataRole.UserRole + 2, sku or "")
            self.product_list.addItem(item)

        self.product_list.blockSignals(False)
        if self.product_list.count() == 1:
            self.product_list.setCurrentRow(0)
        else:
            self.on_product_changed()

    def on_product_changed(self):
        current = self.product_list.currentItem()
        prod_id = current.data(Qt.ItemDataRole.UserRole) if current else None
        has_prod = bool(prod_id)

        self.exact_checkbox.setEnabled(has_prod)
        self.btn_refresh.setEnabled(has_prod)
        self.btn_select.setEnabled(has_prod)

        if not has_prod:
            self.hint_label.setText("Wybierz produkt, aby kontynuować.")
            self.table.setVisible(False)
            self.table.setRowCount(0)
            return

        self.on_exact_changed()

    def on_exact_changed(self):
        current = self.product_list.currentItem()
        prod_id = current.data(Qt.ItemDataRole.UserRole) if current else None
        if not prod_id:
            return

        if self.exact_checkbox.isChecked():
            self.hint_label.setText("Wybierz konkretny stan magazynowy (inventory item) z listy poniżej.")
            self.table.setVisible(True)
            self.refresh_inventory_list()
        else:
            self.hint_label.setText(
                "Źródło nie jest wymagane – system może dobrać lokalizację automatycznie. "
                "(Do zlecenia zostanie dodany tylko produkt i ilość.)"
            )
            self.table.setVisible(False)
            self.table.setRowCount(0)

    def refresh_inventory_list(self):
        if not self.exact_checkbox.isChecked():
            return

        current = self.product_list.currentItem()
        prod_id = current.data(Qt.ItemDataRole.UserRole) if current else None
        if not prod_id:
            return

        items = []
        if self.fixed_source_location_id and self.order_type in ["OUTBOUND", "TRANSFER"]:
            ok, msg, items = self.inv_api.by_location(int(self.fixed_source_location_id))
            if ok:
                items = [it for it in (items or [])
                         if (it.get('product', {}) or {}).get('id') == prod_id or it.get('productId') == prod_id]
        else:
            ok, msg, items = self.inv_api.by_product(int(prod_id))
        if not ok:
            QMessageBox.warning(self, "Błąd", f"Nie udało się pobrać stanów magazynowych: {msg}")
            items = []

        if self.fixed_source_location_id and self.order_type in ["OUTBOUND", "TRANSFER"]:
            self.fixed_loc_label.setText(f"Źródło zlecenia: {self.fixed_source_location_id} (lista ograniczona do tej lokalizacji)")
        else:
            self.fixed_loc_label.setText("")

        self.table.setRowCount(0)
        for row, it in enumerate(items or []):
            inv_id = it.get('id')
            prod = it.get('product') or {}
            loc = it.get('location') or {}

            self.table.insertRow(row)
            self.table.setItem(row, 0, QTableWidgetItem(str(inv_id)))
            self.table.setItem(row, 1, QTableWidgetItem(str(prod.get('name') or it.get('productName') or '')))
            self.table.setItem(row, 2, QTableWidgetItem(str(prod.get('sku') or it.get('productSku') or '')))
            self.table.setItem(row, 3, QTableWidgetItem(str(it.get('availableQuantity') or it.get('quantity') or 0)))
            self.table.setItem(row, 4, QTableWidgetItem(str(loc.get('code') or it.get('locationCode') or '')))
            self.table.setItem(row, 5, QTableWidgetItem(str(it.get('status') or '')))

            self.table.item(row, 0).setData(Qt.ItemDataRole.UserRole, it)

    def accept_selection(self):
        current = self.product_list.currentItem()
        prod_id = current.data(Qt.ItemDataRole.UserRole) if current else None
        if not prod_id:
            QMessageBox.warning(self, "Brak produktu", "Wybierz produkt.")
            return

        qty = self.qty_spin.value()

        if not self.exact_checkbox.isChecked():
            prod_name = current.data(Qt.ItemDataRole.UserRole + 1) if current else ""
            prod_sku = current.data(Qt.ItemDataRole.UserRole + 2) if current else ""
            self._result_item = {
                "inventoryItemId": None,
                "productId": prod_id,
                "productName": prod_name,
                "productSku": prod_sku,
                "requestedQuantity": qty,
                "sourceLocationId": None,
                "sourceLocationCode": "",
                "requiresExactInventory": False,
            }
            self.accept()
            return

        row = self.table.currentRow()
        if row < 0:
            QMessageBox.warning(self, "Brak wyboru", "Wybierz wiersz ze stanem magazynowym.")
            return

        it = self.table.item(row, 0).data(Qt.ItemDataRole.UserRole)
        if not it:
            QMessageBox.warning(self, "Błąd", "Nie można pobrać danych wybranej pozycji.")
            return

        available = it.get('availableQuantity') or it.get('quantity') or 0
        if available is not None and isinstance(available, (int, float)) and available < qty:
            reply = QMessageBox.question(
                self,
                "Niewystarczający stan",
                f"Dostępne: {available} szt.\nWymagane: {qty} szt.\n\nDodać mimo to?",
                QMessageBox.StandardButton.Yes | QMessageBox.StandardButton.No,
                QMessageBox.StandardButton.No
            )
            if reply == QMessageBox.StandardButton.No:
                return

        prod = it.get('product') or {}
        loc = it.get('location') or {}

        self._result_item = {
            "inventoryItemId": it.get('id'),
            "productId": prod.get('id') or it.get('productId'),
            "productName": prod.get('name') or it.get('productName') or "",
            "productSku": prod.get('sku') or it.get('productSku') or "",
            "requestedQuantity": qty,
            "sourceLocationId": loc.get('id') or it.get('locationId'),
            "sourceLocationCode": loc.get('code') or it.get('locationCode') or "",
            "requiresExactInventory": True,
        }

        self.accept()

    def get_selected_item(self):
        return self._result_item


class CreateOrderDialog(QDialog):
    def __init__(self, api_service, parent=None):
        super().__init__(parent)
        self.api = api_service
        self.inventory_api = InventoryApi()

        self.setWindowTitle("Kreator Nowego Zlecenia")
        self.resize(750, 600)

        self.added_items = []

        layout = QVBoxLayout(self)
        layout.setContentsMargins(20, 20, 20, 20)
        layout.setSpacing(15)

        lbl_title = QLabel("Nowe Zlecenie")
        lbl_title.setStyleSheet("font-size: 18px; font-weight: bold; color: #2c3e50; margin-bottom: 10px;")
        layout.addWidget(lbl_title)

        self.tabs = QTabWidget()
        layout.addWidget(self.tabs)

        self.tab_general = QWidget()
        self.init_tab_general()
        self.tabs.addTab(self.tab_general, "1. Dane Podstawowe")

        self.tab_logistics = QWidget()
        self.init_tab_logistics()
        self.tabs.addTab(self.tab_logistics, "2. Logistyka")

        self.tab_items = QWidget()
        self.init_tab_items()
        self.tabs.addTab(self.tab_items, "3. Produkty")

        btn_box = QHBoxLayout()
        self.btn_save = QPushButton("Utwórz Zlecenie")
        self.btn_save.setStyleSheet("background-color: #2ecc71; color: white; font-weight: bold; padding: 8px;")
        self.btn_save.clicked.connect(self.accept)

        self.btn_cancel = QPushButton("Anuluj")
        self.btn_cancel.setStyleSheet("background-color: #95a5a6; color: white;")
        self.btn_cancel.clicked.connect(self.reject)

        btn_box.addStretch()
        btn_box.addWidget(self.btn_cancel)
        btn_box.addWidget(self.btn_save)
        layout.addLayout(btn_box)

        self.load_combo_data()

    def init_tab_general(self):
        layout = QFormLayout(self.tab_general)
        layout.setSpacing(10)

        self.input_type = QComboBox()
        self.input_type.addItem("Przyjęcie (INBOUND)", "INBOUND")
        self.input_type.addItem("Wydanie (OUTBOUND)", "OUTBOUND")
        self.input_type.addItem("Przesunięcie (TRANSFER)", "TRANSFER")
        self.input_type.addItem("Wewnętrzne (INTERNAL)", "INTERNAL")

        self.input_priority = QComboBox()
        self.input_priority.addItem("Normalny", "NORMAL")
        self.input_priority.addItem("Wysoki", "HIGH")
        self.input_priority.addItem("Krytyczny", "CRITICAL")
        self.input_priority.addItem("Niski", "LOW")

        self.input_order_number = QLineEdit()
        self.input_order_number.setPlaceholderText("Auto-generowany (pozostaw puste)")

        self.input_desc = QTextEdit()
        self.input_desc.setPlaceholderText("Dodatkowy opis zlecenia...")
        self.input_desc.setMaximumHeight(100)

        self.input_expected_date = QDateEdit()
        self.input_expected_date.setCalendarPopup(True)
        self.input_expected_date.setDate(QDate.currentDate().addDays(1))

        layout.addRow("Typ zlecenia:", self.input_type)
        layout.addRow("Priorytet:", self.input_priority)
        layout.addRow("Numer Zlecenia:", self.input_order_number)
        layout.addRow("Oczekiwana data:", self.input_expected_date)
        layout.addRow("Opis:", self.input_desc)

    def init_tab_logistics(self):
        layout = QFormLayout(self.tab_logistics)
        layout.setSpacing(10)

        self.combo_source = QComboBox()
        self.combo_dest = QComboBox()
        self.combo_assignee = QComboBox()

        self.combo_source.setEditable(True)
        self.combo_source.setInsertPolicy(QComboBox.InsertPolicy.NoInsert)
        self.combo_source.completer().setCompletionMode(QCompleter.CompletionMode.PopupCompletion)
        self.combo_source.completer().setFilterMode(Qt.MatchFlag.MatchContains)

        self.combo_dest.setEditable(True)
        self.combo_dest.setInsertPolicy(QComboBox.InsertPolicy.NoInsert)
        self.combo_dest.completer().setCompletionMode(QCompleter.CompletionMode.PopupCompletion)
        self.combo_dest.completer().setFilterMode(Qt.MatchFlag.MatchContains)

        self.combo_assignee.setEditable(True)
        self.combo_assignee.setInsertPolicy(QComboBox.InsertPolicy.NoInsert)

        layout.addRow("Lokalizacja Źródłowa:", self.combo_source)
        layout.addRow("Lokalizacja Docelowa:", self.combo_dest)
        layout.addRow("Przypisz do użytkownika:", self.combo_assignee)

        info_label = QLabel("Wskazówka: Dla zleceń typu PRZYJĘCIE wypełnij cel, dla WYDANIE źródło.")
        info_label.setStyleSheet("color: gray; font-style: italic; font-size: 11px;")
        layout.addRow(info_label)

    def init_tab_items(self):
        layout = QVBoxLayout(self.tab_items)

        add_grp = QGroupBox("Dodaj pozycję")
        form_layout = QHBoxLayout()

        info = QLabel("Wybierz konkretny stan magazynowy (inventory item) do dodania.")
        info.setStyleSheet("color: gray; font-style: italic; font-size: 11px;")

        btn_add_item = QPushButton("Wybierz stan magazynowy...")
        btn_add_item.setStyleSheet("background-color: #3498db; color: white; font-weight: bold; padding: 6px;")
        btn_add_item.clicked.connect(self.add_item_to_list)

        form_layout.addWidget(info, 1)
        form_layout.addWidget(btn_add_item)

        add_grp.setLayout(form_layout)
        layout.addWidget(add_grp)

        self.items_table = QTableWidget()
        self.items_table.setColumnCount(5)
        self.items_table.setHorizontalHeaderLabels(["Inv ID", "Produkt", "Ilość", "Źródło", "Akcja"])
        self.items_table.horizontalHeader().setSectionResizeMode(1, QHeaderView.ResizeMode.Stretch)
        self.items_table.horizontalHeader().setSectionResizeMode(3, QHeaderView.ResizeMode.Stretch)
        self.items_table.setStyleSheet("QTableWidget { border: 1px solid #dcdcdc; }")
        layout.addWidget(self.items_table)

        btn_remove = QPushButton("Usuń zaznaczone")
        btn_remove.setStyleSheet("color: red;")
        btn_remove.clicked.connect(self.remove_selected_item)
        layout.addWidget(btn_remove)

    def load_combo_data(self):
        self.combo_source.clear()
        self.combo_dest.clear()
        self.combo_assignee.clear()

        self.combo_source.addItem("--- Brak / Nie dotyczy ---", None)
        self.combo_dest.addItem("--- Brak / Nie dotyczy ---", None)
        self.combo_assignee.addItem("--- Nieprzypisany ---", None)

        locs = self.api.get_simple_locations()
        if locs:
            for lid, name, code in locs:
                display = f"{name} [{code}]"
                self.combo_source.addItem(display, lid)
                self.combo_dest.addItem(display, lid)

        users = self.api.get_simple_users()
        if users:
            for uid, username, fullname in users:
                display = f"{fullname} ({username})" if fullname else username
                self.combo_assignee.addItem(display, uid)

    def add_item_to_list(self):
        order_type = self.input_type.currentData()
        dialog = InventorySelectionDialog(
            self.api,
            self.inventory_api,
            order_type=order_type,
            fixed_source_location_id=self.combo_source.currentData(),
            fixed_destination_location_id=self.combo_dest.currentData(),
            parent=self
        )
        if not dialog.exec():
            return

        selected = dialog.get_selected_item()
        if not selected:
            return

        order_type = self.input_type.currentData()
        if order_type in ['OUTBOUND', 'TRANSFER'] and bool(selected.get('requiresExactInventory', True)) and not selected.get('sourceLocationId'):
            QMessageBox.warning(self, "Błąd walidacji", "Nie wybrano lokalizacji źródłowej (wymagane przy trybie dokładnym).")
            return

        if bool(selected.get('requiresExactInventory', True)):
            src_id = selected.get('sourceLocationId')
            if order_type in ['OUTBOUND', 'TRANSFER'] and not self.combo_source.currentData() and src_id:
                idx = self.combo_source.findData(src_id)
                if idx >= 0:
                    self.combo_source.setCurrentIndex(idx)
            if order_type in ['INBOUND'] and not self.combo_dest.currentData() and src_id:
                idx = self.combo_dest.findData(src_id)
                if idx >= 0:
                    self.combo_dest.setCurrentIndex(idx)

        inv_id = selected.get('inventoryItemId')
        self.added_items.append({
            "inventoryItemId": inv_id,
            "productId": selected.get('productId'),
            "productName": selected.get('productName'),
            "productSku": selected.get('productSku'),
            "requestedQuantity": selected.get('requestedQuantity'),
            "sourceLocationId": selected.get('sourceLocationId'),
            "sourceLocationCode": selected.get('sourceLocationCode'),
            "requiresExactInventory": bool(selected.get('requiresExactInventory')),
            "notes": f"inventoryItemId={inv_id}" if inv_id else ""
        })

        self.refresh_items_table()

    def refresh_items_table(self):
        self.items_table.setRowCount(0)
        for i, item in enumerate(self.added_items):
            self.items_table.insertRow(i)
            self.items_table.setItem(i, 0, QTableWidgetItem(str(item.get('inventoryItemId') or '')))
            prod_label = str(item.get('productName') or '')
            sku = item.get('productSku')
            if sku:
                prod_label = f"{prod_label} ({sku})"
            self.items_table.setItem(i, 1, QTableWidgetItem(prod_label))
            self.items_table.setItem(i, 2, QTableWidgetItem(str(item.get('requestedQuantity') or 0)))
            src = item.get('sourceLocationCode') or ''
            self.items_table.setItem(i, 3, QTableWidgetItem(src))
            self.items_table.setItem(i, 4, QTableWidgetItem(""))

    def remove_selected_item(self):
        row = self.items_table.currentRow()
        if row >= 0:
            self.added_items.pop(row)
            self.refresh_items_table()

    def get_order_data(self):
        qdate = self.input_expected_date.date()
        dt = datetime(qdate.year(), qdate.month(), qdate.day(), 12, 0, 0)
        iso_date = dt.isoformat()

        return {
            "type": self.input_type.currentData(),
            "priority": self.input_priority.currentData(),
            "description": self.input_desc.toPlainText(),
            "orderNumber": self.input_order_number.text() or None,
            "expectedDate": iso_date,
            "sourceLocationId": self.combo_source.currentData(),
            "destinationLocationId": self.combo_dest.currentData(),
            "assignedToId": self.combo_assignee.currentData()
        }

    def get_items_data(self):
        return self.added_items


class EditOrderDialog(QDialog):
    def __init__(self, api: OrderService, order: dict, parent=None):
        super().__init__(parent)
        self.api = api
        self.order = order or {}

        self.setWindowTitle("Edycja zlecenia")
        self.resize(550, 450)

        layout = QFormLayout(self)
        layout.setContentsMargins(20, 20, 20, 20)
        layout.setSpacing(10)

        lbl_title = QLabel("Edycja Zlecenia")
        lbl_title.setStyleSheet("font-size: 18px; font-weight: bold; color: #2c3e50; margin-bottom: 10px;")
        layout.addRow(lbl_title)

        self.input_priority = QComboBox()
        self.input_priority.addItem("Normalny", "NORMAL")
        self.input_priority.addItem("Wysoki", "HIGH")
        self.input_priority.addItem("Krytyczny", "CRITICAL")
        self.input_priority.addItem("Niski", "LOW")

        self.input_expected_date = QDateEdit()
        self.input_expected_date.setCalendarPopup(True)
        self.input_expected_date.setDate(QDate.currentDate().addDays(1))

        self.input_desc = QTextEdit()
        self.input_desc.setMaximumHeight(100)

        self.combo_source = QComboBox()
        self.combo_dest = QComboBox()
        self.combo_assignee = QComboBox()

        self.combo_source.addItem("--- Brak / Nie dotyczy ---", None)
        self.combo_dest.addItem("--- Brak / Nie dotyczy ---", None)
        self.combo_assignee.addItem("--- Nieprzypisany ---", None)

        locs = self.api.get_simple_locations() or []
        for lid, name, code in locs:
            display = f"{name} [{code}]"
            self.combo_source.addItem(display, lid)
            self.combo_dest.addItem(display, lid)

        users = self.api.get_simple_users() or []
        for uid, username, fullname in users:
            display = f"{fullname} ({username})" if fullname else username
            self.combo_assignee.addItem(display, uid)

        self.input_desc.setText(self.order.get('description') or "")

        prio = (self.order.get('priority') or "NORMAL").upper()
        idx = self.input_priority.findData(prio)
        if idx >= 0:
            self.input_priority.setCurrentIndex(idx)

        exp = self.order.get('expectedDate')
        if exp:
            try:
                y, m, d = int(exp[0:4]), int(exp[5:7]), int(exp[8:10])
                self.input_expected_date.setDate(QDate(y, m, d))
            except Exception:
                pass

        assigned_id = self.order.get('assignedToId')
        if assigned_id:
            idx = self.combo_assignee.findData(assigned_id)
            if idx >= 0:
                self.combo_assignee.setCurrentIndex(idx)

        src_id = self.order.get('sourceLocationId')
        if src_id:
            idx = self.combo_source.findData(src_id)
            if idx >= 0:
                self.combo_source.setCurrentIndex(idx)

        dst_id = self.order.get('destinationLocationId')
        if dst_id:
            idx = self.combo_dest.findData(dst_id)
            if idx >= 0:
                self.combo_dest.setCurrentIndex(idx)

        layout.addRow("Priorytet:", self.input_priority)
        layout.addRow("Oczekiwana data:", self.input_expected_date)
        layout.addRow("Opis:", self.input_desc)
        layout.addRow("Lokalizacja źródłowa:", self.combo_source)
        layout.addRow("Lokalizacja docelowa:", self.combo_dest)
        layout.addRow("Przypisz do:", self.combo_assignee)

        btn_row = QHBoxLayout()
        self.btn_cancel = QPushButton("Anuluj")
        self.btn_cancel.setStyleSheet("background-color: #95a5a6; color: white;")
        self.btn_cancel.clicked.connect(self.reject)
        
        self.btn_save = QPushButton("Zapisz")
        self.btn_save.setStyleSheet("background-color: #2ecc71; color: white; font-weight: bold;")
        self.btn_save.clicked.connect(self.accept)
        
        btn_row.addStretch()
        btn_row.addWidget(self.btn_cancel)
        btn_row.addWidget(self.btn_save)
        layout.addRow(btn_row)

    def get_payload(self):
        qdate = self.input_expected_date.date()
        dt = datetime(qdate.year(), qdate.month(), qdate.day(), 12, 0, 0)
        iso_date = dt.isoformat()

        return {
            "description": self.input_desc.toPlainText(),
            "priority": self.input_priority.currentData(),
            "expectedDate": iso_date,
            "sourceLocationId": self.combo_source.currentData(),
            "destinationLocationId": self.combo_dest.currentData(),
            "assignedToId": self.combo_assignee.currentData(),
        }


class OrderManagerWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("QRWare - Zarządzanie Zleceniami")
        self.resize(1300, 850)

        self.api = OrderService()
        self.current_order_id = None
        self.current_order_data = None

        central = QWidget()
        self.setCentralWidget(central)
        main_layout = QHBoxLayout(central)
        main_layout.setContentsMargins(20, 20, 20, 20)
        main_layout.setSpacing(20)

        left_layout = QVBoxLayout()
        
        lbl_list = QLabel("Lista Zleceń")
        lbl_list.setStyleSheet("font-size: 18px; font-weight: bold; color: #2c3e50;")
        left_layout.addWidget(lbl_list)

        self.lbl_stats = QLabel("Ładowanie statystyk...")
        self.lbl_stats.setStyleSheet("font-weight: bold; color: #7f8c8d; margin-bottom: 10px;")
        left_layout.addWidget(self.lbl_stats)

        self.table_orders = QTableWidget()
        self.table_orders.setColumnCount(6)
        self.table_orders.setHorizontalHeaderLabels(["ID", "Numer", "Typ", "Status", "Priorytet", "Data"])
        self.table_orders.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
        self.table_orders.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.table_orders.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.table_orders.setAlternatingRowColors(True)
        self.table_orders.setStyleSheet("QTableWidget { border: 1px solid #dcdcdc; }")
        self.table_orders.itemClicked.connect(self.on_order_selected)
        
        self.table_orders.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu)
        self.table_orders.customContextMenuRequested.connect(self._show_order_context_menu)
        
        left_layout.addWidget(self.table_orders)

        btn_row = QHBoxLayout()
        btn_refresh = QPushButton("Odśwież Listę")
        btn_refresh.clicked.connect(self.load_orders)

        self.combo_status_filter = QComboBox()
        self.combo_status_filter.addItem("Wszystkie", "ALL")
        self.combo_status_filter.addItem("Aktywne", "ACTIVE")
        self.combo_status_filter.addItem("Utworzone", "CREATED")
        self.combo_status_filter.addItem("Przypisane", "ASSIGNED")
        self.combo_status_filter.addItem("W toku", "IN_PROGRESS")
        self.combo_status_filter.addItem("Wstrzymane", "ON_HOLD")
        self.combo_status_filter.addItem("Częściowo", "PARTIALLY_COMPLETED")
        self.combo_status_filter.addItem("Zakończone", "COMPLETED")
        self.combo_status_filter.addItem("Anulowane", "CANCELLED")
        self.combo_status_filter.addItem("Nieudane", "FAILED")

        self.combo_status_filter.setCurrentIndex(1)
        self.combo_status_filter.currentIndexChanged.connect(self.load_orders)

        btn_create = QPushButton("+ Nowe Zlecenie")
        btn_create.setStyleSheet("background-color: #2ecc71; color: white; font-weight: bold; padding: 8px 16px;")
        btn_create.clicked.connect(self.open_create_dialog)

        btn_row.addWidget(btn_refresh)
        btn_row.addWidget(self.combo_status_filter)
        btn_row.addStretch()
        btn_row.addWidget(btn_create)
        left_layout.addLayout(btn_row)

        main_layout.addLayout(left_layout, 40)

        right_layout = QVBoxLayout()
        self.details_group = QGroupBox("Szczegóły Zlecenia")
        self.details_group.setVisible(False)
        self.details_group.setStyleSheet("QGroupBox { font-weight: bold; font-size: 14px; }")

        details_inner_layout = QVBoxLayout()
        details_inner_layout.setSpacing(15)

        header_widget = QWidget()
        header_widget.setStyleSheet("background-color: #f8f9fa; border-radius: 6px; padding: 10px;")
        header_grid = QGridLayout(header_widget)
        header_grid.setSpacing(10)

        self.txt_order_num = QLabel()
        self.txt_order_num.setStyleSheet("font-size: 18px; font-weight: bold; color: #3498db;")
        header_grid.addWidget(QLabel("Numer:"), 0, 0)
        header_grid.addWidget(self.txt_order_num, 0, 1)

        self.txt_status = QLabel()
        self.txt_status.setStyleSheet("font-weight: bold; font-size: 14px;")
        header_grid.addWidget(QLabel("Status:"), 0, 2)
        header_grid.addWidget(self.txt_status, 0, 3)

        self.txt_type = QLabel("-")
        self.txt_prio = QLabel("-")
        self.txt_assigned = QLabel("-")
        self.txt_source = QLabel("-")
        self.txt_dest = QLabel("-")
        self.txt_dates = QLabel("-")
        self.txt_desc = QLabel("-")
        self.txt_desc.setWordWrap(True)
        self.txt_notes = QLabel("-")

        header_grid.addWidget(QLabel("Typ:"), 1, 0)
        header_grid.addWidget(self.txt_type, 1, 1)
        header_grid.addWidget(QLabel("Priorytet:"), 1, 2)
        header_grid.addWidget(self.txt_prio, 1, 3)

        header_grid.addWidget(QLabel("Przypisany:"), 2, 0)
        header_grid.addWidget(self.txt_assigned, 2, 1)
        header_grid.addWidget(QLabel("Daty (Ocz/Utw):"), 2, 2)
        header_grid.addWidget(self.txt_dates, 2, 3)

        header_grid.addWidget(QLabel("Źródło:"), 3, 0)
        header_grid.addWidget(self.txt_source, 3, 1)
        header_grid.addWidget(QLabel("Cel:"), 3, 2)
        header_grid.addWidget(self.txt_dest, 3, 3)

        header_grid.addWidget(QLabel("Opis:"), 4, 0)
        header_grid.addWidget(self.txt_desc, 4, 1, 1, 3)

        details_inner_layout.addWidget(header_widget)
        
        lbl_items = QLabel("Pozycje zlecenia")
        lbl_items.setStyleSheet("font-weight: bold; margin-top: 10px;")
        details_inner_layout.addWidget(lbl_items)

        self.table_items = QTableWidget()
        self.table_items.setColumnCount(5)
        self.table_items.setHorizontalHeaderLabels(["Produkt", "Kod", "Ilość (Plan/Zreal)", "Status", "Lok. Źródłowa"])
        self.table_items.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
        self.table_items.setStyleSheet("QTableWidget { border: 1px solid #dcdcdc; }")
        details_inner_layout.addWidget(self.table_items)

        actions_layout = QHBoxLayout()

        self.btn_edit = QPushButton("Edytuj")
        self.btn_edit.setStyleSheet("background-color: #f39c12; color: white; font-weight: bold;")
        self.btn_edit.clicked.connect(self.open_edit_dialog)

        self.btn_start = QPushButton("Start")
        self.btn_start.setStyleSheet("background-color: #3498db; color: white; font-weight: bold;")
        self.btn_start.clicked.connect(lambda: self.change_status('start'))

        self.btn_complete = QPushButton("Zakończ")
        self.btn_complete.setStyleSheet("background-color: #2ecc71; color: white; font-weight: bold;")
        self.btn_complete.clicked.connect(lambda: self.change_status('complete'))

        self.btn_cancel = QPushButton("Anuluj")
        self.btn_cancel.setStyleSheet("background-color: #e74c3c; color: white; font-weight: bold;")
        self.btn_cancel.clicked.connect(lambda: self.change_status('cancel'))

        actions_layout.addWidget(self.btn_edit)
        actions_layout.addWidget(self.btn_start)
        actions_layout.addWidget(self.btn_complete)
        actions_layout.addWidget(self.btn_cancel)
        details_inner_layout.addLayout(actions_layout)

        self.details_group.setLayout(details_inner_layout)
        right_layout.addWidget(self.details_group)

        main_layout.addLayout(right_layout, 60)

        self.load_orders()
        self.load_stats()

    def _show_order_context_menu(self, pos):
        if not self.table_orders.selectionModel().selectedRows():
            return

        menu = QMenu()
        gen_qr_action = menu.addAction("Generuj kod QR")
        action = menu.exec(self.table_orders.mapToGlobal(pos))

        if action == gen_qr_action:
            self._open_qr_generator()

    def _open_qr_generator(self):
        row = self.table_orders.currentRow()
        if row < 0:
            return

        id_item = self.table_orders.item(row, 0)
        num_item = self.table_orders.item(row, 1)

        if not id_item or not num_item:
            return

        try:
            order_id = int(id_item.text())
            order_num = num_item.text()

            from qr_manager import QRManagerWindow

            self.qr_window = QRManagerWindow()
            self.qr_window.set_form_data(
                data=order_num,
                qr_type="CUSTOM",
                entity_type="order",
                entity_id=order_id
            )
            self.qr_window.show()

        except Exception as e:
            QMessageBox.critical(self, "Błąd", f"Nie udało się otworzyć generatora QR: {str(e)}")

    def load_orders(self):
        success, data = self.api.get_all_orders()
        if not success:
            QMessageBox.warning(self, "Błąd", f"Nie udało się pobrać zleceń:\n{data}")
            return

        filter_val = self.combo_status_filter.currentData()

        filtered_data = []
        for order in data:
            status = order.get('status')

            if filter_val == "ALL":
                filtered_data.append(order)
            elif filter_val == "ACTIVE":
                if status not in ['COMPLETED', 'CANCELLED', 'FAILED']:
                    filtered_data.append(order)
            else:
                if status == filter_val:
                    filtered_data.append(order)

        self.table_orders.setRowCount(0)
        for i, order in enumerate(filtered_data):
            self.table_orders.insertRow(i)

            raw_date = order.get('createdAt')
            display_date = raw_date[:10] if raw_date else ""

            self.table_orders.setItem(i, 0, QTableWidgetItem(str(order.get('id'))))
            self.table_orders.setItem(i, 1, QTableWidgetItem(str(order.get('orderNumber'))))

            type_en = str(order.get('type'))
            self.table_orders.setItem(i, 2, QTableWidgetItem(tr(type_en)))

            status = order.get('status')
            item_status = QTableWidgetItem(tr(status))

            if status == 'COMPLETED':
                item_status.setForeground(Qt.GlobalColor.darkGreen)
            elif status == 'CANCELLED':
                item_status.setForeground(Qt.GlobalColor.red)
            elif status == 'IN_PROGRESS':
                item_status.setForeground(Qt.GlobalColor.blue)

            self.table_orders.setItem(i, 3, item_status)

            prio_en = str(order.get('priority'))
            self.table_orders.setItem(i, 4, QTableWidgetItem(tr(prio_en)))

            self.table_orders.setItem(i, 5, QTableWidgetItem(display_date))

            self.table_orders.item(i, 0).setData(Qt.ItemDataRole.UserRole, order.get('id'))

    def load_stats(self):
        success, stats = self.api.get_statistics()
        if success:
            text_parts = []
            for stat in stats:
                status_pl = tr(stat.get('status'))
                text_parts.append(f"{status_pl}: {stat.get('count')}")
            self.lbl_stats.setText(" | ".join(text_parts))
        else:
            self.lbl_stats.setText("Status: Niedostępny")

    def on_order_selected(self):
        row = self.table_orders.currentRow()
        if row < 0:
            return

        order_id = self.table_orders.item(row, 0).data(Qt.ItemDataRole.UserRole)
        self.current_order_id = order_id

        self.setCursor(Qt.CursorShape.WaitCursor)
        success, order = self.api.get_order_details(order_id)
        self.setCursor(Qt.CursorShape.ArrowCursor)

        if not success:
            QMessageBox.warning(self, "Błąd", f"Błąd pobierania szczegółów:\n{order}")
            return

        self.current_order_data = order
        self.display_details(order)

    def display_details(self, order):
        self.details_group.setVisible(True)

        self.txt_order_num.setText(str(order.get('orderNumber')))
        self.txt_status.setText(tr(order.get('status')))
        self.txt_type.setText(tr(order.get('type')))
        self.txt_prio.setText(tr(order.get('priority')))

        assigned_name = order.get('assignedToFullName') or order.get('assignedToUsername') or "Brak"
        self.txt_assigned.setText(assigned_name)

        src = order.get('sourceLocationName') or order.get('sourceLocationCode') or "-"
        dest = order.get('destinationLocationName') or order.get('destinationLocationCode') or "-"
        self.txt_source.setText(src)
        self.txt_dest.setText(dest)

        exp = (order.get('expectedDate') or "")[:10]
        crt = (order.get('createdAt') or "")[:10]

        self.txt_dates.setText(f"Oczek: {exp} / Utw: {crt}")

        self.txt_desc.setText(order.get('description') or "-")

        status = order.get('status')
        self.btn_start.setEnabled(status == 'CREATED')
        self.btn_complete.setEnabled(status == 'IN_PROGRESS')
        self.btn_cancel.setEnabled(status not in ['COMPLETED', 'CANCELLED'])
        self.btn_edit.setEnabled(status not in ['COMPLETED', 'CANCELLED'])

        items = order.get('orderItems') or []
        self.table_items.setRowCount(0)
        for i, item in enumerate(items):
            self.table_items.insertRow(i)

            prod_name = item.get('productName') or "N/A"
            prod_sku = item.get('productSku') or "-"

            qty_req = item.get('requestedQuantity', 0)
            qty_comp = item.get('completedQuantity', 0)
            qty_text = f"{qty_req} / {qty_comp}"

            qty_item = QTableWidgetItem(qty_text)
            if qty_comp >= qty_req:
                qty_item.setForeground(Qt.GlobalColor.darkGreen)

            loc_src = item.get('sourceLocationCode') or "-"

            self.table_items.setItem(i, 0, QTableWidgetItem(prod_name))
            self.table_items.setItem(i, 1, QTableWidgetItem(prod_sku))
            self.table_items.setItem(i, 2, qty_item)
            self.table_items.setItem(i, 3, QTableWidgetItem(tr(item.get('status'))))
            self.table_items.setItem(i, 4, QTableWidgetItem(loc_src))

    def open_edit_dialog(self):
        if not self.current_order_id or not self.current_order_data:
            return

        dialog = EditOrderDialog(self.api, self.current_order_data, self)
        if dialog.exec():
            payload = dialog.get_payload()
            self.setCursor(Qt.CursorShape.WaitCursor)
            ok, res = self.api.update_order(self.current_order_id, payload)
            self.setCursor(Qt.CursorShape.ArrowCursor)
            if ok:
                QMessageBox.information(self, "Sukces", "Zlecenie zaktualizowane.")
                self.load_orders()
                self.on_order_selected()
                self.load_stats()
            else:
                QMessageBox.warning(self, "Błąd", f"Aktualizacja nieudana:\n{res}")

    def change_status(self, action):
        if not self.current_order_id:
            return

        reason = None
        if action == 'cancel':
            text, ok = QInputDialog.getText(self, "Anuluj Zlecenie", "Podaj powód anulowania:")
            if ok and text:
                reason = text
            else:
                return

        action_pl = action.upper()
        if action == 'start': action_pl = "ROZPOCZĘCIE"
        if action == 'complete': action_pl = "ZAKOŃCZENIE"
        if action == 'cancel': action_pl = "ANULOWANIE"

        confirm = QMessageBox.question(self, "Potwierdzenie", f"Czy na pewno wykonać akcję: {action_pl}?",
                                       QMessageBox.StandardButton.Yes | QMessageBox.StandardButton.No)

        if confirm == QMessageBox.StandardButton.Yes:
            success, msg = self.api.update_order_status(self.current_order_id, action, reason)
            if success:
                QMessageBox.information(self, "Sukces", "Status został zaktualizowany.")
                self.load_orders()
                self.on_order_selected()
                self.load_stats()
            else:
                QMessageBox.warning(self, "Błąd", msg)

    def open_create_dialog(self):
        dialog = CreateOrderDialog(self.api, self)
        if dialog.exec():
            order_data = dialog.get_order_data()
            items_data = dialog.get_items_data()

            self.setCursor(Qt.CursorShape.WaitCursor)

            success, result_order = self.api.create_order(order_data)

            if success:
                order_id = result_order.get('id')
                order_number = result_order.get('orderNumber')
                errors = []

                for item in items_data:
                    payload = {
                        "productId": item['productId'],
                        "requestedQuantity": item['requestedQuantity'],
                        "requiresExactInventory": bool(item.get('requiresExactInventory', True)),
                        "notes": item.get('notes') or "",
                        "sourceLocationId": (item.get('sourceLocationId') or order_data['sourceLocationId'])
                        if bool(item.get('requiresExactInventory', True)) else None,
                        "destinationLocationId": order_data['destinationLocationId']
                    }

                    ok, res = self.api.add_order_item(order_id, payload)
                    if not ok:
                        errors.append(f"Produkt {item['productName']}: {res}")

                self.setCursor(Qt.CursorShape.ArrowCursor)

                if not errors:
                    QMessageBox.information(self, "Sukces",
                                            f"Utworzono zlecenie {order_number} z {len(items_data)} pozycjami.")
                else:
                    QMessageBox.warning(self, "Częściowy Sukces",
                                        f"Zlecenie {order_number} utworzone, ale wystąpiły błędy przy dodawaniu produktów:\n" + "\n".join(
                                            errors))

                self.load_orders()
                self.load_stats()
            else:
                self.setCursor(Qt.CursorShape.ArrowCursor)
                QMessageBox.warning(self, "Błąd", f"Tworzenie zlecenia nieudane:\n{result_order}")
