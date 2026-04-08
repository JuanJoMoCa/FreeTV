# FreeTV

## Cliente de streaming para Android con persistencia local

FreeTV es una aplicación diseñada para la reproducción de canales mediante listas M3U. El proyecto utiliza una arquitectura local-first para gestionar todo el contenido, favoritos e historial directamente en el dispositivo.

## Arquitectura y Persistencia

Anteriormente el proyecto dependía de un backend externo para la gestión de datos. El estado actual de la aplicación elimina esa dependencia moviendo toda la lógica de procesamiento al cliente Android.

- Base de Datos: Se utiliza Room para el almacenamiento persistente. Los datos del usuario (favoritos e historial) se manejan en tablas independientes de la lista de canales para evitar pérdidas de información durante las actualizaciones de contenido.
- Clasificación: Incluye un sistema de filtrado que asigna categorías a los canales basándose en el análisis del nombre y metadatos de la lista de entrada.
- Almacenamiento: Las preferencias de configuración y el estado de la aplicación se mantienen en una tabla de ajustes dedicada.

## Componentes Técnicos

El desarrollo se realizó utilizando los estándares modernos de Android:

- Jetpack Compose para la interfaz de usuario.
- ExoPlayer (Media3) para la reproducción de video.
- Kotlin Coroutines y Flow para el manejo de datos asíncronos.
- ViewModel para la gestión del estado de la pantalla.

## Instrucciones de compilación

Para ejecutar el proyecto es necesario contar con Android Studio actualizado y el JDK 17.

Si se presentan errores de compilación tras cambios en la estructura de datos, se recomienda desinstalar la app compilada, limpiar la carpeta build del proyecto y realizar un Clean Project dentro del IDE para regenerar las clases de Room y KSP. Para que los cambios en la base de datos se apliquen correctamente en el dispositivo, es preferible desinstalar la versión anterior antes de instalar la nueva.
