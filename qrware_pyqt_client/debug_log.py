from __future__ import annotations

from datetime import datetime
from pathlib import Path


_LOG_PATH = Path(__file__).parent / "client_debug.log"


def log(msg: str) -> None:
    """Minimalne logowanie do pliku dla aplikacji uruchamianej bez konsoli."""
    try:
        ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        _LOG_PATH.parent.mkdir(parents=True, exist_ok=True)
        with _LOG_PATH.open("a", encoding="utf-8") as f:
            f.write(f"[{ts}] {msg}\n")
    except Exception:
        # Nie blokuj aplikacji w razie błędu logowania
        pass


def log_exception(context: str, exc: Exception) -> None:
    log(f"{context}: {type(exc).__name__}: {exc}")
