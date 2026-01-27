from __future__ import annotations

from dataclasses import dataclass
from typing import Callable, Iterable, Optional, Sequence, Tuple

from PyQt6.QtWidgets import (
    QComboBox,
    QDateEdit,
    QDoubleSpinBox,
    QLineEdit,
    QMessageBox,
    QSpinBox,
    QTextEdit,
    QWidget,
)


ERROR_STYLE = "border: 2px solid #e74c3c; background-color: #fdecea;"


@dataclass(frozen=True)
class RequiredField:
    label: str
    widget: QWidget
    validator: Optional[Callable[[], Tuple[bool, str]]] = None


def _get_widget_value(widget: QWidget) -> str:
    if isinstance(widget, QLineEdit):
        return widget.text().strip()
    if isinstance(widget, QComboBox):
        data = widget.currentData()
        if data is not None:
            return str(data).strip()
        return (widget.currentText() or "").strip()
    if isinstance(widget, (QSpinBox, QDoubleSpinBox)):
        return str(widget.value())
    if isinstance(widget, QDateEdit):
        return widget.date().toString("yyyy-MM-dd")
    if isinstance(widget, QTextEdit):
        return widget.toPlainText().strip()
    return ""


def clear_validation_styles(widgets: Iterable[QWidget]) -> None:
    for w in widgets:
        if w.styleSheet() == ERROR_STYLE:
            w.setStyleSheet("")


def validate_required(parent: QWidget, fields: Sequence[RequiredField], title: str = "Brak wymaganych danych") -> bool:
    invalid: list[str] = []

    clear_validation_styles([f.widget for f in fields])

    for f in fields:
        if f.validator is not None:
            ok, msg = f.validator()
            if not ok:
                f.widget.setStyleSheet(ERROR_STYLE)
                invalid.append(f"{f.label}: {msg}")
                continue

        value = _get_widget_value(f.widget)
        if value == "" or value.lower() in {"-- wybierz --", "-- brak --"}:
            f.widget.setStyleSheet(ERROR_STYLE)
            invalid.append(f"{f.label}: pole wymagane")

    if not invalid:
        return True

    QMessageBox.warning(parent, title, "Uzupełnij wymagane pola:\n\n- " + "\n- ".join(invalid))

    for f in fields:
        if f.widget.styleSheet() == ERROR_STYLE:
            f.widget.setFocus()
            break

    return False
