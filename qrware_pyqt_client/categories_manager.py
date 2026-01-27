import sys
from typing import Any, Dict, List, Optional

from PyQt6.QtCore import Qt, QPoint
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QLineEdit, QComboBox, QTableWidget, QTableWidgetItem,
    QSpinBox, QDialog, QFormLayout, QMessageBox, QCheckBox, QTreeWidget,
    QTreeWidgetItem, QSplitter, QGroupBox, QHeaderView, QMenu, QFrame, QGridLayout
)

from config import ConfigManager
from categories_api import CategoriesApi


class CategoryFormDialog(QDialog):
    def __init__(self, categories: List[Dict[str, Any]], item: Optional[Dict[str, Any]] = None,
                 preselected_parent_id: Optional[int] = None, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Edycja Kategorii" if item else "Nowa Kategoria")
        self.resize(850, 600)
        self.item = item or {}
        self.categories = categories
        self.preselected_parent_id = preselected_parent_id

        main_layout = QVBoxLayout(self)
        main_layout.setContentsMargins(20, 20, 20, 20)
        main_layout.setSpacing(15)

        lbl_title = QLabel("Edycja Kategorii" if item else "Nowa Kategoria")
        lbl_title.setStyleSheet("font-size: 18px; font-weight: bold; color: #2c3e50; margin-bottom: 10px;")
        main_layout.addWidget(lbl_title)

        content_layout = QHBoxLayout()
        content_layout.setSpacing(20)

        left_group = QGroupBox("Dane Podstawowe")
        left_form = QFormLayout()
        left_form.setSpacing(10)

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
        content_layout.addWidget(left_group, 1)

        right_layout = QVBoxLayout()
        store_group = QGroupBox("Warunki Magazynowania")
        store_form = QFormLayout()
        store_form.setSpacing(10)

        self.chk_special = QCheckBox("Wymaga specjalnej obsługi")
        self.chk_special.setChecked(bool(self.item.get("requiresSpecialHandling", False)))

        self.spn_tmin = QSpinBox()
        self.spn_tmin.setRange(-100, 100)
        self.spn_tmin.setSuffix(" °C")
        self.spn_tmin.setValue(int(self.item.get("storageTemperatureMin") or 0))

        self.spn_tmax = QSpinBox()
        self.spn_tmax.setRange(-100, 100)
        self.spn_tmax.setSuffix(" °C")
        self.spn_tmax.setValue(int(self.item.get("storageTemperatureMax") or 0))

        self.spn_hmin = QSpinBox()
        self.spn_hmin.setRange(0, 100)
        self.spn_hmin.setSuffix(" %")
        self.spn_hmin.setValue(int(self.item.get("storageHumidityMin") or 0))

        self.spn_hmax = QSpinBox()
        self.spn_hmax.setRange(0, 100)
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

        content_layout.addLayout(right_layout, 1)
        main_layout.addLayout(content_layout)

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
        self.resize(1200, 750)
        self.cfg = ConfigManager()
        self.api = CategoriesApi(self.cfg)

        central = QWidget()
        self.setCentralWidget(central)
        root = QVBoxLayout(central)
        root.setContentsMargins(20, 20, 20, 20)
        root.setSpacing(15)

        header = QHBoxLayout()
        title_layout = QVBoxLayout()
        lbl_title = QLabel("Zarządzanie Kategoriami")
        lbl_title.setStyleSheet("font-size: 24px; font-weight: bold; color: #2c3e50;")
        lbl_subtitle = QLabel("Definiuj strukturę drzewiastą asortymentu")
        lbl_subtitle.setStyleSheet("font-size: 14px; color: #7f8c8d;")
        title_layout.addWidget(lbl_title)
        title_layout.addWidget(lbl_subtitle)
        header.addLayout(title_layout)
        header.addStretch()

        root.addLayout(header)

        toolbar = QHBoxLayout()
        toolbar.setSpacing(10)

        self.edt_search = QLineEdit()
        self.edt_search.setPlaceholderText("Szukaj kategorii...")
        self.edt_search.setMinimumWidth(250)
        toolbar.addWidget(self.edt_search)

        btn_search = QPushButton("Szukaj")
        btn_search.clicked.connect(self._search)
        toolbar.addWidget(btn_search)

        self.chk_only_active = QCheckBox("Tylko aktywne")
        self.chk_only_active.setChecked(True)
        self.chk_only_active.stateChanged.connect(self._load)
        toolbar.addWidget(self.chk_only_active)

        toolbar.addStretch()

        btn_refresh = QPushButton("Odśwież")
        btn_refresh.clicked.connect(self._load)
        toolbar.addWidget(btn_refresh)
        root.addLayout(toolbar)

        splitter = QSplitter(Qt.Orientation.Horizontal)
        splitter.setHandleWidth(10)
        splitter.setStyleSheet("QSplitter::handle { background-color: #f0f0f0; }")

        tree_container = QWidget()
        tree_layout = QVBoxLayout(tree_container)
        tree_layout.setContentsMargins(0, 0, 0, 0)
        tree_lbl = QLabel("Struktura Drzewa")
        tree_lbl.setStyleSheet("font-weight: bold; color: #555; margin-bottom: 5px;")
        tree_layout.addWidget(tree_lbl)

        self.tree = QTreeWidget()
        self.tree.setHeaderHidden(True)
        self.tree.itemClicked.connect(self._tree_item_clicked)
        self.tree.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu)
        self.tree.customContextMenuRequested.connect(self._open_context_menu)
        self.tree.setStyleSheet("QTreeWidget { border: 1px solid #dcdcdc; }")
        tree_layout.addWidget(self.tree)
        splitter.addWidget(tree_container)

        right_container = QWidget()
        right_layout = QVBoxLayout(right_container)
        right_layout.setContentsMargins(0, 0, 0, 0)
        right_layout.setSpacing(10)

        tbl_lbl = QLabel("Lista Kategorii")
        tbl_lbl.setStyleSheet("font-weight: bold; color: #555; margin-bottom: 5px;")
        right_layout.addWidget(tbl_lbl)

        self.tbl = QTableWidget(0, 8)
        self.tbl.setHorizontalHeaderLabels(["ID", "Kod", "Nazwa", "Rodzic", "Aktywna", "Sort", "Kolor", "Specjalne"])
        self.tbl.setAlternatingRowColors(True)
        self.tbl.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.tbl.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl.horizontalHeader().setStretchLastSection(True)
        self.tbl.setStyleSheet("QTableWidget { border: 1px solid #dcdcdc; }")
        self.tbl.itemSelectionChanged.connect(self._on_table_selected)
        right_layout.addWidget(self.tbl, 1)

        lbl_details_title = QLabel("Szczegóły kategorii")
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
        self.txt_parent = mk_value()
        self.txt_sort = mk_value()
        self.txt_color = mk_value()
        self.txt_icon = mk_value()
        self.txt_special = mk_value()
        self.txt_temp = mk_value()
        self.txt_hum = mk_value()
        self.txt_desc = mk_value()

        header_grid.addWidget(self.txt_id, 1, 0, 1, 2)
        header_grid.addWidget(self.txt_code, 1, 2, 1, 2)

        header_grid.addWidget(self.txt_parent, 2, 0, 1, 2)
        header_grid.addWidget(self.txt_sort, 2, 2, 1, 2)

        header_grid.addWidget(self.txt_color, 3, 0, 1, 2)
        header_grid.addWidget(self.txt_icon, 3, 2, 1, 2)

        header_grid.addWidget(self.txt_temp, 4, 0, 1, 2)
        header_grid.addWidget(self.txt_hum, 4, 2, 1, 2)

        header_grid.addWidget(self.txt_special, 5, 0, 1, 2)

        header_grid.addWidget(self.txt_desc, 6, 0, 1, 4)

        details_outer.addWidget(header_widget)

        sep2 = QFrame()
        sep2.setFrameShape(QFrame.Shape.HLine)
        sep2.setStyleSheet("color: #e0e0e0;")
        details_outer.addWidget(sep2)

        right_layout.addWidget(self.details_group)
        
        splitter.addWidget(right_container)

        splitter.setStretchFactor(0, 3)
        splitter.setStretchFactor(1, 7)
        root.addWidget(splitter, 1)

        actions = QHBoxLayout()
        btn_add = QPushButton("Dodaj Kategorię")
        btn_add.setStyleSheet("background-color: #2ecc71; color: white; font-weight: bold; padding: 8px 16px;")
        btn_add.clicked.connect(lambda: self._add(None))
        actions.addWidget(btn_add)

        btn_edit = QPushButton("Edytuj")
        btn_edit.clicked.connect(self._edit)
        actions.addWidget(btn_edit)

        actions.addStretch()

        btn_delete = QPushButton("Usuń")
        btn_delete.setStyleSheet("background-color: #e74c3c; color: white;")
        btn_delete.clicked.connect(self._delete)
        actions.addWidget(btn_delete)
        root.addLayout(actions)

        self._load()

    def _save_server(self):
        self.cfg.base_url = self.edt_server.text().strip()
        QMessageBox.information(self, "Zapisano", "Adres serwera zapisany.")

    def _load(self):
        only_active = self.chk_only_active.isChecked()
        ok, msg, cats = self.api.list(only_active=only_active)
        if not ok:
            QMessageBox.warning(self, "Błąd", msg)
            return
        self._current_items = cats or []
        self._populate_tree(cats)
        self._populate_table(cats)
        self._clear_details()

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

    def _clear_details(self):
        for lbl in [self.txt_id, self.txt_code, self.txt_name, self.txt_parent, self.txt_active, self.txt_sort,
                    self.txt_color, self.txt_icon, self.txt_special, self.txt_temp, self.txt_hum, self.txt_desc]:
            lbl.setText("-")

    def _show_details(self, cat: Dict[str, Any]):
        parent = cat.get('parent')
        parent_name = ''
        if isinstance(parent, dict):
            parent_name = parent.get('name') or parent.get('code') or ''
        elif cat.get('parentId'):
            parent_name = f"ID {cat.get('parentId')}"

        self.txt_name.setText(cat.get('name') or "")
        self.txt_active.setText("TAK" if cat.get('active') else "NIE")

        self.txt_id.setText(self._fmt_row("ID", str(cat.get('id', ''))))
        self.txt_code.setText(self._fmt_row("Kod", cat.get('code') or ""))
        self.txt_parent.setText(self._fmt_row("Rodzic", parent_name))
        self.txt_sort.setText(self._fmt_row("Sort", str(cat.get('sortOrder') or '')))
        self.txt_color.setText(self._fmt_row("Kolor", cat.get('color') or ""))
        self.txt_icon.setText(self._fmt_row("Ikona", cat.get('icon') or ""))
        self.txt_special.setText(self._fmt_row("Specjalne", "TAK" if cat.get('requiresSpecialHandling') else "NIE"))
        
        temp_str = f"{cat.get('storageTemperatureMin')} / {cat.get('storageTemperatureMax')}"
        self.txt_temp.setText(self._fmt_row("Temp (min/max)", temp_str))
        
        hum_str = f"{cat.get('storageHumidityMin')} / {cat.get('storageHumidityMax')}"
        self.txt_hum.setText(self._fmt_row("Wilgotność (min/max)", hum_str))
        
        self.txt_desc.setText(self._fmt_row("Opis", cat.get('description') or ""))

    def _on_table_selected(self):
        row = self.tbl.currentRow()
        if row < 0:
            self._clear_details()
            return
        it = self.tbl.item(row, 0)
        if not it:
            self._clear_details()
            return
        try:
            cid = int(it.text())
        except Exception:
            self._clear_details()
            return
        cat = next((c for c in self._current_items if c.get('id') == cid), None)
        if cat:
            self._show_details(cat)

    def _populate_table(self, items: List[Dict[str, Any]]):
        self.tbl.setRowCount(len(items))
        for r, c in enumerate(items):
            def setc(col: int, text: str):
                self.tbl.setItem(r, col, QTableWidgetItem(text))

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
        if not rows: return None
        try:
            return int(self.tbl.item(rows[0].row(), 0).text())
        except:
            return None

    def _selected_id_from_tree(self) -> Optional[int]:
        item = self.tree.currentItem()
        if not item: return None
        try:
            return int(item.text(1))
        except:
            return None

    def _get_active_selection_id(self) -> Optional[int]:
        if self.tbl.hasFocus(): return self._selected_id_from_table()
        if self.tree.hasFocus(): return self._selected_id_from_tree()
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
            if action == action_add_root: self._add(None)

    def _add(self, parent_id: Optional[int] = None):
        if isinstance(parent_id, bool): parent_id = None
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
            QMessageBox.information(self, "Edycja", "Wybierz kategorię.")
            return
        base = self._pick_category_by_id(cid)
        if not base:
            QMessageBox.warning(self, "Błąd", "Nie znaleziono danych.")
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
        if QMessageBox.question(self, "Potwierdzenie", "Usunąć kategorię?") != QMessageBox.StandardButton.Yes:
            return
        ok, msg = self.api.delete(cid)
        if not ok:
            QMessageBox.critical(self, "Błąd Usuwania", msg)
        else:
            self._load()

    def _search(self):
        q = self.edt_search.text().strip()
        if not q:
            self._load()
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

            cat = next((c for c in self._current_items if c.get('id') == cid), None)
            if cat:
                self._show_details(cat)
        except Exception:
            pass


if __name__ == "__main__":
    from theme import apply_modern_style

    app = QApplication(sys.argv)
    apply_modern_style(app, dark=False)
    w = CategoriesManagerWindow()
    w.show()
    sys.exit(app.exec())
