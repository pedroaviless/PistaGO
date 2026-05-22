# Arquitectura del Sistema — PistaGO

Documentación de la arquitectura de PistaGO, sistema de gestión de reservas de pistas de tenis. Se incluyen dos vistas complementarias: el **diagrama de despliegue** (infraestructura física y comunicación entre componentes) y el **diagrama de capas** (organización lógica del código siguiendo Clean Architecture).

---

## 1. Diagrama de despliegue (infraestructura)

Muestra cómo se distribuyen los componentes en el entorno real de producción y cómo se comunican entre sí. El cliente Android se comunica con el backend exclusivamente a través de HTTPS, y las notificaciones push viajan por el canal de Firebase Cloud Messaging.

```mermaid
graph TB
    subgraph cliente["📱 Dispositivo Android"]
        APP["App PistaGO<br/>Kotlin + Jetpack Compose<br/>MVVM + Clean Architecture"]
    end

    subgraph firebase["☁️ Firebase Cloud Messaging"]
        FCM["Servicio FCM<br/>Entrega de notificaciones push"]
    end

    subgraph vps["🖥️ VPS — DigitalOcean (Ubuntu 24.04) · nacimiento.me"]
        subgraph nginx["Nginx — Reverse Proxy (HTTPS / Let's Encrypt)"]
            PROXY["api.nacimiento.me<br/>→ proxy a :9090"]
            STATIC["/uploads/avatars/<br/>→ ficheros estáticos"]
        end

        subgraph backend["Spring Boot 3.5 (Kotlin) · puerto 9090"]
            API["API REST<br/>JWT + Spring Security"]
            FADMIN["Firebase Admin SDK<br/>Envío de push"]
        end

        DB[("PostgreSQL 16<br/>puerto 5432 · localhost")]
        FILES["/var/www/pistago/uploads/<br/>Avatares de usuario"]
    end

    APP -->|"HTTPS · REST/JSON<br/>(login, reservas, perfil...)"| PROXY
    APP -->|"HTTPS · carga avatares"| STATIC
    PROXY -->|"HTTP localhost"| API
    STATIC -.->|"lee"| FILES
    API -->|"JPA / Hibernate"| DB
    API -->|"guarda fotos"| FILES
    API -->|"solicita envío push"| FADMIN
    FADMIN -->|"HTTPS"| FCM
    FCM -->|"push notification"| APP

    classDef device fill:#e3f2fd,stroke:#1976d2,color:#0d47a1
    classDef cloud fill:#fff3e0,stroke:#f57c00,color:#e65100
    classDef server fill:#e8f5e9,stroke:#388e3c,color:#1b5e20
    classDef data fill:#f3e5f5,stroke:#7b1fa2,color:#4a148c

    class APP device
    class FCM cloud
    class PROXY,STATIC,API,FADMIN server
    class DB,FILES data
```

### Flujo destacado: notificación de turno en lista de espera

1. Un usuario cancela su reserva → petición `PATCH /api/reservas/{id}/cancelar` (HTTPS).
2. Nginx hace de proxy hacia Spring Boot en el puerto 9090.
3. El backend marca la reserva como `CANCELADA` en PostgreSQL.
4. Busca al primer usuario de la lista de espera de esa pista y franja (orden por `created_at`).
5. El backend, mediante Firebase Admin SDK, solicita a FCM el envío de una push al `fcm_token` de ese usuario.
6. FCM entrega la notificación al dispositivo Android del usuario.
7. La notificación se registra además en la tabla `notificaciones` para el historial.

---

## 2. Diagrama de capas (Clean Architecture)

El proyecto se organiza en dos aplicaciones independientes (cliente Android y servidor Spring Boot), cada una con separación de responsabilidades por capas. El cliente sigue Clean Architecture con patrón MVVM; el servidor sigue una arquitectura en capas clásica (controller → service → repository).

```mermaid
graph TB
    subgraph android["📱 Cliente Android — Clean Architecture + MVVM"]
        subgraph pres["Capa de Presentación"]
            SCREENS["Screens (Compose)<br/>Login, Home, Reservar,<br/>Perfil, ListaEspera..."]
            VM["ViewModels<br/>AuthViewModel, ReservaViewModel,<br/>ListaEsperaViewModel..."]
        end
        subgraph dom["Capa de Dominio"]
            MODELS["Modelos<br/>Usuario, Pista, Reserva..."]
            REPOIF["Interfaces de Repositorio<br/>AuthRepository, ReservaRepository..."]
        end
        subgraph datalayer["Capa de Datos"]
            REPOIMPL["Implementaciones<br/>AuthRepositoryImpl..."]
            APICLIENT["Retrofit API + DTOs"]
            LOCAL["TokenDataStore<br/>(DataStore Preferences)"]
        end
    end

    subgraph server["🖥️ Servidor Spring Boot — Arquitectura en capas"]
        subgraph web["Capa Web / Presentación"]
            CTRL["Controllers<br/>AuthController, ReservaController,<br/>UsuarioController, PistaController..."]
        end
        subgraph biz["Capa de Lógica de Negocio"]
            SVC["Services<br/>AuthService, ReservaService,<br/>ListaEsperaService, FcmService..."]
        end
        subgraph persist["Capa de Acceso a Datos"]
            REPO["Repositories (Spring Data JPA)<br/>UsuarioRepository, ReservaRepository..."]
            ENT["Entidades JPA<br/>Usuario, Pista, Reserva..."]
        end
    end

    DBP[("PostgreSQL")]

    SCREENS --> VM
    VM --> REPOIF
    REPOIMPL -.implementa.-> REPOIF
    REPOIMPL --> APICLIENT
    REPOIMPL --> LOCAL
    APICLIENT -->|"HTTPS REST/JSON"| CTRL

    CTRL --> SVC
    SVC --> REPO
    REPO --> ENT
    ENT --> DBP

    classDef presentation fill:#e3f2fd,stroke:#1976d2,color:#0d47a1
    classDef domain fill:#fff3e0,stroke:#f57c00,color:#e65100
    classDef data fill:#e8f5e9,stroke:#388e3c,color:#1b5e20
    classDef db fill:#f3e5f5,stroke:#7b1fa2,color:#4a148c

    class SCREENS,VM,CTRL presentation
    class MODELS,REPOIF,SVC domain
    class REPOIMPL,APICLIENT,LOCAL,REPO,ENT data
    class DBP db
```

### Justificación de la arquitectura

**Cliente Android — Clean Architecture + MVVM**

La aplicación se divide en tres capas con dependencias unidireccionales (presentación → dominio ← datos):

- **Presentación**: pantallas en Jetpack Compose y ViewModels que exponen el estado mediante `StateFlow`. La UI es reactiva: observa el estado y se recompone automáticamente.
- **Dominio**: modelos de negocio e interfaces de repositorio. Es el núcleo independiente de frameworks; define *qué* se puede hacer sin saber *cómo*.
- **Datos**: implementaciones concretas de los repositorios, cliente Retrofit con sus DTOs, y almacenamiento local (DataStore para el token JWT).

La inyección de dependencias se gestiona con **Hilt**, lo que permite que los ViewModels dependan de interfaces (no de implementaciones), facilitando el testeo y el desacoplamiento.

**Servidor Spring Boot — Arquitectura en capas**

El backend sigue el patrón clásico de tres capas:

- **Controllers**: exponen los endpoints REST, validan la entrada (Bean Validation con `@Valid`) y delegan en los servicios.
- **Services**: contienen la lógica de negocio (validación de reservas, gestión de lista de espera, envío de notificaciones). Anotados con `@Transactional` para garantizar la integridad.
- **Repositories**: acceso a datos mediante Spring Data JPA, que genera las consultas a partir de interfaces y mapea las entidades a las tablas de PostgreSQL.

Esta separación cumple los principios de responsabilidad única y facilita el mantenimiento: un cambio en la base de datos no afecta a los controllers, y un cambio en la API no afecta a la persistencia.

---

## 3. Stack tecnológico

| Capa | Tecnología |
|------|------------|
| Cliente | Kotlin, Jetpack Compose, MVVM, Hilt, Retrofit, Coil, DataStore |
| Backend | Kotlin, Spring Boot 3.5, Spring Security, Spring Data JPA, JWT (jjwt) |
| Base de datos | PostgreSQL 16 |
| Notificaciones | Firebase Cloud Messaging + Firebase Admin SDK |
| Servidor web | Nginx (reverse proxy + HTTPS con Let's Encrypt) |
| Infraestructura | VPS DigitalOcean, Ubuntu 24.04 |
| Control de versiones | Git + GitHub |
