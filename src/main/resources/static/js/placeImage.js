// Đổi tên địa điểm thành tên file ảnh: "Hồ Gươm" -> "ho-guom"
export function slugify(name) {
    return name
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "") // bỏ dấu
        .replace(/đ/g, "d")
        .replace(/Đ/g, "D")
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, "-")
        .replace(/^-+|-+$/g, "");
}

export function imageSrc(name) {
    return `/images/places/${slugify(name)}.jpg`;
}

// Màu và biểu tượng dùng khi địa điểm chưa có ảnh
const DEFAULT_STYLE = { icon: "📍", from: "#6b6250", to: "#a89f88" };

const CATEGORY_STYLE = {
    "Thiên nhiên": { icon: "🌿", from: "#2f6b62", to: "#7fb59a" },
    "Biển & đảo": { icon: "🏝️", from: "#2c6e8f", to: "#7cc3d9" },
    "Chùa & đền": { icon: "🪷", from: "#8a5a2b", to: "#d9a94a" },
    "Văn hóa - lịch sử": { icon: "🏺", from: "#7a3f2b", to: "#c98a6b" },
    "Bảo tàng": { icon: "🏛️", from: "#3b5679", to: "#8fa7c7" },
    "Chợ & mua sắm": { icon: "🛍️", from: "#a6402d", to: "#e0957f" },
    "Vui chơi & giải trí": { icon: "🎡", from: "#7b4a8f", to: "#c39ad6" },
    "Phố cổ & làng nghề": { icon: "🏮", from: "#b8862f", to: "#e8c577" },
};

// Khung màu thay thế khi chưa có file ảnh
function createPlaceholder(place) {
    const style = CATEGORY_STYLE[place.category] || DEFAULT_STYLE;
    const box = document.createElement("div");
    box.className = "place-img placeholder";
    box.style.background = `linear-gradient(135deg, ${style.from}, ${style.to})`;
    const icon = document.createElement("span");
    icon.textContent = style.icon;
    box.appendChild(icon);
    return box;
}

// Tạo phần tử ảnh; nếu file ảnh không tồn tại thì tự đổi sang khung màu
export function createPlaceImage(place) {
    const img = document.createElement("img");
    img.className = "place-img";
    img.src = imageSrc(place.name);
    img.alt = place.name;
    img.loading = "lazy";
    img.onerror = () => img.replaceWith(createPlaceholder(place));
    return img;
}