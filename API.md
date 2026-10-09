# API Module 1: Quản lý địa điểm

Người phụ trách: Nhung

## 1. Thông tin chung

| Mục | Giá trị |
|---|---|
| Địa chỉ gốc | `http://localhost:8080` |
| Định dạng dữ liệu | JSON (UTF-8) |
| Xác thực | Không có (chưa làm đăng nhập) |
| CORS | Cho phép `http://localhost:3000` và `http://localhost:5173` |

## 2. Mô hình dữ liệu `Place`

| Trường | Kiểu | Ý nghĩa | Ví dụ |
|---|---|---|---|
| `id` | số | Mã địa điểm, tự tăng | `1` |
| `name` | chuỗi | Tên địa điểm (duy nhất) | `"Hồ Gươm"` |
| `city` | chuỗi | Tỉnh/Thành phố | `"Hà Nội"` |
| `region` | chuỗi | Khu vực: `Miền Bắc`, `Miền Trung` hoặc `Miền Nam` | `"Miền Bắc"` |
| `category` | chuỗi | Loại hình (8 loại, xem mục 4) | `"Thiên nhiên"` |
| `teaser` | chuỗi | Mô tả ngắn | `"Trái tim của thủ đô..."` |

---

## 3. Danh sách endpoint

### 3.1. Lấy danh sách / tìm kiếm / lọc địa điểm

`GET /api/places`

**Tham số query** (tất cả đều tùy chọn, không truyền hoặc để trống = không lọc theo tiêu chí đó):

| Tham số | Kiểu | Ý nghĩa |
|---|---|---|
| `region` | chuỗi | Lọc đúng theo khu vực |
| `city` | chuỗi | Lọc đúng theo tỉnh/thành phố |
| `category` | chuỗi | Lọc đúng theo loại hình |
| `keyword` | chuỗi | Tìm theo một phần tên địa điểm, không phân biệt hoa thường và dấu |

**Quy tắc:**
- Nhiều tham số cùng lúc được kết hợp theo kiểu **VÀ** (phải thỏa tất cả).
- Kết quả sắp xếp theo tên.
- Không có địa điểm nào phù hợp thì trả về mảng rỗng `[]`, không phải lỗi.

**Ví dụ 1: lấy tất cả**

```
GET /api/places
```

Phản hồi `200 OK` (rút gọn, thực tế trả về đủ 30 địa điểm):

```json
[
  {
    "id": 1,
    "name": "Hồ Gươm",
    "city": "Hà Nội",
    "region": "Miền Bắc",
    "category": "Thiên nhiên",
    "teaser": "Trái tim của thủ đô, nơi Tháp Rùa soi bóng giữa hồ và gắn liền với nhiều câu chuyện lịch sử."
  },
  {
    "id": 6,
    "name": "Chùa Một Cột",
    "city": "Hà Nội",
    "region": "Miền Bắc",
    "category": "Chùa & đền",
    "teaser": "Ngôi chùa có kiến trúc độc đáo được xây dựng trên một cột đá giữa hồ sen."
  }
]
```

**Ví dụ 2: lọc theo khu vực và loại hình**

```
GET /api/places?region=Miền Trung&category=Thiên nhiên
```

Phản hồi `200 OK`: 3 địa điểm (Hồ Xuân Hương, Thác Datanla, Đồi cát Mũi Né).

**Ví dụ 3: loại hình có dấu `&` (phải mã hóa `&` thành `%26`)**

```
GET /api/places?category=Biển %26 đảo
```

**Ví dụ 4: tìm theo tên**

```
GET /api/places?keyword=chùa
```

Phản hồi `200 OK`: 2 địa điểm (Chùa Một Cột, Chùa Thiên Mụ).

**Ví dụ 5: không có kết quả**

```
GET /api/places?region=Miền Tây
```

Phản hồi `200 OK`:

```json
[]
```

---

### 3.2. Lấy danh sách giá trị cho các ô chọn

`GET /api/places/filters`

Không có tham số. Dùng để đổ dữ liệu vào các ô chọn khu vực, tỉnh/thành, loại hình, không cần gõ cứng ở frontend.

Phản hồi `200 OK` (danh sách `cities` rút gọn, thực tế đủ 17 giá trị):

```json
{
  "regions": ["Miền Bắc", "Miền Nam", "Miền Trung"],
  "cities": ["Bà Rịa - Vũng Tàu", "Bình Thuận", "Cần Thơ", "Đà Nẵng", "Hà Nội"],
  "categories": [
    "Bảo tàng",
    "Biển & đảo",
    "Chợ & mua sắm",
    "Chùa & đền",
    "Phố cổ & làng nghề",
    "Thiên nhiên",
    "Văn hóa - lịch sử",
    "Vui chơi & giải trí"
  ]
}
```

Lưu ý: các danh sách được sắp xếp theo thứ tự chữ cái, không theo địa lý (ví dụ `Miền Nam` đứng trước `Miền Trung`).

---

### 3.3. Xem chi tiết một địa điểm

`GET /api/places/{id}`

| Tham số đường dẫn | Kiểu | Ý nghĩa |
|---|---|---|
| `id` | số | Mã địa điểm |

**Ví dụ:**

```
GET /api/places/1
```

Phản hồi `200 OK`:

```json
{
  "id": 1,
  "name": "Hồ Gươm",
  "city": "Hà Nội",
  "region": "Miền Bắc",
  "category": "Thiên nhiên",
  "teaser": "Trái tim của thủ đô, nơi Tháp Rùa soi bóng giữa hồ và gắn liền với nhiều câu chuyện lịch sử."
}
```

**Khi không tìm thấy:**

```
GET /api/places/999
```

Phản hồi `404 Not Found`:

```json
{
  "timestamp": "2026-10-09T15:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/places/999",
}
```

---

## 4. Danh sách 8 loại hình hiện có

Thiên nhiên · Biển & đảo · Chùa & đền · Văn hóa - lịch sử · Bảo tàng · Chợ & mua sắm · Vui chơi & giải trí · Phố cổ & làng nghề

## 5. Mã trạng thái

| Mã | Ý nghĩa |
|---|---|
| `200` | Thành công |
| `404` | Không tìm thấy địa điểm theo `id` |

## 6. Lưu ý cho Module 2 và Module 3 (B, C)

- **Đổ ô chọn địa điểm (Module 2):** gọi `GET /api/places`, hiển thị `name` (kèm `city`), nhưng giá trị lưu và gửi đi là `id`.
- **Không gán cứng `id` trong code.** `id` do database tự sinh nên có thể khác nhau giữa máy mỗi người. Luôn lấy `id` từ kết quả API.
- **Mã hóa tham số trên URL.** Giá trị có dấu `&` (như `Biển & đảo`) phải mã hóa, nếu không server hiểu sai. Trong JavaScript dùng `URLSearchParams` sẽ tự mã hóa:

```javascript
const params = new URLSearchParams();
params.append("region", "Miền Trung");
params.append("category", "Biển & đảo");

const res = await fetch(`http://localhost:8080/api/places?${params}`);
const places = await res.json();
```

- Với tham số không chọn, **bỏ hẳn tham số đó** hoặc truyền chuỗi rỗng đều được.

## 7. Dự kiến (chưa làm, có thể thay đổi)

| Chức năng | Endpoint dự kiến | Tuần |
|---|---|---|
| Thêm, sửa, xóa địa điểm | `POST /api/places`, `PUT /api/places/{id}`, `DELETE /api/places/{id}` | 4 |
| Gợi ý địa điểm có chấm điểm | `POST /api/places/recommendations` | 5 |
| AI giới thiệu địa điểm | `POST /api/places/{id}/ai-overview` | 6 |