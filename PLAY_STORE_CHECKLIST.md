# Google Play Checkliste

## Vor dem Upload

- App-Name final festlegen
- App-Icon finalisieren
- Paketname final bestätigen: `com.notfallkompass.risikoanalyse`
- Version Code erhöhen, wenn ein Update hochgeladen wird
- Release App Bundle signieren
- App auf mindestens einem echten Android-Gerät testen
- PDF-Export testen
- Bearbeiten, Verschieben und Zurücksetzen testen

## Store-Eintrag

- Kurzbeschreibung
- Vollständige Beschreibung
- Screenshots für Handy
- Optional: Screenshots für Tablet
- Feature-Grafik
- Kategorie
- Kontakt-E-Mail
- Datenschutzangaben

## Datenschutz

Die aktuelle Version speichert Daten lokal auf dem Gerät und hat keine Serververbindung. Sobald Login, Cloud-Speicherung, Kundendaten oder personenbezogene Daten ergänzt werden, müssen Datenschutzangaben und Datenschutzerklärung entsprechend erweitert werden.

## GitHub Actions

- GitHub Repository angelegt
- Workflow sichtbar unter `Actions > Android Build - APK und AAB`
- Upload-Keystore erstellt und sicher gesichert
- GitHub Secrets gesetzt:
  - `ANDROID_KEYSTORE_BASE64`
  - `ANDROID_KEYSTORE_PASSWORD`
  - `ANDROID_KEY_ALIAS`
  - `ANDROID_KEY_PASSWORD`
- Workflow ausgeführt
- Artifact heruntergeladen
- Datei `risikoanalyse-release-...-signed.aab` für Google Play verwendet
