from PyQt6.QtWidgets import QApplication
from PyQt6.QtGui import QPalette, QColor
from PyQt6.QtCore import Qt


def apply_modern_style(app: QApplication, dark: bool = False) -> None:
    # Fusion daje nowocześniejszy wygląd bazowy
    app.setStyle("Fusion")

    if dark:
        palette = QPalette()
        palette.setColor(QPalette.ColorRole.Window, QColor(30, 30, 30))
        palette.setColor(QPalette.ColorRole.WindowText, QColor(230, 230, 230))
        palette.setColor(QPalette.ColorRole.Base, QColor(22, 22, 22))
        palette.setColor(QPalette.ColorRole.AlternateBase, QColor(35, 35, 35))
        palette.setColor(QPalette.ColorRole.ToolTipBase, QColor(255, 255, 220))
        palette.setColor(QPalette.ColorRole.ToolTipText, QColor(0, 0, 0))
        palette.setColor(QPalette.ColorRole.Text, QColor(230, 230, 230))
        palette.setColor(QPalette.ColorRole.Button, QColor(45, 45, 45))
        palette.setColor(QPalette.ColorRole.ButtonText, QColor(230, 230, 230))
        palette.setColor(QPalette.ColorRole.BrightText, QColor(255, 0, 0))
        palette.setColor(QPalette.ColorRole.Highlight, QColor(64, 156, 255))
        palette.setColor(QPalette.ColorRole.HighlightedText, QColor(255, 255, 255))
        app.setPalette(palette)

    # Nowoczesny QSS: zaokrąglenia, kolory akcentu, delikatne cienie
    qss = """
    QWidget { font-size: 14px; }

    QPushButton {
        background-color: #2F80ED;
        color: white;
        border: none;
        padding: 8px 14px;
        border-radius: 8px;
    }
    QPushButton:hover { background-color: #3D8BFF; }
    QPushButton:pressed { background-color: #1F6FE0; }
    QPushButton:disabled { background-color: #A0AFC0; color: #ECEFF4; }

    QLineEdit, QComboBox, QSpinBox, QTextEdit {
        border: 1px solid #D0D7DE;
        border-radius: 8px;
        padding: 6px 8px;
        background: #FFFFFF;
    }
    QLineEdit:focus, QComboBox:focus, QSpinBox:focus, QTextEdit:focus {
        border: 1px solid #2F80ED;
        outline: none;
    }

    QTableWidget {
        gridline-color: #E1E4E8;
        background: #FFFFFF;
        alternate-background-color: #F6F8FA;
        selection-background-color: #E8F0FE;
        selection-color: #000000;
        border: 1px solid #E1E4E8;
        border-radius: 8px;
    }
    QHeaderView::section {
        background: #F1F3F5;
        padding: 8px;
        border: none;
        border-right: 1px solid #E1E4E8;
        font-weight: 600;
    }

    QLabel#titleLabel {
        font-size: 24px; font-weight: 700;
    }

    QToolTip { color: #ffffff; background-color: #2F80ED; border: none; }
    """
    app.setStyleSheet(qss)
