(() => {
    'use strict';

    const API_URL = '/api/books';

    // Tiempo mínimo que se muestran los skeletons. Con datos en memoria la
    // respuesta es casi instantánea y el skeleton no llegaría a verse; cuando
    // se conecte la base de datos este valor se puede bajar o quitar.
    const MIN_SKELETON_MS = 600;
    const SKELETON_CARDS = 6;

    // Espera entre teclazos antes de consultar al backend, para no lanzar
    // una peticion por cada letra escrita.
    const SEARCH_DEBOUNCE_MS = 250;

    // Ruta del detalle en el hash de la URL. Permite compartir el
    // enlace y cerrar el detalle con el boton "atras" del navegador.
    const DETAIL_HASH = /^#obra\/(.+)$/;

    const FORMATS = {
        BOOK: 'Libro',
        COMIC: 'Cómic',
        MANGA: 'Manga'
    };

    const STATUSES = {
        ONGOING: 'En publicación',
        COMPLETED: 'Finalizado',
        HIATUS: 'En pausa',
        CANCELLED: 'Cancelado'
    };

    const state = {
        books: [],
        search: '',
        format: 'ALL',
        status: 'ALL',
        groupBy: 'genre'
    };

    const els = {
        content: document.getElementById('content'),
        summary: document.getElementById('summary'),
        search: document.getElementById('search'),
        formatChips: document.getElementById('format-chips'),
        status: document.getElementById('status'),
        groupBy: document.getElementById('group-by'),
        detail: document.getElementById('detail'),
        detailBody: document.getElementById('detail-body'),
        detailClose: document.getElementById('detail-close'),
        externalSearchForm: document.querySelector('#external-search-form'),
        externalTitle: document.querySelector('#external-title'),
        externalSearchButton: document.querySelector('#external-search-button'),
        externalSearchStatus: document.querySelector('#external-search-status'),
        externalResults: document.querySelector('#external-results'),
    };

    /* ---------- Utilidades de DOM (textContent: nunca innerHTML con datos) ---------- */

    function el(tag, { className, text, attrs } = {}, children = []) {
        const node = document.createElement(tag);
        if (className) node.className = className;
        if (text !== undefined) node.textContent = text;
        if (attrs) Object.entries(attrs).forEach(([k, v]) => node.setAttribute(k, v));
        children.forEach(child => node.appendChild(child));
        return node;
    }

    const normalize = value =>
        value.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');

    /* ---------- Skeletons ---------- */

    function skeletonCard() {
        return el('article', { className: 'card is-skeleton', attrs: { 'aria-hidden': 'true' } }, [
            el('div', { className: 'skeleton sk-title' }),
            el('div', { className: 'skeleton sk-line' }),
            el('div', { className: 'skeleton sk-line-s' }),
            el('div', { className: 'badges' }, [
                el('div', { className: 'skeleton sk-badge' }),
                el('div', { className: 'skeleton sk-badge' })
            ])
        ]);
    }

    function showSkeletons() {
        els.content.setAttribute('aria-busy', 'true');
        els.summary.textContent = 'Cargando catálogo…';

        const grid = el('div', { className: 'grid' });
        for (let i = 0; i < SKELETON_CARDS; i++) grid.appendChild(skeletonCard());

        els.content.replaceChildren(
            el('div', { className: 'group' }, [el('div', { className: 'skeleton sk-heading' }), grid])
        );
    }

    /* ---------- Render ---------- */

    function badges(book) {
        return el('div', { className: 'badges' }, [
            el('span', {
                className: 'badge',
                text: FORMATS[book.format] ?? book.format,
                attrs: { 'data-format': book.format }
            }),
            el('span', {
                className: 'badge',
                text: STATUSES[book.status] ?? book.status,
                attrs: { 'data-status': book.status }
            })
        ]);
    }

    function bookCard(book) {
        // El titulo es un enlace al detalle; con CSS su area cubre toda la tarjeta.
        return el('article', { className: 'card', attrs: { 'data-format': book.format } }, [
            el('h3', { className: 'card-title' }, [
                el('a', {
                    className: 'card-link',
                    text: book.title,
                    attrs: { href: `#obra/${book.id}` }
                })
            ]),
            el('p', { className: 'card-author', text: book.author }),
            el('p', { className: 'card-genre', text: book.genre }),
            badges(book)
        ]);
    }

    function groupLabel(book) {
        if (state.groupBy === 'status') return STATUSES[book.status] ?? book.status;
        return book[state.groupBy];
    }

    function groupBooks(books) {
        if (state.groupBy === 'none') return [['Todas las obras', books]];

        const groups = new Map();
        books.forEach(book => {
            const label = groupLabel(book);
            if (!groups.has(label)) groups.set(label, []);
            groups.get(label).push(book);
        });
        return [...groups.entries()].sort((a, b) => a[0].localeCompare(b[0], 'es'));
    }

    function render() {
        const books = state.books;
        const query = state.search.trim();
        els.content.setAttribute('aria-busy', 'false');
        els.summary.textContent =
            `${books.length} ${books.length === 1 ? 'obra' : 'obras'}` +
            (query ? ` para «${query}»` : '');

        if (books.length === 0) {
            els.content.replaceChildren(el('div', { className: 'state' }, [
                el('h2', { text: 'Sin resultados' }),
                el('p', {
                    text: query
                        ? `No encontramos obras que coincidan con «${query}». Prueba con otro título, autor o género, o quita algún filtro.`
                        : 'Ninguna obra coincide con los filtros. Prueba quitando alguno.'
                })
            ]));
            return;
        }

        els.content.replaceChildren(...groupBooks(books).map(([label, items]) =>
            el('section', { className: 'group' }, [
                el('h2', { className: 'group-title' }, [
                    el('span', { text: label }),
                    el('span', { className: 'group-count', text: `${items.length} ${items.length === 1 ? 'obra' : 'obras'}` })
                ]),
                el('div', { className: 'grid' }, items.map(bookCard))
            ])
        ));
    }

    async function searchExternal(event) {
        event.preventDefault();

        const title = els.externalTitle.value.trim();

        if (!title) {
            els.externalSearchStatus.textContent = 'Escribe un título para buscar.';
            els.externalResults.replaceChildren();
            return;
        }

        els.externalSearchButton.disabled = true;
        els.externalSearchStatus.textContent = 'Buscando en Open Library...';
        els.externalResults.replaceChildren();
        els.externalResults.setAttribute('aria-busy', 'true');

        try {
            const response = await fetch(
                `${API_URL}/external?title=${encodeURIComponent(title)}`,
                {
                    headers: {
                        Accept: 'application/json'
                    }
                }
            );

            if (!response.ok) {
                throw new Error('No se pudo realizar la búsqueda.');
            }

            const books = await response.json();

            console.log('Resultados de Open Library:', books);

            if (!books.length) {
                els.externalSearchStatus.textContent =
                    'No se encontraron obras con ese título.';
                return;
            }

            els.externalSearchStatus.textContent =
                `${books.length} resultado${books.length === 1 ? '' : 's'} encontrado${books.length === 1 ? '' : 's'}.`;

            renderExternalResults(books);

        } catch (error) {
            console.error(error);
            els.externalSearchStatus.textContent =
                'Ocurrió un error al buscar en Open Library.';
        } finally {
            els.externalSearchButton.disabled = false;
            els.externalResults.setAttribute('aria-busy', 'false');
        }
    }

    function renderExternalResults(books) {
        const fragment = document.createDocumentFragment();

        books.forEach(book => {
            const card = el('article', {
                className: 'external-card'
            });

            if (isSafeCoverUrl(book.coverUrl)) {
                const image = el('img', {
                    attrs: {
                        src: book.coverUrl,
                        alt: `Portada de ${book.title}`
                    }
                });

                card.append(image);
            }

            card.append(
                el('h3', {
                    text: book.title || 'Sin título'
                })
            );

            card.append(
                el('p', {
                    text: book.authors?.length
                        ? book.authors.join(', ')
                        : 'Autor desconocido'
                })
            );

            card.append(
                el('p', {
                    text: book.firstPublishYear
                        ? `Primera publicación: ${book.firstPublishYear}`
                        : 'Año de publicación desconocido'
                })
            );

            card.append(
                el('p', {
                    text: book.subjects?.length
                        ? `Género: ${book.subjects[0]}`
                        : 'Género: Sin clasificar'
                })
            );

            if (isSafeSourceUrl(book.sourceUrl)) {
                const link = el('a', {
                    text: 'Ver en Open Library',
                    attrs: {
                        href: book.sourceUrl,
                        target: '_blank',
                        rel: 'noopener noreferrer'
                    }
                });

                card.append(link);
            }

            const formatField = el('label', {
                className: 'field'
            });

            formatField.append(
                el('span', {
                    className: 'field-label',
                    text: 'Formato'
                })
            );

            const formatSelect = createFormatSelect();
            formatField.append(formatSelect);

            card.append(formatField);

            const statusField = el('label', {
                className: 'field'
            });

            statusField.append(
                el('span', {
                    className: 'field-label',
                    text: 'Estado'
                })
            );

            const statusSelect = createStatusSelect();
            statusField.append(statusSelect);

            card.append(statusField);

            const importButton = el('button', {
                className: 'btn',
                text: 'Agregar al catálogo',
                attrs: {
                    type: 'button'
                }
            });

            importButton.addEventListener('click', () => {
                importExternalBook(
                    book,
                    formatSelect,
                    statusSelect,
                    importButton
                );
            });

            card.append(importButton);

            fragment.append(card);
        });

        els.externalResults.replaceChildren(fragment);
    }

    function createFormatSelect() {
        const select = el('select', {
            attrs: {
                required: 'true'
            }
        });

        select.append(
            el('option', {
                text: 'Selecciona un formato',
                attrs: {
                    value: ''
                }
            })
        );

        Object.entries(FORMATS).forEach(([value, label]) => {
            select.append(
                el('option', {
                    text: label,
                    attrs: {
                        value
                    }
                })
            );
        });

        return select;
    }

    function createStatusSelect() {
        const select = el('select', {
            attrs: {
                required: 'true'
            }
        });

        select.append(
            el('option', {
                text: 'Selecciona un estado',
                attrs: {
                    value: ''
                }
            })
        );

        Object.entries(STATUSES).forEach(([value, label]) => {
            select.append(
                el('option', {
                    text: label,
                    attrs: {
                        value
                    }
                })
            );
        });

        return select;
    }

    function isSafeCoverUrl(value) {
    try {
        const url = new URL(value);

        return (
            url.protocol === 'https:' &&
            url.hostname === 'covers.openlibrary.org'
        );
    } catch {
        return false;
    }
}

    function isSafeSourceUrl(value) {
        try {
            const url = new URL(value);

            return (
                url.protocol === 'https:' &&
                (
                    url.hostname === 'openlibrary.org' ||
                    url.hostname.endsWith('.openlibrary.org')
                )
            );
        } catch {
            return false;
        }
    }

    async function importExternalBook(book, formatSelect, statusSelect, button) {
        if (!formatSelect.value || !statusSelect.value) {
            els.externalSearchStatus.textContent =
                'Selecciona el formato y el estado antes de agregar la obra.';
            return;
        }

        button.disabled = true;
        button.textContent = 'Agregando...';

        try {
            const response = await fetch(`${API_URL}/import`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Accept: 'application/json'
                },
                body: JSON.stringify({
                    book,
                    format: formatSelect.value,
                    status: statusSelect.value
                })
            });

            if (!response.ok) {
                const message = await response.text();
                throw new Error(message || 'No se pudo importar la obra.');
            }

            const importedBook = await response.json();

            button.textContent = 'Agregada al catálogo';
            els.externalSearchStatus.textContent =
                `"${importedBook.title}" fue agregada correctamente.`;

            await load();

        } catch (error) {
            console.error(error);

            button.disabled = false;
            button.textContent = 'Agregar al catálogo';

            els.externalSearchStatus.textContent =
                'No se pudo agregar la obra al catálogo.';
        }
    }

    function renderError() {
        els.content.setAttribute('aria-busy', 'false');
        els.summary.textContent = '';

        const retry = el('button', { className: 'btn', text: 'Reintentar', attrs: { type: 'button' } });
        retry.addEventListener('click', load);

        els.content.replaceChildren(el('div', { className: 'state' }, [
            el('h2', { text: 'No pudimos cargar el catálogo' }),
            el('p', { text: 'Revisa que la aplicación siga corriendo e inténtalo de nuevo.' }),
            retry
        ]));
    }

    /* ---------- Detalle de una obra ---------- */

    let latestDetailRequest = 0;

    function showDetailSkeleton() {
        els.detailBody.setAttribute('aria-busy', 'true');
        els.detailBody.replaceChildren(el('div', { attrs: { 'aria-hidden': 'true' } }, [
            el('div', { className: 'skeleton sk-title' }),
            el('div', { className: 'skeleton sk-line' }),
            el('div', { className: 'skeleton sk-line-s' }),
            el('div', { className: 'badges' }, [
                el('div', { className: 'skeleton sk-badge' }),
                el('div', { className: 'skeleton sk-badge' })
            ])
        ]));
    }

    function renderDetail(book) {
        const row = (label, value) => [el('dt', { text: label }), el('dd', { text: value })];

        els.detail.dataset.format = book.format;
        els.detailBody.setAttribute('aria-busy', 'false');
        els.detailBody.replaceChildren(
            el('h2', { className: 'detail-title', text: book.title }),
            el('p', { className: 'card-author', text: book.author }),
            badges(book),
            el('dl', { className: 'detail-data' }, [
                ...row('Autor', book.author),
                ...row('Género', book.genre),
                ...row('Formato', FORMATS[book.format] ?? book.format),
                ...row('Estatus de publicación', STATUSES[book.status] ?? book.status),
                ...row('Identificador', String(book.id))
            ])
        );
    }

    function renderDetailState(title, message, retryId) {
        delete els.detail.dataset.format;
        els.detailBody.setAttribute('aria-busy', 'false');

        const children = [el('h2', { text: title }), el('p', { text: message })];
        if (retryId !== undefined) {
            const retry = el('button', { className: 'btn', text: 'Reintentar', attrs: { type: 'button' } });
            retry.addEventListener('click', () => loadDetail(retryId));
            children.push(retry);
        }
        els.detailBody.replaceChildren(el('div', { className: 'state detail-state' }, children));
    }

    async function loadDetail(id) {
        const requestId = ++latestDetailRequest;

        delete els.detail.dataset.format;
        showDetailSkeleton();
        if (!els.detail.open) els.detail.showModal();

        try {
            const [response] = await Promise.all([
                fetch(`${API_URL}/${encodeURIComponent(id)}`, {
                    headers: { Accept: 'application/json' }
                }),
                new Promise(resolve => setTimeout(resolve, MIN_SKELETON_MS))
            ]);

            if (requestId !== latestDetailRequest) return;

            if (response.status === 404 || response.status === 400) {
                renderDetailState(
                    'Obra no encontrada',
                    'La obra que buscas no existe o el enlace no es válido.'
                );
                return;
            }

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            renderDetail(await response.json());
        } catch (error) {
            if (requestId !== latestDetailRequest) return;

            console.error('Error al cargar el detalle:', error);
            renderDetailState(
                'No pudimos cargar el detalle',
                'Revisa que la aplicación siga corriendo e inténtalo de nuevo.',
                id
            );
        }
    }

    // Abre o cierra el detalle segun el hash de la URL.
    function route() {
        const match = location.hash.match(DETAIL_HASH);

        if (match) {
            loadDetail(decodeURIComponent(match[1]));
        } else if (els.detail.open) {
            els.detail.close();
        }
    }

    function setupDetail() {
        els.detailClose.addEventListener('click', () => els.detail.close());

        // Clic fuera del contenido (sobre el fondo) cierra el detalle.
        els.detail.addEventListener('click', event => {
            if (event.target === els.detail) els.detail.close();
        });

        // Al cerrar (boton, Esc o fondo) se quita el hash sin agregar
        // otra entrada al historial.
        els.detail.addEventListener('close', () => {
            latestDetailRequest++;
            if (DETAIL_HASH.test(location.hash)) {
                history.replaceState(null, '', location.pathname + location.search);
            }
        });

        window.addEventListener('hashchange', route);
    }

    /* ---------- Controles ---------- */

    function buildControls() {
        const chip = (value, label) => {
            const btn = el('button', {
                className: 'chip',
                text: label,
                attrs: { type: 'button', 'aria-pressed': String(value === state.format), 'data-value': value }
            });
            btn.addEventListener('click', () => {
                state.format = value;
                els.formatChips.querySelectorAll('.chip').forEach(c =>
                    c.setAttribute('aria-pressed', String(c.dataset.value === value)));
                load();
            });
            return btn;
        };

        els.formatChips.replaceChildren(
            chip('ALL', 'Todos'),
            ...Object.entries(FORMATS).map(([value, label]) => chip(value, label))
        );

        els.status.replaceChildren(
            el('option', { text: 'Todos', attrs: { value: 'ALL' } }),
            ...Object.entries(STATUSES).map(([value, label]) => el('option', { text: label, attrs: { value } }))
        );

        let searchTimer;
        els.search.addEventListener('input', () => {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(() => {
                state.search = els.search.value;
                load();
            }, SEARCH_DEBOUNCE_MS);
        });

        els.status.addEventListener('change', () => {
            state.status = els.status.value;
            load();
        });
        els.groupBy.addEventListener('change', () => { state.groupBy = els.groupBy.value; render(); });
    }

    /* ---------- Carga de datos ---------- */
    let latestRequest = 0;

    async function load() {
        const requestId = ++latestRequest;

        showSkeletons();

        try {
            const params = new URLSearchParams();

            const query = state.search.trim();

            if (query) {
                params.set('q', query);
            }

            if (state.format !== 'ALL') {
                params.set('format', state.format);
            }

            if (state.status !== 'ALL') {
                params.set('status', state.status);
            }

            const queryString = params.toString();
            const url = queryString
                ? `${API_URL}?${queryString}`
                : API_URL;

            const [response] = await Promise.all([
                fetch(url, {
                    headers: { Accept: 'application/json' }
                }),
                new Promise(resolve =>
                    setTimeout(resolve, MIN_SKELETON_MS)
                )
            ]);

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const books = await response.json();

            if (requestId !== latestRequest) return;

            state.books = books;
            render();
        } catch (error) {
            if (requestId !== latestRequest) return;

            console.error('Error al cargar el catálogo:', error);
            renderError();
        }
    }
    buildControls();
    setupDetail();

    els.externalSearchForm.addEventListener('submit', searchExternal);

    load();
    route();
})();
