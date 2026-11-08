// authModal.js: cửa sổ đăng nhập / đăng ký hiện ngay trên trang đang xem.
// Trang nào cũng dùng được: import { openAuthModal } from "/js/authModal.js"

import { login, register, AuthError } from "./auth.js";

// Nội dung cố định do nhóm viết (không chứa dữ liệu của người dùng)
const HTML = `
<div id="auth-modal" class="auth-backdrop" hidden>
  <div class="auth-dialog" role="dialog" aria-modal="true" aria-label="Đăng nhập hoặc đăng ký">
    <button id="am-close" class="auth-close" type="button" aria-label="Đóng">×</button>

    <div class="auth-tabs">
      <button id="am-tab-login" class="auth-tab active" type="button">Đăng nhập</button>
      <button id="am-tab-register" class="auth-tab" type="button">Đăng ký</button>
    </div>

    <form id="am-login-form" novalidate>
      <h2>Chào mừng trở lại</h2>
      <p id="am-login-error" class="auth-error" hidden></p>
      <div class="auth-field">
        <label for="am-l-email">Email</label>
        <input id="am-l-email" type="email" autocomplete="username">
      </div>
      <div class="auth-field">
        <label for="am-l-password">Mật khẩu</label>
        <input id="am-l-password" type="password" autocomplete="current-password">
      </div>
      <button class="auth-submit" type="submit">Đăng nhập</button>
    </form>

    <form id="am-register-form" novalidate hidden>
      <h2>Tạo tài khoản</h2>
      <p id="am-register-error" class="auth-error" hidden></p>
      <div class="auth-field">
        <label for="am-r-name">Tên hiển thị</label>
        <input id="am-r-name" type="text" maxlength="100" autocomplete="name">
        <span class="field-error" data-for="displayName"></span>
      </div>
      <div class="auth-field">
        <label for="am-r-email">Email</label>
        <input id="am-r-email" type="email" autocomplete="username">
        <span class="field-error" data-for="email"></span>
      </div>
      <div class="auth-field">
        <label for="am-r-password">Mật khẩu (từ 6 ký tự)</label>
        <input id="am-r-password" type="password" autocomplete="new-password">
        <span class="field-error" data-for="password"></span>
      </div>
      <div class="auth-field">
        <label for="am-r-confirm">Nhập lại mật khẩu</label>
        <input id="am-r-confirm" type="password" autocomplete="new-password">
        <span class="field-error" data-for="confirm"></span>
      </div>
      <button class="auth-submit" type="submit">Tạo tài khoản</button>
    </form>
  </div>
</div>`;

const $ = (id) => document.getElementById(id);

let built = false;
let onSuccess = () => window.location.reload(); // mặc định: tải lại trang cho mọi thứ cập nhật

// ---- Thông báo lỗi ----
function clearErrors() {
    $("am-login-error").hidden = true;
    $("am-register-error").hidden = true;
    document.querySelectorAll("#auth-modal .field-error").forEach((span) => (span.textContent = ""));
}

function showError(id, message) {
    const target = $(id);
    target.textContent = message;
    target.hidden = false;
}

function setFieldError(field, message) {
    const span = document.querySelector(`#am-register-form .field-error[data-for="${field}"]`);
    if (span) span.textContent = message;
}

// Lỗi kiểm tra dữ liệu có nhiều ô: lấy câu đầu tiên, ngược lại lấy câu chung
function firstMessage(err) {
    return err.errors ? Object.values(err.errors)[0] : err.message;
}

// ---- Chuyển giữa đăng nhập và đăng ký ----
function showMode(mode) {
    const isLogin = mode !== "register";
    $("am-login-form").hidden = !isLogin;
    $("am-register-form").hidden = isLogin;
    $("am-tab-login").classList.toggle("active", isLogin);
    $("am-tab-register").classList.toggle("active", !isLogin);
    clearErrors();
    $(isLogin ? "am-l-email" : "am-r-name").focus();
}

let onCancel = null; // chạy khi người dùng đóng cửa sổ mà chưa đăng nhập

function close(success = false) {
    $("auth-modal").hidden = true;
    document.body.style.overflow = "";
    if (!success && onCancel) onCancel();
}

// ---- Xử lý gửi biểu mẫu ----
async function handleLogin(e) {
    e.preventDefault();
    clearErrors();
    const button = e.target.querySelector(".auth-submit");
    button.disabled = true;
    try {
        await login($("am-l-email").value, $("am-l-password").value);
        close(true);
        window.dispatchEvent(new Event("auth-changed")); // báo cho sidebar vẽ lại
        onSuccess();
    } catch (err) {
        showError("am-login-error", err instanceof AuthError ? firstMessage(err) : "Không kết nối được máy chủ");
    } finally {
        button.disabled = false;
    }
}

async function handleRegister(e) {
    e.preventDefault();
    clearErrors();

    const password = $("am-r-password").value;
    if (password !== $("am-r-confirm").value) {
        setFieldError("confirm", "Mật khẩu nhập lại không khớp");
        return;
    }

    const button = e.target.querySelector(".auth-submit");
    button.disabled = true;
    try {
        await register($("am-r-email").value, $("am-r-name").value, password);
        close(true);
        window.dispatchEvent(new Event("auth-changed")); // báo cho sidebar vẽ lại
        onSuccess();
    } catch (err) {
        if (err instanceof AuthError) {
            if (err.errors) {
                Object.entries(err.errors).forEach(([field, message]) => setFieldError(field, message));
            }
            showError("am-register-error", err.message); // ví dụ: "Email này đã được đăng ký"
        } else {
            showError("am-register-error", "Không kết nối được máy chủ");
        }
    } finally {
        button.disabled = false;
    }
}

// Tạo cửa sổ vào trang một lần duy nhất, ở lần mở đầu tiên
function build() {
    if (built) return;
    document.body.insertAdjacentHTML("beforeend", HTML);

    $("am-close").addEventListener("click", () => close());
    $("auth-modal").addEventListener("click", (e) => {
        if (e.target === $("auth-modal")) close(); // chỉ đóng khi bấm vào nền tối
    });
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && !$("auth-modal").hidden) close();
    });
    $("am-tab-login").addEventListener("click", () => showMode("login"));
    $("am-tab-register").addEventListener("click", () => showMode("register"));
    $("am-login-form").addEventListener("submit", handleLogin);
    $("am-register-form").addEventListener("submit", handleRegister);
    built = true;
}

// mode: "login" hoặc "register".
// afterSuccess: chạy sau khi đăng nhập hoặc đăng ký thành công (không truyền thì tải lại trang).
// afterCancel: chạy khi người dùng đóng cửa sổ mà chưa đăng nhập (không bắt buộc).
export function openAuthModal(mode = "login", afterSuccess, afterCancel) {
    build();
    onSuccess = afterSuccess || (() => window.location.reload());
    onCancel = afterCancel || null;
    $("am-login-form").reset();
    $("am-register-form").reset();
    $("auth-modal").hidden = false;
    document.body.style.overflow = "hidden"; // khóa cuộn trang phía sau
    showMode(mode);
}