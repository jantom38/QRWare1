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


class ZonesApi:
    def __init__(self, cfg: Optional[ConfigManager] = None, timeout: float = 10.0):
        self.cfg = cfg or ConfigManager()
        self.timeout = timeout

    def list_active(self) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/zones/active"
        try:
            resp = requests.get(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json()
        except Exception as e:
            return False, f"Błąd pobierania stref: {e}", []
        if resp.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {resp.status_code}", []
        return True, "OK", data

    def page(self, page: int = 0, size: int = 50, active: Optional[bool] = None) -> Tuple[bool, str, List[Dict[str, Any]], Dict[str, Any]]:
        base = self.cfg.base_url.rstrip('/')
        params = {"page": page, "size": size}
        if active is not None:
            params["active"] = str(active).lower()
        url = f"{base}/api/zones"
        try:
            resp = requests.get(url, params=params, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json()
        except Exception as e:
            return False, f"Błąd pobierania: {e}", [], {}
        if resp.status_code != 200 or not isinstance(data, dict):
            return False, f"Błąd {resp.status_code}", [], {}
        content = data.get("content")
        if content is None:
            return False, "Nieprawidłowa odpowiedź (brak content)", [], {}
        return True, "OK", content, data

    def search(self, query: str) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/zones/search"
        try:
            resp = requests.get(url, params={"query": query}, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json()
        except Exception as e:
            return False, f"Błąd wyszukiwania: {e}", []
        if resp.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {resp.status_code}", []
        return True, "OK", data

    def create(self, payload: Dict[str, Any]) -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/zones"
        try:
            resp = requests.post(url, json=payload, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json() if resp.text else None
        except Exception as e:
            return False, f"Błąd tworzenia: {e}", None
        if resp.status_code not in (200, 201):
            return False, f"Błąd {resp.status_code}", None
        return True, "Utworzono", data

    def update(self, zone_id: int, payload: Dict[str, Any]) -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/zones/{zone_id}"
        try:
            resp = requests.put(url, json=payload, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json() if resp.text else None
        except Exception as e:
            return False, f"Błąd aktualizacji: {e}", None
        if resp.status_code != 200:
            return False, f"Błąd {resp.status_code}", None
        return True, "Zaktualizowano", data

    def delete(self, zone_id: int) -> Tuple[bool, str]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/zones/{zone_id}"
        try:
            resp = requests.delete(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd usuwania: {e}"
        if resp.status_code not in (200, 204):
            return False, f"Błąd {resp.status_code}"
        return True, "Usunięto"

    def toggle_active(self, zone_id: int) -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/zones/{zone_id}/toggle-active"
        try:
            resp = requests.patch(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json() if resp.text else None
        except Exception as e:
            return False, f"Błąd zmiany statusu: {e}", None
        if resp.status_code != 200:
            return False, f"Błąd {resp.status_code}", None
        return True, "Zmieniono status", data
