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


class MovementHistoryApi:
    def __init__(self, cfg: Optional[ConfigManager] = None, timeout: float = 10.0):
        self.cfg = cfg or ConfigManager()
        self.timeout = timeout

    def recent(self, limit: int = 50) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/movement-history/recent"
        try:
            r = requests.get(url, params={"limit": limit}, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json()
        except Exception as e:
            return False, f"Błąd pobierania: {e}", []
        if r.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {r.status_code}", []
        return True, "OK", data

    def search(self, keyword: str, search_in: str = "reason") -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/movement-history/search"
        try:
            r = requests.get(url, params={"keyword": keyword, "searchIn": search_in}, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json()
        except Exception as e:
            return False, f"Błąd wyszukiwania: {e}", []
        if r.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {r.status_code}", []
        return True, "OK", data

    def by_date_range(self, start_iso: str, end_iso: str) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/movement-history/date-range"
        try:
            r = requests.get(url, params={"startDate": start_iso, "endDate": end_iso}, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json()
        except Exception as e:
            return False, f"Błąd zakresu dat: {e}", []
        if r.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {r.status_code}", []
        return True, "OK", data

    def by_type(self, mtype: str) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/movement-history/by-type/{mtype}"
        try:
            r = requests.get(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json()
        except Exception as e:
            return False, f"Błąd pobrania: {e}", []
        if r.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {r.status_code}", []
        return True, "OK", data

    def pending(self) -> Tuple[bool, str, List[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/movement-history/pending-approval"
        try:
            r = requests.get(url, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json()
        except Exception as e:
            return False, f"Błąd pobrania: {e}", []
        if r.status_code != 200 or not isinstance(data, list):
            return False, f"Błąd {r.status_code}", []
        return True, "OK", data

    def approve(self, movement_id: int, approver_comment: str = "") -> Tuple[bool, str, Optional[Dict[str, Any]]]:
        base = self.cfg.base_url.rstrip('/')
        url = f"{base}/api/movement-history/{movement_id}/approve"
        payload = {"approverComment": approver_comment} if approver_comment else {}
        try:
            r = requests.post(url, json=payload or None, headers=_auth_headers(self.cfg), timeout=self.timeout)
            data = r.json() if r.text else None
        except Exception as e:
            return False, f"Błąd zatwierdzania: {e}", None
        if r.status_code != 200:
            return False, f"Błąd {r.status_code}", None
        return True, "Zatwierdzono", data
