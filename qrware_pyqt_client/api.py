import requests
from dataclasses import dataclass
from typing import List, Optional, Tuple


@dataclass
class AuthData:
    accessToken: str
    refreshToken: str
    tokenType: str
    expiresIn: int
    userId: int
    username: str
    email: str
    fullName: Optional[str]
    roles: List[str]


@dataclass
class ApiResponse:
    success: bool
    message: str
    data: Optional[AuthData]


class QRWareApiClient:
    def __init__(self, base_url: str, timeout: float = 10.0):
        self.base_url = base_url.rstrip('/')
        self.timeout = timeout

    def login(self, username_or_email: str, password: str) -> Tuple[bool, str, Optional[AuthData]]:
        url = f"{self.base_url}/api/auth/login"
        payload = {
            "usernameOrEmail": username_or_email,
            "password": password,
        }
        headers = {"Content-Type": "application/json"}

        try:
            resp = requests.post(url, json=payload, headers=headers, timeout=self.timeout)
        except requests.RequestException as e:
            return False, f"Błąd połączenia: {e}", None

        # Backend zwraca ApiResponse { success, message, data }
        try:
            resp_json = resp.json()
        except ValueError:
            return False, f"Niepoprawna odpowiedź serwera: {resp.text[:200]}", None

        if resp.status_code != 200:
            # Może być 401 albo 400 itp., ale i tak spróbujmy odczytać 'message'
            msg = resp_json.get("message") or f"Błąd {resp.status_code}"
            return False, msg, None

        if not isinstance(resp_json, dict):
            return False, "Nieoczekiwany format odpowiedzi", None

        success = resp_json.get("success", False)
        message = resp_json.get("message", "")
        data = resp_json.get("data")

        if not success or not data:
            return False, message or "Logowanie nieudane", None

        try:
            auth = AuthData(
                accessToken=data.get("accessToken"),
                refreshToken=data.get("refreshToken"),
                tokenType=data.get("tokenType", "Bearer"),
                expiresIn=int(data.get("expiresIn") or 0),
                userId=int(data.get("userId") or 0),
                username=data.get("username"),
                email=data.get("email"),
                fullName=data.get("fullName"),
                roles=list(data.get("roles") or []),
            )
        except Exception as e:
            return False, f"Błąd parsowania odpowiedzi: {e}", None

        return True, message or "Zalogowano", auth
