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


class InventoryApi:
    def __init__(self, cfg: Optional[ConfigManager] = None, timeout: float = 10.0):
        self.cfg = cfg or ConfigManager()
        self.timeout = timeout

    def page(self, page: int = 0, size: int = 50) -> Tuple[bool, str, List[Dict[str, Any]], Dict[str, Any]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/inventory"
        try:
            resp = requests.get(url, params={"page": page, "size": size}, headers=_auth_headers(self.cfg), timeout=self.timeout)
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
        url = f"{base}/api/inventory/search"
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
        url = f"{base}/api/inventory"
        try:
            resp = requests.post(url, json=payload, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json() if resp.text else None
        except Exception as e:
            return False, f"Błąd tworzenia: {e}", None
        if resp.status_code not in (200, 201):
            return False, f"Błąd {resp.status_code}", None
        return True, "Utworzono", data

    def update(self, inv_id: int, payload: Dict[str, Any]) -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/inventory/{inv_id}"
        try:
            resp = requests.put(url, json=payload, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json() if resp.text else None
        except Exception as e:
            return False, f"Błąd aktualizacji: {e}", None
        if resp.status_code != 200:
            return False, f"Błąd {resp.status_code}", None
        return True, "Zaktualizowano", data

    def delete(self, inv_id: int) -> Tuple[bool, str]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/inventory/{inv_id}"
        try:
            resp = requests.delete(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd usuwania: {e}"
        if resp.status_code not in (200, 204):
            return False, f"Błąd {resp.status_code}"
        return True, "Usunięto"

    def receive(self, inv_id: int, quantity: int, reason: str = "") -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/inventory/{inv_id}/receive"
        try:
            resp = requests.post(url, json={"quantity": quantity, "reason": reason}, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json() if resp.text else None
        except Exception as e:
            return False, f"Błąd przyjęcia: {e}", None
        if resp.status_code != 200:
            return False, f"Błąd {resp.status_code}", None
        return True, "Przyjęto", data

    def issue(self, inv_id: int, quantity: int, reason: str = "") -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/inventory/{inv_id}/issue"
        try:
            resp = requests.post(url, json={"quantity": quantity, "reason": reason}, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json() if resp.text else None
        except Exception as e:
            return False, f"Błąd wydania: {e}", None
        if resp.status_code != 200:
            return False, f"Błąd {resp.status_code}", None
        return True, "Wydano", data

    def by_status(self, status: str) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/inventory/status/{status}"
        try:
            resp = requests.get(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json()
        except Exception as e:
            return False, f"Błąd pobierania statusu: {e}", []
        if resp.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {resp.status_code}", []
        return True, "OK", data

    def by_product(self, product_id: int) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/inventory/product/{product_id}"
        try:
            resp = requests.get(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json()
        except Exception as e:
            return False, f"Błąd pobierania: {e}", []
        if resp.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {resp.status_code}", []
        return True, "OK", data

    def by_location(self, location_id: int) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/inventory/location/{location_id}"
        try:
            resp = requests.get(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json()
        except Exception as e:
            return False, f"Błąd pobierania: {e}", []
        if resp.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {resp.status_code}", []
        return True, "OK", data

    def get_alerts(self) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/inventory/alerts"
        try:
            resp = requests.get(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json()
        except Exception as e:
            return False, f"Błąd pobierania alertów: {e}", []
        if resp.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {resp.status_code}", []
        return True, "OK", data

    def get_low_stock(self) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/inventory/low-stock"
        try:
            resp = requests.get(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = resp.json()
        except Exception as e:
            return False, f"Błąd pobierania niskiego stanu: {e}", []
        if resp.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {resp.status_code}", []
        return True, "OK", data
