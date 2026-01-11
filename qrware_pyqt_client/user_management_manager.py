import sys
from typing import Any, Dict, List, Optional

from PyQt6.QtCore import Qt
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QPushButton, QLineEdit, QListWidget, QListWidgetItem,
    QTableWidget, QTableWidgetItem, QDialog, QFormLayout, QMessageBox,
    QCheckBox, QTabWidget, QGroupBox, QHeaderView
)

from config import ConfigManager
from user_management_api import UserManagementApi


class UserFormDialog(QDialog):
    def __init__(self, all_roles: List[str], user: Optional[Dict[str, Any]] = None, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Edycja Użytkownika" if user else "Nowy Użytkownik")
        self.resize(750, 600)
        self.user = user or {}

        main_layout = QVBoxLayout(self)

        top_container = QWidget()
        top_layout = QHBoxLayout(top_container)
        top_layout.setContentsMargins(0, 0, 0, 0)

        grp_account = QGroupBox("Dane Konta")
        lay_account = QFormLayout()

        self.edt_username = QLineEdit(self.user.get("username", ""))
        self.edt_email = QLineEdit(self.user.get("email", ""))
        self.edt_password = QLineEdit("")
        self.edt_password.setEchoMode(QLineEdit.EchoMode.Password)
        self.edt_password.setPlaceholderText("Pozostaw puste, aby nie zmieniać")

        lay_account.addRow("Login:", self.edt_username)
        lay_account.addRow("E-mail:", self.edt_email)
        lay_account.addRow("Hasło:", self.edt_password)
        grp_account.setLayout(lay_account)

        grp_personal = QGroupBox("Dane Osobowe i Status")
        lay_personal = QFormLayout()

        self.edt_first = QLineEdit(self.user.get("firstName", ""))
        self.edt_last = QLineEdit(self.user.get("lastName", ""))
        self.edt_phone = QLineEdit(self.user.get("phone", ""))

        status_container = QWidget()
        status_layout = QHBoxLayout(status_container)
        status_layout.setContentsMargins(0, 0, 0, 0)
        self.chk_active = QCheckBox("Aktywny")
        self.chk_active.setChecked(bool(self.user.get("active", True)))
        self.chk_verified = QCheckBox("Zweryfikowany")
        self.chk_verified.setChecked(bool(self.user.get("emailVerified", True)))
        status_layout.addWidget(self.chk_active)
        status_layout.addWidget(self.chk_verified)
        status_layout.addStretch()

        lay_personal.addRow("Imię:", self.edt_first)
        lay_personal.addRow("Nazwisko:", self.edt_last)
        lay_personal.addRow("Telefon:", self.edt_phone)
        lay_personal.addRow("Status:", status_container)
        grp_personal.setLayout(lay_personal)

        top_layout.addWidget(grp_account)
        top_layout.addWidget(grp_personal)
        main_layout.addWidget(top_container)

        grp_roles = QGroupBox("Przypisane Role")
        lay_roles = QVBoxLayout()
        self.list_roles = QListWidget()
        self.list_roles.setStyleSheet("QListWidget::item { padding: 4px; }")

        current_roles = self.user.get("roles", [])
        for role_name in all_roles:
            item = QListWidgetItem(role_name)
            item.setFlags(item.flags() | Qt.ItemFlag.ItemIsUserCheckable)
            if role_name in current_roles:
                item.setCheckState(Qt.CheckState.Checked)
            else:
                item.setCheckState(Qt.CheckState.Unchecked)
            self.list_roles.addItem(item)

        lay_roles.addWidget(self.list_roles)
        grp_roles.setLayout(lay_roles)
        main_layout.addWidget(grp_roles)

        btn_layout = QHBoxLayout()
        btn_layout.addStretch()
        self.btn_cancel = QPushButton("Anuluj")
        self.btn_ok = QPushButton("Zapisz")
        self.btn_ok.setDefault(True)
        self.btn_ok.setStyleSheet("font-weight: bold; padding: 5px 15px;")

        btn_layout.addWidget(self.btn_cancel)
        btn_layout.addWidget(self.btn_ok)
        main_layout.addLayout(btn_layout)

        self.btn_ok.clicked.connect(self.validate_and_save)
        self.btn_cancel.clicked.connect(self.reject)

    def validate_and_save(self):
        username = self.edt_username.text().strip()
        email = self.edt_email.text().strip()
        password = self.edt_password.text()

        if not username:
            QMessageBox.warning(self, "Błąd walidacji", "Pole 'Login' jest wymagane.")
            return

        if not email or "@" not in email or "." not in email:
            QMessageBox.warning(self, "Błąd walidacji", "Podaj poprawny adres e-mail.")
            return

        if not self.user.get("id") and len(password) < 8:
            QMessageBox.warning(self, "Błąd walidacji", "Nowe konto musi mieć hasło (min. 8 znaków).")
            return

        self.accept()

    def get_selected_roles(self) -> List[str]:
        selected = []
        for i in range(self.list_roles.count()):
            item = self.list_roles.item(i)
            if item.checkState() == Qt.CheckState.Checked:
                selected.append(item.text())
        return selected

    def build_create_payload(self) -> Dict[str, Any]:
        return {
            "username": self.edt_username.text().strip(),
            "email": self.edt_email.text().strip(),
            "password": self.edt_password.text(),
            "firstName": self.edt_first.text().strip(),
            "lastName": self.edt_last.text().strip(),
            "phone": self.edt_phone.text().strip(),
            "roles": self.get_selected_roles(),
            "active": bool(self.chk_active.isChecked()),
            "emailVerified": bool(self.chk_verified.isChecked()),
        }

    def build_update_payload(self) -> Dict[str, Any]:
        return {
            "email": self.edt_email.text().strip(),
            "firstName": self.edt_first.text().strip(),
            "lastName": self.edt_last.text().strip(),
            "phone": self.edt_phone.text().strip(),
            "active": bool(self.chk_active.isChecked()),
            "emailVerified": bool(self.chk_verified.isChecked()),
            "roles": self.get_selected_roles(),
        }


class RoleFormDialog(QDialog):
    def __init__(self, all_permissions: List[str], role: Optional[Dict[str, Any]] = None, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Edycja Roli" if role else "Nowa Rola")
        self.resize(500, 600)
        self.role = role or {}

        layout = QVBoxLayout(self)

        grp_info = QGroupBox("Informacje podstawowe")
        form = QFormLayout()
        self.edt_name = QLineEdit(self.role.get("name", ""))
        self.edt_desc = QLineEdit(self.role.get("description", ""))
        self.chk_active = QCheckBox("Rola aktywna")
        self.chk_active.setChecked(bool(self.role.get("active", True)))

        form.addRow("Nazwa roli:", self.edt_name)
        form.addRow("Opis:", self.edt_desc)
        form.addRow("", self.chk_active)
        grp_info.setLayout(form)
        layout.addWidget(grp_info)

        grp_perms = QGroupBox("Uprawnienia")
        l_perms = QVBoxLayout()
        self.list_perms = QListWidget()
        self.list_perms.setStyleSheet("QListWidget::item { padding: 3px; }")

        current_perms = self.role.get("permissions", [])
        for perm_name in all_permissions:
            item = QListWidgetItem(perm_name)
            item.setFlags(item.flags() | Qt.ItemFlag.ItemIsUserCheckable)
            if perm_name in current_perms:
                item.setCheckState(Qt.CheckState.Checked)
            else:
                item.setCheckState(Qt.CheckState.Unchecked)
            self.list_perms.addItem(item)

        l_perms.addWidget(self.list_perms)
        grp_perms.setLayout(l_perms)
        layout.addWidget(grp_perms, 1)

        btns = QHBoxLayout()
        btns.addStretch()
        self.btn_cancel = QPushButton("Anuluj")
        self.btn_ok = QPushButton("Zapisz")
        self.btn_ok.setDefault(True)
        btns.addWidget(self.btn_cancel)
        btns.addWidget(self.btn_ok)
        layout.addLayout(btns)

        self.btn_ok.clicked.connect(self.accept)
        self.btn_cancel.clicked.connect(self.reject)

    def get_selected_permissions(self) -> List[str]:
        selected = []
        for i in range(self.list_perms.count()):
            item = self.list_perms.item(i)
            if item.checkState() == Qt.CheckState.Checked:
                selected.append(item.text())
        return selected

    def build_payload(self) -> Dict[str, Any]:
        return {
            "name": self.edt_name.text().strip(),
            "description": self.edt_desc.text().strip(),
            "active": bool(self.chk_active.isChecked()),
            "permissions": self.get_selected_permissions(),
        }


class PermissionFormDialog(QDialog):
    def __init__(self, perm: Optional[Dict[str, Any]] = None, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Edycja Uprawnienia" if perm else "Nowe Uprawnienie")
        self.resize(450, 350)
        self.perm = perm or {}

        layout = QVBoxLayout(self)
        grp = QGroupBox("Szczegóły")
        form = QFormLayout()

        self.edt_name = QLineEdit(self.perm.get("name", ""))
        self.edt_desc = QLineEdit(self.perm.get("description", ""))
        self.edt_res = QLineEdit(self.perm.get("resource", ""))
        self.edt_res.setPlaceholderText("np. user, role, document")
        self.edt_act = QLineEdit(self.perm.get("action", ""))
        self.edt_act.setPlaceholderText("np. read, write, delete")
        self.chk_active = QCheckBox("Aktywne")
        self.chk_active.setChecked(bool(self.perm.get("active", True)))

        form.addRow("Nazwa (klucz):", self.edt_name)
        form.addRow("Opis:", self.edt_desc)
        form.addRow("Zasób (Resource):", self.edt_res)
        form.addRow("Akcja (Action):", self.edt_act)
        form.addRow("", self.chk_active)
        grp.setLayout(form)
        layout.addWidget(grp)

        btns = QHBoxLayout()
        btns.addStretch()
        self.btn_cancel = QPushButton("Anuluj")
        self.btn_ok = QPushButton("Zapisz")
        self.btn_ok.setDefault(True)
        btns.addWidget(self.btn_cancel)
        btns.addWidget(self.btn_ok)
        layout.addLayout(btns)

        self.btn_ok.clicked.connect(self.accept)
        self.btn_cancel.clicked.connect(self.reject)

    def build_payload(self) -> Dict[str, Any]:
        return {
            "name": self.edt_name.text().strip(),
            "description": self.edt_desc.text().strip(),
            "resource": self.edt_res.text().strip(),
            "action": self.edt_act.text().strip(),
            "active": bool(self.chk_active.isChecked()),
        }


class UserManagementWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("QRWare - Panel Administratora")
        self.resize(1200, 800)
        self.cfg = ConfigManager()
        self.api = UserManagementApi(self.cfg)

        central = QWidget()
        self.setCentralWidget(central)
        root = QVBoxLayout(central)
        root.setContentsMargins(15, 15, 15, 15)

        self.tabs = QTabWidget()
        self.tabs.setStyleSheet("QTabBar::tab { height: 30px; width: 120px; }")
        root.addWidget(self.tabs)

        self._init_users_tab()
        self._init_roles_tab()
        self._init_perms_tab()

        self._users_load()
        self._roles_load()
        self._perms_load()

    def _init_users_tab(self):
        tab = QWidget()
        layout = QVBoxLayout(tab)
        layout.setSpacing(10)

        toolbar = QHBoxLayout()

        self.edt_us_search = QLineEdit()
        self.edt_us_search.setPlaceholderText("Szukaj (login, email, nazwisko)...")
        self.edt_us_search.setFixedWidth(250)

        btn_search = QPushButton("Szukaj")
        btn_search.clicked.connect(self._users_search)

        btn_load = QPushButton("Odśwież")
        btn_load.clicked.connect(self._users_load)

        btn_add = QPushButton(" + Nowy")
        btn_add.clicked.connect(self._users_add)
        btn_edit = QPushButton("Edytuj")
        btn_edit.clicked.connect(self._users_edit)

        btn_lock = QPushButton("Zablokuj")
        btn_lock.clicked.connect(lambda: self._users_lock(True))
        btn_unlock = QPushButton("Odblokuj")
        btn_unlock.clicked.connect(lambda: self._users_lock(False))
        btn_del = QPushButton("Usuń")
        btn_del.setStyleSheet("color: red;")
        btn_del.clicked.connect(self._users_del)

        toolbar.addWidget(self.edt_us_search)
        toolbar.addWidget(btn_search)
        toolbar.addWidget(btn_load)
        toolbar.addStretch()
        toolbar.addWidget(btn_add)
        toolbar.addWidget(btn_edit)
        toolbar.addSpacing(20)
        toolbar.addWidget(btn_lock)
        toolbar.addWidget(btn_unlock)
        toolbar.addWidget(btn_del)

        layout.addLayout(toolbar)

        self.tbl_users = QTableWidget(0, 8)
        headers = ["ID", "Login", "Email", "Imię", "Nazwisko", "Aktywny", "Zweryfikowany", "Role"]
        self.tbl_users.setHorizontalHeaderLabels(headers)
        self.tbl_users.setAlternatingRowColors(True)
        self.tbl_users.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl_users.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.tbl_users.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl_users.horizontalHeader().setStretchLastSection(True)
        self.tbl_users.setColumnWidth(0, 50)
        self.tbl_users.setColumnWidth(5, 70)
        self.tbl_users.setColumnWidth(6, 90)

        layout.addWidget(self.tbl_users)
        self.tabs.addTab(tab, "Użytkownicy")

    def _init_roles_tab(self):
        tab = QWidget()
        layout = QVBoxLayout(tab)
        layout.setSpacing(10)

        toolbar = QHBoxLayout()
        btn_load = QPushButton("Odśwież")
        btn_load.clicked.connect(self._roles_load)

        btn_add = QPushButton(" + Nowa Rola")
        btn_add.clicked.connect(self._roles_add)
        btn_edit = QPushButton("Edytuj")
        btn_edit.clicked.connect(self._roles_edit)
        btn_del = QPushButton("Usuń")
        btn_del.setStyleSheet("color: red;")
        btn_del.clicked.connect(self._roles_del)

        toolbar.addWidget(btn_load)
        toolbar.addStretch()
        toolbar.addWidget(btn_add)
        toolbar.addWidget(btn_edit)
        toolbar.addWidget(btn_del)
        layout.addLayout(toolbar)

        self.tbl_roles = QTableWidget(0, 5)
        headers = ["ID", "Nazwa", "Opis", "Aktywna", "Uprawnienia"]
        self.tbl_roles.setHorizontalHeaderLabels(headers)
        self.tbl_roles.setAlternatingRowColors(True)
        self.tbl_roles.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl_roles.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.tbl_roles.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl_roles.horizontalHeader().setStretchLastSection(True)
        self.tbl_roles.setColumnWidth(0, 50)
        self.tbl_roles.setColumnWidth(3, 70)

        layout.addWidget(self.tbl_roles)
        self.tabs.addTab(tab, "Role")

    def _init_perms_tab(self):
        tab = QWidget()
        layout = QVBoxLayout(tab)
        layout.setSpacing(10)

        toolbar = QHBoxLayout()
        btn_load = QPushButton("Odśwież")
        btn_load.clicked.connect(self._perms_load)

        btn_add = QPushButton(" + Nowe Uprawnienie")
        btn_add.clicked.connect(self._perms_add)
        btn_edit = QPushButton("Edytuj")
        btn_edit.clicked.connect(self._perms_edit)
        btn_del = QPushButton("Usuń")
        btn_del.setStyleSheet("color: red;")
        btn_del.clicked.connect(self._perms_del)

        toolbar.addWidget(btn_load)
        toolbar.addStretch()
        toolbar.addWidget(btn_add)
        toolbar.addWidget(btn_edit)
        toolbar.addWidget(btn_del)
        layout.addLayout(toolbar)

        self.tbl_perms = QTableWidget(0, 6)
        headers = ["ID", "Nazwa", "Opis", "Zasób", "Akcja", "Aktywne"]
        self.tbl_perms.setHorizontalHeaderLabels(headers)
        self.tbl_perms.setAlternatingRowColors(True)
        self.tbl_perms.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl_perms.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.tbl_perms.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl_perms.horizontalHeader().setStretchLastSection(True)
        self.tbl_perms.setColumnWidth(0, 50)
        self.tbl_perms.setColumnWidth(5, 70)

        layout.addWidget(self.tbl_perms)
        self.tabs.addTab(tab, "Uprawnienia")

    def _selected_id(self, table: QTableWidget) -> Optional[int]:
        rows = table.selectionModel().selectedRows()
        if not rows:
            return None
        try:
            return int(table.item(rows[0].row(), 0).text())
        except Exception:
            return None

    def _users_load(self):
        ok, msg, page = self.api.list_users()
        if not ok:
            QMessageBox.warning(self, "Błąd", msg)
            self.tbl_users.setRowCount(0) # Wyczyść tabelę w razie błędu
            return
        
        # Zabezpieczenie przed None
        content = page.get("content") if isinstance(page, dict) else []
        if content is None:
            content = []
            
        self.tbl_users.setRowCount(len(content))
        for r, u in enumerate(content):
            def setc(c: int, t: str):
                self.tbl_users.setItem(r, c, QTableWidgetItem(t))

            setc(0, str(u.get("id", "")))
            setc(1, u.get("username") or "")
            setc(2, u.get("email") or "")
            setc(3, u.get("firstName") or "")
            setc(4, u.get("lastName") or "")
            setc(5, "TAK" if u.get("active") else "NIE")
            setc(6, "TAK" if u.get("emailVerified") else "NIE")
            setc(7, ", ".join(u.get("roles") or []))

    def _users_search(self):
        q = self.edt_us_search.text().strip()
        if not q:
            self._users_load()
            return
        ok, msg, page = self.api.search_users(q)
        if not ok:
            QMessageBox.warning(self, "Błąd", msg)
            self.tbl_users.setRowCount(0)
            return
            
        content = page.get("content") if isinstance(page, dict) else []
        if content is None:
            content = []
            
        self.tbl_users.setRowCount(len(content))
        for r, u in enumerate(content):
            def setc(c: int, t: str):
                self.tbl_users.setItem(r, c, QTableWidgetItem(t))

            setc(0, str(u.get("id", "")))
            setc(1, u.get("username") or "")
            setc(2, u.get("email") or "")
            setc(3, u.get("firstName") or "")
            setc(4, u.get("lastName") or "")
            setc(5, "TAK" if u.get("active") else "NIE")
            setc(6, "TAK" if u.get("emailVerified") else "NIE")
            setc(7, ", ".join(u.get("roles") or []))

    def _users_add(self):
        ok, msg, roles = self.api.list_roles()
        roles_names = [r.get("name") for r in roles] if ok else []
        dlg = UserFormDialog(roles_names, parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_create_payload()
            ok, msg, _ = self.api.create_user(payload)
            if not ok:
                QMessageBox.critical(self, "Błąd", msg)
            self._users_load()

    def _users_edit(self):
        uid = self._selected_id(self.tbl_users)
        if uid is None:
            QMessageBox.information(self, "Info", "Wybierz użytkownika.")
            return
        row = self.tbl_users.currentRow()

        roles_str = self.tbl_users.item(row, 7).text()
        current_roles = [r.strip() for r in roles_str.split(",")] if roles_str else []

        user = {
            "id": uid,
            "username": self.tbl_users.item(row, 1).text(),
            "email": self.tbl_users.item(row, 2).text(),
            "firstName": self.tbl_users.item(row, 3).text(),
            "lastName": self.tbl_users.item(row, 4).text(),
            "active": self.tbl_users.item(row, 5).text() == "TAK",
            "emailVerified": self.tbl_users.item(row, 6).text() == "TAK",
            "roles": current_roles
        }
        ok, msg, roles = self.api.list_roles()
        roles_names = [r.get("name") for r in roles] if ok else []
        dlg = UserFormDialog(roles_names, user=user, parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_update_payload()
            ok, msg, _ = self.api.update_user(uid, payload)
            if not ok:
                QMessageBox.critical(self, "Błąd", msg)
            self._users_load()

    def _users_del(self):
        uid = self._selected_id(self.tbl_users)
        if uid is None:
            QMessageBox.information(self, "Info", "Wybierz użytkownika.")
            return
        if QMessageBox.question(self, "Potwierdzenie",
                                "Czy na pewno usunąć użytkownika?") != QMessageBox.StandardButton.Yes:
            return
        ok, msg = self.api.delete_user(uid)
        if not ok:
            QMessageBox.critical(self, "Błąd", msg)
        self._users_load()

    def _users_lock(self, lock: bool):
        uid = self._selected_id(self.tbl_users)
        if uid is None:
            QMessageBox.information(self, "Info", "Wybierz użytkownika.")
            return
        ok, msg = self.api.lock_user(uid, lock)
        if not ok:
            QMessageBox.critical(self, "Błąd", msg)
        self._users_load()

    def _roles_load(self):
        ok, msg, roles = self.api.list_roles()
        if not ok:
            QMessageBox.warning(self, "Błąd", msg)
            return
        self.tbl_roles.setRowCount(len(roles))
        for r, role in enumerate(roles):
            def setc(c: int, t: str):
                self.tbl_roles.setItem(r, c, QTableWidgetItem(t))

            setc(0, str(role.get("id", "")))
            setc(1, role.get("name") or "")
            setc(2, role.get("description") or "")
            setc(3, "TAK" if role.get("active") else "NIE")
            setc(4, ", ".join(role.get("permissions") or []))

    def _roles_add(self):
        ok, msg, perms = self.api.list_permissions()
        perm_names = [p.get("name") for p in perms] if ok else []
        dlg = RoleFormDialog(perm_names, parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_payload()
            if not payload.get("name"):
                QMessageBox.warning(self, "Błąd", "Wymagane: nazwa roli.")
                return
            ok, msg, _ = self.api.create_role(payload)
            if not ok:
                QMessageBox.critical(self, "Błąd", msg)
            self._roles_load()

    def _roles_edit(self):
        rid = self._selected_id(self.tbl_roles)
        if rid is None:
            QMessageBox.information(self, "Info", "Wybierz rolę.")
            return
        row = self.tbl_roles.currentRow()

        perms_str = self.tbl_roles.item(row, 4).text()
        current_perms = [p.strip() for p in perms_str.split(",")] if perms_str else []

        role = {
            "id": rid,
            "name": self.tbl_roles.item(row, 1).text(),
            "description": self.tbl_roles.item(row, 2).text(),
            "active": self.tbl_roles.item(row, 3).text() == "TAK",
            "permissions": current_perms
        }
        ok, msg, perms = self.api.list_permissions()
        perm_names = [p.get("name") for p in perms] if ok else []
        dlg = RoleFormDialog(perm_names, role=role, parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_payload()
            ok, msg, _ = self.api.update_role(rid, payload)
            if not ok:
                QMessageBox.critical(self, "Błąd", msg)
            self._roles_load()

    def _roles_del(self):
        rid = self._selected_id(self.tbl_roles)
        if rid is None:
            QMessageBox.information(self, "Info", "Wybierz rolę.")
            return
        if QMessageBox.question(self, "Potwierdzenie", "Czy na pewno usunąć rolę?") != QMessageBox.StandardButton.Yes:
            return
        ok, msg = self.api.delete_role(rid)
        if not ok:
            QMessageBox.critical(self, "Błąd", msg)
        self._roles_load()

    def _perms_load(self):
        ok, msg, perms = self.api.list_permissions()
        if not ok:
            QMessageBox.warning(self, "Błąd", msg)
            return
        self.tbl_perms.setRowCount(len(perms))
        for r, p in enumerate(perms):
            def setc(c: int, t: str):
                self.tbl_perms.setItem(r, c, QTableWidgetItem(t))

            setc(0, str(p.get("id", "")))
            setc(1, p.get("name") or "")
            setc(2, p.get("description") or "")
            setc(3, p.get("resource") or "")
            setc(4, p.get("action") or "")
            setc(5, "TAK" if p.get("active") else "NIE")

    def _perms_add(self):
        dlg = PermissionFormDialog(parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_payload()
            if not payload.get("name") or not payload.get("resource") or not payload.get("action"):
                QMessageBox.warning(self, "Błąd", "Wymagane: name, resource, action.")
                return
            ok, msg, _ = self.api.create_permission(payload)
            if not ok:
                QMessageBox.critical(self, "Błąd", msg)
            self._perms_load()

    def _perms_edit(self):
        pid = self._selected_id(self.tbl_perms)
        if pid is None:
            QMessageBox.information(self, "Info", "Wybierz uprawnienie.")
            return
        row = self.tbl_perms.currentRow()
        perm = {
            "id": pid,
            "name": self.tbl_perms.item(row, 1).text(),
            "description": self.tbl_perms.item(row, 2).text(),
            "resource": self.tbl_perms.item(row, 3).text(),
            "action": self.tbl_perms.item(row, 4).text(),
            "active": self.tbl_perms.item(row, 5).text() == "TAK",
        }
        dlg = PermissionFormDialog(perm=perm, parent=self)
        if dlg.exec() == QDialog.DialogCode.Accepted:
            payload = dlg.build_payload()
            ok, msg, _ = self.api.update_permission(pid, payload)
            if not ok:
                QMessageBox.critical(self, "Błąd", msg)
            self._perms_load()

    def _perms_del(self):
        pid = self._selected_id(self.tbl_perms)
        if pid is None:
            QMessageBox.information(self, "Info", "Wybierz uprawnienie.")
            return
        if QMessageBox.question(self, "Potwierdzenie",
                                "Czy na pewno usunąć uprawnienie?") != QMessageBox.StandardButton.Yes:
            return
        ok, msg = self.api.delete_permission(pid)
        if not ok:
            QMessageBox.critical(self, "Błąd", msg)
        self._perms_load()


if __name__ == "__main__":
    from theme import apply_modern_style

    app = QApplication(sys.argv)
    apply_modern_style(app, dark=False)
    w = UserManagementWindow()
    w.show()
    sys.exit(app.exec())