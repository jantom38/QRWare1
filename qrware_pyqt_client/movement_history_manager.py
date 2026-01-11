import sys
from typing import Any, Dict, List, Optional

from PyQt6.QtCore import Qt, QDateTime
from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QLineEdit, QComboBox, QTableWidget, QTableWidgetItem,
    QDialog, QFormLayout, QMessageBox, QDateTimeEdit
)

from config import ConfigManager
from movement_history_api import MovementHistoryApi


class MovementHistoryWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("QRWare - Historia ruchów")
        self.resize(1200, 700)
        self.cfg = ConfigManager()
        self.api = MovementHistoryApi(self.cfg)

        central = QWidget(); self.setCentralWidget(central)
        root = QVBoxLayout(); central.setLayout(root)
        root.setContentsMargins(16, 16, 16, 16)
        root.setSpacing(12)

        # Pasek narzędzi
        top = QHBoxLayout()
        self.edt_server = QLineEdit(self.cfg.base_url)
        btn_save_server = QPushButton("Zapisz serwer"); btn_save_server.clicked.connect(self._save_server)
        self.edt_keyword = QLineEdit(); self.edt_keyword.setPlaceholderText("Szukaj po reason/notes…")
        self.cmb_search_in = QComboBox(); self.cmb_search_in.addItems(["reason", "notes", "both"])
        self.cmb_type = QComboBox(); self.cmb_type.addItems([
            "", "RECEIPT","ISSUE","TRANSFER","MOVE","ADJUSTMENT","CYCLE_COUNT","PHYSICAL_COUNT",
            "RESERVE","UNRESERVE","PICK","PACK","SHIP","RETURN","PUTAWAY","REPLENISHMENT","ALLOCATION","DEALLOCATION",
            "QUARANTINE","RELEASE","HOLD","UNHOLD","DAMAGE","DISPOSAL","LOSS","FOUND","EXPIRY","RECALL","STAGING","CROSSDOCK",
            "CONSOLIDATION","SPLIT","MERGE","CONVERSION","PRODUCTION","CONSUMPTION","SCRAP","REWORK","SAMPLE","LOAN","LOAN_RETURN",
            "ORDER_RECEIPT","ORDER_ISSUE","ORDER_PICK","ORDER_PACK","ORDER_CANCEL","ORDER_RETURN","ORDER_ADJUSTMENT"
        ])
        self.dt_start = QDateTimeEdit(QDateTime.currentDateTime().addDays(-7)); self.dt_start.setCalendarPopup(True)
        self.dt_end = QDateTimeEdit(QDateTime.currentDateTime()); self.dt_end.setCalendarPopup(True)
        btn_filter = QPushButton("Filtruj"); btn_filter.clicked.connect(self._apply_filters)
        btn_recent = QPushButton("Ostatnie"); btn_recent.clicked.connect(self._load_recent)
        btn_pending = QPushButton("Oczekujące"); btn_pending.clicked.connect(self._load_pending)
        top.addWidget(QLabel("Serwer:")); top.addWidget(self.edt_server, 2)
        top.addWidget(btn_save_server)
        top.addWidget(QLabel("Szukaj:")); top.addWidget(self.edt_keyword, 1)
        top.addWidget(self.cmb_search_in)
        top.addWidget(QLabel("Typ:")); top.addWidget(self.cmb_type)
        top.addWidget(QLabel("Od:")); top.addWidget(self.dt_start)
        top.addWidget(QLabel("Do:")); top.addWidget(self.dt_end)
        top.addWidget(btn_filter)
        top.addWidget(btn_recent)
        top.addWidget(btn_pending)
        root.addLayout(top)

        # Tabela
        self.tbl = QTableWidget(0, 10)
        self.tbl.setHorizontalHeaderLabels([
            "ID","Data","Typ","Item","From","To","Qty Δ","Użytkownik","Approved","Ref"
        ])
        self.tbl.setEditTriggers(QTableWidget.EditTrigger.NoEditTriggers)
        self.tbl.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.tbl.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.tbl.horizontalHeader().setStretchLastSection(True)
        root.addWidget(self.tbl, 1)

        # Akcje
        actions = QHBoxLayout()
        btn_approve = QPushButton("Zatwierdź…"); btn_approve.clicked.connect(self._approve)
        actions.addWidget(btn_approve)
        root.addLayout(actions)

        self._load_recent()

    def _save_server(self):
        self.cfg.base_url = self.edt_server.text().strip()
        QMessageBox.information(self, "Zapisano", "Adres serwera zapisany.")

    def _apply_filters(self):
        # priorytet: data -> typ -> keyword
        start = self.dt_start.dateTime().toString("yyyy-MM-ddTHH:mm:ss")
        end = self.dt_end.dateTime().toString("yyyy-MM-ddTHH:mm:ss")
        if start and end:
            ok, msg, items = self.api.by_date_range(start, end)
            if not ok:
                QMessageBox.warning(self, "Filtr", msg); return
            self._populate(items)
            return
        mtype = self.cmb_type.currentText().strip()
        if mtype:
            ok, msg, items = self.api.by_type(mtype)
            if not ok:
                QMessageBox.warning(self, "Filtr", msg); return
            self._populate(items)
            return
        kw = self.edt_keyword.text().strip()
        if kw:
            ok, msg, items = self.api.search(kw, self.cmb_search_in.currentText())
            if not ok:
                QMessageBox.warning(self, "Szukaj", msg); return
            self._populate(items)
            return
        self._load_recent()

    def _load_recent(self):
        ok, msg, items = self.api.recent(100)
        if not ok:
            QMessageBox.warning(self, "Historia", msg); return
        self._populate(items)

    def _load_pending(self):
        ok, msg, items = self.api.pending()
        if not ok:
            QMessageBox.warning(self, "Oczekujące", msg); return
        self._populate(items)

    def _populate(self, items: List[Dict[str, Any]]):
        self.tbl.setRowCount(len(items))
        for r, it in enumerate(items):
            def setc(c: int, text: str):
                self.tbl.setItem(r, c, QTableWidgetItem(text))
            setc(0, str(it.get("id", "")))
            setc(1, it.get("movementDate") or "")
            setc(2, it.get("movementType") or "")
            inv = it.get("inventoryItem") or {}
            setc(3, str(inv.get("id") or ""))
            fl = it.get("fromLocation") or {}
            tl = it.get("toLocation") or {}
            setc(4, (fl.get("code") or fl.get("name") or ""))
            setc(5, (tl.get("code") or tl.get("name") or ""))
            setc(6, str(it.get("quantityChanged", "") or ""))
            setc(7, it.get("userName") or it.get("userId") or "")
            setc(8, "TAK" if it.get("approved") else "NIE")
            setc(9, it.get("referenceNumber") or "")
        self.tbl.resizeColumnsToContents()

    def _selected_id(self) -> Optional[int]:
        rows = self.tbl.selectionModel().selectedRows()
        if not rows:
            return None
        try:
            return int(self.tbl.item(rows[0].row(), 0).text())
        except Exception:
            return None

    def _approve(self):
        mid = self._selected_id()
        if mid is None:
            QMessageBox.information(self, "Zatwierdź", "Wybierz rekord.")
            return
        from PyQt6.QtWidgets import QInputDialog
        comment, ok = QInputDialog.getText(self, "Zatwierdź", "Komentarz (opcjonalnie):")
        if not ok:
            return
        ok2, msg, _ = self.api.approve(mid, comment.strip())
        if not ok2:
            QMessageBox.critical(self, "Zatwierdzanie", msg)
        else:
            QMessageBox.information(self, "OK", "Zatwierdzono.")
            self._load_pending()


if __name__ == "__main__":
    from theme import apply_modern_style
    app = QApplication(sys.argv)
    apply_modern_style(app, dark=False)
    w = MovementHistoryWindow()
    w.show()
    sys.exit(app.exec())
