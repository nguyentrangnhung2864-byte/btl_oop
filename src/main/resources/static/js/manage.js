import {
    fetchPlaces,
    fetchFilterOptions,
    createPlace,
    updatePlace,
    deletePlace,
    ApiError,
} from "./api.js";

// ---- Lấy các phần tử trên trang ----
const searchInput = document.getElementById("search");
const addBtn = document.getElementById("add-btn");
const rows = document.getElementById("place-rows");
const listStatus = document.getElementById("list-status");
const listError = document.getElementById("list-error");
const notice = document.getElementById("notice");

const backdrop = document.getElementById("form-backdrop");
const form = document.getElementById("place-form");
const formTitle = document.getElementById("form-title");
const formError = document.getElementById("form-error");
const saveBtn = document.getElementById("save-btn");
const categoryList = document.getElementById("category-list");

const FIELDS = ["name", "city", "region", "category", "teaser", "latitude", "longitude"];
let editingId = null; // null nghĩa là đang thêm mới

// ---- Hàm tiện ích ----
function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
}

function fieldInput(name) {
    return document.getElementById(`f-${name}`);
}

let noticeTimer;
function showNotice(message, isError = false) {
    notice.textContent = message;
    notice.className = "notice" + (isError ? " bad" : "");
    notice.hidden = false;
    clearTimeout(noticeTimer);
    noticeTimer = setTimeout(() => (notice.hidden = true), 4000);
}

// ---- Bảng danh sách ----
function formatCoord(place) {
    return place.latitude != null && place.longitude != null
        ? `${place.latitude}, ${place.longitude}`
        : "—";
}

function renderRows(places) {
    rows.replaceChildren();
    places.forEach((place) => {
        const tr = el("tr");
        tr.append(
            el("td", "col-name", place.name),
            el("td", "", place.city),
            el("td", "", place.region),
            el("td", "", place.category),
            el("td", "col-coord", formatCoord(place))
        );

        const editBtn = el("button", "btn-small", "Sửa");
        editBtn.type = "button";
        editBtn.addEventListener("click", () => openForm(place));

        const deleteBtn = el("button", "btn-small btn-danger", "Xóa");
        deleteBtn.type = "button";
        deleteBtn.addEventListener("click", () => removePlace(place));

        const actions = el("td", "col-actions");
        actions.append(editBtn, deleteBtn);
        tr.appendChild(actions);

        rows.appendChild(tr);
    });
}

let timer;
let requestId = 0;

async function loadPlaces() {
    const myId = ++requestId;
    try {
        const places = await fetchPlaces({ keyword: searchInput.value.trim() });
        if (myId !== requestId) return; // đã có yêu cầu mới hơn
        renderRows(places);
        listError.hidden = true;
        listStatus.textContent = places.length > 0 ? `${places.length} địa điểm` : "Không có địa điểm nào.";
    } catch (e) {
        if (myId !== requestId) return;
        listError.textContent = "Không tải được danh sách địa điểm";
        listError.hidden = false;
        listStatus.textContent = "";
    }
}

searchInput.addEventListener("input", () => {
    clearTimeout(timer);
    timer = setTimeout(loadPlaces, 300);
});

// ---- Biểu mẫu thêm / sửa ----
function clearErrors() {
    formError.hidden = true;
    document.querySelectorAll(".field-error").forEach((span) => (span.textContent = ""));
}

function openForm(place = null) {
    editingId = place ? place.id : null;
    formTitle.textContent = place ? "Sửa địa điểm" : "Thêm địa điểm";
    clearErrors();
    FIELDS.forEach((name) => {
        fieldInput(name).value = place ? place[name] ?? "" : "";
    });
    backdrop.hidden = false;
    fieldInput("name").focus();
}

function closeForm() {
    backdrop.hidden = true;
}

function readForm() {
    const text = (name) => fieldInput(name).value.trim();
    const number = (name) => {
        const value = fieldInput(name).value.trim();
        return value === "" ? null : Number(value);
    };
    return {
        name: text("name"),
        city: text("city"),
        region: text("region"),
        category: text("category"),
        teaser: text("teaser"),
        latitude: number("latitude"),
        longitude: number("longitude"),
    };
}

form.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearErrors();
    saveBtn.disabled = true;

    const isNew = editingId === null;
    try {
        const data = readForm();
        if (isNew) {
            await createPlace(data);
        } else {
            await updatePlace(editingId, data);
        }
        closeForm();
        showNotice(isNew ? "Đã thêm địa điểm." : "Đã lưu thay đổi.");
        loadPlaces();
    } catch (err) {
        // Lỗi theo từng ô (mã 400): hiện ngay dưới ô đó
        if (err instanceof ApiError && err.errors) {
            Object.entries(err.errors).forEach(([field, message]) => {
                const span = document.querySelector(`.field-error[data-for="${field}"]`);
                if (span) span.textContent = message;
            });
        }
        formError.textContent =
            err instanceof ApiError ? err.message : "Không kết nối được máy chủ";
        formError.hidden = false;
    } finally {
        saveBtn.disabled = false;
    }
});

// ---- Xóa ----
async function removePlace(place) {
    if (!confirm(`Xóa địa điểm "${place.name}"?\nThao tác này không thể hoàn tác.`)) return;
    try {
        await deletePlace(place.id);
        showNotice(`Đã xóa "${place.name}".`);
        loadPlaces();
    } catch (err) {
        // Ví dụ mã 409: địa điểm đang nằm trong một lịch trình
        showNotice(err.message || "Không xóa được địa điểm", true);
    }
}

// ---- Gắn sự kiện ----
addBtn.addEventListener("click", () => openForm());
document.getElementById("form-close").addEventListener("click", closeForm);
document.getElementById("cancel-btn").addEventListener("click", closeForm);
backdrop.addEventListener("click", (e) => {
    if (e.target === backdrop) closeForm();
});
document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") closeForm();
});

// ---- Khởi động ----
async function init() {
    try {
        // Gợi ý các loại hình đã có khi nhập (vẫn cho nhập loại hình mới)
        const options = await fetchFilterOptions();
        options.categories.forEach((category) => {
            const option = document.createElement("option");
            option.value = category;
            categoryList.appendChild(option);
        });
    } catch (e) {
        // Không có gợi ý cũng không sao
    }
    loadPlaces();
}

init();