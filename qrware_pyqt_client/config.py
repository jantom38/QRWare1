import json
from pathlib import Path
from typing import Optional, Dict, Any


class ConfigManager:
    def __init__(self, path: Optional[Path] = None):
        self.path = path or Path(__file__).parent / "client_state.json"
        self._data: Dict[str, Any] = {}
        self.load()

    def load(self) -> None:
        try:
            if self.path.exists():
                self._data = json.load(self.path.open("r", encoding="utf-8"))
            else:
                self._data = {}
        except Exception:
            self._data = {}

    def save(self) -> None:
        try:
            with self.path.open("w", encoding="utf-8") as f:
                json.dump(self._data, f, ensure_ascii=False, indent=2)
        except Exception:
            pass

    @property
    def base_url(self) -> str:
        return str(self._data.get("base_url", "http://localhost:8080"))

    @base_url.setter
    def base_url(self, value: str) -> None:
        self._data["base_url"] = (value or "").strip()
        self.save()

    @property
    def access_token(self) -> str:
        return str(self._data.get("access_token", ""))

    @property
    def refresh_token(self) -> str:
        return str(self._data.get("refresh_token", ""))
    # ------------------------------

    def save_tokens(self, access_token: str, refresh_token: str) -> None:
        self._data["access_token"] = access_token
        self._data["refresh_token"] = refresh_token
        self.save()

    def load_tokens(self) -> Dict[str, str]:
        return {
            "access_token": str(self._data.get("access_token", "")),
            "refresh_token": str(self._data.get("refresh_token", "")),
        }