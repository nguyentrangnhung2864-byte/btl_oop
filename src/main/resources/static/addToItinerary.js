// Dùng ở trang Khám phá:  window.addToItinerary(placeId, placeName)
(function () {
    const css = `
    .ati-dialog { border: 0; border-radius: 12px; padding: 0; width: calc(100% - 32px); max-width: 400px; }
    .ati-dialog::backdrop { background: rgba(0, 0, 0, .45); }
    .ati-form { display: flex; flex-direction: column; gap: 12px; padding: 20px; }
    .ati-form h3 { margin: 0; }
    .ati-field { display: flex; flex-direction: column; gap: 4px; font-size: 13px; color: var(--ink-soft, #6b6250); }
    .ati-field select, .ati-field input { padding: 8px; border: 1px solid var(--line, #dccfa8); border-radius: 6px; font: inherit; }
    .ati-row { display: flex; gap: 10px; }
    .ati-row .ati-field { flex: 1; }
    .ati-actions { display: flex; justify-content: flex-end; gap: 8px; }
    .ati-actions button { padding: 8px 14px; border-radius: 6px; cursor: pointer; font: inherit; border: 1px solid var(--line, #dccfa8); background: var(--card, #fbf7ec); }
    .ati-actions .ati-primary { background: var(--bg-deep, #1e2b2e); color: #fff; border: 0; }
    .ati-msg { margin: 0; font-size: 13px; }
    .ati-msg.error { color: var(--red, #a6402d); }
    .ati-msg.ok { color: #1e7e34; }
    `;

    let dialog, form, title, daySelect, hourSelect, minuteSelect, duration, msg;
    let current = null;

    async function request(url, options) {
        const res = await fetch(url, Object.assign({ headers: { 'Content-Type': 'application/json' } }, options));
        const data = res.status === 204 ? null : await res.json().catch(() => null);
        if (!res.ok) {
            const detail = data && data.errors ? Object.values(data.errors).join('; ') : '';
            const text = (data && data.message) || 'Có lỗi xảy ra';
            throw new Error(detail ? text + ': ' + detail : text);
        }
        return data;
    }

    function fill(select, values, pad) {
        select.innerHTML = '';
        values.forEach(v => {
            const opt = document.createElement('option');
            opt.value = v;
            opt.textContent = pad ? String(v).padStart(2, '0') : v;
            select.appendChild(opt);
        });
    }

    function showMsg(text, kind) {
        msg.hidden = !text;
        msg.className = 'ati-msg ' + (kind || '');
        msg.textContent = text || '';
    }

    function ensureDialog() {
        if (dialog) return;
        const style = document.createElement('style');
        style.textContent = css;
        document.head.appendChild(style);

        dialog = document.createElement('dialog');
        dialog.className = 'ati-dialog';
        dialog.innerHTML =
            '<form class="ati-form">' +
            '<h3></h3>' +
            '<label class="ati-field"><span>Thêm vào</span><select class="ati-day"></select></label>' +
            '<div class="ati-row">' +
            '<label class="ati-field"><span>Giờ</span><select class="ati-hour"></select></label>' +
            '<label class="ati-field"><span>Phút</span><select class="ati-minute"></select></label>' +
            '<label class="ati-field"><span>Thời lượng (giờ)</span><input class="ati-duration" type="number" min="0.5" step="0.5" value="1"></label>' +
            '</div>' +
            '<p class="ati-msg" hidden></p>' +
            '<div class="ati-actions"><button type="button" class="ati-close">Đóng</button><button type="submit" class="ati-primary">Thêm</button></div>' +
            '</form>';
        document.body.appendChild(dialog);

        form = dialog.querySelector('form');
        title = dialog.querySelector('h3');
        daySelect = dialog.querySelector('.ati-day');
        hourSelect = dialog.querySelector('.ati-hour');
        minuteSelect = dialog.querySelector('.ati-minute');
        duration = dialog.querySelector('.ati-duration');
        msg = dialog.querySelector('.ati-msg');

        fill(hourSelect, Array.from({ length: 24 }, (_, i) => i), true);
        fill(minuteSelect, Array.from({ length: 12 }, (_, i) => i * 5), true);
        dialog.querySelector('.ati-close').addEventListener('click', () => dialog.close());
        form.addEventListener('submit', submit);
    }

    async function submit(e) {
        e.preventDefault();
        if (!current) return;
        try {
            let dayId = daySelect.value;
            let dayNumber = daySelect.selectedOptions[0].dataset.number;
            if (dayId === 'new') {
                const created = await request('/api/days', { method: 'POST' });
                dayId = created.id;
                dayNumber = created.dayNumber;
            }
            const hh = String(hourSelect.value).padStart(2, '0');
            const mm = String(minuteSelect.value).padStart(2, '0');
            await request('/api/days/' + dayId + '/items', {
                method: 'POST',
                body: JSON.stringify({
                    placeId: current.placeId,
                    startTime: hh + ':' + mm,
                    durationHours: Number(duration.value)
                })
            });
            showMsg('Đã thêm "' + current.placeName + '" vào Ngày ' + dayNumber + '.', 'ok');
            const link = document.createElement('a');
            link.href = '/itinerary.html';
            link.textContent = ' Xem lịch trình';
            msg.appendChild(link);
        } catch (err) {
            showMsg(err.message, 'error');
        }
    }

    async function open(placeId, placeName) {
        ensureDialog();
        current = { placeId, placeName };
        title.textContent = 'Thêm "' + placeName + '" vào lịch trình';
        showMsg('', '');
        daySelect.innerHTML = '';
        try {
            const days = await request('/api/days');
            days.forEach(d => {
                const opt = document.createElement('option');
                opt.value = d.id;
                opt.dataset.number = d.dayNumber;
                opt.textContent = 'Ngày ' + d.dayNumber;
                daySelect.appendChild(opt);
            });
        } catch (err) {
            showMsg(err.message, 'error');
        }
        const created = document.createElement('option');
        created.value = 'new';
        created.textContent = '+ Tạo ngày mới';
        daySelect.appendChild(created);
        hourSelect.value = '9';
        minuteSelect.value = '0';
        duration.value = 1;
        dialog.showModal();
    }

    window.addToItinerary = open;
})();