# 🎾 PistaGO

**Sistema de gestión de reservas de pistas de tenis** — aplicación móvil Android nativa con backend propio (API REST).

Proyecto Intermodular del Ciclo Formativo de Grado Superior en **Desarrollo de Aplicaciones Multiplataforma (DAM)** · IES Portada Alta, Málaga · Curso 2025/2026.

---

## Descripción

PistaGO resuelve la gestión manual de reservas en clubes de tenis (llamadas, WhatsApp, papel), que provoca solapamientos, conflictos de horarios e infrautilización de las pistas. La aplicación permite a los socios consultar la disponibilidad, reservar, cancelar y apuntarse a una lista de espera que avisa automáticamente cuando se libera una plaza mediante notificaciones push.

El proyecto se compone de dos aplicaciones independientes: un **cliente Android nativo** (Kotlin + Jetpack Compose) y una **API REST** (Spring Boot + PostgreSQL), desplegada en un servidor propio con HTTPS.

---

## Funcionalidades principales

- **Autenticación con roles** (Usuario, Administrador, Superusuario) mediante JWT.
- **Gestión de reservas:** creación con validación de solapamientos, consulta y cancelación.
- **Consulta de disponibilidad** de pistas por fecha y franja horaria.
- **Lista de espera** con cálculo dinámico de posición y reasignación automática.
- **Notificaciones push** (Firebase Cloud Messaging): aviso al primer usuario de la cola cuando se cancela una reserva.
- **Gestión de perfil:** edición de datos, foto de perfil y cambio de contraseña.
- **Cierre de sesión automático** al expirar el token.

---

## Stack tecnológico

### Cliente Android
- Kotlin + Jetpack Compose (UI declarativa)
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
- Firebase Admin SDK (envío de notificaciones)
- PostgreSQL 16 (producción) / PostgreSQL 15 en Docker (desarrollo local)

### Infraestructura
- VPS Ubuntu (DigitalOcean)
- Nginx (proxy inverso + archivos estáticos)
- HTTPS con Let's Encrypt

### Pruebas
- JUnit 5 + Mockito (mockito-kotlin) — pruebas unitarias de la capa de servicios

---

## Arquitectura

El sistema sigue una arquitectura cliente-servidor. El cliente Android se comunica con la API REST sobre HTTPS (JSON), y las notificaciones push viajan por el canal de Firebase Cloud Messaging.

Los diagramas del proyecto están disponibles en [`docs/diagrams/`](docs/diagrams):

- **Diagrama entidad-relación** de la base de datos
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
3. Configura la URL base de la API si es necesario.
4. Ejecuta la app en un emulador o dispositivo (Android 8.0+).

### Pruebas

```bash
cd backend
./gradlew test
```

El informe de resultados se genera en `backend/build/reports/tests/test/index.html`.

---

## Estado del proyecto

Aplicación funcional y desplegada. Líneas de trabajo futuro previstas:

- Panel de estadísticas para administradores.
- Caché local offline (Room) y sincronización en segundo plano.
- Expiración automática del turno en la lista de espera.
- Recordatorios y nuevos tipos de notificación.
- Ampliación de la cobertura de pruebas (integración).

---

## Autor

**Pedro Manuel Avilés Aguilera**
2º DAM · IES Portada Alta, Málaga · Curso 2025/2026

---

## Licencia

Este proyecto se distribuye bajo los términos de la licencia incluida en el fichero [LICENSE](LICENSE).
