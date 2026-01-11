import sys
from typing import Any, Dict, List, Optional

from PyQt6.QtCore import Qt, QPoint
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QLineEdit, QComboBox, QTableWidget, QTableWidgetItem,
    QSpinBox, QDialog, QFormLayout, QMessageBox, QCheckBox, QTreeWidget,
    QTreeWidgetItem, QSplitter, QGroupBox, QHeaderView, QMenu
)

from config import ConfigManager
from categories_api import CategoriesApi


class CategoryFormDialog(QDialog):
    def __init__(self, categories: List[Dict[str, Any]], item: Optional[Dict[str, Any]] = None,
                 preselected_parent_id: Optional[int] = None, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Edycja Kategorii" if item else "Nowa Kategoria")
        self.resize(800, 500)
        self.item = item or {}
        self.categories = categories
        self.preselected_parent_id = preselected_parent_id

        main_layout = QHBoxLayout(self)

        left_group = QGroupBox("Dane Podstawowe")
        left_form = QFormLayout()

        self.edt_code = QLineEdit(self.item.get("code", ""))
        self.edt_name = QLineEdit(self.item.get("name", ""))
        self.edt_desc = QLineEdit(self.item.get("description", ""))

        self.cmb_parent = QComboBox()
        self._fill_parents()

        self.chk_remove_parent = QCheckBox("Usuń przypisanie rodzica")

        self.chk_active = QCheckBox("Aktywna")
        self.chk_active.setChecked(bool(self.item.get("active", True)))

        self.spn_order = QSpinBox()
        self.spn_order.setRange(0, 100000)
        self.spn_order.setValue(int(self.item.get("sortOrder") or 0))

        self.edt_icon = QLineEdit(self.item.get("icon", ""))
        self.edt_icon.setPlaceholderText("np. fa-box")

        self.edt_color = QLineEdit(self.item.get("color", ""))
        self.edt_color.setPlaceholderText("np. #FF0000")

        left_form.addRow("Kod:", self.edt_code)
        left_form.addRow("Nazwa:", self.edt_name)
        left_form.addRow("Opis:", self.edt_desc)
        left_form.addRow("Rodzic:", self.cmb_parent)
        if self.item:
            left_form.addRow("", self.chk_remove_parent)
        left_form.addRow("", self.chk_active)
        left_form.addRow("Kolejność:", self.spn_order)
        left_form.addRow("Ikona:", self.edt_icon)
        left_form.addRow("Kolor:", self.edt_color)

        left_group.setLayout(left_form)
        main_layout.addWidget(left_group)

        right_layout = QVBoxLayout()

        store_group = QGroupBox("Warunki Magazynowania")
        store_form = QFormLayout()

        self.chk_special = QCheckBox("Wymaga specjalnej obsługi")
        self.chk_special.setChecked(bool(self.item.get("requiresSpecialHandling", False)))

        self.spn_tmin = QSpinBox();
        self.spn_tmin.setRange(-100, 100);
        self.spn_tmin.setSuffix(" °C")
        self.spn_tmin.setValue(int(self.item.get("storageTemperatureMin") or 0))

        self.spn_tmax = QSpinBox();
        self.spn_tmax.setRange(-100, 100);
        self.spn_tmax.setSuffix(" °C")
        self.spn_tmax.setValue(int(self.item.get("storageTemperatureMax") or 0))

        self.spn_hmin = QSpinBox();
        self.spn_hmin.setRange(0, 100);
        self.spn_hmin.setSuffix(" %")
        self.spn_hmin.setValue(int(self.item.get("storageHumidityMin") or 0))

        self.spn_hmax = QSpinBox();
        self.spn_hmax.setRange(0, 100);
        self.spn_hmax.setSuffix(" %")
        self.spn_hmax.setValue(int(self.item.get("storageHumidityMax") or 0))

        store_form.addRow("", self.chk_special)
        store_form.addRow("Temp. min:", self.spn_tmin)
        store_form.addRow("Temp. max:", self.spn_tmax)
        store_form.addRow("Wilgotność min:", self.spn_hmin)
        store_form.addRow("Wilgotność max:", self.spn_hmax)
        store_group.setLayout(store_form)

        right_layout.addWidget(store_group)
        right_layout.addStretch()

        btns = QHBoxLayout()
        self.btn_cancel = QPushButton("Anuluj")
        self.btn_ok = QPushButton("Zapisz")
        self.btn_ok.setDefault(True)
        self.btn_ok.setStyleSheet("font-weight: bold; padding: 6px;")

        btns.addWidget(self.btn_cancel)
        btns.addWidget(self.btn_ok)
        right_layout.addLayout(btns)

        main_layout.addLayout(right_layout)

        self.btn_ok.clicked.connect(self.accept)
        self.btn_cancel.clicked.connect(self.reject)

    def _fill_parents(self):
        self.cmb_parent.addItem("-- Kategoria główna --", None)
        sorted_cats = sorted(self.categories, key=lambda x: x.get("name", "").lower())

        my_id = self.item.get("id")
        for c in sorted_cats:
            if my_id and c.get("id") == my_id:
                continue
            name = f"{c.get('name')} ({c.get('code')})"
            self.cmb_parent.addItem(name, c.get("id"))

        target_pid = None

        parent = self.item.get("parent") or {}
        item_pid = parent.get("id") if isinstance(parent, dict) else self.item.get("parentId")

        if item_pid:
            target_pid = item_pid
        elif self.preselected_parent_id:
            target_pid = self.preselected_parent_id

        if target_pid:
            idx = self.cmb_parent.findData(target_pid)
            if idx >= 0:
                self.cmb_parent.setCurrentIndex(idx)

    def build_create_payload(self) -> Dict[str, Any]:
        payload = {
            "code": self.edt_code.text().strip(),
            "name": self.edt_name.text().strip(),
            "description": self.edt_desc.text().strip(),
            "active": bool(self.chk_active.isChecked()),
            "sortOrder": int(self.spn_order.value()),
            "icon": self.edt_icon.text().strip(),
            "color": self.edt_color.text().strip(),
            "requiresSpecialHandling": bool(self.chk_special.isChecked()),
            "storageTemperatureMin": int(self.spn_tmin.value()),
            "storageTemperatureMax": int(self.spn_tmax.value()),
            "storageHumidityMin": int(self.spn_hmin.value()),
            "storageHumidityMax": int(self.spn_hmax.value()),
        }
        pid = self.cmb_parent.currentData()
        if pid:
            payload["parentId"] = int(pid)
        return payload

    def build_update_payload(self) -> Dict[str, Any]:
        payload = self.build_create_payload()

        if self.chk_remove_parent.isChecked():
            payload["removeParent"] = True
            payload["parentId"] = None

        return payload


class CategoriesManagerWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("QRWare - Zarządzanie Kategoriami")
        self.resize(1100, 700)
        self.cfg = ConfigManager()
        self.api = CategoriesApi(self.cfg)

        central = QWidget();
        self.setCentralWidget(central)
        root = QVBoxLayout();
        central.setLayout(root)
        root.setContentsMargins(10, 10, 10, 10)
        root.setSpacing(10)

        toolbar = QHBoxLayout()

        self.edt_search = QLineEdit()
        self.edt_search.setPlaceholderText("Szukaj kategorii...")
        self.edt_search.setFixedWidth(250)
        btn_search = QPushButton("Szukaj")
        btn_search.clicked.connect(self._search)

        btn_refresh = QPushButton("Odśwież")
        btn_refresh.clicked.connect(self._load)

        self.cmb_filter = QComboBox()
        self.cmb_filter.addItems(["Wszystkie", "Tylko Aktywne"])
        self.cmb_filter.currentIndexChanged.connect(self._load)

        toolbar.addWidget(self.edt_search)
        toolbar.addWidget(btn_search)
        toolbar.addWidget(self.cmb_filter)
        toolbar.addWidget(btn_refresh)
        toolbar.addStretch()

        btn_add = QPushButton(" + Nowa Kategoria")
        btn_add.clicked.connect(lambda: self._add(None))
        btn_edit = QPushButton("Edytuj")
        btn_edit.clicked.connect(self._edit)
        btn_delete = QPushButton("Usuń")
        btn_delete.setStyleSheet("color: red;")
        btn_delete.clicked.connect(self._delete)

        toolbar.addWidget(btn_add)
        toolbar.addWidget(btn_edit)
        toolbar.addWidget(btn_delete)

        root.addLayout(toolbar)

        splitter = QSplitter(Qt.Orientation.Horizontal)
        splitter.setHandleWidth(5)

        tree_container = QWidget()
        tree_layout = QVBoxLayout(tree_container)
        tree_layout.setContentsMargins(0, 0, 0, 0)
        tree_layout.addWidget(QLabel("<b>Struktura Drzewa</b>"))

        self.tree = QTreeWidget()
        self.tree.setHeaderHidden(True)
        self.tree.itemClicked.connect(self._tree_item_clicked)
        self.tree.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu)
        self.tree.customContextMenuRequested.connect(self._open_context_menu)

        tree_layout.addWidget(self.tree)

        splitter.addWidget(tree_container)

        table_container = QWidget()
        table_layout = QVBoxLayout(table_container)
        table_layout.setContentsMargins(0, 0, 0, 0)
        table_layout.addWidget(QLabel("<b>Lista Kategorii</b>"))

        self.tbl = QTableWidget(0, 8)
        self.tbl.setHorizontalHeaderLabels(["ID", "Kod", "Nazwa", "Rodzic", "Aktywna", "Sort", "Kolor", "Specjalne"])
        self.tbl.setAlternatingRowColors(True)
        self.tbl.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.tbl.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl.horizontalHeader().setStretchLastSection(True)
        self.tbl.setColumnWidth(0, 50)
        self.tbl.setColumnWidth(4, 70)

        table_layout.addWidget(self.tbl)
        splitter.addWidget(table_container)

        splitter.setStretchFactor(0, 3)
        splitter.setStretchFactor(1, 7)

        root.addWidget(splitter)

        self._load()

    def _load(self):
        only_active = self.cmb_filter.currentIndex() == 1
        ok, msg, cats = self.api.list(only_active=only_active)
        if not ok:
            QMessageBox.warning(self, "Błąd", msg)
            return

        self._populate_tree(cats)
        self._populate_table(cats)

    def _get_parent_id(self, cat: Dict[str, Any]) -> Optional[int]:
        parent = cat.get("parent")
        if isinstance(parent, dict):
            return parent.get("id")
        return cat.get("parentId")

    def _populate_tree(self, items: List[Dict[str, Any]]):
        self.tree.clear()
        nodes: Dict[int, QTreeWidgetItem] = {}

        for c in items:
            name = c.get("name") or c.get("code") or f"ID {c.get('id')}"
            node = QTreeWidgetItem([name, str(c.get("id", ""))])
            nodes[c.get("id")] = node

        roots: List[QTreeWidgetItem] = []
        for c in items:
            node = nodes.get(c.get("id"))
            pid = self._get_parent_id(c)

            if pid and pid in nodes:
                nodes[pid].addChild(node)
            else:
                roots.append(node)

        for r in roots:
            self.tree.addTopLevelItem(r)

        self.tree.expandAll()

    def _populate_table(self, items: List[Dict[str, Any]]):
        self.tbl.setRowCount(len(items))
        for r, c in enumerate(items):
            def setc(col: int, text: str):
                item = QTableWidgetItem(text)
                self.tbl.setItem(r, col, item)

            pid = self._get_parent_id(c)
            parent_name = ""
            if pid:
                parent_obj = next((x for x in items if x.get("id") == pid), None)
                if parent_obj:
                    parent_name = parent_obj.get("name", "")

            setc(0, str(c.get("id", "")))
            setc(1, c.get("code") or "")
            setc(2, c.get("name") or "")
            setc(3, parent_name)
            setc(4, "TAK" if c.get("active") else "NIE")
            setc(5, str(c.get("sortOrder", "") or "0"))
            setc(6, c.get("color") or "")
            setc(7, "TAK" if c.get("requiresSpecialHandling") else "NIE")

        self.tbl.resizeColumnsToContents()

    def _selected_id_from_table(self) -> Optional[int]:
        rows = self.tbl.selectionModel().selectedRows()
        if not rows:
            return None
        try:
            return int(self.tbl.item(rows[0].row(), 0).text())
        except Exception:
            return None

    def _selected_id_from_tree(self) -> Optional[int]:
        item = self.tree.currentItem()
        if not item:
            return None
        try:
            return int(item.text(1))
        except:
            return None

    def _get_active_selection_id(self) -> Optional[int]:
        if self.tbl.hasFocus():
            return self._selected_id_from_table()
        if self.tree.hasFocus():
            return self._selected_id_from_tree()
        return self._selected_id_from_table() or self._selected_id_from_tree()

    def _pick_category_by_id(self, cat_id: int) -> Optional[Dict[str, Any]]:
        ok, _, cats = self.api.list(only_active=False)
        if not ok: return None
        return next((c for c in cats if c.get("id") == cat_id), None)

    def _open_context_menu(self, position: QPoint):
        item = self.tree.itemAt(position)
        menu = QMenu()

        if item:
            action_add_sub = menu.addAction("Dodaj podkategorię")
            action_edit = menu.addAction("Edytuj")
            action_delete = menu.addAction("Usuń")

            action = menu.exec(self.tree.viewport().mapToGlobal(position))

            if action == action_add_sub:
                try:
                    parent_id = int(item.text(1))
                    self._add(parent_id)
                except:
                    pass
            elif action == action_edit:
                self._edit()
            elif action == action_delete:
                self._delete()
        else:
            action_add_root = menu.addAction("Dodaj kategorię główną")
            action = menu.exec(self.tree.viewport().mapToGlobal(position))

            if action == action_add_root:
                self._add(None)

    def _add(self, parent_id: Optional[int] = None):
        if isinstance(parent_id, bool):
            parent_id = None

        ok, msg, cats = self.api.list(only_active=True)
        dlg = CategoryFormDialog(categories=cats if ok else [], preselected_parent_id=parent_id, parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_create_payload()
            if not payload.get("code") or not payload.get("name"):
                QMessageBox.warning(self, "Walidacja", "Kod i Nazwa są wymagane.")
                return

            ok, msg, _ = self.api.create(payload)
            if not ok:
                QMessageBox.critical(self, "Błąd Tworzenia", msg)
            else:
                self._load()

    def _edit(self):
        cid = self._get_active_selection_id()
        if cid is None:
            QMessageBox.information(self, "Edycja", "Wybierz kategorię (z drzewa lub tabeli).")
            return

        base = self._pick_category_by_id(cid)
        if not base:
            QMessageBox.warning(self, "Błąd", "Nie znaleziono danych kategorii.")
            return

        ok, msg, cats = self.api.list(only_active=False)
        dlg = CategoryFormDialog(categories=cats if ok else [], item=base, parent=self)

        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_update_payload()
            ok, msg, _ = self.api.update(cid, payload)
            if not ok:
                QMessageBox.critical(self, "Błąd Aktualizacji", msg)
            else:
                self._load()

    def _delete(self):
        cid = self._get_active_selection_id()
        if cid is None:
            QMessageBox.information(self, "Usuwanie", "Wybierz kategorię.")
            return

        if QMessageBox.question(self, "Potwierdzenie",
                                "Czy na pewno usunąć kategorię?") != QMessageBox.StandardButton.Yes:
            return

        ok, msg = self.api.delete(cid)
        if not ok:
            QMessageBox.critical(self, "Błąd Usuwania", msg)
        else:
            self._load()

    def _search(self):
        q = self.edt_search.text().strip()
        if not q:
            self._load();
            return
        ok, msg, items = self.api.search(q)
        if not ok:
            QMessageBox.warning(self, "Błąd", msg)
            return
        self._populate_tree(items)
        self._populate_table(items)

    def _tree_item_clicked(self, item: QTreeWidgetItem, column: int):
        try:
            cid = int(item.text(1))
            for row in range(self.tbl.rowCount()):
                if self.tbl.item(row, 0).text() == str(cid):
                    self.tbl.selectRow(row)
                    self.tbl.scrollToItem(self.tbl.item(row, 0))
                    break
        except:
            pass


if __name__ == "__main__":
    from theme import apply_modern_style

    app = QApplication(sys.argv)
    apply_modern_style(app, dark=False)
    w = CategoriesManagerWindow()
    w.show()
    sys.exit(app.exec())