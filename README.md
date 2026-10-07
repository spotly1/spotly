# Spotly

Aplicación Android para compartir descubrimientos urbanos mediante fotos, descripciones y ubicación.

## Integrantes

- Agustin Cabeda
- Bruno Said

## Funcionalidades actuales

- Registro, inicio y cierre de sesión con Firebase Authentication.
- Perfil con username, descripción y foto editable.
- Creación de publicaciones con cámara o galería y ubicación opcional.
- Feed con las últimas 50 publicaciones.
- Cuadrícula de publicaciones propias y vista de detalle.
- Dirección aproximada y mapa integrado de OpenStreetMap.
- Compresión de fotos y almacenamiento en Cloudinary.

## Tecnologías y arquitectura

Kotlin, Jetpack Compose, Material 3, Navigation Compose con `NavController`, Firebase Authentication, Firestore, Cloudinary y Coil.

El proyecto usa MVVM y está dividido en 10 módulos:

- `app`: inicio, navegación e inyección manual con `AppContainer`.
- `features:auth`, `posts`, `profile` y `search`: pantallas por funcionalidad; búsqueda todavía es una pantalla provisional.
- `core:domain`: modelos, interfaces de repositorios y casos de uso en Kotlin puro.
- `core:data`: implementaciones de repositorios y mappers.
- `core:network`: clientes externos, DTO y preparación de imágenes.
- `core:database`: caché de direcciones en memoria; todavía no usa Room.
- `core:ui`: componentes, tema y recursos compartidos.

Las Routes conectan los ViewModels con las Screens, que reciben datos y callbacks. Los ViewModels usan `StateFlow` y estados `sealed`; los resultados de operaciones usan `AppResult`. Guardar y publicar generan estados que la Route consume al navegar. Las features dependen del dominio y la UI compartida, no de las implementaciones de datos.

## Cómo ejecutarlo

1. Clonar el repositorio y abrir la carpeta raíz en Android Studio.
2. Obtener `google-services.json` de un integrante autorizado o desde Firebase Console → Configuración del proyecto → app Android `com.example.spotly`.
3. Copiarlo en `app/google-services.json` (está excluido de Git).
4. Sincronizar Gradle y ejecutar `app` en un emulador o celular con Android 8.0 / API 26 o superior.

Al usar el Firebase del equipo no hay que volver a publicar reglas por cada clonación. Para usar un backend propio, hay que configurar Authentication, Firestore y Cloudinary, adaptar sus valores y publicar las reglas correspondientes; no alcanza con cambiar el JSON.

## Datos y seguridad

Las reglas del repositorio están en `firestore.rules`:

- `users`: perfiles públicos para usuarios autenticados; cada dueño edita su descripción y foto.
- `privateUsers`: email privado, accesible únicamente por su dueño.
- `usernames`: reservas de nombres únicos.
- `posts`: publicaciones consultables por usuarios autenticados; creación validada para su autor. Edición y eliminación todavía bloqueadas.

La búsqueda/listado de usuarios sigue bloqueada. Cambiar el archivo de reglas local no actualiza Firebase automáticamente.

## Validación

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :core:domain:test testDebugUnitTest lintDebug
```

Las pruebas de interfaz están en las features de publicaciones y perfil y requieren un dispositivo. Las pruebas de seguridad están en `tests/firestore` y usan el emulador de Firebase, no la base real.

## Pendiente

- Edición y eliminación de publicaciones.
- Búsqueda de usuarios, likes y seguimiento.
- Notificaciones.
- Caché offline con Room.
