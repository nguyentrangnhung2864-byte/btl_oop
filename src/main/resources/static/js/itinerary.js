const state = {
    days: [], places: [], items: [],
    activeDayId: null, selectedItemId: null,
    lastAction: null, moveMode: false, menuDayId: null, editingItem: null,
    startDate: null
};
const $ = id => document.getElementById(id);
let drag = null;
let suppressClick = false;

const HOUR_PX = 28;      // chiều cao mỗi giờ trên trục thời gian
const WHEEL_ITEM = 36;   // phải khớp chiều cao .sch-wheel div trong CSS
const MINUTE_STEP = 5;   // bước phút của trục quay

const CATEGORY_COLORS = ['#1a73e8', '#f29900', '#d93025', '#188038', '#8e44ef', '#00897b', '#e8710a'];
const ICON_TRASH = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M8 6V4h8v2"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/></svg>';
const ICON_EDIT = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>';
// Mỗi địa điểm trong ngày một màu sáng riêng (tránh màu đỏ vì đỏ dành cho "Trùng giờ")
const PLACE_COLORS = ['#7CC4FF', '#FFD166', '#7EE0C3', '#B9A3FF', '#FF9CCB', '#B6E88B', '#6FE3F0', '#FFC08A', '#E0A3FF', '#8FE3B0'];
const TRANSPORTS = {
    CAR: ['🚗', 'Ô tô'],
    MOTORBIKE: ['🛵', 'Xe máy'],
    PLANE: ['✈️', 'Máy bay'],
    BOAT: ['⛵', 'Thuyền'],
    TRAIN: ['🚆', 'Tàu hỏa'],
    BICYCLE: ['🚲', 'Xe đạp'],
    BUS: ['🚌', 'Xe buýt']
};
const SELECTED_KEY = 'selectedPlaces';   // A ghi vào khóa này: [{ placeId, name, city, ... }, ...]

// Trả về mảng id địa điểm A đã gửi, hoặc null nếu không có
function getSelectedPlaceIds() {
    try {
        const list = JSON.parse(localStorage.getItem(SELECTED_KEY));
        if (!Array.isArray(list)) return null;
        const ids = list.map(p => Number(p.placeId)).filter(Number.isFinite);
        return ids.length ? ids : null;
    } catch (e) { return null; }
}

function placeColor(index) { return PLACE_COLORS[index % PLACE_COLORS.length]; }

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
    const box = $('schMessage');
    box.textContent = msg;
    box.classList.remove('ok');
    box.hidden = false;
    clearTimeout(showError.timer);
    showError.timer = setTimeout(() => { box.hidden = true; }, 5000);
}

function toMinutes(time) {
    const [h, m] = time.split(':').map(Number);
    return h * 60 + m;
}

function endTime(start, hours) {
    const total = toMinutes(start) + Math.round(hours * 60);
    const t = total % 1440;
    const text = String(Math.floor(t / 60)).padStart(2, '0') + ':' + String(t % 60).padStart(2, '0');
    return total >= 1440 ? text + ' (hôm sau)' : text;
}

function setLastAction(action) {
    state.lastAction = action;
    $('schUndo').disabled = !action;
}

function categoryColor(category) {
    let hash = 0;
    for (const ch of category || '') hash = (hash * 31 + ch.charCodeAt(0)) % 997;
    return CATEGORY_COLORS[hash % CATEGORY_COLORS.length];
}

function addressOf(item) {
    // Place chưa có cột địa chỉ nên tạm hiện "thành phố, khu vực"
    return item.address || [item.city, item.region].filter(Boolean).join(', ');
}

/* ---------- Ảnh địa điểm (lấy từ /images/places/ của Module 1) ---------- */

function slugify(text) {
    return (text || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '')
        .replace(/đ/g, 'd').replace(/Đ/g, 'D')
        .toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
}

// Ảnh của Module 1 nằm ở /images/places/<tên-địa-điểm-không-dấu>.jpg, ví dụ chua-mot-cot.jpg
function placeImageUrl(placeName) {
    return '/images/places/' + slugify(placeName) + '.jpg';
}

function renderThumb(placeId, placeName, extraClass) {
    const wrap = document.createElement('div');
    wrap.className = 'sch-thumb' + (extraClass ? ' ' + extraClass : '');
    const img = document.createElement('img');
    img.alt = placeName;
    img.loading = 'lazy';
    // Không có ảnh thì hiện chữ cái đầu của tên địa điểm
    img.onerror = () => { wrap.textContent = (placeName || '?').charAt(0).toUpperCase(); };
    img.src = placeImageUrl(placeName);
    wrap.appendChild(img);
    return wrap;
}

/* ---------- Tổng quan chuyến đi ---------- */

function renderOverview() {
    $('schOvDays').textContent = state.days.length;
    $('schOvPlaces').textContent = state.days.reduce((sum, d) => sum + (d.itemCount || 0), 0);
}
/* ---------- Ngày thực tế và thứ của từng ngày ---------- */

const WEEKDAYS = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];

// Ngày thực tế của "Ngày N" = ngày khởi hành + (N - 1)
function dateOfDay(dayNumber) {
    if (!state.startDate) return null;
    const [y, m, d] = state.startDate.split('-').map(Number);
    const date = new Date(y, m - 1, d);
    date.setDate(date.getDate() + dayNumber - 1);
    return date;
}

function shortDate(date) {
    return String(date.getDate()).padStart(2, '0') + '/' + String(date.getMonth() + 1).padStart(2, '0');
}

function todayIso() {
    const t = new Date();
    return t.getFullYear() + '-' + String(t.getMonth() + 1).padStart(2, '0') + '-' + String(t.getDate()).padStart(2, '0');
}

async function loadTrip() {
    try {
        const trip = await api('/api/trip');
        state.startDate = trip.startDate;
        if (!state.startDate) {
            // Chưa có ngày khởi hành: lấy hôm nay và lưu lại để các tab có ngày hiển thị ngay
            const saved = await api('/api/trip', { method: 'PUT', body: JSON.stringify({ startDate: todayIso() }) });
            state.startDate = saved.startDate;
        }
        $('schStartDate').value = state.startDate || '';
    } catch (e) { showError(e.message); }
}

$('schStartDate').addEventListener('change', async () => {
    const value = $('schStartDate').value || null;
    try {
        const saved = await api('/api/trip', { method: 'PUT', body: JSON.stringify({ startDate: value }) });
        state.startDate = saved.startDate;
        renderTabs();
    } catch (e) {
        showError(e.message);
        $('schStartDate').value = state.startDate || '';
    }
});
/* ---------- Ngày ---------- */

function renderTabs() {
    const box = $('schTabs');
    box.innerHTML = '';
    box.classList.toggle('editing', state.moveMode);
    $('schMoveHint').hidden = !state.moveMode;
    state.days.forEach(day => {
        const b = document.createElement('button');
        b.type = 'button';
        b.className = 'sch-tab' + (day.id === state.activeDayId ? ' active' : '');
        b.dataset.id = day.id;
        const name = document.createElement('span');
        name.className = 'sch-tab-name';
        name.textContent = 'Ngày ' + day.dayNumber;
        b.appendChild(name);
        const date = dateOfDay(day.dayNumber);
        if (date) {
            const sub = document.createElement('span');
            sub.className = 'sch-tab-date';
            sub.textContent = shortDate(date) + ' · ' + WEEKDAYS[date.getDay()];
            b.appendChild(sub);
        }
        b.onclick = () => {
            if (suppressClick || state.moveMode) return;
            state.activeDayId = day.id;
            state.selectedItemId = null;
            setLastAction(null);
            renderTabs();
            loadItems();
        };
        enableTabGestures(b);
        box.appendChild(b);
    });

    const last = document.createElement('button');
    last.type = 'button';
    if (state.moveMode) {
        last.className = 'sch-done';
        last.textContent = 'Xong';
        last.onclick = () => { state.moveMode = false; renderTabs(); };
    } else {
        last.className = 'sch-add-day';
        last.textContent = '+';
        last.title = 'Thêm ngày';
        last.setAttribute('aria-label', 'Thêm ngày');
        last.onclick = addDay;
    }
    box.appendChild(last);

    const active = state.days.find(d => d.id === state.activeDayId);
    $('schDaySection').hidden = !active;
    $('schEmptyDays').hidden = !!active;
    if (active) {
        const date = dateOfDay(active.dayNumber);
        $('schDayTitle').textContent = 'Ngày ' + active.dayNumber +
            (date ? ' · ' + WEEKDAYS[date.getDay()] + ', ' + shortDate(date) + '/' + date.getFullYear() : '');
    }
}

async function loadDays() {
    try {
        state.days = await api('/api/days');
        if (!state.days.some(d => d.id === state.activeDayId)) {
            state.activeDayId = state.days.length ? state.days[0].id : null;
            state.selectedItemId = null;
        }
        renderOverview();
        renderTabs();
        if (state.activeDayId !== null) await loadItems();
    } catch (e) { showError(e.message); }
}

async function addDay() {
    try {
        const day = await api('/api/days', { method: 'POST' });
        state.activeDayId = day.id;
        state.selectedItemId = null;
        setLastAction(null);
        await loadDays();
    } catch (e) { showError(e.message); }
}

async function deleteDay(dayId) {
    const day = state.days.find(d => d.id === dayId);
    if (!day) return;
    if (!confirm('Xóa Ngày ' + day.dayNumber + ' và tất cả địa điểm trong ngày? Thao tác này không hoàn tác được.')) return;
    try {
        await api('/api/days/' + day.id, { method: 'DELETE' });
        if (state.activeDayId === day.id) state.activeDayId = null;
        setLastAction(null);
        await loadDays();
    } catch (e) { showError(e.message); }
}

/* ---------- Nhấn giữ một ngày: menu Di chuyển / Xóa ngày ---------- */

function openTabMenu(tab, fromLongPress) {
    state.menuDayId = Number(tab.dataset.id);
    const menu = $('schMenu');
    menu.hidden = false;
    const r = tab.getBoundingClientRect();
    const left = Math.min(r.left, window.innerWidth - menu.offsetWidth - 8);
    menu.style.left = Math.max(8, left) + 'px';
    menu.style.top = (r.bottom + 6) + 'px';
    if (fromLongPress) {
        // Lúc thả tay sau khi nhấn giữ sẽ sinh một cú click, bỏ qua để menu không bị đóng ngay
        suppressClick = true;
        window.addEventListener('pointerup', () => setTimeout(() => { suppressClick = false; }, 0), { once: true });
    }
}

function closeTabMenu() {
    $('schMenu').hidden = true;
    state.menuDayId = null;
}

document.addEventListener('click', e => {
    if (suppressClick) return;
    const menu = $('schMenu');
    if (!menu.hidden && !menu.contains(e.target)) closeTabMenu();
});

$('schMenuMove').addEventListener('click', () => {
    closeTabMenu();
    state.moveMode = true;
    renderTabs();
});

$('schMenuDelete').addEventListener('click', () => {
    const id = state.menuDayId;
    closeTabMenu();
    deleteDay(id);
});

function enableTabGestures(tab) {
    tab.addEventListener('pointerdown', e => {
        if (e.pointerType === 'mouse' && e.button !== 0) return;
        if (state.moveMode) { startDrag(tab); return; }

        const startX = e.clientX;
        const startY = e.clientY;
        const timer = setTimeout(() => openTabMenu(tab, true), 450);
        const cancel = ev => {
            if (ev.type === 'pointermove' && Math.hypot(ev.clientX - startX, ev.clientY - startY) < 8) return;
            clearTimeout(timer);
            window.removeEventListener('pointermove', cancel);
            window.removeEventListener('pointerup', cancel);
            window.removeEventListener('pointercancel', cancel);
        };
        window.addEventListener('pointermove', cancel);
        window.addEventListener('pointerup', cancel);
        window.addEventListener('pointercancel', cancel);
    });
    tab.addEventListener('contextmenu', e => {
        e.preventDefault();
        if (!state.moveMode) openTabMenu(tab, false);
    });
}

/* ---------- Kéo đổi vị trí ngày (trong chế độ Di chuyển) ---------- */

function tabOrder() {
    return [...$('schTabs').querySelectorAll('.sch-tab')].map(t => Number(t.dataset.id));
}

function blockScroll(e) { e.preventDefault(); }

function startDrag(tab) {
    drag = { el: tab, startOrder: tabOrder() };
    tab.classList.add('dragging');
    document.addEventListener('touchmove', blockScroll, { passive: false });
    window.addEventListener('pointermove', onDragMove);
    window.addEventListener('pointerup', endDrag);
    window.addEventListener('pointercancel', endDrag);
}

function onDragMove(e) {
    if (!drag) return;
    const tabs = [...$('schTabs').querySelectorAll('.sch-tab')];
    const target = tabs.find(t => {
        if (t === drag.el) return false;
        const r = t.getBoundingClientRect();
        return e.clientX >= r.left && e.clientX <= r.right && e.clientY >= r.top && e.clientY <= r.bottom;
    });
    if (!target) return;
    if (tabs.indexOf(drag.el) < tabs.indexOf(target)) target.after(drag.el);
    else target.before(drag.el);
}

async function endDrag() {
    if (!drag) return;
    const { el, startOrder } = drag;
    drag = null;
    el.classList.remove('dragging');
    document.removeEventListener('touchmove', blockScroll);
    window.removeEventListener('pointermove', onDragMove);
    window.removeEventListener('pointerup', endDrag);
    window.removeEventListener('pointercancel', endDrag);

    const newOrder = tabOrder();
    if (newOrder.join() === startOrder.join()) return;
    try {
        await api('/api/days/order', { method: 'PUT', body: JSON.stringify({ dayIds: newOrder }) });
    } catch (e) { showError(e.message); }
    await loadDays();
}

/* ---------- Địa điểm trong ngày ---------- */

async function loadPlaces() {
    try {
        state.places = await api('/api/places');
        const ids = getSelectedPlaceIds();
        const picked = ids ? state.places.filter(p => ids.includes(p.id)) : [];
        state.pool = picked.length ? picked : state.places;   // tập địa điểm A gửi (không có thì dùng tất cả)
        await refreshPlaceOptions();
    } catch (e) { showError(e.message); }
}

// Lấy lại các địa điểm đã xếp vào lịch rồi dựng lại ô chọn địa điểm
async function refreshPlaceOptions(includePlaceId) {
    try {
        state.usedPlaceIds = new Set(await api('/api/items/place-ids'));
    } catch (e) {
        state.usedPlaceIds = new Set();
    }
    fillPlaceSelect(includePlaceId);
}

function fillPlaceSelect(includePlaceId) {
    const select = $('schPlace');
    const used = state.usedPlaceIds || new Set();
    select.innerHTML = '';
    const available = state.pool.filter(p => !used.has(p.id) || p.id === includePlaceId);

    const first = document.createElement('option');
    first.value = '';
    first.textContent = available.length ? '-- Chọn địa điểm --' : '-- Đã xếp hết các địa điểm --';
    select.appendChild(first);
    available.forEach(p => {
        const opt = document.createElement('option');
        opt.value = p.id;
        opt.textContent = p.name + ' (' + p.city + ')';
        select.appendChild(opt);
    });
}

async function loadItems() {
    const status = $('schStatus');
    status.textContent = 'Đang tải...';
    try {
        state.items = await api('/api/days/' + state.activeDayId + '/items');
        if (!state.items.some(i => i.id === state.selectedItemId)) state.selectedItemId = null;

        status.textContent = state.items.length ? '' : 'Chưa có địa điểm nào trong ngày này.';
        const list = $('schList');
        list.innerHTML = '';
        state.items.forEach((item, index) => {
            const prev = state.items[index - 1];
            if (prev && item.distanceFromPreviousKm != null) list.appendChild(renderLeg(prev, item));
            list.appendChild(renderItem(item, index));
        });
        renderTimeline(state.items);
        renderDayInfo(state.items);
        applySelection(false);
    } catch (e) {
        status.textContent = '';
        showError(e.message);
    }
}

function renderItem(item, index) {
    const li = document.createElement('li');
    li.className = 'sch-item' + (item.conflict ? ' conflict' : '');
    li.dataset.id = item.id;
    li.style.setProperty('--dot', placeColor(index));
    const start = item.startTime.slice(0, 5);

    // Cột giờ bên trái
    const rail = document.createElement('div');
    rail.className = 'sch-rail';
    const t1 = document.createElement('div');
    t1.textContent = start;
    const t2 = document.createElement('div');
    t2.className = 'sch-rail-end';
    t2.textContent = endTime(start, item.durationHours).replace(' (hôm sau)', ' +1');
    rail.append(t1, t2);

    // Thẻ địa điểm
    const card = document.createElement('div');
    card.className = 'sch-card';
    const row = document.createElement('div');
    row.className = 'sch-item-row';

    const info = document.createElement('div');
    info.className = 'sch-info';
    const name = document.createElement('div');
    name.className = 'sch-name';
    name.textContent = item.placeName;
    if (item.conflict) {
        const badge = document.createElement('span');
        badge.className = 'sch-badge';
        badge.textContent = 'Trùng giờ';
        name.appendChild(badge);
    }
    const addr = document.createElement('div');
    addr.className = 'sch-meta';
    addr.textContent = '📍 ' + addressOf(item);
    const meta = document.createElement('div');
    meta.className = 'sch-meta';
    meta.textContent = item.category + ' · ' + item.durationHours + ' giờ';
    info.append(name, addr, meta);
    if (item.note) {
        const noteLine = document.createElement('div');
        noteLine.className = 'sch-note-line';
        noteLine.textContent = '📝 ' + item.note;
        info.appendChild(noteLine);
    }
    const detail = document.createElement('button');
    detail.type = 'button';
    detail.className = 'sch-link';
    detail.textContent = 'Chi tiết ›';
    detail.onclick = () => openDetail(item);
    info.appendChild(detail);

    const actions = document.createElement('div');
    actions.className = 'sch-actions';
    const edit = document.createElement('button');
    edit.type = 'button';
    edit.className = 'sch-icon-edit';
    edit.title = 'Chỉnh sửa';
    edit.setAttribute('aria-label', 'Chỉnh sửa');
    edit.innerHTML = ICON_EDIT;      // chuỗi SVG cố định
    edit.onclick = () => openForm({ item });
    const del = document.createElement('button');
    del.type = 'button';
    del.className = 'sch-icon-delete';
    del.title = 'Xóa địa điểm';
    del.setAttribute('aria-label', 'Xóa địa điểm');
    del.innerHTML = ICON_TRASH;      // chuỗi SVG cố định
    del.onclick = () => deleteItem(item);
    actions.append(edit, del);

    row.append(renderThumb(item.placeId, item.placeName), info, actions);
    // Dấu + dính ở mép dưới thẻ khi thẻ được chọn và có khoảng cách tới địa điểm kế tiếp
    const next = state.items[index + 1];
    if (next && next.distanceFromPreviousKm != null) {
        const plus = document.createElement('button');
        plus.type = 'button';
        plus.className = 'sch-transport-plus';
        plus.textContent = '+';
        plus.title = 'Chọn phương tiện đến địa điểm kế tiếp';
        plus.setAttribute('aria-label', 'Chọn phương tiện đến địa điểm kế tiếp');
        plus.onclick = e => { e.stopPropagation(); openTransportMenu(item, next, plus); };
        card.append(row, plus);
    } else {
        card.append(row);
    }
    li.append(rail, card);
    li.addEventListener('click', e => {
        if (e.target.closest('button')) return;
        selectItem(item.id, false);
    });
    return li;
}

/* ---------- Chọn một địa điểm (đồng bộ danh sách và trục thời gian) ---------- */

function selectItem(id, fromTimeline) {
    state.selectedItemId = (!fromTimeline && state.selectedItemId === id) ? null : id;
    applySelection(fromTimeline);
}

function keepVisible(container, element) {
    const c = container.getBoundingClientRect();
    const e = element.getBoundingClientRect();
    if (e.top < c.top + 8 || e.bottom > c.bottom - 8) {
        container.scrollTo({
            top: container.scrollTop + (e.top - c.top) - container.clientHeight / 2 + e.height / 2,
            behavior: 'smooth'
        });
    }
}

function applySelection(scrollList) {
    const id = state.selectedItemId;
    document.querySelectorAll('.sch-item').forEach(el => el.classList.toggle('selected', Number(el.dataset.id) === id));
    document.querySelectorAll('.sch-vblock').forEach(el => el.classList.toggle('selected', Number(el.dataset.id) === id));
    if (id === null) return;
    const li = document.querySelector('.sch-item[data-id="' + id + '"]');
    const block = document.querySelector('.sch-vblock[data-id="' + id + '"]');
    if (scrollList && li) li.scrollIntoView({ behavior: 'smooth', block: 'center' });
    if (block) keepVisible($('schTimeline'), block);
}

/* ---------- Cột phải: lịch trình trong ngày + thông tin ngày ---------- */

function renderTimeline(items) {
    const box = $('schTimeline');
    box.innerHTML = '';

    const wrap = document.createElement('div');
    wrap.className = 'sch-vwrap';
    wrap.style.height = (24 * HOUR_PX) + 'px';

    const labels = document.createElement('div');
    labels.className = 'sch-vlabels';
    for (let h = 0; h <= 24; h += 2) {
        const s = document.createElement('span');
        s.style.top = (h / 24 * 100) + '%';
        s.textContent = h + 'h';
        labels.appendChild(s);
    }

    const track = document.createElement('div');
    track.className = 'sch-vtrack';

    const lanes = [];
    const placed = items.map((item, index) => {
        const start = toMinutes(item.startTime);
        const end = Math.min(start + Math.round(item.durationHours * 60), 1440);
        let lane = lanes.findIndex(laneEnd => laneEnd <= start);
        if (lane === -1) { lane = lanes.length; lanes.push(end); } else { lanes[lane] = end; }
        return { item, index, start, end, lane };
    });
    const total = Math.max(lanes.length, 1);

    placed.forEach(({ item, index, start, end, lane }) => {
        const block = document.createElement('div');
        block.className = 'sch-vblock' + (item.conflict ? ' conflict' : '');
        block.dataset.id = item.id;
        if (!item.conflict) block.style.background = placeColor(index);
        block.style.top = (start / 1440 * 100) + '%';
        block.style.height = ((end - start) / 1440 * 100) + '%';
        block.style.left = (lane / total * 100) + '%';
        block.style.width = (100 / total) + '%';
        block.textContent = item.placeName;
        const s = item.startTime.slice(0, 5);
        block.title = item.placeName + ' (' + s + ' - ' + endTime(s, item.durationHours) + ')';
        block.onclick = () => selectItem(item.id, true);
        track.appendChild(block);
    });

    wrap.append(labels, track);
    box.appendChild(wrap);

    // Cuộn tới khu vực có địa điểm đầu tiên (hoặc 6h sáng nếu ngày còn trống)
    box.scrollTop = placed.length ? Math.max(0, placed[0].start / 60 * HOUR_PX - 24) : 6 * HOUR_PX;
}

function renderDayInfo(items) {
    const box = $('schDayInfo');
    box.innerHTML = '';
    const day = state.days.find(d => d.id === state.activeDayId);

    const totalMin = Math.round(items.reduce((sum, i) => sum + i.durationHours, 0) * 60);
    const h = Math.floor(totalMin / 60);
    const m = totalMin % 60;
    const time = totalMin === 0 ? '0 phút' : [h ? h + ' giờ' : '', m ? m + ' phút' : ''].filter(Boolean).join(' ');

    const title = document.createElement('h4');
    title.textContent = 'Thông tin ngày ' + (day ? day.dayNumber : '');
    box.appendChild(title);

    const rows = [['Địa điểm', items.length + ' địa điểm'], ['Thời gian tham quan', time]];
    const distances = items.map(i => i.distanceFromPreviousKm).filter(d => d != null);
    if (distances.length) {
        const total = Math.round(distances.reduce((a, b) => a + b, 0) * 10) / 10;
        rows.push(['Quãng đường (chim bay)', '≈ ' + total.toLocaleString('vi-VN') + ' km']);
    }
    rows.forEach(([label, value]) => {
        const row = document.createElement('div');
        row.className = 'sch-info-row';
        const l = document.createElement('span');
        l.className = 'sch-info-label';
        l.textContent = label;
        const v = document.createElement('strong');
        v.textContent = value;
        row.append(l, v);
        box.appendChild(row);
    });
}

/* ---------- Xóa và hoàn tác ---------- */

async function deleteItem(item) {
    try {
        await api('/api/items/' + item.id, { method: 'DELETE' });
        setLastAction({
            type: 'delete',
            dayId: state.activeDayId,
            data: {
                placeId: item.placeId,
                startTime: item.startTime,
                durationHours: item.durationHours,
                note: item.note
            }
        });
        await loadDays();
    } catch (e) { showError(e.message); }
}

async function undo() {
    const action = state.lastAction;
    if (!action) return;
    try {
        if (action.type === 'add') {
            await api('/api/items/' + action.itemId, { method: 'DELETE' });
        } else if (action.type === 'delete') {
            await api('/api/days/' + action.dayId + '/items', { method: 'POST', body: JSON.stringify(action.data) });
        } else if (action.type === 'edit') {
            await api('/api/items/' + action.itemId, { method: 'PUT', body: JSON.stringify(action.data) });
        }
        setLastAction(null);
        await loadDays();
    } catch (e) { showError(e.message); }
}

/* ---------- Trục quay chọn giờ 0-24h ---------- */

function buildWheel(el, values) {
    el.innerHTML = '';
    values.forEach((v, i) => {
        const d = document.createElement('div');
        d.textContent = String(v).padStart(2, '0');
        d.dataset.value = v;
        d.addEventListener('click', () => el.scrollTo({ top: i * WHEEL_ITEM, behavior: 'smooth' }));
        el.appendChild(d);
    });
    let ticking = false;
    el.addEventListener('scroll', () => {
        if (ticking) return;
        ticking = true;
        requestAnimationFrame(() => { ticking = false; markSelected(el); });
    });
}

function wheelIndex(el) {
    return Math.max(0, Math.min(el.children.length - 1, Math.round(el.scrollTop / WHEEL_ITEM)));
}

function markSelected(el) {
    const idx = wheelIndex(el);
    [...el.children].forEach((c, i) => c.classList.toggle('sel', i === idx));
}

function wheelValue(el) {
    return Number(el.children[wheelIndex(el)].dataset.value);
}

function setWheel(el, value) {
    const idx = [...el.children].findIndex(c => Number(c.dataset.value) === value);
    el.scrollTop = Math.max(idx, 0) * WHEEL_ITEM;
    markSelected(el);
}

function initWheels() {
    buildWheel($('schHour'), Array.from({ length: 24 }, (_, i) => i));
    buildWheel($('schMinute'), Array.from({ length: 60 / MINUTE_STEP }, (_, i) => i * MINUTE_STEP));
}

/* ---------- Bảng thêm / chỉnh sửa địa điểm ---------- */

function showFormError(msg) {
    const p = $('schFormError');
    p.textContent = msg || '';
    p.hidden = !msg;
}

async function openForm(opts = {}) {
    const editing = opts.item || null;
    state.editingItem = editing;
    $('schForm').reset();
    showFormError('');
    $('schFormTitle').textContent = editing ? 'Chỉnh sửa địa điểm' : 'Thêm địa điểm';
    $('schSubmit').textContent = editing ? 'Lưu' : 'Thêm';

    // Khi sửa thì vẫn giữ địa điểm của mục đó trong ô chọn, dù nó đã được xếp
    await refreshPlaceOptions(editing ? editing.placeId : null);
    $('schPlace').disabled = !!editing;
    $('schPlace').value = editing ? String(editing.placeId) : '';
    $('schDuration').value = editing ? editing.durationHours : 1;
    $('schNote').value = editing ? (editing.note || '') : '';

    let startMin = 9 * 60;
    if (editing) startMin = toMinutes(editing.startTime);
    startMin = Math.min(Math.round(startMin / MINUTE_STEP) * MINUTE_STEP, 24 * 60 - MINUTE_STEP);

    $('schDialog').showModal();
    requestAnimationFrame(() => {   // phải đợi bảng hiện ra thì mới đặt vị trí cuộn được
        setWheel($('schHour'), Math.floor(startMin / 60));
        setWheel($('schMinute'), startMin % 60);
    });
}

$('schOpenForm').addEventListener('click', () => openForm());
$('schCancel').addEventListener('click', () => $('schDialog').close());
$('schUndo').addEventListener('click', undo);

$('schForm').addEventListener('submit', async e => {
    e.preventDefault();
    const hh = String(wheelValue($('schHour'))).padStart(2, '0');
    const mm = String(wheelValue($('schMinute'))).padStart(2, '0');
    const payload = {
        startTime: hh + ':' + mm,
        durationHours: Number($('schDuration').value),
        note: $('schNote').value
    };
    try {
        if (state.editingItem) {
            const old = state.editingItem;
            await api('/api/items/' + old.id, { method: 'PUT', body: JSON.stringify(payload) });
            setLastAction({
                type: 'edit',
                itemId: old.id,
                data: { startTime: old.startTime, durationHours: old.durationHours, note: old.note }
            });
        } else {
            const created = await api('/api/days/' + state.activeDayId + '/items', {
                method: 'POST',
                body: JSON.stringify(Object.assign({ placeId: Number($('schPlace').value) }, payload))
            });
            setLastAction({ type: 'add', itemId: created.id });
            state.selectedItemId = created.id;
        }
        $('schDialog').close();
        await loadDays();
    } catch (err) {
        showFormError(err.message);   // hiện lỗi ngay trong bảng vì bảng đang che trang
    }
});

/* ---------- Bảng chi tiết địa điểm ---------- */

function openDetail(item) {
    const body = $('schDetailBody');
    body.innerHTML = '';
    body.appendChild(renderThumb(item.placeId, item.placeName, 'sch-thumb-lg'));

    const title = document.createElement('h3');
    title.textContent = item.placeName;
    body.appendChild(title);

    const start = item.startTime.slice(0, 5);
    [
        ['Địa chỉ', addressOf(item)],
        ['Loại', item.category],
        ['Thời gian', start + ' - ' + endTime(start, item.durationHours)],
        ['Thời lượng', item.durationHours + ' giờ'],
        ['Ghi chú', item.note || '—']
    ].forEach(([label, value]) => {
        const row = document.createElement('div');
        row.className = 'sch-detail-row';
        const l = document.createElement('span');
        l.textContent = label;
        const v = document.createElement('div');
        v.textContent = value;
        row.append(l, v);
        body.appendChild(row);
    });

    if (item.teaser) {
        const p = document.createElement('p');
        p.textContent = item.teaser;
        body.appendChild(p);
    }
    $('schDetail').showModal();
}

$('schDetailClose').addEventListener('click', () => $('schDetail').close());

/* ---------- Khởi động ---------- */
/* ---------- Gợi ý lịch trình bằng AI ---------- */

function setAiStatus(text, isError) {
    const p = $('schAiStatus');
    p.textContent = text;
    p.className = 'sch-status' + (isError ? ' error' : '');
}

function openAi() {
    state.suggestion = null;
    state.aiSelected = new Set();
    $('schAiResult').innerHTML = '';
    $('schAiApply').hidden = true;
    $('schAiPickPanel').hidden = true;
    renderAiPick();
    setAiStatus('', false);
    $('schAiDialog').showModal();
}

function renderSuggestion(data) {
    const box = $('schAiResult');
    box.innerHTML = '';
    data.days.forEach((day, i) => {
        const wrap = document.createElement('div');
        wrap.className = 'sch-ai-day';
        const title = document.createElement('h4');
        title.textContent = 'Ngày ' + (i + 1);
        const ul = document.createElement('ul');
        day.items.forEach(item => {
            const li = document.createElement('li');
            const start = item.startTime.slice(0, 5);
            li.textContent = start + ' - ' + endTime(start, item.durationHours) + ' · ' + item.placeName;
            if (item.note) {
                const note = document.createElement('div');
                note.className = 'sch-ai-note';
                note.textContent = item.note;
                li.appendChild(note);
            }
            ul.appendChild(li);
        });
        wrap.append(title, ul);
        box.appendChild(wrap);
    });
}

async function generateSuggestion() {
    const picked = state.aiSelected.size ? [...state.aiSelected] : state.pool.map(p => p.id);
    if (picked.length < 2) {
        setAiStatus('Cần ít nhất 2 địa điểm để gợi ý lịch trình.', true);
        return;
    }

    const btn = $('schAiGenerate');
    btn.disabled = true;
    state.suggestion = null;
    $('schAiResult').innerHTML = '';
    $('schAiApply').hidden = true;
    setAiStatus('Đang nhờ AI lập lịch trình, vui lòng chờ vài chục giây...', false);
    try {
        const data = await api('/api/suggestions', {
            method: 'POST',
            body: JSON.stringify({
                days: Number($('schAiDays').value),
                preferences: $('schAiPrefs').value,
                placeIds: picked
            })
        });
        state.suggestion = data;
        renderSuggestion(data);
        $('schAiApply').hidden = false;
        setAiStatus('', false);
    } catch (e) {
        setAiStatus(e.message, true);   // hiện lỗi ngay trong bảng vì bảng đang che trang
    } finally {
        btn.disabled = false;
    }
}

async function applySuggestion() {
    const data = state.suggestion;
    if (!data) return;
    const btn = $('schAiApply');
    btn.disabled = true;
    setAiStatus('Đang tạo lịch trình...', false);
    try {
        let firstId = null;
        for (const day of data.days) {
            const created = await api('/api/days', { method: 'POST' });
            if (firstId === null) firstId = created.id;
            for (const item of day.items) {
                await api('/api/days/' + created.id + '/items', {
                    method: 'POST',
                    body: JSON.stringify({
                        placeId: item.placeId,
                        startTime: item.startTime,
                        durationHours: item.durationHours,
                        note: item.note
                    })
                });
            }
        }
        $('schAiDialog').close();
        state.activeDayId = firstId;
        state.selectedItemId = null;
        setLastAction(null);
        await loadDays();
    } catch (e) {
        setAiStatus(e.message, true);
        await loadDays();
    } finally {
        btn.disabled = false;
    }
}
/* ---------- Chọn địa điểm cho gợi ý AI ---------- */

function updateAiPickLabel() {
    const n = state.aiSelected.size;
    $('schAiPickBtn').textContent = n === 0
        ? 'Tất cả địa điểm đã chọn (' + state.pool.length + ')'
        : 'Đã chọn ' + n + ' địa điểm';
}

function renderAiPick() {
    const panel = $('schAiPickPanel');
    panel.innerHTML = '';

    const tools = document.createElement('div');
    tools.className = 'sch-pick-tools';
    [
        ['Chọn tất cả', () => state.pool.forEach(p => state.aiSelected.add(p.id))],
        ['Bỏ chọn tất cả', () => state.aiSelected.clear()]
    ].forEach(([label, action]) => {
        const b = document.createElement('button');
        b.type = 'button';
        b.className = 'sch-link';
        b.textContent = label;
        b.onclick = () => { action(); renderAiPick(); };
        tools.appendChild(b);
    });
    panel.appendChild(tools);

    state.pool.forEach(p => {
        const row = document.createElement('label');
        row.className = 'sch-pick-row';
        const cb = document.createElement('input');
        cb.type = 'checkbox';
        cb.checked = state.aiSelected.has(p.id);
        cb.onchange = () => {
            if (cb.checked) state.aiSelected.add(p.id);
            else state.aiSelected.delete(p.id);
            updateAiPickLabel();
        };
        const name = document.createElement('span');
        name.textContent = p.name;
        const city = document.createElement('small');
        city.textContent = p.city;
        row.append(cb, name, city);
        panel.appendChild(row);
    });
    updateAiPickLabel();
}

$('schOpenAi').addEventListener('click', openAi);
$('schAiClose').addEventListener('click', () => $('schAiDialog').close());
$('schAiGenerate').addEventListener('click', generateSuggestion);
$('schAiApply').addEventListener('click', applySuggestion);
$('schAiPickBtn').addEventListener('click', () => {
    const panel = $('schAiPickPanel');
    panel.hidden = !panel.hidden;
});
$('schAiDialog').addEventListener('click', e => {
    if (!e.target.closest('#schAiPick')) $('schAiPickPanel').hidden = true;   // bấm ra ngoài thì đóng bảng
});
/* ---------- Lưu lịch trình ---------- */

const EDIT_KEY = 'sch.editing';   // nhớ bản đã lưu đang được chỉnh sửa (trang "Các lịch trình đã tạo" cũng dùng)

function getEditing() {
    try { return JSON.parse(localStorage.getItem(EDIT_KEY)); } catch (e) { return null; }
}

function setEditing(value) {
    if (value) localStorage.setItem(EDIT_KEY, JSON.stringify(value));
    else localStorage.removeItem(EDIT_KEY);
}

function showNotice(msg) {
    const box = $('schMessage');
    box.textContent = msg;
    box.classList.add('ok');
    box.hidden = false;
    clearTimeout(showError.timer);
    showError.timer = setTimeout(() => { box.hidden = true; box.classList.remove('ok'); }, 4000);
}

function renderEditingNote() {
    const box = $('schEditingNote');
    const editing = getEditing();
    box.innerHTML = '';
    box.hidden = !editing;
    if (!editing) return;
    const text = document.createElement('span');
    text.textContent = 'Lịch trình: ' + editing.name + '   ';
    const fresh = document.createElement('button');
    fresh.type = 'button';
    fresh.className = 'sch-link';
    fresh.textContent = 'Tạo lịch trình mới';
    fresh.onclick = startNewTrip;
    box.append(text, fresh);
}

async function startNewTrip() {
    if (!confirm('Xóa toàn bộ các ngày đang soạn để bắt đầu lịch trình mới? Các bản đã lưu không bị ảnh hưởng.')) return;
    try {
        await api('/api/days', { method: 'DELETE' });
        setEditing(null);
        renderEditingNote();
        state.activeDayId = null;
        state.selectedItemId = null;
        setLastAction(null);
        await loadDays();
    } catch (e) { showError(e.message); }
}

function showSaveError(msg) {
    const p = $('schSaveError');
    p.textContent = msg || '';
    p.hidden = !msg;
}

function openSaveDialog() {
    if (!state.days.length) { showError('Chưa có ngày nào để lưu.'); return; }
    const editing = getEditing();
    showSaveError('');
    $('schSaveName').value = editing ? editing.name : '';
    $('schSaveTitle').textContent = editing ? 'Cập nhật lịch trình' : 'Lưu lịch trình';
    $('schSaveHint').hidden = !editing;
    if (editing) {
        $('schSaveHint').textContent = 'Bạn đang chỉnh sửa "' + editing.name +
            '". Bấm Cập nhật để ghi đè bản này, hoặc lưu thành một bản mới.';
    }
    $('schSaveAsNew').hidden = !editing;
    $('schSaveConfirm').textContent = editing ? 'Cập nhật' : 'Lưu';
    $('schSaveDialog').showModal();
}

async function submitSave(asNew) {
    const name = $('schSaveName').value.trim();
    if (!name) { showSaveError('Hãy đặt tên cho lịch trình.'); return; }
    const editing = getEditing();
    try {
        const body = JSON.stringify({ name });
        const saved = (editing && !asNew)
            ? await api('/api/itineraries/' + editing.id, { method: 'PUT', body })
            : await api('/api/itineraries', { method: 'POST', body });
        setEditing({ id: saved.id, name: saved.name });
        renderEditingNote();
        $('schSaveDialog').close();
        showNotice('Đã lưu "' + saved.name + '". Xem tại mục Các lịch trình đã tạo.');
    } catch (e) {
        if (editing && !asNew && /Không tìm thấy/.test(e.message)) {
            setEditing(null);          // bản đã lưu không còn nữa
            renderEditingNote();
            showSaveError('Bản đã lưu này không còn tồn tại. Bấm Lưu để tạo bản mới.');
            $('schSaveConfirm').textContent = 'Lưu';
            $('schSaveAsNew').hidden = true;
        } else {
            showSaveError(e.message);
        }
    }
}

$('schSave').addEventListener('click', openSaveDialog);
$('schSaveCancel').addEventListener('click', () => $('schSaveDialog').close());
$('schSaveConfirm').addEventListener('click', () => submitSave(false));
$('schSaveAsNew').addEventListener('click', () => submitSave(true));
$('schSaveName').addEventListener('keydown', e => { if (e.key === 'Enter') submitSave(false); });

// Nhãn giữa hai thẻ: phương tiện của địa điểm phía trên và khoảng cách tới địa điểm kế tiếp
function renderLeg(prev, next) {
    const mode = TRANSPORTS[prev.transport];
    const parts = [];
    if (mode) parts.push(mode[0] + ' ' + mode[1]);
    parts.push('≈ ' + next.distanceFromPreviousKm.toLocaleString('vi-VN') + ' km');

    const li = document.createElement('li');
    li.className = 'sch-leg';
    const chip = document.createElement('button');
    chip.type = 'button';
    chip.className = 'sch-leg-chip';
    chip.textContent = parts.join(' · ');
    chip.title = 'Chọn phương tiện';
    chip.onclick = e => { e.stopPropagation(); openTransportMenu(prev, next, chip); };
    li.appendChild(chip);
    return li;
}
/* ---------- Chọn phương tiện giữa hai địa điểm ---------- */

function closeTransportMenu() {
    $('schTransportMenu').hidden = true;
}

function openTransportMenu(prev, next, anchor) {
    $('schTransportInfo').textContent = prev.placeName + ' → ' + next.placeName + '\n' +
        'Khoảng cách ≈ ' + next.distanceFromPreviousKm.toLocaleString('vi-VN') + ' km (đường chim bay)';

    const box = $('schTransportOptions');
    box.innerHTML = '';
    Object.entries(TRANSPORTS).forEach(([code, [icon, label]]) => {
        const b = document.createElement('button');
        b.type = 'button';
        b.textContent = icon + ' ' + label + (prev.transport === code ? '  ✓' : '');
        b.onclick = () => setTransport(prev, code);
        box.appendChild(b);
    });
    if (prev.transport) {
        const clear = document.createElement('button');
        clear.type = 'button';
        clear.textContent = '✕ Bỏ chọn phương tiện';
        clear.onclick = () => setTransport(prev, null);
        box.appendChild(clear);
    }

    const menu = $('schTransportMenu');
    menu.hidden = false;
    const r = anchor.getBoundingClientRect();
    const w = menu.offsetWidth;
    const h = menu.offsetHeight;
    const left = Math.min(Math.max(8, r.left + r.width / 2 - w / 2), window.innerWidth - w - 8);
    let top = r.bottom + 6;
    if (top + h > window.innerHeight - 8) top = Math.max(8, r.top - h - 6);
    menu.style.left = left + 'px';
    menu.style.top = top + 'px';
}

async function setTransport(item, code) {
    closeTransportMenu();
    try {
        await api('/api/items/' + item.id + '/transport', {
            method: 'PATCH',
            body: JSON.stringify({ transport: code })
        });
        state.selectedItemId = item.id;   // giữ thẻ đang chọn
        await loadItems();
    } catch (e) { showError(e.message); }
}

document.addEventListener('click', e => {
    const menu = $('schTransportMenu');
    if (!menu.hidden && !menu.contains(e.target)) closeTransportMenu();
});

initWheels();
renderEditingNote();
loadTrip().then(() => { loadPlaces(); loadDays(); });