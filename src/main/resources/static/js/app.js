(() => {
    'use strict';

    const API_URL = '/api/books';

    // Tiempo mínimo que se muestran los skeletons. Con datos en memoria la
    // respuesta es casi instantánea y el skeleton no llegaría a verse; cuando
    // se conecte la base de datos este valor se puede bajar o quitar.
    const MIN_SKELETON_MS = 600;
    const SKELETON_CARDS = 6;

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
        groupBy: document.getElementById('group-by')
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

    function bookCard(book) {
        return el('article', { className: 'card', attrs: { 'data-format': book.format } }, [
            el('h3', { className: 'card-title', text: book.title }),
            el('p', { className: 'card-author', text: book.author }),
            el('p', { className: 'card-genre', text: book.genre }),
            el('div', { className: 'badges' }, [
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
            ])
        ]);
    }

    function filteredBooks() {
        return state.books;
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
        const books = filteredBooks();
        els.content.setAttribute('aria-busy', 'false');
        els.summary.textContent =
            `${books.length} de ${state.books.length} obras`;

        if (books.length === 0) {
            els.content.replaceChildren(el('div', { className: 'state' }, [
                el('h2', { text: 'Sin resultados' }),
                el('p', { text: 'Prueba con otra búsqueda o quita algún filtro.' })
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

        els.search.addEventListener('input', () => {
            state.search = els.search.value;
            load();
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
    load();
})();
