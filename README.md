# Gasolina Barata

![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white) ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white) ![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)

Gasolineras más baratas cerca de ti en España con los precios oficiales del Ministerio, ahorro por depósito y coste de un viaje.

App Android nativa desarrollada en **Kotlin** con **Jetpack Compose** (Material 3).

- Gasolina 95, Gasolina 98, Diésel, Diésel Premium y GLP.
- Radio de 5, 10 o 25 km, ordenado por precio o por distancia.
- Precio medio de la zona y cuánto ahorras al llenar el depósito en cada gasolinera.
- Botón "Cómo llegar" que abre la app de mapas.
- La ubicación se usa solo en el móvil: se descarga la lista completa y se filtra en el dispositivo.
- Funciona sin conexión con la última lista descargada.
- Calculadora del coste de un viaje repostando en la más barata frente al precio medio (Premium o 24 h con un anuncio).
- Anuncios de AdMob y versión Premium sin anuncios.

## Datos

Servicio REST público del Ministerio para la Transición Ecológica y el Reto Demográfico, sin clave y actualizado cada 30 minutos:

```
https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/
```

Devuelve unas 12.000 gasolineras con todos los valores como texto y decimales con coma (`"1,459"`). Se descartan las de venta restringida (`"Tipo Venta": "R"`). La lista se guarda en disco y no se vuelve a descargar hasta pasados 30 minutos.

## Anuncios y Premium

| Formato | Dónde | Límite |
|---|---|---|
| Banner adaptativo | Pantallas de consulta | — |
| Intersticial | Solo en pausas naturales (al volver de Ajustes a la lista) | Uno cada 3 min como mucho |
| App Open | Al volver a la app desde segundo plano, nunca en el primer arranque | Uno cada 5 min como mucho |
| Recompensado | Cuando el usuario lo pide para desbloquear la calculadora del coste de un viaje durante 24 h | — |

Premium (Google Play Billing) quita todos los anuncios y desbloquea la calculadora del coste de un viaje para siempre:

- `premium_monthly`: suscripción mensual.
- `premium_lifetime`: producto de compra única.

Créalos en Play Console con esos IDs. Al arrancar se consulta la tienda, así que si la suscripción caduca vuelven los anuncios. Para una app con muchos ingresos conviene añadir verificación de compras en servidor.

Los IDs de AdMob van en `admob.properties` (no se sube a git; plantilla en `admob.properties.example`). Sin ese archivo se usan las unidades de prueba de Google. El consentimiento RGPD se pide con UMP antes de cargar ningún anuncio.

## Idiomas

Castellano (por defecto), catalán, gallego e inglés.

## Estructura

```
core/   Kotlin puro, sin Android: lectura de la API, distancias, búsqueda y ahorro (con tests)
app/    Android: Compose, ViewModel, DataStore, OkHttp, AdMob
  data/       API, caché en disco, ajustes
  location/   ubicación con el LocationManager del sistema (sin Google Play Services)
  ui/         pantallas y componentes
  monetization/  consentimiento, anuncios y compras de Premium
```

## Compilar

Requisitos: Android Studio reciente, Android SDK 36 y un JDK entre el 17 y el 25 (probado con Java 25). Gradle 9.7, Android Gradle Plugin 9.4 (con Kotlin integrado) y Kotlin 2.3.

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
2. Copia `admob.properties.example` como `admob.properties` y pon los IDs reales de AdMob de esta app.
3. Crea en Play Console los productos `premium_monthly` (suscripción) y `premium_lifetime` (compra única).
4. Publica `docs/privacy.html` (por ejemplo con GitHub Pages) y enlázala en la ficha de Play. En la ficha, declara que la ubicación se usa solo para la funcionalidad de la app y no se comparte.

## Licencia

Los datos son del Ministerio para la Transición Ecológica y el Reto Demográfico y se muestran con atribución.
