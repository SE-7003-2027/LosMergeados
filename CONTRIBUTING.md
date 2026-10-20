# Guía de contribución

Resumen de cómo trabajamos en este repositorio. La versión completa, con ejemplos de comandos, está en la página [Guía de colaboración](docs/wiki/Guia-de-colaboracion.qmd) de la wiki; si algo difiere, se corrige para que ambas digan lo mismo.

## Flujo de trabajo general

1. Toma o crea un Issue en el tablero del proyecto (GitHub Projects).
2. Actualiza `main` y crea una rama a partir de ella.
3. Trabaja y haz commits pequeños y descriptivos.
4. Actualiza tu rama con `main` (se recomienda `git rebase origin/main`).
5. Corre las pruebas: `./mvnw test`.
6. Abre un Pull Request hacia `main`, vincula el Issue y pide revisión.
7. Atiende los comentarios en la misma rama hasta que lo aprueben.
8. Haz merge y elimina la rama si ya no se necesita.

## Ramas

Nunca se trabaja directamente sobre `main` (está protegida). Nombres en minúsculas, en español y con guiones:

| Tipo | Formato | Ejemplo |
|---|---|---|
| Funcionalidad | `feature/descripcion` | `feature/filtro-por-estatus-publicacion` |
| Corrección | `fix/descripcion` | `fix/duplicados-en-catalogo` |
| Documentación | `docs/descripcion` | `docs/actualizar-prd` |
| Investigación | `spike/descripcion` | `spike/evaluacion-api` |

## Commits

Usamos *Conventional Commits*, en español e imperativo, describiendo qué hace el cambio (no qué archivos toca):

| Prefijo | Uso |
|---|---|
| `feat:` | Funcionalidad nueva |
| `fix:` | Corrección de un bug |
| `docs:` | Documentación |
| `test:` | Pruebas |
| `refactor:` | Cambio interno sin alterar comportamiento |
| `chore:` | Configuración, dependencias, mantenimiento |

Ejemplo: `feat: agregar filtro de recomendaciones por género`

Evita mensajes generales como `cambios`, `arreglos` o `avance`.

## Issues

Antes de crear uno, verifica que no exista ya uno similar. Incluye, según el caso:

- **Bug:** pasos para reproducirlo, rama o versión, comportamiento esperado y el que ocurre, capturas o log del error.
- **Funcionalidad:** descripción, qué problema resuelve, criterios de aceptación y, si aplica, bocetos o módulos afectados.
- **Documentación:** qué páginas están desactualizadas y qué hay que cambiar.

## Antes de abrir un Pull Request

- La rama está limpia, solo con los commits necesarios y actualizada con `main`.
- `./mvnw test` pasa y la funcionalidad es correcta.
- Agregaste pruebas que el revisor pueda replicar.
- El PR se enfoca en un solo bug o funcionalidad y vincula su Issue (`Closes #N`).
- Actualizaste el README y la wiki (`docs/wiki/`) si el cambio lo requiere.

## Revisión de código

Todo Pull Request necesita **al menos una aprobación** y resolver las conversaciones antes de integrarse.

El revisor verifica que el sistema funcione como se describe, que las pruebas pasen y que la rama esté actualizada. Si propone un cambio de algoritmo o diseño, indica fuentes, pros y contras.

- **Request changes:** no se puede integrar hasta resolverlo (rompe pruebas o el sistema, o no está actualizado con `main`).
- **Comment:** no bloquea (typos, estilo, documentación menor, ajustes mínimos de interfaz).

## Documentación y decisiones de arquitectura

- La wiki se escribe en `docs/wiki/*.qmd` y se publica con `./scripts/build-wiki.sh` (ver `scripts/README.md`). No se editan páginas directamente en GitHub.
- Las decisiones técnicas importantes se registran en el [RFC](docs/wiki/RFC.qmd) y, cuando cambian el rumbo del proyecto, en una página de decisión (como [Decisión de unificación](docs/wiki/Decision-de-unificacion.qmd)).
