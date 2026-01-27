import requests
from config import ConfigManager


class QRService:
    def __init__(self):
        self.cfg = ConfigManager()

    def _get_headers(self):
        return {
            "Authorization": f"Bearer {self.cfg.access_token}",
            "Content-Type": "application/json"
        }

    def get_all_qr_codes(self, page=0, size=50):
        url = f"{self.cfg.base_url}/api/qr-codes"
        params = {"page": page, "size": size, "sort": "id,desc"}

        try:
            response = requests.get(url, headers=self._get_headers(), params=params)
            if response.status_code == 200:
                return True, response.json().get('content', [])
            return False, f"Error {response.status_code}: {response.text}"
        except Exception as e:
            return False, str(e)

    def generate_qr_code(self, data_dict):
        url = f"{self.cfg.base_url}/api/qr-codes/generate-with-image"

        payload = {
            "data": data_dict.get("data"),
            "type": data_dict.get("type", "URL"),
            "entityType": data_dict.get("entityType", ""),
            "entityId": data_dict.get("entityId", 0),
            "size": data_dict.get("size", 300),
            "generatedBy": "PythonDesktopApp",
            "generationReason": "Manual generation"
        }

        try:
            response = requests.post(url, json=payload, headers=self._get_headers())
            if response.status_code in (200, 201):
                return True, response.json()
            return False, f"Error {response.status_code}: {response.text}"
        except Exception as e:
            return False, str(e)

    def toggle_active_status(self, qr_id):
        url = f"{self.cfg.base_url}/api/qr-codes/{qr_id}/toggle-active"
        try:
            response = requests.patch(url, headers=self._get_headers())
            if response.status_code == 200:
                return True, response.json()
            return False, f"Error {response.status_code}"
        except Exception as e:
            return False, str(e)

    def delete_qr_code(self, qr_id):
        url = f"{self.cfg.base_url}/api/qr-codes/{qr_id}"
        try:
            response = requests.delete(url, headers=self._get_headers())
            if response.status_code in (200, 204):
                return True, "Deleted"
            return False, f"Error {response.status_code}"
        except Exception as e:
            return False, str(e)

    def download_qr_image(self, image_url):
        try:
            if not image_url.startswith("http"):
                 if image_url.startswith("/"):
                     url = f"{self.cfg.base_url}{image_url}"
                 else:
                     url = f"{self.cfg.base_url}/api/qr-codes/image/{image_url}"
            else:
                url = image_url

            headers = self._get_headers()

            response = requests.get(url, headers=headers)

            if response.status_code == 200:
                return True, response.content
            return False, f"Error {response.status_code}"
        except Exception as e:
            return False, str(e)
