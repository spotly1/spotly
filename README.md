# Spotly

Spotly es una aplicación Android nativa para compartir descubrimientos urbanos. Los usuarios pueden crear una cuenta, configurar su perfil y publicar fotografías de edificios, murales, plazas, vehículos, carteles y otros elementos interesantes de la ciudad junto con una descripción y su ubicación.

## Estado actual

- Registro e inicio de sesión con Firebase Authentication.
- Sesión observada automáticamente ante cambios de Firebase.
- Perfiles almacenados en Cloud Firestore.
- Edición de descripción y foto de perfil.
- Captura de imágenes con la cámara o selección desde la galería.
- Almacenamiento de imágenes mediante Cloudinary.
- Eliminación automática de fotos de perfil reemplazadas mediante Cloud Functions.
- Navegación principal construida con Jetpack Compose.

## Tecnologías

- Kotlin
- Jetpack Compose y Material 3
- MVVM
- Navigation Compose
- Firebase Authentication
- Cloud Firestore y Firestore Security Rules
- Cloud Functions
- Cloudinary
- Coil

## Configuración de Firebase en Android

El archivo `google-services.json` no se incluye en el repositorio. Cada integrante autorizado debe obtenerlo desde Firebase Console:

1. Abrir el proyecto de Spotly en [Firebase Console](https://console.firebase.google.com/).
2. Ingresar a **Configuración del proyecto**.
3. Seleccionar la aplicación Android con el paquete `com.example.spotly`.
4. Descargar `google-services.json`.
5. Copiarlo en `Spotly/app/google-services.json`.

Sin este archivo, la aplicación no puede conectarse al proyecto de Firebase.

## Backend y seguridad

Las reglas están en `firestore.rules`. Los datos se guardan separados:

- `users/{uid}`: username, descripción, URL y publicId de la foto. Un usuario autenticado puede consultar un perfil concreto. Las escrituras solo admiten estos campos públicos.
- `privateUsers/{uid}`: email, legible únicamente por su dueño.
- `usernames/{username}`: reserva del nombre asociada al uid.

Los listados permanecen bloqueados hasta implementar la búsqueda con consultas compatibles.

El registro crea los tres documentos en una única transacción. Las reglas comprueban que coincidan, que el email privado sea el de Firebase Authentication y que no se puedan reservar nombres adicionales ni apropiarse de reservas existentes. Solo se permite consultar un nombre concreto estando autenticado. La pantalla propia combina el perfil público con el email de su documento privado.

Las actualizaciones solo permiten descripción y foto, y validan todos los campos del perfil. Una foto requiere URL de nuestro entorno de Cloudinary e identificador bajo la carpeta del usuario. Para quitarla, ambos campos deben quedar vacíos.

El cliente no puede eliminar perfiles ni reservas: el borrado de cuenta necesitará un flujo de servidor que limpie ambos. Las demás colecciones permanecen bloqueadas. Estas reglas solo se activan al publicarlas; cambiar el archivo local no cambia Firebase.

Para comprobar las reglas con una base desechable (Node y Java requeridos):

```powershell
cd tests/firestore
npm ci
npm test
```

Las pruebas utilizan exclusivamente el emulador y el proyecto ficticio `demo-spotly-rules`: verifican registro, privacidad del email, reservas de nombres, validaciones de fotos y rechazos de acceso ajeno.

### Activar las reglas en Firebase

Copiar el contenido de `firestore.rules` en Firebase Console → Firestore Database → Reglas y publicar. Usar la app actualizada, que crea perfil, email privado y reserva de nombre en una transacción. No hay cuentas existentes que requieran conversión de datos.

La carpeta `functions` contiene funciones que eliminan de Cloudinary la foto anterior cuando se reemplaza o se elimina el perfil. La API secreta de Cloudinary nunca se guarda en Android ni en Git.

Una persona autorizada en Firebase y Cloudinary debe preparar y desplegar el backend:

```powershell
cd functions
npm install
cd ..
firebase login
firebase use --add
firebase functions:secrets:set CLOUDINARY_API_KEY
firebase functions:secrets:set CLOUDINARY_API_SECRET
firebase deploy --only firestore:rules,functions
```

Durante el despliegue, Firebase solicita `CLOUDINARY_CLOUD_NAME`. Las imágenes nuevas se guardan bajo `spotly/profiles/{uid}` para que las funciones solo eliminen archivos pertenecientes al usuario correspondiente.

## Ejecución de Android

1. Clonar el repositorio.
2. Agregar `app/google-services.json`.
3. Abrir el proyecto con Android Studio.
4. Sincronizar Gradle.
5. Ejecutarlo en un emulador o dispositivo con Android API 26 o superior.

## Próximas funcionalidades

- Creación, edición y eliminación de publicaciones.
- Feed y detalle de publicaciones.
- Geolocalización y mapa.
- Búsqueda de usuarios.
- Likes y seguimiento de usuarios.
- Notificaciones.
# Publicaciones y feed

La pantalla Crear publicación permite tomar una foto con la cámara o seleccionarla de la galería y escribir una descripción de hasta 1000 caracteres. Publicar sube la imagen a Cloudinary (`spotly/posts/{uid}`) y guarda autor, username público, URL, public ID, descripción, ubicación opcional y fecha del servidor en `posts/{id}`. Luego vuelve al feed, que escucha las últimas 50 publicaciones. La cámara se abre mediante la aplicación del sistema; cancelar conserva la imagen anterior y cada captura usa un archivo temporal distinto.

Antes de usar este flujo en Firebase real hay que publicar las reglas actualizadas de `firestore.rules`: el código no las despliega automáticamente. No se ha validado una publicación real desde el entorno de desarrollo. El preset unsigned debe permitir la carpeta indicada. Si la imagen se sube pero Firestore falla, se reutiliza al reintentar mientras se conserve el ViewModel; abandonar el borrador o cerrar la app puede dejar una imagen huérfana (limpieza manual mientras se use Spark).

## Optimización de fotos y ubicación

- La ubicación se muestra como una dirección azul y pulsable en el borrador, feed y detalle del perfil. Android Geocoder resuelve las coordenadas sin bloquear la interfaz; una caché acotada en memoria evita consultas repetidas. Si el dispositivo no dispone del servicio, no hay conexión o no se obtiene una dirección, se muestra «Ubicación seleccionada» y el mapa sigue disponible. La dirección es aproximada, especialmente con permiso de ubicación aproximada. Las coordenadas ya guardadas no cambian y no se requiere migrar documentos ni reglas por este cambio. El proveedor de geocodificación del dispositivo recibe el punto consultado para traducirlo a una dirección.
- Las fotos de publicaciones nuevas se comprimen en el dispositivo como JPEG al 80 %, con un máximo de 1600 × 1600 píxeles, conservando proporciones y orientación EXIF. El archivo original no se modifica.
- La cuadrícula pide imágenes de hasta 480 px, el feed 1080 px y el detalle 1600 px mediante transformaciones de Cloudinary (`q_auto`, `f_auto`, `c_limit`). Las imágenes ya publicadas también usan estas versiones de visualización, sin reescribir sus documentos. Si el proveedor tiene transformaciones estrictas habilitadas, hay que permitirlas en Cloudinary.
- La ubicación se solicita únicamente al tocar Agregar ubicación actual, sin seguimiento en segundo plano. Se acepta permiso aproximado; la precisión depende de Android. No se deduce dónde fue tomada una foto de galería. Puede quitarse antes de publicar y es visible para quienes pueden leer la publicación.
- El campo `location` es un `GeoPoint` opcional de Firestore. Las publicaciones antiguas sin ese campo siguen siendo válidas. Volver a publicar las reglas del repositorio antes de probar el guardado.
- El botón Ver ubicación abre un mapa integrado de OpenStreetMap en una WebView sin acceso a archivos, geolocalización web ni puente JavaScript/Android. El HTML local carga Leaflet 1.9.4 desde unpkg con verificación de integridad y teselas HTTPS de OSM. Conserva la atribución y requiere Internet; OSM recibe las solicitudes de la zona mostrada y ambos proveedores reciben los datos habituales de conexión. No requiere una clave de Google.
- Este uso del mapa está pensado para el TP, sin descargas masivas ni mapas offline. Antes de un lanzamiento público hay que revisar la [política de uso de OSM](https://operations.osmfoundation.org/policies/tiles/) y la política vigente de permisos de ubicación de Google Play para el SDK objetivo.
