# Spotly

Spotly es una aplicación Android nativa para compartir descubrimientos urbanos. Los usuarios pueden crear una cuenta, configurar su perfil y publicar fotografías de edificios, murales, plazas, vehículos, carteles y otros elementos interesantes de la ciudad junto con una descripción y su ubicación.

## Estado actual

- Registro e inicio de sesión con Firebase Authentication.
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
- Cloud Firestore
- Cloudinary
- Coil

## Configuración de Firebase

El archivo `google-services.json` no se incluye en el repositorio. Cada integrante autorizado debe obtenerlo desde Firebase Console:

1. Abrir el proyecto de Spotly en [Firebase Console](https://console.firebase.google.com/).
2. Ingresar a **Configuración del proyecto**.
3. Seleccionar la aplicación Android con el paquete `com.example.spotly`.
4. Descargar `google-services.json`.
5. Copiarlo en la carpeta `app` del proyecto:

```text
Spotly/app/google-services.json
```

Sin este archivo, la aplicación no puede conectarse al proyecto de Firebase.

## Ejecución

1. Clonar el repositorio.
2. Agregar `app/google-services.json` siguiendo los pasos anteriores.
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
