# Cálculo de Cuentas — APK sin Android Studio

Genera un **APK firmado** usando **GitHub Actions** (gratis) sin Android Studio.

## Pasos
1) Crea un repositorio en GitHub y sube todo el contenido de esta carpeta.
2) Ve a **Settings → Secrets and variables → Actions** y agrega estos secretos:
   - `CC_KEYSTORE_B64` → tu keystore en **base64** (no subas el archivo al repo).
   - `CC_KEYSTORE_PASSWORD` → contraseña de la keystore (**0250** si es la que usaste).
   - `CC_KEY_ALIAS` → alias (**Madariaga**).
   - `CC_KEY_PASSWORD` → contraseña de la clave (puede ser **0250**).
3) Ve a **Actions → Build Signed APK → Run workflow**.
4) Al terminar, descarga el artifact **CalculoDeCuentas-APK → app-release.apk**.
5) En tu teléfono Android: abre el `.apk` e **instala**.

### Convertir tu keystore a base64
- **Windows (PowerShell)**: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("calculo_de_cuentas.keystore")) > keystore.b64.txt`
- **Mac / Linux**: `base64 calculo_de_cuentas.keystore > keystore.b64.txt`

> **No tienes keystore?** Puedes crear uno con `keytool` (viene con Java).
