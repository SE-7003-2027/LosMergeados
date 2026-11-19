# Automatización de la Wiki

## Descripción

El script `build-wiki.sh` automatiza la generación y publicación de la documentación del proyecto LosMergeados en la Wiki de GitHub.

Las páginas se escriben en formato Quarto (`.qmd`), se convierten a Markdown (`.md`) y se publican en el repositorio de la Wiki.

## Requisitos

* Git.
* Quarto instalado.
* Acceso de lectura y escritura al repositorio de la Wiki.
* SSH configurado para GitHub.

## Estructura

```text
docs/
└── wiki/
    ├── Home.qmd                          Portada de la Wiki
    ├── _Sidebar.qmd                      Menú lateral
    ├── PRD.qmd
    ├── RFC.qmd
    ├── Arquitectura.qmd
    ├── Modelo-de-datos.qmd
    ├── Decision-de-unificacion.qmd
    ├── Evaluacion-de-tecnologias.qmd
    ├── Fuentes-de-datos-y-APIs.qmd
    ├── Algoritmos-de-recomendacion.qmd
    ├── Prueba-tecnica-backend-framework.qmd
    ├── Instalacion-y-ejecucion.qmd
    ├── API-REST.qmd
    ├── Vista-web.qmd
    ├── Guia-de-colaboracion.qmd
    └── Roadmap-de-sprints.qmd

scripts/
├── README.md
└── build-wiki.sh
```

El nombre del archivo es el nombre de la página en la Wiki (los guiones se muestran como espacios). Los enlaces entre páginas usan ese nombre sin extensión, por ejemplo `[Arquitectura](Arquitectura)`.

Los archivos `.qmd` son las fuentes que deben editarse. El Markdown generado se utiliza para publicar las páginas en la Wiki.

## Cómo agregar una página

1. Crear un archivo `.qmd` dentro de `docs/wiki/`, sin acentos ni espacios en el nombre (por ejemplo `Modelo-de-datos.qmd`).
2. Iniciar el archivo con el encabezado YAML del título y escribir el contenido en Markdown. Quarto convierte el `title` en el encabezado principal de la página, así que no hace falta repetirlo con `#`:

   ```markdown
   ---
   title: "Modelo de datos"
   ---

   ## Primera sección
   ```

3. Agregar la página a `Home.qmd` y a `_Sidebar.qmd`.
4. Revisar el contenido antes de publicarlo (por Pull Request, en una rama `docs/...`).
5. Después de integrar el cambio en `main`, ejecutar el script desde la raíz del repositorio:

```bash
./scripts/build-wiki.sh
```

6. Verificar en GitHub que los cambios aparezcan en la Wiki.

## Cómo ejecutar el script

Desde la raíz de LosMergeados:

```bash
./scripts/build-wiki.sh
```

El script genera las páginas Markdown, clona el repositorio de la Wiki en una carpeta temporal, copia las páginas generadas y publica los cambios si hay modificaciones.

## Consideraciones

* Los cambios de documentación deben revisarse antes de publicarlos.
* El script publica directamente en la Wiki; no utiliza el Pull Request del repositorio principal para aprobar esa publicación.
* Los archivos `.qmd` deben mantenerse en el repositorio principal.
* Se necesita permiso de escritura en la Wiki para publicar cambios.
* Si la Wiki ya contiene modificaciones manuales, hay que asegurarse de conservarlas antes de publicar nuevas versiones.

