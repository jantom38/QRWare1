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


class UserManagementApi:
    def __init__(self, cfg: Optional[ConfigManager] = None, timeout: float = 10.0):
        self.cfg = cfg or ConfigManager()
        self.timeout = timeout

    # --- USERS ---
    def list_users(self, page: int = 0, size: int = 50) -> Tuple[bool, str, Dict[str, Any]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/users"
        try:
            r = requests.get(url, params={"page": page, "size": size}, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json()
        except Exception as e:
            return False, f"Błąd pobierania użytkowników: {e}", {}
        if r.status_code != 200:
            return False, f"Błąd {r.status_code}", {}
        return True, "OK", data

    def search_users(self, query: str) -> Tuple[bool, str, Dict[str, Any]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/users/search"
        try:
            r = requests.get(url, params={"query": query}, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json()
        except Exception as e:
            return False, f"Błąd wyszukiwania: {e}", {}
        if r.status_code != 200:
            return False, f"Błąd {r.status_code}", {}
        return True, "OK", data

    def create_user(self, payload: Dict[str, Any]) -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/users"
        try:
            r = requests.post(url, json=payload, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json() if r.text else None
        except Exception as e:
            return False, f"Błąd tworzenia: {e}", None
        if r.status_code not in (200, 201):
            return False, f"Błąd {r.status_code}", None
        return True, "Utworzono", data

    def update_user(self, uid: int, payload: Dict[str, Any]) -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/users/{uid}"
        try:
            r = requests.put(url, json=payload, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json() if r.text else None
        except Exception as e:
            return False, f"Błąd aktualizacji: {e}", None
        if r.status_code != 200:
            return False, f"Błąd {r.status_code}", None
        return True, "Zaktualizowano", data

    def delete_user(self, uid: int) -> Tuple[bool, str]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/users/{uid}"
        try:
            r = requests.delete(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd usuwania: {e}"
        if r.status_code not in (200, 204):
            return False, f"Błąd {r.status_code}"
        return True, "Usunięto"

    def lock_user(self, uid: int, lock: bool) -> Tuple[bool, str]:
        base = self.cfg.base_url.rstrip('/')
        action = "lock" if lock else "unlock"
        url = f"{base}/api/users/{uid}/{action}"
        try:
            r = requests.post(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd blokady: {e}"
        if r.status_code != 200:
            return False, f"Błąd {r.status_code}"
        return True, "Zmieniono status blokady"

    # --- ROLES ---
    def list_roles(self) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/roles"
        try:
            r = requests.get(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json()
        except Exception as e:
            return False, f"Błąd ról: {e}", []
        if r.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {r.status_code}", []
        return True, "OK", data

    def create_role(self, payload: Dict[str, Any]) -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/roles"
        try:
            r = requests.post(url, json=payload, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json() if r.text else None
        except Exception as e:
            return False, f"Błąd tworzenia roli: {e}", None
        if r.status_code not in (200, 201):
            return False, f"Błąd {r.status_code}", None
        return True, "Utworzono", data

    def update_role(self, rid: int, payload: Dict[str, Any]) -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/roles/{rid}"
        try:
            r = requests.put(url, json=payload, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json() if r.text else None
        except Exception as e:
            return False, f"Błąd aktualizacji roli: {e}", None
        if r.status_code != 200:
            return False, f"Błąd {r.status_code}", None
        return True, "Zaktualizowano", data

    def delete_role(self, rid: int) -> Tuple[bool, str]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/roles/{rid}"
        try:
            r = requests.delete(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd usuwania roli: {e}"
        if r.status_code not in (200, 204):
            return False, f"Błąd {r.status_code}"
        return True, "Usunięto"

    # --- PERMISSIONS ---
    def list_permissions(self) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/permissions"
        try:
            r = requests.get(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json()
        except Exception as e:
            return False, f"Błąd uprawnień: {e}", []
        if r.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {r.status_code}", []
        return True, "OK", data

    def create_permission(self, payload: Dict[str, Any]) -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/permissions"
        try:
            r = requests.post(url, json=payload, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json() if r.text else None
        except Exception as e:
            return False, f"Błąd tworzenia uprawnienia: {e}", None
        if r.status_code not in (200, 201):
            return False, f"Błąd {r.status_code}", None
        return True, "Utworzono", data

    def update_permission(self, pid: int, payload: Dict[str, Any]) -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/permissions/{pid}"
        try:
            r = requests.put(url, json=payload, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json() if r.text else None
        except Exception as e:
            return False, f"Błąd aktualizacji uprawnienia: {e}", None
        if r.status_code != 200:
            return False, f"Błąd {r.status_code}", None
        return True, "Zaktualizowano", data

    def delete_permission(self, pid: int) -> Tuple[bool, str]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/permissions/{pid}"
        try:
            r = requests.delete(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
        except Exception as e:
            return False, f"Błąd usuwania uprawnienia: {e}"
        if r.status_code not in (200, 204):
            return False, f"Błąd {r.status_code}"
        return True, "Usunięto"