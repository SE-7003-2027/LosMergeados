# LosMergeados

**Nombre del equipo:** LosMergeados

## Integrantes

- Alair ?
- Irving ?
- Jimmy
- Miguel ?
- Pablo ?

## Descripción del proyecto

LosMergeados es un sistema de exploración y recomendación de libros, cómics y manga disponibles en México.

El objetivo es permitir que los usuarios descubran y organicen obras según sus preferencias, autores, géneros, formatos y estatus de publicación. A futuro, el sistema utilizará las preferencias y valoraciones de los usuarios para generar recomendaciones personalizadas.

## Objetivo

Desarrollar una aplicación capaz de proporcionar recomendaciones personalizadas de libros, cómics y manga, utilizando las preferencias y valoraciones de los usuarios.

## Origen del proyecto

LosMergeados se formó con los integrantes activos de dos equipos que habían elegido este mismo proyecto: **Git-Wreckers** y **Equipo 7**.

Para no empezar de cero, el repositorio combina lo mejor de ambos:

- **Código:** la base de Equipo 7 (Java + Spring Boot), con mejoras tomadas del backend de Git-Wreckers.
- **Documentación:** la de Git-Wreckers (PRD, RFC, arquitectura e investigación), actualizada y complementada con la de Equipo 7.

El detalle está en la página de la wiki [Decisión de unificación](docs/wiki/Decision-de-unificacion.qmd).

## Estado del proyecto

El proyecto cuenta actualmente con:

- ✅ Aplicación Spring Boot 3 con Java 21.
- ✅ API REST para consultar, filtrar, buscar e importar obras.
- ✅ Persistencia mediante PostgreSQL y Spring Data JPA.
- ✅ Base de datos alojada en Neon.
- ✅ Integración con Open Library para consultar obras externas.
- ✅ Importación de obras externas al catálogo local.
- ✅ Prevención de importaciones duplicadas mediante identificadores de origen.
- ✅ Interfaz web para explorar y organizar el catálogo.
- ✅ Vista de detalle de las obras.
- ✅ Documentación interactiva de la API mediante Swagger UI.
- ✅ Pruebas automatizadas.

Las funcionalidades de usuarios, valoraciones y recomendaciones personalizadas continúan pendientes.

## Funcionalidades

Se marcan conforme se completan, tomando como referencia el [PRD](docs/wiki/PRD.qmd).

- [x] Catálogo de obras con formato y estatus de publicación.
- [x] Exploración mediante filtros y búsqueda.
- [x] Organización por autor, género y estatus.
- [x] Detalle de una obra desde la API y la vista web.
- [x] Búsqueda de obras en el catálogo local.
- [x] Búsqueda de obras externas mediante Open Library.
- [x] Importación de obras externas al catálogo.
- [x] Persistencia de información en PostgreSQL.
- [x] Prevención de obras duplicadas mediante su identificador de origen.
- [ ] Registro e inicio de sesión.
- [ ] Valoración de obras e historial de valoraciones.
- [ ] Recomendaciones personalizadas basadas en contenido.
- [ ] Información de disponibilidad en México a partir de fuentes externas.

## Stack tecnológico

| Capa | Tecnología | Estado |
|---|---|---|
| Backend | Java 21 + Spring Boot 3 | Implementado |
| API REST | Spring Web | Implementado |
| Persistencia | Spring Data JPA | Implementado |
| Base de datos | PostgreSQL (Neon) | Implementado |
| Integración externa | Open Library API | Implementado |
| Frontend | HTML + CSS + JavaScript | Implementado |
| Documentación de API | OpenAPI / Swagger UI | Implementado |
| Gestor de dependencias | Maven Wrapper | Implementado |
| Pruebas | JUnit 5 + Mockito + MockMvc | Implementado |
| Documentación | Quarto + GitHub Wiki | Implementado |
| Seguridad | Spring Security | Pendiente |
| Recomendaciones | Filtrado basado en contenido | Pendiente |
| Contenedores | Docker + Docker Compose | Pendiente |
| CI | GitHub Actions | Pendiente |

## Cómo ejecutar el proyecto

### Requisitos previos

- Java 21 (JDK).
- Git.
- Acceso a la base de datos PostgreSQL de Neon.
- Variables de entorno para la conexión a la base de datos.

No es necesario instalar Maven: el proyecto incluye Maven Wrapper.

### 1. Clonar el repositorio

```bash
git clone https://github.com/SE-7003-2027/LosMergeados.git
cd LosMergeados
```

### 2. Configurar la conexión a PostgreSQL

La aplicación utiliza variables de entorno para conectarse a Neon. Antes de iniciarla, configura:

```bash
export DB_URL='jdbc:postgresql://HOST/DATABASE?sslmode=require'
export DB_USERNAME='USUARIO'
export DB_PASSWORD='CONTRASEÑA'
```

Sustituye los valores de ejemplo por los datos de conexión de tu propia instancia de Neon.

**Importante:** no guardes credenciales reales en el repositorio ni las incluyas en commits. Utiliza variables de entorno para mantenerlas fuera del código fuente.

### 3. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Una vez iniciada, la aplicación estará disponible en:

| URL | Descripción |
|---|---|
| http://localhost:8080/ | Interfaz web del catálogo |
| http://localhost:8080/swagger-ui.html | Documentación interactiva de la API |
| http://localhost:8080/api/books | Catálogo en formato JSON |
| http://localhost:8080/api/health | Estado del servicio |

## Endpoints disponibles

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/books` | Lista y busca obras del catálogo local. |
| GET | `/api/books/external?title=` | Busca obras externas en Open Library por título. |
| GET | `/api/books/{id}` | Consulta el detalle de una obra local. |
| POST | `/api/books/import` | Importa una obra de Open Library al catálogo local. |
| GET | `/api/health` | Consulta el estado del servicio. |

### Parámetros de búsqueda del catálogo

El endpoint `GET /api/books` acepta los siguientes parámetros opcionales:

| Parámetro | Descripción | Valores |
|---|---|---|
| `q` | Busca por título, autor o género. | Texto libre |
| `format` | Filtra por formato. | `BOOK`, `COMIC`, `MANGA` |
| `status` | Filtra por estatus de publicación. | `ONGOING`, `COMPLETED`, `HIATUS`, `CANCELLED` |

Ejemplo:

```text
GET /api/books?format=MANGA&status=ONGOING&q=One%20Piece
```

### Importación de obras

La importación recibe una obra obtenida de Open Library, junto con el formato y el estatus seleccionados por el usuario.

Ejemplo de solicitud:

```json
{
  "book": {
    "sourceId": "/works/OL8215661W",
    "title": "ONE PIECE 1",
    "authors": ["Eiichiro Oda"],
    "subjects": [],
    "firstPublishYear": 1997,
    "coverUrl": null,
    "sourceUrl": "https://openlibrary.org/works/OL8215661W"
  },
  "format": "MANGA",
  "status": "ONGOING"
}
```

La aplicación utiliza el identificador de Open Library (`sourceId`) para evitar importar varias veces la misma obra.

## Ejecutar las pruebas

Para ejecutar todas las pruebas automatizadas:

```bash
./mvnw clean test
```

Las pruebas verifican el comportamiento de los controladores, la lógica del catálogo y las operaciones relacionadas con la importación de obras.

## Estructura del repositorio

```text
.
├── README.md
├── CONTRIBUTING.md
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/mx/losmergeados/recomendador/
│   │   │   ├── RecomendadorApplication.java
│   │   │   ├── book/
│   │   │   │   ├── source/
│   │   │   │   └── ...
│   │   │   └── health/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html
│   │           ├── css/
│   │           └── js/
│   └── test/
│       └── java/
├── docs/
│   └── wiki/
└── scripts/
    ├── README.md
    └── build-wiki.sh
```

## Organización del proyecto

- La documentación del proyecto (requerimientos, decisiones de diseño, investigación y guías) se escribe en `docs/wiki/` y se publica en la [Wiki de GitHub](https://github.com/SE-7003-2027/LosMergeados/wiki).
- Para publicar la Wiki se utiliza `./scripts/build-wiki.sh`. Consulta [`scripts/README.md`](scripts/README.md).
- Las historias de usuario y tareas se gestionan con GitHub Issues y GitHub Projects.
- Las reglas de ramas, commits y Pull Requests están en [`CONTRIBUTING.md`](CONTRIBUTING.md) y en la [Guía de colaboración](docs/wiki/Guia-de-colaboracion.qmd).
