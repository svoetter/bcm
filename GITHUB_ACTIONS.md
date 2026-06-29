# GitHub Actions: Android `.aab` automatisch bauen

Diese Version enthält bereits einen GitHub-Actions-Workflow:

```text
.github/workflows/android-build.yml
```

Der Workflow baut automatisch:

- eine **Debug-APK** zum Testen auf dem Android-Handy
- eine **Release-AAB** für Google Play

Ohne Signing-Secrets wird die AAB nur **unsigned** erzeugt. Diese Datei ist praktisch zum technischen Test, aber **nicht** für Google Play geeignet. Mit Signing-Secrets wird die AAB signiert und kann für die Play Console verwendet werden.

## 1. Projekt zu GitHub hochladen

Der GitHub-Repository-Root muss diese Dateien/Ordner enthalten:

```text
.github/
app/
build.gradle
gradle.properties
settings.gradle
```

Wenn du den kompletten Ordner `risiko-android-native-github-actions` als Repository hochlädst, passt die Struktur bereits.

## 2. Upload-Keystore erstellen

Für Google Play brauchst du einen Upload-Keystore. Den kannst du entweder mit Android Studio erstellen oder mit `keytool`, das mit Java/JDK mitkommt.

Beispiel-Befehl:

```bash
keytool -genkeypair \
  -v \
  -keystore upload-keystore.jks \
  -storetype JKS \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias upload
```

Die Passwörter gut sichern. Keystore und Passwörter brauchst du später für Updates der App.

## 3. Keystore in Base64 umwandeln

### macOS/Linux

```bash
./scripts/keystore-to-base64.sh upload-keystore.jks
```

Den ausgegebenen langen Text kopieren.

### Windows PowerShell

```powershell
.\scripts\keystore-to-base64.ps1 .\upload-keystore.jks
```

Den ausgegebenen langen Text kopieren.

## 4. GitHub Secrets anlegen

In GitHub öffnen:

```text
Repository > Settings > Secrets and variables > Actions > New repository secret
```

Diese vier Secrets anlegen:

| Secret | Inhalt |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Base64-Text der Datei `upload-keystore.jks` |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore-Passwort |
| `ANDROID_KEY_ALIAS` | Alias, z. B. `upload` |
| `ANDROID_KEY_PASSWORD` | Key-Passwort |

## 5. Workflow starten

In GitHub öffnen:

```text
Actions > Android Build - APK und AAB > Run workflow
```

Nach dem Lauf findest du unten im Workflow unter **Artifacts** eine ZIP-Datei. Darin liegen:

```text
risikoanalyse-debug-<nummer>.apk
risikoanalyse-release-<nummer>-signed.aab
```

Wenn keine Secrets gesetzt sind, heißt die AAB:

```text
risikoanalyse-release-<nummer>-unsigned.aab
```

Diese unsigned Datei bitte nicht in Google Play hochladen.

## 6. Datei für Google Play

Für Google Play verwendest du die Datei:

```text
risikoanalyse-release-<nummer>-signed.aab
```

Die Debug-APK ist nur zum schnellen Installieren und Testen auf einem Android-Gerät gedacht.

## Hinweise

- Die Datei `upload-keystore.jks` niemals öffentlich ins Repository laden.
- Der Keystore gehört in ein sicheres Backup oder einen Passwortmanager.
- Bei jedem Update der App muss `versionCode` in `app/build.gradle` erhöht werden.
- Wenn du später eine Veröffentlichung direkt zu Google Play automatisieren möchtest, kann ein zweiter Workflow mit Play-Console-Service-Account ergänzt werden.
