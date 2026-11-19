# Decisiones del modelo E-R inicial

## Objetivo

Definir una representación conceptual inicial de las obras, los usuarios y las valoraciones del recomendador de LosMergeados.

## Entidades

Se consideran las entidades Obra, Usuario, Valoracion, Genero, FuenteExt, IdentExt y Autor.

- **Obra:** representa una obra que puede recomendarse.
- **Usuario:** representa a una persona registrada en la plataforma.
- **Valoracion:** registra la puntuación asignada por un usuario a una obra.
- **Genero:** permite clasificar las obras.
- **FuenteExt:** representa una fuente externa de información.
- **IdentExt:** permite relacionar una obra con su identificador en una fuente externa.
- **Autor:** representa a los autores asociados con las obras.

## Decisiones iniciales

- Las valoraciones utilizan una escala de 0 a 10.
- Una obra puede registrarse aunque no tenga un autor conocido.
- Una obra puede pertenecer a varios géneros.
- Una obra puede estar asociada con identificadores de fuentes externas.
- Se decidió no incluir la entidad Edicion en esta versión inicial, ya que no forma parte del alcance actual.

## Pendientes

- Determinar si las puntuaciones admitirán decimales.
- Definir las restricciones de unicidad y de integridad del modelo relacional.
- Revisar cómo se identificarán obras duplicadas procedentes de distintas fuentes externas.
- Confirmar si se requieren atributos adicionales según las funcionalidades finales del recomendador.