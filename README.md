# Spotly

Spotly es una aplicación Android nativa para compartir descubrimientos urbanos. Los usuarios pueden crear una cuenta, configurar su perfil y publicar fotografías de edificios, murales, plazas, vehículos, carteles y otros elementos interesantes de la ciudad junto con una descripción y su ubicación.

## Estado actual

- Registro e inicio de sesión con Firebase Authentication.
- Sesión observada automáticamente ante cambios de Firebase.
- Perfiles almacenados en Cloud Firestore.
- Edición de descripción y foto de perfil.
- Captura de imágenes con la cámara o selección desde la galería.
- Almacenamiento de imágenes mediante Cloudinary.
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
