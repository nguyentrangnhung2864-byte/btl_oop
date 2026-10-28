import { fetchPlaces, fetchFilterOptions, fetchAiOverview } from "./api.js";
import { createPlaceImage } from "./placeImage.js";
import { isSelected, addPlace, removePlace, onChange } from "./selection.js";
import "./tripPanel.js";

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

const aiText = document.getElementById("ai-text");
const aiError = document.getElementById("ai-error");
const aiBtn = document.getElementById("ai-btn");

let currentPlace = null; // địa điểm đang mở trong cửa sổ chi tiết

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
// ---- Ô tỉnh/thành phố phụ thuộc vào khu vực ----
let allCities = [];       // tất cả tỉnh/thành phố
let citiesByRegion = {};  // { "Miền Bắc": ["Cao Bằng", "Hà Nội", ...], ... }

// Ghép "khu vực → các tỉnh/thành" từ danh sách địa điểm
function buildCityMap(places) {
    const sets = {};
    places.forEach((place) => {
        if (!sets[place.region]) sets[place.region] = new Set();
        sets[place.region].add(place.city);
    });
    citiesByRegion = {};
    Object.entries(sets).forEach(([region, set]) => {
        citiesByRegion[region] = [...set].sort((a, b) => a.localeCompare(b, "vi"));
    });
}

// Vẽ lại ô tỉnh/thành phố: chọn khu vực nào thì chỉ hiện tỉnh/thành của khu vực đó
function refillCities() {
    const region = regionSelect.value;
    const cities = region && citiesByRegion[region] ? citiesByRegion[region] : allCities;
    citySelect.replaceChildren(new Option("Tất cả", ""));
    fillSelect(citySelect, cities);
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
    currentPlace = place;
    modalMedia.replaceChildren(createPlaceImage(place));
    modalTags.replaceChildren(
        el("span", "tag", place.region),
        el("span", "tag", place.city),
        el("span", "tag", place.category)
    );
    modalName.textContent = place.name;
    modalTeaser.textContent = place.teaser || "";
    showAiState(place);
    updateSelectBtn();
    backdrop.hidden = false;
}

function closeModal() {
    currentPlace = null;
    backdrop.hidden = true;
}

modalClose.addEventListener("click", closeModal);
backdrop.addEventListener("click", (e) => {
    if (e.target === backdrop) closeModal(); // chỉ đóng khi bấm vào nền tối
});
document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") closeModal();
});

// ---- Nút "Thêm vào danh sách" trong cửa sổ chi tiết ----
const selectBtn = document.getElementById("select-btn");

function updateSelectBtn() {
    if (!currentPlace) return;
    const chosen = isSelected(currentPlace.id);
    selectBtn.textContent = chosen ? "✓ Đã thêm vào danh sách · Bấm để bỏ" : "+ Thêm vào danh sách";
    selectBtn.classList.toggle("chosen", chosen);
}

selectBtn.addEventListener("click", () => {
    if (!currentPlace) return;
    if (isSelected(currentPlace.id)) {
        removePlace(currentPlace.id);
    } else {
        addPlace(currentPlace);
    }
});

onChange(updateSelectBtn); // thêm hay bỏ xong thì chữ trên nút tự cập nhật

// ---- Phần AI giới thiệu trong cửa sổ chi tiết ----
// Hiện bản giới thiệu đã có (nếu có) và đặt đúng chữ trên nút
function showAiState(place) {
    aiError.hidden = true;
    aiBtn.disabled = false;
    if (place.aiOverview) {
        aiText.textContent = place.aiOverview;
        aiText.hidden = false;
        aiBtn.textContent = "Tạo lại";
    } else {
        aiText.hidden = true;
        aiBtn.textContent = "Giới thiệu bằng AI";
    }
}

async function loadAiOverview() {
    const place = currentPlace;
    if (!place) return;

    aiBtn.disabled = true; // chặn bấm liên tiếp
    aiBtn.textContent = "Đang tạo...";
    aiError.hidden = true;

    try {
        // Đã có bản giới thiệu mà vẫn bấm nghĩa là muốn tạo lại
        const data = await fetchAiOverview(place.id, Boolean(place.aiOverview));
        place.aiOverview = data.overview; // nhớ lại để mở lần sau không phải gọi nữa
        if (currentPlace === place) showAiState(place);
    } catch (e) {
        if (currentPlace !== place) return; // người dùng đã chuyển sang địa điểm khác
        aiError.textContent = e.message;
        aiError.hidden = false;
        aiBtn.disabled = false;
        aiBtn.textContent = place.aiOverview ? "Tạo lại" : "Giới thiệu bằng AI";
    }
}

aiBtn.addEventListener("click", loadAiOverview);

// ---- Gắn sự kiện cho bộ lọc ----
keywordInput.addEventListener("input", scheduleLoad);
// Đổi khu vực: cập nhật lại ô tỉnh/thành phố (và đưa nó về "Tất cả") rồi mới tải danh sách
regionSelect.addEventListener("change", () => {
    refillCities();
    scheduleLoad();
});
[citySelect, categorySelect].forEach((select) =>
    select.addEventListener("change", scheduleLoad)
);

resetBtn.addEventListener("click", () => {
    keywordInput.value = "";
    regionSelect.value = "";
    refillCities(); // trả ô tỉnh/thành phố về đầy đủ
    categorySelect.value = "";
    scheduleLoad();
});

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
        fillSelect(categorySelect, options.categories);
        allCities = options.cities;
        try {
            // Lấy toàn bộ địa điểm một lần để biết tỉnh/thành nào thuộc khu vực nào
            buildCityMap(await fetchPlaces());
        } catch (e) {
            // Không có thông tin này thì ô tỉnh/thành phố hiện tất cả như cũ
        }
        refillCities();
    } catch (e) {
        showError("Không kết nối được máy chủ");
    }
    loadPlaces();
}

init();