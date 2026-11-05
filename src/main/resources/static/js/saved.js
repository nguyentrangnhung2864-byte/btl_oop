const $ = id => document.getElementById(id);
const EDIT_KEY = 'sch.editing';
const WEEKDAYS = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
let detailData = null;

async function api(url, options) {
    const res = await fetch(url, Object.assign({ headers: { 'Content-Type': 'application/json' } }, options));
    if (res.status === 204) return null;
    const data = await res.json().catch(() => null);
    if (!res.ok) {
        const detail = data && data.errors ? Object.values(data.errors).join('; ') : '';
        const msg = (data && data.message) || 'Có lỗi xảy ra';
        throw new Error(detail ? msg + ': ' + detail : msg);
    }
    return data;
}

function showError(msg) {
    const box = $('svMessage');
    box.textContent = msg;
    box.hidden = false;
    clearTimeout(showError.timer);
    showError.timer = setTimeout(() => { box.hidden = true; }, 5000);
}

const pad = n => String(n).padStart(2, '0');

function endTime(start, hours) {
    const [h, m] = start.split(':').map(Number);
    const total = h * 60 + m + Math.round(hours * 60);
    const t = total % 1440;
    return pad(Math.floor(t / 60)) + ':' + pad(t % 60) + (total >= 1440 ? ' (hôm sau)' : '');
}

function dateOfDay(startDate, dayNumber) {
    if (!startDate) return null;
    const [y, m, d] = startDate.split('-').map(Number);
    const date = new Date(y, m - 1, d);
    date.setDate(date.getDate() + dayNumber - 1);
    return date;
}

function formatDate(date) {
    return pad(date.getDate()) + '/' + pad(date.getMonth() + 1) + '/' + date.getFullYear();
}

function slugify(text) {
    return (text || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '')
        .replace(/đ/g, 'd').replace(/Đ/g, 'D')
        .toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
}

function renderThumb(placeName) {
    const wrap = document.createElement('div');
    wrap.className = 'sv-thumb';
    const img = document.createElement('img');
    img.alt = placeName;
    img.loading = 'lazy';
    img.onerror = () => { wrap.textContent = (placeName || '?').charAt(0).toUpperCase(); };
    img.src = '/images/places/' + slugify(placeName) + '.jpg';
    wrap.appendChild(img);
    return wrap;
}

/* ---------- Danh sách ---------- */

async function loadList() {
    const list = $('svList');
    list.innerHTML = '';
    $('svStatus').textContent = 'Đang tải...';
    try {
        const saved = await api('/api/itineraries');
        $('svStatus').textContent = saved.length ? '' : 'Chưa có lịch trình nào. Hãy tạo ở mục Lập lịch trình rồi bấm Lưu.';
        saved.forEach(s => list.appendChild(renderCard(s)));
    } catch (e) {
        $('svStatus').textContent = '';
        showError(e.message);
    }
}

function renderCard(s) {
    const li = document.createElement('li');
    li.className = 'sv-card';

    const info = document.createElement('div');
    const name = document.createElement('div');
    name.className = 'sv-name';
    name.textContent = s.name;
    const meta = document.createElement('p');
    meta.className = 'sv-meta';
    let text = s.dayCount + ' ngày · ' + s.placeCount + ' địa điểm';
    if (s.startDate) text += ' · Khởi hành ' + formatDate(dateOfDay(s.startDate, 1));
    text += ' · Lưu lúc ' + new Date(s.savedAt).toLocaleString('vi-VN');
    meta.textContent = text;
    info.append(name, meta);

    const actions = document.createElement('div');
    actions.className = 'sv-actions';
    const detail = document.createElement('button');
    detail.type = 'button';
    detail.className = 'sv-ghost';
    detail.textContent = 'Chi tiết';
    detail.onclick = () => openDetail(s.id);
    const edit = document.createElement('button');
    edit.type = 'button';
    edit.className = 'sv-btn';
    edit.textContent = 'Chỉnh sửa';
    edit.onclick = () => editItinerary(s.id, s.name);
    const del = document.createElement('button');
    del.type = 'button';
    del.className = 'sv-danger';
    del.textContent = 'Xóa';
    del.onclick = () => deleteItinerary(s);
    actions.append(detail, edit, del);

    li.append(info, actions);
    return li;
}

/* ---------- Bảng chi tiết ---------- */

async function openDetail(id) {
    try {
        detailData = await api('/api/itineraries/' + id);
        renderDetail(detailData);
        $('svDetail').showModal();
    } catch (e) { showError(e.message); }
}

function renderDetail(d) {
    $('svDetailTitle').textContent = d.name;
    const placeCount = d.days.reduce((sum, day) => sum + day.items.length, 0);
    $('svDetailMeta').textContent = d.days.length + ' ngày · ' + placeCount + ' địa điểm';

    const body = $('svDetailBody');
    body.innerHTML = '';
    d.days.forEach(day => {
        const date = dateOfDay(d.startDate, day.dayNumber);
        const h = document.createElement('h4');
        h.textContent = 'Ngày ' + day.dayNumber + (date ? ' · ' + WEEKDAYS[date.getDay()] + ', ' + formatDate(date) : '');
        body.appendChild(h);

        if (!day.items.length) {
            const empty = document.createElement('p');
            empty.className = 'sv-empty';
            empty.textContent = 'Chưa có hoạt động nào.';
            body.appendChild(empty);
            return;
        }

        const wrap = document.createElement('div');
        wrap.className = 'sv-table-wrap';
        const table = document.createElement('table');
        table.className = 'sv-table';
        const head = document.createElement('tr');
        ['Giờ', 'Hoạt động', 'Ghi chú'].forEach(t => {
            const th = document.createElement('th');
            th.textContent = t;
            head.appendChild(th);
        });
        const thead = document.createElement('thead');
        thead.appendChild(head);
        table.appendChild(thead);

        const tbody = document.createElement('tbody');
        day.items.forEach(item => {
            const tr = document.createElement('tr');

            const tdTime = document.createElement('td');
            tdTime.className = 'sv-time';
            const start = item.startTime.slice(0, 5);
            tdTime.textContent = start + ' - ' + endTime(start, item.durationHours);

            const tdPlace = document.createElement('td');
            const place = document.createElement('div');
            place.className = 'sv-place';
            const label = document.createElement('div');
            const nm = document.createElement('strong');
            nm.textContent = item.placeName;
            const sub = document.createElement('div');
            sub.className = 'sv-meta';
            sub.textContent = item.city + ' · ' + item.category + ' · ' + item.durationHours + ' giờ';
            label.append(nm, sub);
            place.append(renderThumb(item.placeName), label);
            tdPlace.appendChild(place);

            const tdNote = document.createElement('td');
            tdNote.textContent = item.note || '—';

            tr.append(tdTime, tdPlace, tdNote);
            tbody.appendChild(tr);
        });
        table.appendChild(tbody);
        wrap.appendChild(table);
        body.appendChild(wrap);
    });
}

/* ---------- Chỉnh sửa và xóa ---------- */

async function editItinerary(id, name) {
    if (!confirm('Mở "' + name + '" để chỉnh sửa sẽ thay thế lịch trình đang soạn ở trang Lập lịch trình. Tiếp tục?')) return;
    try {
        await api('/api/itineraries/' + id + '/load', { method: 'POST' });
        localStorage.setItem(EDIT_KEY, JSON.stringify({ id, name }));
        location.href = '/itinerary.html';
    } catch (e) { showError(e.message); }
}

async function deleteItinerary(s) {
    if (!confirm('Xóa lịch trình "' + s.name + '"?')) return;
    try {
        await api('/api/itineraries/' + s.id, { method: 'DELETE' });
        try {
            const editing = JSON.parse(localStorage.getItem(EDIT_KEY));
            if (editing && editing.id === s.id) localStorage.removeItem(EDIT_KEY);
        } catch (e) { /* bỏ qua */ }
        await loadList();
    } catch (e) { showError(e.message); }
}

$('svDetailClose').addEventListener('click', () => $('svDetail').close());
$('svDetailEdit').addEventListener('click', () => {
    if (detailData) editItinerary(detailData.id, detailData.name);
});

loadList();