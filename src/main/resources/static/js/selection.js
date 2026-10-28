// selection.js: lưu danh sách địa điểm người dùng đã chọn (trong trình duyệt)
// Trang nào cũng có thể import file này để đọc danh sách, kể cả trang Lập lịch trình của B.

const KEY = "selectedPlaces";
const listeners = [];

function load() {
    try {
        const data = JSON.parse(localStorage.getItem(KEY));
        return Array.isArray(data) ? data : [];
    } catch (e) {
        return [];
    }
}

function save(items) {
    try {
        localStorage.setItem(KEY, JSON.stringify(items));
    } catch (e) {
        // Trình duyệt chặn lưu trữ thì bỏ qua
    }
    listeners.forEach((fn) => fn(items));
}

// Danh sách đã chọn: [{ placeId, name, city, region, category, latitude, longitude }]
export function getSelected() {
    return load();
}

export function isSelected(placeId) {
    return load().some((item) => item.placeId === placeId);
}

export function addPlace(place) {
    const items = load();
    if (items.some((item) => item.placeId === place.id)) return;
    items.push({
        placeId: place.id,
        name: place.name,
        city: place.city,
        region: place.region,
        category: place.category,
        latitude: place.latitude ?? null,
        longitude: place.longitude ?? null,
    });
    save(items);
}

export function removePlace(placeId) {
    save(load().filter((item) => item.placeId !== placeId));
}

export function clearAll() {
    save([]);
}

// Đăng ký hàm được gọi mỗi khi danh sách thay đổi
export function onChange(fn) {
    listeners.push(fn);
}