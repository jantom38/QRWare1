import requests
import json
from config import ConfigManager


class OrderService:
    def __init__(self):
        self.cfg = ConfigManager()

    def _get_headers(self):
        return {
            "Authorization": f"Bearer {self.cfg.access_token}",
            "Content-Type": "application/json"
        }

    def get_all_orders(self, page=0, size=20):
        url = f"{self.cfg.base_url}/api/orders"
        params = {"page": page, "size": size, "sort": "createdAt,desc"}
        try:
            response = requests.get(url, headers=self._get_headers(), params=params)
            if response.status_code == 200:
                data = response.json()
                content = data.get('data', {}).get('content', []) if 'data' in data else []
                return True, content
            print(f"DEBUG: Błąd pobierania zamówień: {response.status_code} - {response.text}")
            return False, f"Błąd {response.status_code}"
        except Exception as e:
            print(f"DEBUG: Wyjątek get_all_orders: {e}")
            return False, str(e)

    def get_order_details(self, order_id):
        url = f"{self.cfg.base_url}/api/orders/{order_id}"
        try:
            response = requests.get(url, headers=self._get_headers())
            if response.status_code == 200:
                return True, response.json().get('data', {})
            return False, f"Błąd {response.status_code}"
        except Exception as e:
            return False, str(e)

    def create_order(self, payload):
        url = f"{self.cfg.base_url}/api/orders"
        try:
            response = requests.post(url, json=payload, headers=self._get_headers())
            if response.status_code in (200, 201):
                return True, response.json().get('data', {})
            return False, f"Błąd {response.status_code}: {response.text}"
        except Exception as e:
            return False, str(e)

    def add_order_item(self, order_id, payload):
        url = f"{self.cfg.base_url}/api/order-items/order/{order_id}"
        try:
            response = requests.post(url, json=payload, headers=self._get_headers())
            if response.status_code in (200, 201):
                return True, response.json().get('data', {})
            return False, f"Błąd {response.status_code}: {response.text}"
        except Exception as e:
            return False, str(e)

    def update_order(self, order_id, payload):
        url = f"{self.cfg.base_url}/api/orders/{order_id}"
        try:
            response = requests.put(url, json=payload, headers=self._get_headers())
            if response.status_code == 200:
                return True, response.json().get('data', {})
            return False, f"Błąd {response.status_code}: {response.text}"
        except Exception as e:
            return False, str(e)

    def update_order_status(self, order_id, action, reason=None):
        url = f"{self.cfg.base_url}/api/orders/{order_id}/{action}"
        payload = {}
        if action == 'cancel' and reason:
            payload = {"reason": reason}
        try:
            response = requests.put(url, json=payload, headers=self._get_headers())
            if response.status_code == 200:
                return True, "Status zaktualizowany"
            return False, f"Błąd {response.status_code}: {response.text}"
        except Exception as e:
            return False, str(e)

    def get_statistics(self):
        url = f"{self.cfg.base_url}/api/orders/statistics/status"
        try:
            response = requests.get(url, headers=self._get_headers())
            if response.status_code == 200:
                return True, response.json().get('data', [])
            return False, "Błąd statystyk"
        except Exception as e:
            return False, str(e)

    def check_inventory_availability(self, location_id, product_id):
        url = f"{self.cfg.base_url}/api/inventory"
        params = {
            "locationId": location_id,
            "productId": product_id,
            "size": 100
        }
        try:
            response = requests.get(url, headers=self._get_headers(), params=params)
            if response.status_code == 200:
                items = self._extract_list(response, "INVENTORY_CHECK")
                total_qty = 0
                for item in items:
                    total_qty += item.get('quantity', 0)
                return total_qty
            return 0
        except Exception as e:
            print(f"DEBUG: Błąd sprawdzania stanu: {e}")
            return 0

    def _extract_list(self, response, context_name=""):
        try:
            json_data = response.json()
            data_node = json_data.get('data')

            if data_node is None:
                data_node = json_data

            if isinstance(data_node, dict) and 'content' in data_node:
                items = data_node['content']
                print(f"DEBUG: {context_name} - Znaleziono stronę, elementów: {len(items)}")
                return items

            if isinstance(data_node, list):
                print(f"DEBUG: {context_name} - Znaleziono listę, elementów: {len(data_node)}")
                return data_node

            print(f"DEBUG: {context_name} - Nie rozpoznano formatu. JSON: {str(json_data)[:100]}...")
            return []
        except Exception as e:
            print(f"DEBUG: {context_name} - Błąd parsowania: {e}")
            return []

    def get_simple_products(self):
        url = f"{self.cfg.base_url}/api/products"
        try:
            response = requests.get(url, headers=self._get_headers(), params={"size": 500})
            if response.status_code == 200:
                items = self._extract_list(response, "PRODUKTY")
                result = []
                for p in items:
                    pid = p.get('id')
                    name = p.get('name', 'Bez nazwy')
                    sku = p.get('sku', '')
                    if pid:
                        result.append((pid, name, sku))
                return result
            else:
                print(f"DEBUG: Produkty błąd HTTP {response.status_code}")
                return []
        except Exception as e:
            print(f"DEBUG: Wyjątek pobierania produktów: {e}")
            return []

    def get_simple_locations(self):
        url = f"{self.cfg.base_url}/api/locations"
        try:
            response = requests.get(url, headers=self._get_headers(), params={"size": 500})
            if response.status_code == 200:
                items = self._extract_list(response, "LOKALIZACJE")
                result = []
                for l in items:
                    lid = l.get('id')
                    name = l.get('name', 'Bez nazwy')
                    code = l.get('code', '')
                    if lid:
                        result.append((lid, name, code))
                return result
            else:
                print(f"DEBUG: Lokalizacje błąd HTTP {response.status_code}")
                return []
        except Exception as e:
            print(f"DEBUG: Wyjątek pobierania lokalizacji: {e}")
            return []

    def get_simple_users(self):
        url = f"{self.cfg.base_url}/api/users"
        try:
            response = requests.get(url, headers=self._get_headers())
            if response.status_code == 200:
                items = self._extract_list(response, "UŻYTKOWNICY")
                result = []
                for u in items:
                    uid = u.get('id')
                    username = u.get('username', '')
                    fullname = u.get('fullName', '')
                    if uid:
                        result.append((uid, username, fullname))
                return result
            else:
                print(f"DEBUG: Użytkownicy błąd HTTP {response.status_code}")
                return []
        except Exception as e:
            print(f"DEBUG: Wyjątek pobierania użytkowników: {e}")
            return []