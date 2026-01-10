# -*- coding: utf-8 -*-
"""
Skrypt do generowania obrazow PNG z plikow PlantUML.
Wymaga zainstalowanego PlantUML lub uzywa serwera online.

Sposob uzycia:
    python generate_diagrams.py [--local] [--format png|svg]

Opcje:
    --local     Uzyj lokalnej instalacji PlantUML (wymaga Java i plantuml.jar)
    --format    Format wyjsciowy: png (domyslnie) lub svg
"""

import os
import sys
import subprocess
import urllib.request
import urllib.parse
import zlib
import base64
import argparse
from pathlib import Path


# Katalog z plikami .puml
DIAGRAMS_DIR = Path(__file__).parent
OUTPUT_DIR = DIAGRAMS_DIR / "output"


def encode_plantuml(text):
    """Koduje tekst PlantUML do formatu URL dla serwera online."""
    # Kompresja deflate
    compressed = zlib.compress(text.encode('utf-8'))[2:-4]
    
    # Kodowanie base64 z alfabetem PlantUML
    alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_"
    result = ""
    
    for i in range(0, len(compressed), 3):
        if i + 2 < len(compressed):
            b1, b2, b3 = compressed[i], compressed[i+1], compressed[i+2]
        elif i + 1 < len(compressed):
            b1, b2, b3 = compressed[i], compressed[i+1], 0
        else:
            b1, b2, b3 = compressed[i], 0, 0
        
        result += alphabet[b1 >> 2]
        result += alphabet[((b1 & 0x3) << 4) | (b2 >> 4)]
        result += alphabet[((b2 & 0xF) << 2) | (b3 >> 6)]
        result += alphabet[b3 & 0x3F]
    
    return result


def generate_online(puml_file, output_file, format="png"):
    """Generuje diagram uzywajac serwera PlantUML online."""
    with open(puml_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    encoded = encode_plantuml(content)
    url = f"http://www.plantuml.com/plantuml/{format}/{encoded}"
    
    print(f"  Pobieranie z serwera online...")
    try:
        urllib.request.urlretrieve(url, output_file)
        return True
    except Exception as e:
        print(f"  BLAD: {e}")
        return False


def generate_local(puml_file, output_file, format="png", plantuml_jar="plantuml.jar"):
    """Generuje diagram uzywajac lokalnej instalacji PlantUML."""
    cmd = [
        "java", "-jar", plantuml_jar,
        f"-t{format}",
        "-o", str(output_file.parent),
        str(puml_file)
    ]
    
    print(f"  Uruchamianie PlantUML lokalnie...")
    try:
        result = subprocess.run(cmd, capture_output=True, text=True)
        if result.returncode != 0:
            print(f"  BLAD: {result.stderr}")
            return False
        return True
    except FileNotFoundError:
        print("  BLAD: Nie znaleziono Java lub plantuml.jar")
        return False


def main():
    parser = argparse.ArgumentParser(description="Generowanie diagramow UML z plikow PlantUML")
    parser.add_argument("--local", action="store_true", help="Uzyj lokalnej instalacji PlantUML")
    parser.add_argument("--format", choices=["png", "svg"], default="png", help="Format wyjsciowy")
    parser.add_argument("--jar", default="plantuml.jar", help="Sciezka do plantuml.jar (dla --local)")
    args = parser.parse_args()
    
    # Utworz katalog wyjsciowy
    OUTPUT_DIR.mkdir(exist_ok=True)
    
    # Znajdz wszystkie pliki .puml
    puml_files = list(DIAGRAMS_DIR.glob("*.puml"))
    
    if not puml_files:
        print("Nie znaleziono plikow .puml w katalogu diagrams/")
        return
    
    print(f"Znaleziono {len(puml_files)} plikow .puml")
    print(f"Format wyjsciowy: {args.format.upper()}")
    print(f"Metoda: {'lokalna' if args.local else 'online (plantuml.com)'}")
    print("-" * 50)
    
    success_count = 0
    
    for puml_file in puml_files:
        output_file = OUTPUT_DIR / f"{puml_file.stem}.{args.format}"
        print(f"\nPrzetwarzanie: {puml_file.name}")
        
        if args.local:
            success = generate_local(puml_file, output_file, args.format, args.jar)
        else:
            success = generate_online(puml_file, output_file, args.format)
        
        if success:
            print(f"  -> Zapisano: {output_file.name}")
            success_count += 1
        else:
            print(f"  -> NIEPOWODZENIE")
    
    print("-" * 50)
    print(f"Wygenerowano {success_count}/{len(puml_files)} diagramow")
    print(f"Pliki zapisano w: {OUTPUT_DIR}")


if __name__ == "__main__":
    main()
