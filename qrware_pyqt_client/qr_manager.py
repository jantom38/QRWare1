import json
import os
from PyQt6.QtWidgets import (
    QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, QTableWidget,
    QTableWidgetItem, QPushButton, QLineEdit, QComboBox,
    QMessageBox, QHeaderView, QFormLayout, QGroupBox, QLabel, QScrollArea, QFileDialog, QFrame, QCheckBox
)
from PyQt6.QtCore import Qt
from PyQt6.QtGui import QPixmap
from qr_api import QRService
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm


class QRManagerWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("QRWare - Menedżer QR")
        self.resize(1200, 750)

        self.api_service = QRService()
        self.all_data = []  # Store all loaded data for filtering

        self.custom_field_widgets = []

        central = QWidget()
        self.setCentralWidget(central)
        main_layout = QHBoxLayout(central)
        main_layout.setContentsMargins(20, 20, 20, 20)
        main_layout.setSpacing(20)

        # --- LEWA STRONA (Lista) ---
        left_layout = QVBoxLayout()
        
        lbl_list = QLabel("Lista Kodów QR")
        lbl_list.setStyleSheet("font-size: 18px; font-weight: bold; color: #2c3e50;")
        left_layout.addWidget(lbl_list)

        self.table = QTableWidget()
        self.table.setColumnCount(7)
        self.table.setHorizontalHeaderLabels(["ID", "Kod", "Typ", "Dane", "Skany", "Status", "Obraz"])
        self.table.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch)
        self.table.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.table.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        self.table.setAlternatingRowColors(True)
        self.table.setStyleSheet("QTableWidget { border: 1px solid #dcdcdc; }")
        left_layout.addWidget(self.table)

        btn_layout = QHBoxLayout()
        self.btn_refresh = QPushButton("Odśwież")
        self.btn_refresh.clicked.connect(self.load_data)

        # Zmiana: Checkbox
        self.chk_only_active = QCheckBox("Tylko aktywne")
        self.chk_only_active.setChecked(True)
        self.chk_only_active.stateChanged.connect(self.filter_and_populate)

        self.btn_toggle = QPushButton("Zmień status")
        self.btn_toggle.clicked.connect(self.toggle_active)

        self.btn_delete = QPushButton("Usuń")
        self.btn_delete.setStyleSheet("color: red;")
        self.btn_delete.clicked.connect(self.delete_qr)

        self.btn_print_label = QPushButton("Drukuj etykietę")
        self.btn_print_label.setStyleSheet("background-color: #3498db; color: white; font-weight: bold;")
        self.btn_print_label.clicked.connect(self.print_label)

        btn_layout.addWidget(self.btn_refresh)
        btn_layout.addWidget(self.chk_only_active)
        btn_layout.addWidget(self.btn_toggle)
        btn_layout.addWidget(self.btn_delete)
        btn_layout.addStretch()
        btn_layout.addWidget(self.btn_print_label)
        left_layout.addLayout(btn_layout)

        main_layout.addLayout(left_layout, 60)

        # --- PRAWA STRONA (Generator) ---
        right_layout = QVBoxLayout()
        
        lbl_gen = QLabel("Generator Kodów")
        lbl_gen.setStyleSheet("font-size: 18px; font-weight: bold; color: #2c3e50;")
        right_layout.addWidget(lbl_gen)

        scroll_area = QScrollArea()
        scroll_area.setWidgetResizable(True)
        scroll_area.setFrameShape(QFrame.Shape.NoFrame)
        scroll_content = QWidget()
        scroll_layout = QVBoxLayout(scroll_content)
        scroll_layout.setContentsMargins(0, 0, 0, 0)
        scroll_layout.setSpacing(15)

        group_box = QGroupBox("Parametry podstawowe")
        form_layout = QFormLayout()
        form_layout.setSpacing(10)

        self.input_data = QLineEdit()
        self.input_data.setPlaceholderText("Wpisz treść kodu...")

        self.combo_type = QComboBox()
        self.combo_type.addItems(["INVENTORY_ITEM", "PRODUCT", "CUSTOM", "LOCATION", "ASSET"])

        self.combo_entity_type = QComboBox()
        self.combo_entity_type.addItems(["Inventory_item", "product"])

        self.input_entity_id = QLineEdit()
        self.input_entity_id.setPlaceholderText("0")

        self.input_size = QLineEdit("300")

        form_layout.addRow("Dane / URL:", self.input_data)
        form_layout.addRow("Typ QR (Biznesowy):", self.combo_type)
        form_layout.addRow("Typ obiektu (Baza):", self.combo_entity_type)
        form_layout.addRow("ID obiektu:", self.input_entity_id)
        form_layout.addRow("Rozmiar (px):", self.input_size)

        group_box.setLayout(form_layout)
        scroll_layout.addWidget(group_box)

        custom_group = QGroupBox("Dane niestandardowe (Dodatkowe pola)")
        custom_layout = QVBoxLayout()

        self.fields_container = QWidget()
        self.fields_layout = QVBoxLayout(self.fields_container)
        self.fields_layout.setContentsMargins(0, 0, 0, 0)
        self.fields_layout.setAlignment(Qt.AlignmentFlag.AlignTop)

        custom_layout.addWidget(self.fields_container)

        self.btn_add_field = QPushButton("+ Dodaj pole")
        self.btn_add_field.clicked.connect(self.add_custom_field_row)
        custom_layout.addWidget(self.btn_add_field)

        custom_group.setLayout(custom_layout)
        scroll_layout.addWidget(custom_group)

        self.btn_generate = QPushButton("Generuj kod QR")
        self.btn_generate.setStyleSheet("font-weight: bold; padding: 10px; background-color: #2ecc71; color: white;")
        self.btn_generate.clicked.connect(self.generate_qr)

        scroll_layout.addWidget(self.btn_generate)
        scroll_layout.addStretch()

        scroll_area.setWidget(scroll_content)
        right_layout.addWidget(scroll_area)

        main_layout.addLayout(right_layout, 40)

        self.add_custom_field_row()

        self.load_data()

    def add_custom_field_row(self):
        row_widget = QWidget()
        row_layout = QHBoxLayout(row_widget)
        row_layout.setContentsMargins(0, 2, 0, 2)

        key_input = QLineEdit()
        key_input.setPlaceholderText("Klucz (np. SKU)")

        val_input = QLineEdit()
        val_input.setPlaceholderText("Wartość")

        btn_remove = QPushButton("X")
        btn_remove.setFixedWidth(30)
        btn_remove.setStyleSheet("color: red; font-weight: bold;")

        btn_remove.clicked.connect(lambda: self.remove_custom_field_row(row_widget))

        row_layout.addWidget(key_input)
        row_layout.addWidget(val_input)
        row_layout.addWidget(btn_remove)

        self.fields_layout.addWidget(row_widget)

        self.custom_field_widgets.append({
            "widget": row_widget,
            "key": key_input,
            "val": val_input
        })

    def remove_custom_field_row(self, row_widget):
        self.fields_layout.removeWidget(row_widget)
        row_widget.deleteLater()

        self.custom_field_widgets = [
            item for item in self.custom_field_widgets
            if item["widget"] != row_widget
        ]

    def get_custom_data_as_dict(self):
        data = {}
        for item in self.custom_field_widgets:
            key = item["key"].text().strip()
            val = item["val"].text().strip()
            if key:
                data[key] = val
        return data

    def load_data(self):
        success, result = self.api_service.get_all_qr_codes()

        if success:
            self.all_data = result
            self.filter_and_populate()
        else:
            QMessageBox.warning(self, "Błąd", f"Nie udało się pobrać danych:\n{result}")

    def filter_and_populate(self):
        only_active = self.chk_only_active.isChecked()
        filtered_data = []
        for item in self.all_data:
            if only_active and not item.get('active'):
                continue
            filtered_data.append(item)
        self.populate_table(filtered_data)

    def populate_table(self, data):
        self.table.setRowCount(0)
        for row_idx, item in enumerate(data):
            self.table.insertRow(row_idx)

            self.table.setItem(row_idx, 0, QTableWidgetItem(str(item.get('id'))))
            self.table.setItem(row_idx, 1, QTableWidgetItem(str(item.get('code'))))
            self.table.setItem(row_idx, 2, QTableWidgetItem(str(item.get('type'))))
            self.table.setItem(row_idx, 3, QTableWidgetItem(str(item.get('data'))))
            self.table.setItem(row_idx, 4, QTableWidgetItem(str(item.get('scanCount'))))

            status_text = "Aktywny" if item.get('active') else "Nieaktywny"
            status_item = QTableWidgetItem(status_text)
            if item.get('active'):
                status_item.setForeground(Qt.GlobalColor.darkGreen)
            else:
                status_item.setForeground(Qt.GlobalColor.red)
            self.table.setItem(row_idx, 5, status_item)

            image_path = item.get('imagePath')
            if image_path:
                self.table.setItem(row_idx, 6, QTableWidgetItem("Dostępny"))
            else:
                self.table.setItem(row_idx, 6, QTableWidgetItem("Brak"))

            self.table.item(row_idx, 0).setData(Qt.ItemDataRole.UserRole, item)

    def generate_qr(self):
        qr_data = self.input_data.text()
        if not qr_data:
            QMessageBox.warning(self, "Walidacja", "Pole danych jest wymagane!")
            return

        try:
            entity_id = int(self.input_entity_id.text()) if self.input_entity_id.text() else 0
            size = int(self.input_size.text()) if self.input_size.text() else 300
        except ValueError:
            QMessageBox.warning(self, "Walidacja", "ID obiektu i rozmiar muszą być liczbami.")
            return

        custom_data_dict = self.get_custom_data_as_dict()


        encoded_data = qr_data
        if custom_data_dict:
            encoded_data += "\n"
            for k, v in custom_data_dict.items():
                encoded_data += f"{k}: {v}\n"

        encoded_data = encoded_data.strip()

        payload = {
            "data": encoded_data,
            "type": self.combo_type.currentText(),
            "entityType": self.combo_entity_type.currentText(),
            "entityId": entity_id,
            "size": size,
            "custom_data": custom_data_dict
        }

        success, result = self.api_service.generate_qr_code(payload)

        if success:
            QMessageBox.information(self, "Sukces", "Kod QR został wygenerowany pomyślnie.")
            self.input_data.clear()
            self.load_data()
        else:
            QMessageBox.warning(self, "Błąd", f"Generowanie nie powiodło się: {result}")

    def toggle_active(self):
        row = self.table.currentRow()
        if row < 0:
            return

        item_id = self.table.item(row, 0).text()
        success, result = self.api_service.toggle_active_status(item_id)

        if success:
            self.load_data()
        else:
            QMessageBox.warning(self, "Błąd", f"Nie udało się zmienić statusu: {result}")

    def delete_qr(self):
        row = self.table.currentRow()
        if row < 0:
            QMessageBox.information(self, "Info", "Wybierz wiersz do usunięcia.")
            return

        confirm = QMessageBox.question(
            self, "Potwierdzenie",
            "Czy na pewno chcesz usunąć ten kod QR?",
            QMessageBox.StandardButton.Yes | QMessageBox.StandardButton.No
        )

        if confirm == QMessageBox.StandardButton.No:
            return

        item_id = self.table.item(row, 0).text()
        success, result = self.api_service.delete_qr_code(item_id)

        if success:
            self.load_data()
        else:
            QMessageBox.warning(self, "Błąd", f"Nie udało się usunąć: {result}")

    def set_form_data(self, data: str, qr_type: str, entity_type: str, entity_id: int):
        self.input_data.setText(str(data))

        if qr_type and qr_type.upper() == "INVENTORY":
            qr_type = "INVENTORY_ITEM"

        idx_type = self.combo_type.findText(qr_type, Qt.MatchFlag.MatchContains)
        if idx_type >= 0:
            self.combo_type.setCurrentIndex(idx_type)

        idx_entity = self.combo_entity_type.findText(entity_type, Qt.MatchFlag.MatchContains)
        if idx_entity >= 0:
            self.combo_entity_type.setCurrentIndex(idx_entity)

        self.input_entity_id.setText(str(entity_id))

    def print_label(self):
        row = self.table.currentRow()
        if row < 0:
            QMessageBox.information(self, "Info", "Wybierz kod QR do wydruku.")
            return

        item = self.table.item(row, 0).data(Qt.ItemDataRole.UserRole)
        image_path = item.get('imagePath')

        if not image_path:
            QMessageBox.warning(self, "Błąd", "Ten kod QR nie posiada wygenerowanego obrazu.")
            return

        success, image_data = self.api_service.download_qr_image(image_path)
        if not success:
            QMessageBox.warning(self, "Błąd", f"Nie udało się pobrać obrazu: {image_data}")
            return

        temp_img_path = "temp_qr.png"
        with open(temp_img_path, "wb") as f:
            f.write(image_data)

        file_path, _ = QFileDialog.getSaveFileName(self, "Zapisz etykietę PDF", f"label_{item.get('code')}.pdf", "PDF Files (*.pdf)")

        if not file_path:
            return

        try:
            self.generate_pdf_label(file_path, temp_img_path, item)
            QMessageBox.information(self, "Sukces", f"Etykieta zapisana w: {file_path}")
        except Exception as e:
            QMessageBox.critical(self, "Błąd", f"Błąd podczas generowania PDF: {str(e)}")
        finally:
            if os.path.exists(temp_img_path):
                os.remove(temp_img_path)

    def generate_pdf_label(self, pdf_path, img_path, item_data):
        label_width = 50 * mm
        label_height = 30 * mm

        c = canvas.Canvas(pdf_path, pagesize=(label_width, label_height))

        # QR Code size and position
        qr_size = 20 * mm
        margin = 2 * mm

        # Draw QR Code on the left
        c.drawImage(img_path, margin, (label_height - qr_size) / 2, width=qr_size, height=qr_size)

        # Text position (to the right of QR code)
        text_x = margin + qr_size + 2 * mm
        # Start text from top, centered vertically relative to QR code
        text_start_y = (label_height + qr_size) / 2 - 2 * mm

        c.setFont("Helvetica-Bold", 8)
        # Removed ID line as requested

        # Code
        c.setFont("Helvetica", 6)
        code = item_data.get('code', '')
        if len(code) > 15:
            code = code[:12] + "..."
        c.drawString(text_x, text_start_y, f"Kod: {code}")

        # Type
        text_y = text_start_y - 4 * mm
        c.drawString(text_x, text_y, f"Typ: {item_data.get('type')}")

        # Data
        text_y -= 4 * mm
        data_str = item_data.get('data', '')
        if len(data_str) > 15:
            data_str = data_str[:12] + "..."
        c.drawString(text_x, text_y, f"Dane: {data_str}")

        c.showPage()
        c.save()
