-- Giờ mở cửa / đóng cửa của các địa điểm (dữ liệu tham khảo, gần đúng).
-- Địa điểm không có dòng nào ở đây = không giới hạn giờ (mở cả ngày).
-- Hãy đối chiếu lại với thông tin chính thức của từng nơi trước khi demo.

-- MIỀN BẮC
UPDATE place SET open_time = '06:00:00', close_time = '18:00:00' WHERE name = 'Vịnh Hạ Long' AND open_time IS NULL;
UPDATE place SET open_time = '07:00:00', close_time = '16:00:00' WHERE name = 'Tràng An' AND open_time IS NULL;
UPDATE place SET open_time = '07:30:00', close_time = '17:30:00' WHERE name = 'Fansipan' AND open_time IS NULL;
UPDATE place SET open_time = '07:00:00', close_time = '18:00:00' WHERE name = 'Chùa Một Cột' AND open_time IS NULL;
UPDATE place SET open_time = '08:00:00', close_time = '17:00:00' WHERE name = 'Văn Miếu - Quốc Tử Giám' AND open_time IS NULL;
UPDATE place SET open_time = '08:30:00', close_time = '17:30:00' WHERE name = 'Bảo tàng Dân tộc học Việt Nam' AND open_time IS NULL;
UPDATE place SET open_time = '07:00:00', close_time = '17:00:00' WHERE name = 'Thác Bản Giốc' AND open_time IS NULL;
UPDATE place SET open_time = '06:00:00', close_time = '18:00:00' WHERE name = 'Chợ Đồng Xuân' AND open_time IS NULL;

-- MIỀN TRUNG
UPDATE place SET open_time = '07:30:00', close_time = '17:30:00' WHERE name = 'Đại Nội Huế' AND open_time IS NULL;
UPDATE place SET open_time = '07:00:00', close_time = '17:30:00' WHERE name = 'Chùa Thiên Mụ' AND open_time IS NULL;
UPDATE place SET open_time = '08:00:00', close_time = '17:00:00' WHERE name = 'Cầu Vàng' AND open_time IS NULL;
UPDATE place SET open_time = '08:00:00', close_time = '17:00:00' WHERE name = 'Bà Nà Hills' AND open_time IS NULL;
UPDATE place SET open_time = '07:00:00', close_time = '17:00:00' WHERE name = 'Bảo tàng Điêu khắc Chăm' AND open_time IS NULL;
UPDATE place SET open_time = '07:30:00', close_time = '17:00:00' WHERE name = 'Thác Datanla' AND open_time IS NULL;

-- MIỀN NAM
UPDATE place SET open_time = '06:00:00', close_time = '19:00:00' WHERE name = 'Chợ Bến Thành' AND open_time IS NULL;
UPDATE place SET open_time = '07:30:00', close_time = '17:00:00' WHERE name = 'Địa đạo Củ Chi' AND open_time IS NULL;
UPDATE place SET open_time = '05:00:00', close_time = '09:00:00' WHERE name = 'Chợ nổi Cái Răng' AND open_time IS NULL;
UPDATE place SET open_time = '06:00:00', close_time = '20:00:00' WHERE name = 'Núi Bà Đen' AND open_time IS NULL;
UPDATE place SET open_time = '07:30:00', close_time = '17:30:00' WHERE name = 'Bảo tàng Chứng tích Chiến tranh' AND open_time IS NULL;
UPDATE place SET open_time = '08:00:00', close_time = '17:30:00' WHERE name = 'Suối Tiên' AND open_time IS NULL;
UPDATE place SET open_time = '07:00:00', close_time = '17:00:00' WHERE name = 'Làng nổi Tân Lập' AND open_time IS NULL;
