// Icon dạng SVG (nội dung cố định do nhóm viết, không lấy từ người dùng)
const ICONS = {
    compass: '<circle cx="12" cy="12" r="10"/><polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"/>',
    calendar: '<rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>',
    chevron: '<polyline points="9 18 15 12 9 6"/>',
};

function icon(name) {
    return `<svg class="icon" viewBox="0 0 24 24" aria-hidden="true">${ICONS[name]}</svg>`;
}

// Danh sách mục menu. Muốn thêm trang mới, chỉ cần thêm một khối vào đây.
const MENU = [
    { href: "/index.html", icon: "compass", title: "Khám phá địa điểm", subtitle: "Tìm cảm hứng mới" },
    { href: "/itinerary.html", icon: "calendar", title: "Lập lịch trình", subtitle: "Tạo chuyến đi của bạn" },
];

// Mở "/" thì coi như đang ở "/index.html"
function currentPath() {
    const path = window.location.pathname;
    return path === "/" ? "/index.html" : path;
}

function renderSidebar() {
    const sidebar = document.getElementById("sidebar");
    if (!sidebar) return;

    const items = MENU.map(
        (item) => `
      <a class="menu-item${item.href === currentPath() ? " active" : ""}" href="${item.href}">
        <span class="menu-icon">${icon(item.icon)}</span>
        <span class="menu-text">
          <strong>${item.title}</strong>
          <small>${item.subtitle}</small>
        </span>
        <span class="menu-chevron">${icon("chevron")}</span>
      </a>`
    ).join("");

    sidebar.innerHTML = `
    <a class="sidebar-brand" href="/index.html">
      <span class="brand-logo"><span class="icon-box">${icon("compass")}</span></span>
      <span class="brand-text">
        <strong>Sổ Tay Lịch Trình</strong>
        <small>VIỆT NAM</small>
      </span>
    </a>
    <nav class="menu">${items}</nav>`;
}

renderSidebar();