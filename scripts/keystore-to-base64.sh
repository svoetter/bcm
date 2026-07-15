#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -ne 1 ]; then
  echo "Nutzung: $0 <pfad-zur-keystore-datei.jks>" >&2
  exit 1
fi

file="$1"
if [ ! -f "$file" ]; then
  echo "Datei nicht gefunden: $file" >&2
  exit 1
fi

if base64 --help 2>&1 | grep -q -- '-w'; then
  base64 -w 0 "$file"
else
  base64 < "$file" | tr -d '\n'
fi
printf '\n'
