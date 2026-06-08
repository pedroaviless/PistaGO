# 🎾 PistaGO

**Sistema de gestión de reservas de pistas de tenis** — aplicación móvil Android nativa con backend propio (API REST).

Proyecto Intermodular del Ciclo Formativo de Grado Superior en **Desarrollo de Aplicaciones Multiplataforma (DAM)** · IES Portada Alta, Málaga · Curso 2025/2026.

🔗 **Recursos públicos del proyecto:**
- **Portfolio y caso de estudio:** https://nacimiento.me/proyectos/pistago/
- **Descargar APK firmada:** https://nacimiento.me/pistago/
- **API en producción:** https://api.nacimiento.me

---

## Descripción

PistaGO resuelve la gestión manual de reservas en clubes de tenis (llamadas, WhatsApp, papel), que provoca solapamientos, conflictos de horarios e infrautilización de las pistas. La aplicación permite a los socios consultar la disponibilidad, reservar, cancelar y apuntarse a una lista de espera que avisa automáticamente cuando se libera una plaza mediante notificaciones push.

El proyecto se compone de dos aplicaciones independientes: un **cliente Android nativo** (Kotlin + Jetpack Compose) y una **API REST** (Spring Boot + PostgreSQL), desplegada en un servidor propio con HTTPS.

---

## Funcionalidades principales

- **Autenticación con roles** (Usuario, Administrador, Superusuario) mediante JWT.
- **Gestión de reservas:** creación con validación de solapamientos, consulta y cancelación.
- **Regla de una reserva activa por usuario:** garantiza el reparto equitativo entre socios (los administradores quedan exentos).
- **Consulta de disponibilidad** de pistas por fecha y franja horaria, con bloqueo automático de franjas pasadas.
- **Lista de espera** con cálculo dinámico de posición y reasignación automática al producirse una cancelación.
- **Notificaciones push** (Firebase Cloud Messaging): aviso al primer usuario de la cola cuando se libera una franja.
- **Panel de administración:** gestión de pistas y reservas con filtros y contadores.
- **Estadísticas de uso para administradores:** totales, ranking de pistas y usuarios, tasa de cancelación y distribución por día de la semana, con gráfico de barras dibujado a mano en Canvas de Compose.
- **Gestión de perfil:** edición de datos, foto de perfil y cambio de contraseña.
- **Cierre de sesión automático** al expirar el token.
- **Manejo unificado de errores** entre backend y cliente mediante una función de extensión común sobre `Response<T>` de Retrofit.

---

## Stack tecnológico

### Cliente Android
- Kotlin + Jetpack Compose (UI declarativa)
- Material Design 3 + identidad visual propia
- Arquitectura MVVM + Clean Architecture (capas presentation / domain / data)
- Hilt (inyección de dependencias)
- Retrofit + OkHttp (cliente HTTP con interceptor JWT)
- Coroutines + Flow (asincronía y estado reactivo)
- Coil (carga de imágenes)
- DataStore (almacenamiento del token)
- Firebase Cloud Messaging (notificaciones push)

### Backend
- Kotlin + Spring Boot 3.5
- Spring Security + JWT (autenticación y autorización)
- Spring Data JPA + Hibernate (acceso a datos)
- Bean Validation (validación de entrada)
- `@RestControllerAdvice` para manejo global de errores con estructura JSON consistente
- Firebase Admin SDK (envío de notificaciones)
- PostgreSQL 16 (producción) / PostgreSQL 15 en Docker (desarrollo local)

### Infraestructura
- VPS Ubuntu (DigitalOcean)
- Nginx (proxy inverso + archivos estáticos)
- HTTPS con Let's Encrypt
- APK firmada con keystore propia (RSA 2048, validez 27 años)

### Pruebas
- JUnit 5 + Mockito Kotlin
- **30 pruebas unitarias** (100% éxito, ejecución en < 2 segundos) distribuidas en:
  - `ReservaServiceTest`: 13 pruebas (creación, cancelación, solapamientos, regla de reserva única)
  - `AuthServiceTest`: 9 pruebas (registro, login, hash de contraseñas, JWT)
  - `ListaEsperaServiceTest`: 7 pruebas (apuntarse, salir, posición dinámica, notificación FCM)
  - `PistagoBackendApplicationTests`: 1 prueba (arranque de Spring Boot)

---

## Arquitectura

El sistema sigue una arquitectura cliente-servidor. El cliente Android se comunica con la API REST sobre HTTPS (JSON), y las notificaciones push viajan por el canal de Firebase Cloud Messaging.

Los diagramas del proyecto están disponibles en [`docs/diagrams/`](docs/diagrams):

- **Diagrama entidad-relación** de la base de datos (7 tablas normalizadas en 3FN)
- **Diagrama de arquitectura** (vista de despliegue y vista de capas)
- **Diagrama de casos de uso**

---

## Estructura del repositorio

```
PistaGO/
├── android-app/      Cliente Android (Kotlin + Jetpack Compose)
├── backend/          API REST (Spring Boot + Kotlin)
├── docs/
│   ├── diagrams/     Diagramas E-R, arquitectura y casos de uso
│   ├── memoria/      Memoria del proyecto
│   ├── wireframes/   Wireframes iniciales
│   └── capturas/     Capturas de la aplicación
├── LICENSE
└── README.md
```

---

## Puesta en marcha

### Requisitos previos
- JDK 17
- Android Studio (para el cliente)
- PostgreSQL 15+ (o Docker)
- Un proyecto de Firebase (para las notificaciones push)

### Backend
1. Crea la base de datos PostgreSQL (en local puedes usar Docker):
```bash
   docker run --name pistago-db -e POSTGRES_DB=pistago \
     -e POSTGRES_USER=pistago_user -e POSTGRES_PASSWORD=tu_password \
     -p 5432:5432 -d postgres:15
```
2. Copia la plantilla de configuración y rellena tus valores:
```bash
   cp backend/src/main/resources/application.yaml.example backend/src/main/resources/application.yaml
```
   Edita `application.yaml` con tus credenciales de base de datos, tu secreto JWT y, si vas a usar notificaciones, la ruta al fichero de credenciales de Firebase.
3. Compila y arranca:
```bash
   cd backend
   ./gradlew bootRun
```
   La API quedará disponible en `http://localhost:9090`.

### Cliente Android
1. Abre la carpeta `android-app/` en Android Studio.
2. Añade tu fichero `google-services.json` de Firebase en `android-app/app/`.
3. Configura la URL base de la API si es necesario (`BASE_URL` en `RetrofitModule`).
4. Ejecuta la app en un emulador o dispositivo (Android 8.0+).

### Pruebas
```bash
cd backend
./gradlew test
```
El informe de resultados se genera en `backend/build/reports/tests/test/index.html`.

---

## Estado del proyecto

Aplicación funcional, **desplegada en producción** y accesible públicamente desde https://nacimiento.me/pistago/.

Líneas de trabajo futuro previstas:

- Caché local offline con Room y sincronización en segundo plano.
- Pruebas de integración end-to-end con base de datos en memoria.
- Documentación interactiva de la API con Swagger/OpenAPI.
- Expiración automática del turno en la lista de espera.
- Recordatorios push previos al partido reservado.
- Refresco en tiempo real de la posición en la lista de espera.

---

## Autor

**Pedro Manuel Avilés Aguilera**
2º DAM · IES Portada Alta, Málaga · Curso 2025/2026

- 🌐 Portfolio: https://nacimiento.me
- 💼 LinkedIn: https://www.linkedin.com/in/pedroaviless/
- 💻 GitHub: https://github.com/pedroaviless

---

## Licencia

Este proyecto se distribuye bajo los términos de la licencia incluida en el fichero [LICENSE](LICENSE).
