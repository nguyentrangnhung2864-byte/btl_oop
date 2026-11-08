import { getMe, logout } from "./auth.js";
import { openAuthModal } from "./authModal.js";
import { clearAll } from "./selection.js";

// Icon dạng SVG (nội dung cố định do nhóm viết, không lấy từ người dùng)
const ICONS = {
    bookmark: '<path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>',
    compass: '<circle cx="12" cy="12" r="10"/><polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76"/>',
    calendar: '<rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>',
    chevron: '<polyline points="9 18 15 12 9 6"/>',
    list: '<line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/>',
};

function icon(name) {
    return `<svg class="icon" viewBox="0 0 24 24" aria-hidden="true">${ICONS[name]}</svg>`;
}

// Danh sách mục menu. adminOnly: chỉ quản trị viên mới thấy.
// Nếu nhóm chưa làm trang Quản lý địa điểm thì xóa dòng thứ ba.
const MENU = [
    { href: "/index.html", icon: "compass", title: "Khám phá địa điểm", subtitle: "Tìm cảm hứng mới" },
    { href: "/itinerary.html", icon: "calendar", title: "Lập lịch trình", subtitle: "Tạo chuyến đi của bạn",loginOnly: true },
    { href: "/saved.html", icon: "bookmark", title: "Các lịch trình đã tạo", subtitle: "Xem lại và chỉnh sửa" ,loginOnly: true},
    { href: "/manage.html", icon: "list", title: "Quản lý địa điểm", subtitle: "Thêm, sửa, xóa", adminOnly: true },
];

// Mở "/" thì coi như đang ở "/index.html"
function currentPath() {
    const path = window.location.pathname;
    return path === "/" ? "/index.html" : path;
}

function createEl(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
}

// Khu vực người dùng ở cuối sidebar.
// me === undefined: đang hỏi máy chủ, tạm để trống cho khỏi nhấp nháy.
function renderUserBox(me) {
    const box = document.getElementById("sidebar-user");
    if (!box) return;
    box.replaceChildren();
    if (me === undefined) return;

    if (me.authenticated) {
        const user = me.user;
        const initial = Array.from(user.displayName.trim())[0] || "?";

        const name = createEl("div", "user-name");
        name.appendChild(createEl("span", "", user.displayName));
        if (user.role === "ADMIN") name.appendChild(createEl("span", "role-badge", "ADMIN"));

        const info = createEl("div", "user-info");
        info.append(name, createEl("div", "user-email", user.email));

        const card = createEl("div", "user-card");
        card.append(createEl("div", "user-avatar", initial.toUpperCase()), info);

        const logoutBtn = createEl("button", "user-btn", "Đăng xuất");
        logoutBtn.type = "button";
        logoutBtn.addEventListener("click", async () => {
            logoutBtn.disabled = true;
            try {
                await logout();
            } catch (e) {
                // Lỗi gì cũng vẫn chuyển về trang chủ
            }
            clearAll(); // xóa danh sách đã chọn để người dùng sau không thấy
            window.location.href = "/index.html";
        });

        const actions = createEl("div", "user-actions");
        actions.appendChild(logoutBtn);
        box.append(card, actions);
    } else {
        // Bấm vào thì mở cửa sổ đăng nhập, đăng ký ngay trên trang đang xem
        const loginBtn = createEl("button", "user-btn primary", "Đăng nhập");
        loginBtn.type = "button";
        loginBtn.addEventListener("click", () => openAuthModal("login"));

        const registerBtn = createEl("button", "user-btn", "Đăng ký");
        registerBtn.type = "button";
        registerBtn.addEventListener("click", () => openAuthModal("register"));

        const actions = createEl("div", "user-actions");
        actions.append(loginBtn, registerBtn);

        box.append(
            createEl("p", "user-hint", "Bạn chưa đăng nhập. Đăng nhập để lập lịch trình và dùng AI giới thiệu."),
            actions
        );
    }
}

function renderSidebar(me) {
    const sidebar = document.getElementById("sidebar");
    if (!sidebar) return;

    const enforced = Boolean(me && me.authEnabled);
    const isAdmin = Boolean(me && me.authenticated && me.user.role === "ADMIN");
    // Mục dành cho quản trị: hiện với quản trị viên, hoặc khi chưa bật kiểm tra quyền (để dễ phát triển)
    const canSeeAdmin = isAdmin || Boolean(me && !me.authEnabled);

    const items = MENU.filter((item) => !item.adminOnly || canSeeAdmin)
        .map(
            (item) => `
      <a class="menu-item${item.href === currentPath() ? " active" : ""}" href="${item.href}"${item.loginOnly ? ' data-login="1"' : ""}>
        <span class="menu-icon">${icon(item.icon)}</span>
        <span class="menu-text">
          <strong>${item.title}</strong>
          <small>${item.subtitle}</small>
        </span>
        <span class="menu-chevron">${icon("chevron")}</span>
      </a>`
        )
        .join("");

    sidebar.innerHTML = `
    <a class="sidebar-brand" href="/index.html">
      <span class="brand-logo"><span class="icon-box">${icon("compass")}</span></span>
      <span class="brand-text">
        <strong>Sổ Tay Lịch Trình</strong>
        <small>VIỆT NAM</small>
      </span>
    </a>
    <nav class="menu">${items}</nav>
    <div id="sidebar-user" class="sidebar-user"></div>`;

    // Khách bấm vào mục cần đăng nhập thì hiện cửa sổ đăng nhập, đăng nhập xong mới chuyển trang
    if (enforced && !me.authenticated) {
        sidebar.querySelectorAll("a[data-login]").forEach((link) => {
            link.addEventListener("click", (e) => {
                e.preventDefault();
                openAuthModal("login", () => {
                    window.location.href = link.getAttribute("href");
                });
            });
        });
    }

    renderUserBox(me);
}
// Chặn trang cần đăng nhập hoặc cần quyền quản trị.
// Trang nào cũng có thể tự khai báo bằng <body data-require="login"> hoặc <body data-require="admin">.
function guardCurrentPage(me) {
    if (!me.authEnabled) return; // chưa bật kiểm tra quyền (đang phát triển) thì không chặn

    const item = MENU.find((m) => m.href === currentPath());
    const need =
        document.body.dataset.require || (item?.adminOnly ? "admin" : item?.loginOnly ? "login" : "");
    if (!need) return;

    if (!me.authenticated) {
        // Đóng cửa sổ mà không đăng nhập thì quay về trang chủ
        openAuthModal("login", undefined, () => window.location.replace("/index.html"));
        return;
    }

    if (need === "admin" && me.user.role !== "ADMIN") {
        const main = document.querySelector(".app-main");
        if (!main) return;
        const back = createEl("a", "user-btn primary", "← Về trang khám phá");
        back.href = "/index.html";
        back.style.display = "inline-block";
        const box = createEl("div", "page");
        box.append(
            createEl("h1", "page-title", "Bạn không có quyền truy cập trang này"),
            createEl("p", "page-desc", "Trang này chỉ dành cho quản trị viên."),
            back
        );
        main.replaceChildren(box);
    }
}

renderSidebar(); // vẽ ngay, lúc này chưa biết ai đăng nhập

getMe().then((me) => {
    renderSidebar(me); // biết rồi thì vẽ lại: có khu vực người dùng, mục dành cho quản trị
    guardCurrentPage(me);
});

// Đăng nhập hoặc đăng ký xong (không tải lại trang) thì vẽ lại sidebar
window.addEventListener("auth-changed", () => getMe().then(renderSidebar));