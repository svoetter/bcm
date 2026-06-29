# Risikoanalyse Native Android App

Dies ist eine native Android-Version der Risikoanalyse-App. Sie ist **kein WebView/PWA-Wrapper**, sondern nutzt Android Views, Canvas-Zeichnung, lokale Speicherung und PDF-Export über Android-Funktionen.

## Enthaltene Funktionen

- 20 vorgegebene Risikofelder beim Start
- Orange Felder für Arbeitnehmerschutz und **Verstoß gegen Vorschriften**
- Graue Felder für andere Unternehmensrisiken
- Kleines Symbol pro Feld
- Achsenbeschriftung mit Symbolen
- Risiken antippen und bearbeiten
- Risiken per Ziehen in andere Matrixfelder verschieben
- Zusätzliche Risiken anlegen
- Vorgegebene Standardfelder sind bearbeitbar, aber nicht löschbar
- Zusätzliche Risiken können gelöscht werden
- Lokale Speicherung auf dem Gerät über SharedPreferences
- A4-PDF-Export im Querformat mit 300-dpi-Seitengröße
- Keine Serververbindung, keine externe Bibliothek, keine Anmeldung

## Projekt öffnen

1. Android Studio öffnen.
2. `File > Open` wählen.
3. Den Ordner `risiko-android-native` auswählen.
4. Gradle Sync abwarten.
5. App über `Run` auf Emulator oder Android-Handy starten.

## Test auf echtem Android-Handy

1. Am Handy Entwickleroptionen aktivieren.
2. USB-Debugging aktivieren.
3. Handy per USB anschließen.
4. In Android Studio oben das Gerät auswählen.
5. `Run` drücken.


## GitHub Actions / Cloud-Build

Diese Version enthält einen fertigen GitHub-Actions-Workflow unter:

```text
.github/workflows/android-build.yml
```

Damit kannst du direkt in GitHub eine Debug-APK und eine Release-AAB bauen lassen. Die genaue Einrichtung mit Keystore und GitHub Secrets steht in `GITHUB_ACTIONS.md`.

## Release für Google Play

Für Google Play wird üblicherweise ein signiertes Android App Bundle benötigt.

In Android Studio:

1. `Build > Generate Signed Bundle / APK`
2. `Android App Bundle` auswählen
3. Keystore erstellen oder vorhandenen Keystore verwenden
4. Release Bundle erzeugen
5. `.aab` Datei in der Google Play Console hochladen

## Wichtige Projektdaten

- Package/Application ID: `com.notfallkompass.risikoanalyse`
- App-Name: `Risikoanalyse`
- Version: `0.1.0`
- Version Code: `1`
- Min SDK: `23`
- Target SDK: `35`
- Compile SDK: `35`
- Android Gradle Plugin: `8.7.3`

## Nächste sinnvolle Erweiterungen

- Firmen-/Kundenverwaltung
- Mehrere Risikoanalysen pro Kunde
- Bericht mit Maßnahmenplan als mehrseitiges PDF
- Datenexport/-import als JSON
- Logo, Corporate Design und eigenes App-Icon
- Optional: Cloud-Synchronisierung mit Login
- Optional: Tablet-optimiertes Querformat
