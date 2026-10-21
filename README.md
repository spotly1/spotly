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
- Detalle de publicación desde el feed o la lista del perfil.
- Edición de la descripción y eliminación con confirmación, solo para el autor.
- Cuadrícula de publicaciones propias y vista de detalle.
- Dirección aproximada y mapa integrado de OpenStreetMap.
- Compresión de fotos y almacenamiento en Cloudinary.

## Tecnologías y arquitectura

Kotlin, Jetpack Compose, Material 3, Navigation Compose con `NavController`, Firebase Authentication, Firestore, Cloudinary y Coil.

El proyecto usa MVVM y está dividido en 11 módulos:

- `app`: inicio, navegación e inyección manual con `AppContainer`.
- `features:feed`: inicio y listado de publicaciones, con su Route, Screen, ViewModel y estado.
- `features:posts`: creación, detalle, edición de descripción y eliminación de publicaciones.
- `features:auth`, `profile` y `search`: autenticación, perfiles y búsqueda; búsqueda todavía es una pantalla provisional.
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
- `posts`: publicaciones consultables por usuarios autenticados; creación validada, edición de descripción y eliminación solo por el autor. Foto, ubicación, autor y fecha no se modifican al editar.

La búsqueda/listado de usuarios sigue bloqueada. Cambiar el archivo de reglas local no actualiza Firebase automáticamente.
Para habilitar la edición y eliminación, el responsable del backend debe publicar el `firestore.rules` actualizado. El borrado quita el documento de Firestore; la imagen permanece en Cloudinary hasta su limpieza manual.

## Validación

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

## Pendiente

- Búsqueda de usuarios, likes y seguimiento.
- Notificaciones.
- Caché offline con Room.
