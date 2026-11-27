// auth.js: gọi các API đăng nhập, dùng chung cho mọi trang

// Lỗi từ backend: có mã trạng thái và (nếu là lỗi kiểm tra dữ liệu) lỗi theo từng ô
export class AuthError extends Error {
    constructor(message, status, errors) {
        super(message);
        this.status = status;
        this.errors = errors || null;
    }
}

async function request(url, method, body) {
    const res = await fetch(url, {
        method,
        headers: body ? { "Content-Type": "application/json" } : undefined,
        body: body ? JSON.stringify(body) : undefined,
    });
    if (res.status === 204) return null;
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new AuthError(data.message || "Có lỗi xảy ra", res.status, data.errors);
    return data;
}

const GUEST = { authenticated: false, user: null, authEnabled: false };
let mePromise = null;

// Người đang đăng nhập: { authenticated, user }.
// Gọi nhiều lần trong cùng một trang thì chỉ hỏi máy chủ một lần.
export function getMe() {
    if (!mePromise) {
        mePromise = request("/api/auth/me", "GET").catch(() => GUEST);
    }
    return mePromise;
}

export async function login(email, password) {
    const user = await request("/api/auth/login", "POST", { email, password });
    mePromise = null;
    return user;
}

export async function register(email, displayName, password) {
    const user = await request("/api/auth/register", "POST", { email, displayName, password });
    mePromise = null;
    return user;
}

export async function logout() {
    await request("/api/auth/logout", "POST");
    mePromise = null;
}