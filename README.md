# Gasolina Barata

App Android (Jetpack Compose) que muestra las gasolineras más baratas cerca de ti en España.

- Gasolina 95, Gasolina 98, Diésel, Diésel Premium y GLP.
- Radio de 5, 10 o 25 km, ordenado por precio o por distancia.
- Precio medio de la zona y cuánto ahorras al llenar el depósito en cada gasolinera.
- Botón "Cómo llegar" que abre la app de mapas.
- La ubicación se usa solo en el móvil: se descarga la lista completa y se filtra en el dispositivo.
- Funciona sin conexión con la última lista descargada.
- Banner de AdMob con consentimiento RGPD (UMP).

## Datos

Servicio REST público del Ministerio para la Transición Ecológica y el Reto Demográfico, sin clave y actualizado cada 30 minutos:

```
https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/
```

Devuelve unas 12.000 gasolineras con todos los valores como texto y decimales con coma (`"1,459"`). Se descartan las de venta restringida (`"Tipo Venta": "R"`). La lista se guarda en disco y no se vuelve a descargar hasta pasados 30 minutos.

## Estructura

```
core/   Kotlin puro, sin Android: lectura de la API, distancias, búsqueda y ahorro (con tests)
app/    Android: Compose, ViewModel, DataStore, OkHttp, AdMob
  data/       API, caché en disco, ajustes
  location/   ubicación con el LocationManager del sistema (sin Google Play Services)
  ui/         pantallas y componentes
  ads/        consentimiento y banner
```

## Compilar

Requisitos: Android Studio reciente (JDK 17+), Android SDK 36.

```bash
./gradlew :core:test        # tests de la lógica
./gradlew installDebug      # instala en el emulador o el móvil (anuncios de prueba)
./gradlew bundleRelease     # AAB para Google Play
```

En el emulador, fija una ubicación en España desde *Extended controls → Location*.

### Release

1. Crea el keystore de subida y un `keystore.properties` en la raíz (no se sube a git):
   ```
   storeFile=upload.jks
   storePassword=...
   keyAlias=upload
   keyPassword=...
   ```
2. Pon los IDs reales de AdMob en `~/.gradle/gradle.properties`:
   ```
   ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
   ADMOB_BANNER_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
   ```
   Sin ellos, el build de release usa las unidades de prueba de Google.
3. Publica `docs/privacy.html` (por ejemplo con GitHub Pages) y enlázala en la ficha de Play. En la ficha, declara que la ubicación se usa solo para la funcionalidad de la app y no se comparte.

## Licencia

Los datos son del Ministerio para la Transición Ecológica y el Reto Demográfico y se muestran con atribución.
