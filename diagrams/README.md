# Diagramy UML - System QRWare

Ten katalog zawiera diagramy UML opisujace architekture i dzialanie systemu QRWare.

## Lista diagramow

| Plik | Typ | Opis |
|------|-----|------|
| `use_case_diagram.puml` | Diagram przypadkow uzycia | Wszystkie scenariusze dla Administratora, Menadzera i Magazyniera |
| `class_diagram.puml` | Diagram klas | Model domenowy systemu z encjami i relacjami |
| `erd_diagram.puml` | Diagram ERD | Schemat bazy danych PostgreSQL |
| `component_diagram.puml` | Diagram komponentow | Architektura systemu (Android + Spring Boot) |
| `deployment_diagram.puml` | Diagram wdrozenia | Infrastruktura i srodowisko uruchomieniowe |
| `sequence_order_pick.puml` | Diagram sekwencji | Proces realizacji zlecenia kompletacji (PICK) |
| `sequence_inbound.puml` | Diagram sekwencji | Proces przyjecia towaru (INBOUND) |
| `sequence_qr_scan.puml` | Diagram sekwencji | Proces skanowania kodow QR |
| `activity_order_flow.puml` | Diagram aktywnosci | Przeplyw realizacji zlecenia magazynowego |
| `state_order.puml` | Diagram stanow | Cykl zycia zlecenia (Order) |
| `state_inventory.puml` | Diagram stanow | Cykl zycia stanu magazynowego (InventoryItem) |

## Generowanie obrazow

### Metoda 1: Serwer online (zalecana)

Uzyj dolaczonego skryptu Python:

```bash
cd diagrams
python generate_diagrams.py
```

Obrazy PNG zostana zapisane w katalogu `output/`.

### Metoda 2: Lokalna instalacja PlantUML

1. Zainstaluj Jave (JRE 8+)
2. Pobierz [plantuml.jar](https://plantuml.com/download)
3. Uruchom:

```bash
python generate_diagrams.py --local --jar sciezka/do/plantuml.jar
```

### Metoda 3: Edytor online

Skopiuj zawartosc pliku `.puml` do edytora:
- https://www.plantuml.com/plantuml/uml
- https://plantuml-editor.kkeisuke.com/

### Metoda 4: IDE z pluginem

- **IntelliJ IDEA**: Plugin "PlantUML Integration"
- **VS Code**: Extension "PlantUML"
- **Eclipse**: Plugin "PlantUML"

## Formaty wyjsciowe

Skrypt wspiera generowanie w formatach:
- **PNG** (domyslny) - `python generate_diagrams.py --format png`
- **SVG** (wektorowy) - `python generate_diagrams.py --format svg`

## Struktura katalogu

```
diagrams/
├── README.md                    # Ten plik
├── generate_diagrams.py         # Skrypt generujacy obrazy
├── use_case_diagram.puml        # Diagram przypadkow uzycia
├── class_diagram.puml           # Diagram klas
├── erd_diagram.puml             # Diagram ERD
├── component_diagram.puml       # Diagram komponentow
├── deployment_diagram.puml      # Diagram wdrozenia
├── sequence_order_pick.puml     # Sekwencja - kompletacja
├── sequence_inbound.puml        # Sekwencja - przyjecie
├── sequence_qr_scan.puml        # Sekwencja - skanowanie QR
├── activity_order_flow.puml     # Aktywnosc - przeplyw zlecenia
├── state_order.puml             # Stany - zlecenie
├── state_inventory.puml         # Stany - stan magazynowy
└── output/                      # Wygenerowane obrazy (PNG/SVG)
    ├── use_case_diagram.png
    ├── class_diagram.png
    └── ...
```

## Wymagania

- Python 3.7+
- Polaczenie internetowe (dla metody online)
- Java 8+ i plantuml.jar (dla metody lokalnej)
