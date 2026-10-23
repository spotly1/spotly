# Spotly

Aplicación Android para compartir descubrimientos urbanos mediante fotos, descripciones y ubicación.

## Integrantes

- Agustin Cabeda
- Bruno Said

## Funcionalidades

- Registro, inicio y cierre de sesión.
- Perfil con foto, descripción y cuadrícula de publicaciones.
- Publicaciones con cámara o galería, descripción y ubicación opcional.
- Detalle de publicaciones y edición de descripción o eliminación por su autor.
- Inicio con publicaciones de las personas que seguís.
- Explorar con publicaciones de personas que no seguís, excluyendo las propias.
- Búsqueda de usuarios por inicio del username.
- Seguidores, seguidos y likes, con acceso a las listas de personas.
- Ubicación aproximada y mapa integrado de OpenStreetMap.
- Notificaciones de likes y nuevos seguidores.
- Lectura sin conexión del contenido guardado de Inicio.

## Tecnologías y estructura

Kotlin, Jetpack Compose, Material 3, Navigation Compose, Firebase Authentication, Firestore, Cloudinary, Room y Coil.

Usamos MVVM, casos de uso e inyección manual de dependencias con `AppContainer`:

- `app`: arranque, navegación y conexión de dependencias.
- `features`: autenticación, inicio y explorar, publicaciones, perfiles, búsqueda y notificaciones.
- `core/domain`: modelos, contratos y casos de uso.
- `core/data`: repositorios y mappers.
- `core/network`: servicios externos y DTO.
- `core/database`: persistencia local.
- `core/ui`: componentes, tema y recursos compartidos.

## Cómo ejecutarlo

1. Clonar el repositorio y abrir la carpeta raíz en Android Studio.
2. Obtener `google-services.json` de un integrante autorizado o desde Firebase Console → Configuración del proyecto → app Android `com.example.spotly`.
3. Colocarlo en `app/google-services.json`. El archivo está excluido de Git y no debe subirse al repositorio.
4. Sincronizar Gradle y ejecutar el módulo `app` en un emulador o celular con Android 8.0 / API 26 o superior.

Con el Firebase del equipo no hace falta volver a configurar reglas e índices por cada clonación. Para usar servicios propios, hay que configurar Authentication con email y contraseña, Firestore y Cloudinary, adaptar sus valores en el proyecto y publicar `firestore.rules` y `firestore.indexes.json`. Cambiar solo el JSON no alcanza.

## Datos y permisos

Firebase Authentication gestiona las cuentas; Firestore guarda perfiles, publicaciones e interacciones; Cloudinary almacena las imágenes. Las reglas de `firestore.rules` protegen los datos privados y las operaciones de cada usuario.

La ubicación se solicita al agregarla a una publicación y puede ser aproximada. En Android 13 o superior se pide permiso para mostrar notificaciones del sistema.

## Sin conexión y notificaciones

Room conserva hasta 50 publicaciones de Inicio por cuenta y permite abrir sus detalles sin Internet. Las fotos se ven si siguen disponibles en la caché de imágenes. Explorar, mapas y acciones sociales requieren conexión.

La campanita conserva los avisos en Firestore. También se generan notificaciones locales de Android mientras la app está visible; no son push y no se garantiza recibirlas con la app cerrada.
