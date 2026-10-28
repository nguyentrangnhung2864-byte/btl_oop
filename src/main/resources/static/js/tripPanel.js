import { getSelected, removePlace, clearAll, onChange } from "./selection.js";
import { createPlaceImage } from "./placeImage.js";

// ---- Lấy các phần tử trên trang ----
const tripBtn = document.getElementById("trip-btn");
const tripCount = document.getElementById("trip-count");
const backdrop = document.getElementById("trip-backdrop");
const closeBtn = document.getElementById("trip-close");
const list = document.getElementById("trip-list");
const empty = document.getElementById("trip-empty");
const moreBtn = document.getElementById("trip-more");
const clearBtn = document.getElementById("trip-clear");
const goLink = document.getElementById("trip-go");

const TRASH_ICON =
    '<svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>';

function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
}

// ---- Vẽ một địa điểm trong danh sách ----
function buildItem(item) {
    const row = el("div", "trip-item");

    const thumb = el("div", "trip-thumb");
    thumb.appendChild(createPlaceImage(item)); // item có name, category nên dùng được luôn

    const info = el("div", "trip-info");
    info.append(
        el("div", "trip-name", item.name),
        el("div", "trip-meta", `${item.city} · ${item.category}`)
    );

    const removeBtn = el("button", "trip-remove");
    removeBtn.type = "button";
    removeBtn.title = "Bỏ khỏi danh sách";
    removeBtn.innerHTML = TRASH_ICON; // biểu tượng cố định do nhóm viết
    removeBtn.addEventListener("click", () => removePlace(item.placeId));

    row.append(thumb, info, removeBtn);
    return row;
}

// ---- Vẽ lại toàn bộ (số đếm, danh sách, trạng thái các nút) ----
function render() {
    const items = getSelected();
    tripCount.textContent = String(items.length);
    tripCount.classList.toggle("zero", items.length === 0);
    empty.hidden = items.length > 0;
    clearBtn.disabled = items.length === 0;
    goLink.classList.toggle("disabled", items.length === 0);
    list.replaceChildren(...items.map(buildItem));
}

// ---- Mở, đóng ngăn kéo ----
function openPanel() {
    backdrop.hidden = false;
}

function closePanel() {
    backdrop.hidden = true;
}

// ---- Gắn sự kiện ----
tripBtn.addEventListener("click", openPanel);
closeBtn.addEventListener("click", closePanel);
backdrop.addEventListener("click", (e) => {
    if (e.target === backdrop) closePanel(); // chỉ đóng khi bấm vào nền tối
});
document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") closePanel();
});

moreBtn.addEventListener("click", () => {
    closePanel();
    document.getElementById("place-grid")?.scrollIntoView({ behavior: "smooth" });
});

clearBtn.addEventListener("click", () => {
    if (confirm("Xóa tất cả địa điểm đã chọn?")) clearAll();
});

onChange(render);
render();