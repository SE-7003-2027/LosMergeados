# LosMergeados

Nombre del equipo: LosMergeados

## Integrantes

- Alair
- Irving
- Jimmy
- Miguel
- Pablo

## Descripción del proyecto

Este proyecto consiste en desarrollar un sistema de recomendación de libros, cómics y manga disponibles en México.

La idea es que el sistema pueda utilizar información sobre las preferencias y valoraciones de los usuarios para generar recomendaciones que se adapten a sus intereses. Las obras se pueden explorar y organizar por **autor**, **género** y **estatus de publicación** (en publicación, finalizado, en pausa, cancelado).

## Objetivo

Desarrollar una aplicación capaz de proporcionar recomendaciones personalizadas de libros, cómics y manga, utilizando las preferencias y valoraciones de los usuarios.

## Origen del proyecto

LosMergeados se formó con los integrantes activos de dos equipos que habían elegido este mismo proyecto: **Git-Wreckers** y **Equipo 7**. Para no empezar de cero, el repositorio combina lo mejor de ambos:

- **Código:** la base de Equipo 7 (Java + Spring Boot), con mejoras tomadas del backend de Git-Wreckers (capa de servicio, detalle por id, filtros y estado del servicio).
- **Documentación:** la de Git-Wreckers (PRD, RFC, arquitectura e investigación), actualizada y complementada con la de Equipo 7.

El detalle está en la página de la wiki [Decisión de unificación](docs/wiki/Decision-de-unificacion.qmd).

## Estado del proyecto

- ✅ Proyecto Spring Boot 3 + Maven con Java 21
- ✅ API REST del catálogo con datos de prueba en memoria
- ✅ Documentación interactiva de la API (Swagger UI)
- ✅ Vista web para explorar el catálogo
- ✅ Pruebas automatizadas
- ✅ PRD, RFC, arquitectura e investigación documentados en la wiki
- 📋 Base de datos, usuarios, valoraciones y motor de recomendaciones (próximos sprints)

## Funcionalidades

Se van marcando conforme se completan (prioridades del [PRD](docs/wiki/PRD.qmd)).

- [x] Catálogo de obras con formato y estatus de publicación
- [x] Exploración con filtros y organización por autor, género y estatus (vista web)
- [x] Detalle de una obra por API
- [ ] Registro e inicio de sesión
- [ ] Búsqueda de obras desde el servidor
- [ ] Valoración de obras e historial de valoraciones
- [ ] Recomendaciones personalizadas basadas en contenido
- [ ] Información de disponibilidad en México a partir de fuentes externas

## Stack

| Capa | Tecnología | Estado |
|---|---|---|
| Backend | Java 21 + Spring Boot 3 (Spring Web) | ✅ Implementado |
| Frontend | HTML + CSS + JavaScript servidos por Spring Boot | ✅ Implementado |
| Documentación de API | OpenAPI / Swagger UI (springdoc) | ✅ Implementado |
| Gestor de dependencias | Maven (wrapper incluido) | ✅ Implementado |
| Pruebas | JUnit 5 + MockMvc | ✅ Implementado |
| Documentación | Quarto + Wiki de GitHub | ✅ Implementado |
| Base de datos | PostgreSQL 16 | 📋 Planificado |
| Persistencia | Spring Data JPA | 📋 Planificado |
| Seguridad | Spring Security | 📋 Planificado |
| Recomendaciones | Filtrado basado en contenido | 📋 Planificado |
| Contenedores | Docker + Docker Compose | 📋 Planificado |
| CI | GitHub Actions | 📋 Planificado |

## Cómo ejecutar el proyecto

### Requisitos previos

- Java 21 (JDK)
- Git

> Maven no hace falta instalarlo (se usa `./mvnw`). Docker y PostgreSQL **no son necesarios todavía**: por ahora la aplicación usa datos en memoria.

### Pasos

```bash
git clone https://github.com/SE-7003-2027/LosMergeados.git
cd LosMergeados
./mvnw spring-boot:run
```

En Windows: `.\mvnw.cmd spring-boot:run`

| URL | Qué encuentras |
|---|---|
| <http://localhost:8080> | Vista web del catálogo |
| <http://localhost:8080/swagger-ui.html> | Documentación interactiva de la API |
| <http://localhost:8080/api/books> | Catálogo en JSON |

### Endpoints disponibles

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/books` | Lista el catálogo; filtros opcionales `?format=BOOK\|COMIC\|MANGA` y `?status=ONGOING\|COMPLETED\|HIATUS\|CANCELLED` |
| GET | `/api/books/{id}` | Detalle de una obra (`404` si no existe) |
| GET | `/api/health` | Estado del servicio |

### Correr las pruebas

```bash
./mvnw test
```

## Estructura del repositorio

```text
.
├── README.md
├── CONTRIBUTING.md                     Resumen de reglas de colaboración
├── pom.xml                             Dependencias y build (Maven)
├── mvnw, mvnw.cmd, .mvn/               Maven Wrapper
├── src/
│   ├── main/java/mx/losmergeados/recomendador/
│   │   ├── RecomendadorApplication.java
│   │   ├── book/                       Catálogo: modelo, servicio y API
│   │   └── health/                     Estado del servicio
│   ├── main/resources/
│   │   ├── application.properties
│   │   └── static/                     Vista web (index.html, css/, js/)
│   └── test/java/...                   Pruebas
├── docs/
│   └── wiki/                           Fuentes .qmd de la Wiki
└── scripts/
    ├── README.md                       Cómo publicar la Wiki
    └── build-wiki.sh
```

## Organización del proyecto

- La documentación del proyecto (requerimientos, decisiones de diseño, investigación y guías) se escribe en `docs/wiki/` y se publica en la [Wiki](https://github.com/SE-7003-2027/LosMergeados/wiki) con `./scripts/build-wiki.sh` (ver [`scripts/README.md`](scripts/README.md)).
- Las historias de usuario y tareas se gestionan con GitHub Issues y GitHub Projects.
- Las reglas de ramas, commits y Pull Requests están en [`CONTRIBUTING.md`](CONTRIBUTING.md) y en la [Guía de colaboración](docs/wiki/Guia-de-colaboracion.qmd).
