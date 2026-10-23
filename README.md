# Spotly

Aplicación Android para compartir descubrimientos urbanos mediante fotos, descripciones y ubicación.

## Integrantes

- Agustin Cabeda
- Bruno Said

## Funcionalidades actuales

- Registro, inicio y cierre de sesión con Firebase Authentication.
- Perfil con username, descripción y foto editable.
- Creación de publicaciones con cámara o galería y ubicación opcional.
- Inicio con las últimas 50 publicaciones de las personas que seguís; las propias quedan en el perfil.
- Explorar, junto a Inicio, muestra publicaciones de personas que no seguís y excluye las propias. Consulta tandas de 50 por fecha e ID, continúa si una tanda queda vacía al filtrar y ofrece Cargar más. Se reinicia al cambiar los seguidos o al actualizar; requiere conexión y no modifica la caché de Inicio. Para grandes volúmenes convendrá un feed de descubrimiento preparado en el backend.
- Seguir y dejar de seguir usuarios, con contadores de seguidores y seguidos en tiempo real.
- Búsqueda por inicio del username (mínimo 2 caracteres, hasta 20 resultados, sin distinguir mayúsculas), con acceso al perfil. El registro ya guarda los nombres en minúsculas y el buscador normaliza el texto de la misma manera.
- Detalle de publicación desde el feed o la lista del perfil.
- Edición de la descripción y eliminación con confirmación, solo para el autor.
- Cuadrícula de publicaciones propias y vista de detalle.
- Dirección aproximada y mapa integrado de OpenStreetMap.
- Compresión de fotos y almacenamiento en Cloudinary.

Los likes se guardan en `posts/{postId}/likes/{uid}` y se sincronizan entre Inicio y detalle. Cada usuario solo puede modificar su propio like. Para habilitarlos hay que publicar las reglas actualizadas; no necesitan otro índice. Los contadores escuchan la subcolección completa (adecuado para el TP; para gran volumen conviene agregarlos en el backend). Borrar un post no borra sus subcolecciones: los likes quedan inaccesibles y su limpieza requiere un proceso de backend posterior.

Al tocar Seguidores o Seguidos, tanto en el perfil propio como en el ajeno, se abre la lista de personas con acceso a cada perfil. Usa las relaciones existentes y solo consulta datos públicos.

El número de Me gusta en Inicio y detalle abre la lista de personas que reaccionaron, con acceso a sus perfiles y actualización de la lista al cambiar los likes. No requiere nuevas reglas ni índices si ya están publicadas las reglas de likes.

## Tecnologías y arquitectura

Kotlin, Jetpack Compose, Material 3, Navigation Compose con `NavController`, Firebase Authentication, Firestore, Cloudinary y Coil.

El proyecto usa MVVM y está dividido en 12 módulos:

- `app`: inicio, navegación e inyección manual con `AppContainer`.
- `features:feed`: inicio y listado de publicaciones, con su Route, Screen, ViewModel y estado.
- `features:notifications`: campanita, avisos de likes y seguimientos, filtro sin leer y apertura del destino.
- `features:posts`: creación, detalle, edición de descripción y eliminación de publicaciones.
- `features:auth`, `profile` y `search`: autenticación, perfiles y búsqueda de usuarios.
- `core:domain`: modelos, interfaces de repositorios y casos de uso en Kotlin puro.
- `core:data`: implementaciones de repositorios y mappers.
- `core:network`: clientes externos, DTO y preparación de imágenes.
- `core:database`: Room para el feed por cuenta y caché de direcciones en memoria.
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
- `follows`: relaciones por UID; solo quien sigue puede crear o quitar su seguimiento.
- `posts`: publicaciones consultables por usuarios autenticados; creación validada, edición de descripción y eliminación solo por el autor. Foto, ubicación, autor y fecha no se modifican al editar.

La búsqueda consulta únicamente perfiles públicos y requiere publicar las reglas actualizadas (consultas autenticadas con límite de 20). No cambia el esquema de las cuentas existentes ni consulta emails. Cambiar el archivo de reglas local no actualiza Firebase automáticamente.
Para habilitar la edición y eliminación, el responsable del backend debe publicar el `firestore.rules` actualizado. El borrado quita el documento de Firestore; la imagen permanece en Cloudinary hasta su limpieza manual.

## Validación

Para habilitar los seguimientos, publicar `firestore.rules` y crear el índice de `firestore.indexes.json`: colección `posts`, `authorId` ascendente y `createdAt` descendente, alcance colección. El responsable del backend puede desplegar ambos con `firebase deploy --only firestore:rules,firestore:indexes`. Esperar a que el índice esté habilitado antes de probar Inicio. No se despliegan automáticamente al ejecutar Android.

El feed consulta únicamente autores seguidos en grupos de hasta 30 y combina las 50 publicaciones más recientes. Los contadores escuchan las relaciones; para volúmenes grandes convendrá usar contadores agregados.

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat lintDebug
```

## Notificaciones internas

La campanita de Inicio indica si quedan avisos sin leer. Muestra los últimos 50 y permite filtrar Sin leer para acceder a pendientes anteriores. Tocar un aviso lo marca leído y abre el perfil o post.

Mientras la app está visible, los nuevos likes y seguidores también generan avisos locales del sistema Android. Se solicita permiso en Android 13 o superior; rechazarlo no afecta la campanita. El engranaje de Notificaciones abre los ajustes del sistema. El primer resultado confirmado se toma como referencia: no se avisa por notificaciones antiguas, al volver a abrir ni por cada actualización del mismo aviso. Al tocar un aviso se comprueba la cuenta antes de abrir el destino y se intenta marcarlo leído; sin conexión queda pendiente en la campanita. Los avisos de otras cuentas se retiran al cambiar de sesión.

No hay FCM, servidor adicional ni servicio en segundo plano: con la app cerrada o en segundo plano no se garantiza recibir avisos locales. Los avisos internos permanecen en Firestore para consultarlos después.

Los nuevos likes y seguimientos guardan su aviso en la misma transacción. Deshacer la acción también retira el aviso; no hay avisos por likes propios ni se generan retroactivamente para acciones anteriores. Solo el destinatario puede leerlos y marcarlos leídos. Las reglas exigen la acción real, evitando avisos inventados o duplicados. Una publicación eliminada puede dejar un aviso cuyo destino ya no existe; el detalle muestra que no está disponible.

Antes de probar, actualizar la app del equipo, publicar `firestore.rules` y crear los dos índices de `notifications` definidos en `firestore.indexes.json`: `recipientId ASC, createdAt DESC` y `recipientId ASC, read ASC, createdAt DESC`. Las versiones anteriores de la app no podrán crear likes/seguimientos ajenos con las reglas nuevas porque no incluyen el aviso atómico. No requiere Blaze ni Functions; sigue pendiente la limpieza de datos huérfanos y el control de spam para producción.

## Lectura sin conexión

Room conserva las últimas 50 publicaciones del feed por cuenta, con los datos públicos del autor y la ubicación. Inicio lee la base local y la sincroniza con Firestore al recuperar conexión; los resultados confirmados reemplazan el feed anterior y retiran publicaciones borradas o de autores que ya no seguís. Los fallos de sincronización no borran el contenido guardado.

Sin Internet se pueden abrir esos detalles. Coil conserva las imágenes que ya cargó; no se descargan todas de antemano ni se guardan dentro de Room. El detalle offline reutiliza la variante de imagen del feed. Si no está en caché, se muestra un aviso. Los mapas, perfiles, búsqueda, notificaciones y acciones sociales requieren conexión; no se encolan escrituras offline.

La caché está separada por UID, no contiene emails ni contraseñas y queda excluida del backup. Si todavía no se cargó un feed para esa cuenta, se informa que no hay contenido guardado. No requiere cambios de reglas ni índices de Firebase.
