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
export async function fetchAiOverview(id, refresh = false) {
    const res = await fetch(`${BASE_URL}/${id}/ai-overview?refresh=${refresh}`, {
        method: "POST",
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.message || "Không tạo được giới thiệu bằng AI");
    return data;
}