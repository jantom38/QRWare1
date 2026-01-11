import requests
from typing import Any, Dict, List, Optional, Tuple

from config import ConfigManager


def _auth_headers(cfg: ConfigManager) -> Dict[str, str]:
    tokens = cfg.load_tokens()
    access = tokens.get("access_token")
    headers = {"Content-Type": "application/json"}
    if access:
        headers["Authorization"] = f"Bearer {access}"
    return headers


class CategoriesApi:
    def __init__(self, cfg: Optional[ConfigManager] = None, timeout: float = 10.0):
        self.cfg = cfg or ConfigManager()
        self.timeout = timeout

    def _unwrap(self, resp) -> Tuple[bool, str, Any]:
        try:
            data = resp.json()
        except Exception as e:
            return False, f"Błąd formatu odpowiedzi: {e}", None

        # ZMIANA: Akceptujemy kody 200 (OK) i 201 (Created)
        if resp.status_code not in (200, 201):
            return False, f"Błąd {resp.status_code}", None

        if not isinstance(data, dict) or not data.get("success", False):
            return False, data.get("message") if isinstance(data, dict) else "Błąd API", None
        return True, "OK", data.get("data")

    def list(self, only_active: bool = False) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/categories/active" if only_active else f"{base}/api/categories"
        try:
            resp = requests.get(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd pobierania kategorii: {e}", []
        ok, msg, data = self._unwrap(resp)
        if not ok:
            return False, msg, []
        if not isinstance(data, list):
            return False, "Nieprawidłowy format danych kategorii", []
        return True, "OK", data

    def list_root(self) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/categories/root"
        try:
            resp = requests.get(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd pobierania kategorii: {e}", []
        ok, msg, data = self._unwrap(resp)
        if not ok:
            return False, msg, []
        return True, "OK", data or []

    def children(self, parent_id: int) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/categories/{parent_id}/children"
        try:
            resp = requests.get(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd pobierania podkategorii: {e}", []
        ok, msg, data = self._unwrap(resp)
        if not ok:
            return False, msg, []
        return True, "OK", data or []

    def search(self, query: str) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/categories/search"
        try:
            resp = requests.get(url, params={"query": query}, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd wyszukiwania: {e}", []
        ok, msg, data = self._unwrap(resp)
        if not ok:
            return False, msg, []
        return True, "OK", data or []

    def create(self, payload: Dict[str, Any]) -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/categories"
        try:
            resp = requests.post(url, json=payload, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd tworzenia: {e}", None
        ok, msg, data = self._unwrap(resp)
        if not ok:
            return False, msg, None
        return True, "Utworzono", data

    def update(self, cat_id: int, payload: Dict[str, Any]) -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/categories/{cat_id}"
        try:
            resp = requests.put(url, json=payload, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd aktualizacji: {e}", None
        ok, msg, data = self._unwrap(resp)
        if not ok:
            return False, msg, None
        return True, "Zaktualizowano", data

    def delete(self, cat_id: int) -> Tuple[bool, str]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/categories/{cat_id}"
        try:
            resp = requests.delete(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd usuwania: {e}"
        # DELETE zwykle zwraca 200 lub 204
        if resp.status_code not in (200, 204):
            return False, f"Błąd {resp.status_code}"
        return True, "Usunięto"

    def toggle_active(self, cat_id: int) -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/categories/{cat_id}/toggle-active"
        try:
            resp = requests.patch(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd zmiany statusu: {e}", None
        ok, msg, data = self._unwrap(resp)
        if not ok:
            return False, msg, None
        return True, "Zmieniono status", data