#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
SOURCE_DIR="$ROOT_DIR/docs/wiki"
WIKI_REMOTE="git@github.com:SE-7003-2027/LosMergeados.wiki.git"

command -v git >/dev/null || {
    echo "Error: Git no está instalado."
    exit 1
}

command -v quarto >/dev/null || {
    echo "Error: Quarto no está instalado."
    exit 1
}

if [[ ! -d "$SOURCE_DIR" ]]; then
    echo "Error: no existe $SOURCE_DIR"
    exit 1
fi

mapfile -d '' QMD_FILES < <(
    find "$SOURCE_DIR" -maxdepth 1 -type f -name '*.qmd' -print0 | sort -z
)

if [[ ${#QMD_FILES[@]} -eq 0 ]]; then
    echo "Error: no se encontraron archivos .qmd en $SOURCE_DIR"
    exit 1
fi

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

RENDER_DIR="$TMP_DIR/rendered"
WIKI_DIR="$TMP_DIR/wiki"

mkdir -p "$RENDER_DIR"

echo "==> Generando páginas con Quarto..."

for file in "${QMD_FILES[@]}"; do
    quarto render "$file" --to gfm --output-dir "$RENDER_DIR"
done

echo "==> Clonando la Wiki de GitHub..."

git clone "$WIKI_REMOTE" "$WIKI_DIR"

echo "==> Copiando páginas generadas..."

find "$RENDER_DIR" -maxdepth 1 -type f -name '*.md' \
    -exec cp -t "$WIKI_DIR" {} +

cd "$WIKI_DIR"

git add -A

if git diff --cached --quiet; then
    echo "==> La Wiki ya está actualizada. No hay cambios."
    exit 0
fi

git commit -m "docs: actualizar wiki desde Quarto"

echo "==> Publicando cambios en GitHub..."

git push origin HEAD

echo "==> Wiki publicada correctamente."
