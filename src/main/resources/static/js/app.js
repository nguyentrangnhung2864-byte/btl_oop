import { fetchPlaces, fetchFilterOptions } from "./api.js";
import { createPlaceImage } from "./placeImage.js";

// ---- Lấy các phần tử trên trang ----
const keywordInput = document.getElementById("keyword");
const regionSelect = document.getElementById("region");
const citySelect = document.getElementById("city");
const categorySelect = document.getElementById("category");
const resetBtn = document.getElementById("reset-btn");
const errorEl = document.getElementById("error");
const statusEl = document.getElementById("status");
const grid = document.getElementById("place-grid");

const backdrop = document.getElementById("modal-backdrop");
const modalClose = document.getElementById("modal-close");
const modalMedia = document.getElementById("modal-media");
const modalTags = document.getElementById("modal-tags");
const modalName = document.getElementById("modal-name");
const modalTeaser = document.getElementById("modal-teaser");

// ---- Hàm tiện ích ----
// Tạo nhanh một phần tử HTML. Dùng textContent để dữ liệu không bị hiểu thành mã HTML.
function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
}

function fillSelect(select, values) {
    values.forEach((value) => {
        const option = document.createElement("option");
        option.value = value;
        option.textContent = value;
        select.appendChild(option);
    });
}

function showError(message) {
    errorEl.textContent = message;
    errorEl.hidden = false;
}

// ---- Hiển thị danh sách thẻ địa điểm ----
function renderPlaces(places) {
    grid.replaceChildren(); // xóa các thẻ cũ

    places.forEach((place) => {
        const card = el("article", "place-card");

        const media = el("div", "card-media");
        media.appendChild(createPlaceImage(place));
        media.appendChild(el("span", "badge", place.region));

        const body = el("div", "card-body");
        body.appendChild(el("h3", "", place.name));
        body.appendChild(el("p", "meta", `📍 ${place.city} · ${place.category}`));
        body.appendChild(el("p", "teaser", place.teaser || ""));

        card.append(media, body);
        card.addEventListener("click", () => openModal(place));
        grid.appendChild(card);
    });
}

// ---- Tải dữ liệu ----
let timer;
let requestId = 0;

async function loadPlaces() {
    const myId = ++requestId;
    try {
        const places = await fetchPlaces({
            region: regionSelect.value,
            city: citySelect.value,
            category: categorySelect.value,
            keyword: keywordInput.value.trim(),
        });
        if (myId !== requestId) return; // đã có yêu cầu mới hơn, bỏ kết quả này
        renderPlaces(places);
        errorEl.hidden = true;
        statusEl.textContent =
            places.length > 0 ? `${places.length} điểm đến phù hợp` : "Chưa có điểm đến nào phù hợp.";
    } catch (e) {
        if (myId !== requestId) return;
        showError("Không tải được danh sách địa điểm");
        statusEl.textContent = "";
    }
}

// Chờ 300ms sau khi ngừng gõ/chọn rồi mới gọi API
function scheduleLoad() {
    clearTimeout(timer);
    statusEl.textContent = "Đang tải...";
    resetBtn.hidden = !(
        keywordInput.value || regionSelect.value || citySelect.value || categorySelect.value
    );
    timer = setTimeout(loadPlaces, 300);
}

// ---- Cửa sổ chi tiết ----
function openModal(place) {
    modalMedia.replaceChildren(createPlaceImage(place));
    modalTags.replaceChildren(
        el("span", "tag", place.region),
        el("span", "tag", place.city),
        el("span", "tag", place.category)
    );
    modalName.textContent = place.name;
    modalTeaser.textContent = place.teaser || "";
    backdrop.hidden = false;
}

function closeModal() {
    backdrop.hidden = true;
}

modalClose.addEventListener("click", closeModal);
backdrop.addEventListener("click", (e) => {
    if (e.target === backdrop) closeModal(); // chỉ đóng khi bấm vào nền tối
});
document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") closeModal();
});

// ---- Gắn sự kiện cho bộ lọc ----
keywordInput.addEventListener("input", scheduleLoad);
[regionSelect, citySelect, categorySelect].forEach((select) =>
    select.addEventListener("change", scheduleLoad)
);

resetBtn.addEventListener("click", () => {
    keywordInput.value = "";
    regionSelect.value = "";
    citySelect.value = "";
    categorySelect.value = "";
    scheduleLoad();
});

// ---- Khởi động khi mở trang ----
async function init() {
    statusEl.textContent = "Đang tải...";
    try {
        const options = await fetchFilterOptions();
        fillSelect(regionSelect, options.regions);
        fillSelect(citySelect, options.cities);
        fillSelect(categorySelect, options.categories);
    } catch (e) {
        showError("Không kết nối được máy chủ");
    }
    loadPlaces();
}

init();