// Cùng nguồn với trang web nên chỉ cần đường dẫn tương đối
const BASE_URL = "/api/places";

// Lấy danh sách địa điểm, tiêu chí nào để trống thì bỏ qua
export async function fetchPlaces({ region, city, category, keyword } = {}) {
    const params = new URLSearchParams();
    if (region) params.append("region", region);
    if (city) params.append("city", city);
    if (category) params.append("category", category);
    if (keyword) params.append("keyword", keyword);

    const res = await fetch(`${BASE_URL}?${params}`);
    if (!res.ok) throw new Error("Không tải được danh sách địa điểm");
    return res.json();
}

// Lấy danh sách giá trị cho các ô chọn
export async function fetchFilterOptions() {
    const res = await fetch(`${BASE_URL}/filters`);
    if (!res.ok) throw new Error("Không tải được bộ lọc");
    return res.json();
}

// Nhờ AI giới thiệu địa điểm (refresh = true để tạo lại)
// Nhờ AI giới thiệu địa điểm, mỗi lần gọi là một bản viết mới
export async function fetchAiOverview(id) {
    const res = await fetch(`${BASE_URL}/${id}/ai-overview`, { method: "POST" });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
        const error = new Error(data.message || "Không tạo được giới thiệu bằng AI");
        error.status = res.status; // để biết có phải lỗi 401 (chưa đăng nhập) không
        throw error;
    }
    return data;
}
// Lỗi từ backend: có mã trạng thái và (nếu là lỗi kiểm tra dữ liệu) lỗi theo từng ô
export class ApiError extends Error {
    constructor(message, status, errors) {
        super(message);
        this.status = status;
        this.errors = errors || null;
    }
}

async function sendJson(url, method, body) {
    const res = await fetch(url, {
        method,
        headers: body ? { "Content-Type": "application/json" } : undefined,
        body: body ? JSON.stringify(body) : undefined,
    });
    if (res.status === 204) return null; // xóa thành công, không có nội dung trả về
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new ApiError(data.message || "Có lỗi xảy ra", res.status, data.errors);
    return data;
}

export const createPlace = (place) => sendJson(BASE_URL, "POST", place);
export const updatePlace = (id, place) => sendJson(`${BASE_URL}/${id}`, "PUT", place);
export const deletePlace = (id) => sendJson(`${BASE_URL}/${id}`, "DELETE");