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
    └── Home.qmd

scripts/
├── README.md
└── build-wiki.sh
```

Los archivos `.qmd` son las fuentes que deben editarse. El Markdown generado se utiliza para publicar las páginas en la Wiki.

## Cómo agregar una página

1. Crear un archivo `.qmd` dentro de `docs/wiki/`.
2. Escribir el contenido usando Markdown y las opciones de Quarto necesarias.
3. Revisar el contenido antes de publicarlo.
4. Ejecutar el script desde la raíz del repositorio:

```bash
./scripts/build-wiki.sh
```

5. Verificar en GitHub que los cambios aparezcan en la Wiki.

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

