-- Tọa độ (vĩ độ, kinh độ) của 30 địa điểm. Chỉ điền cho dòng chưa có tọa độ.
-- Các dòng ghi "kiểm tra lại" là tọa độ gần đúng, hãy đối chiếu trên Google Maps.

-- MIỀN BẮC
UPDATE place SET latitude = 21.0285, longitude = 105.8522 WHERE name = 'Hồ Gươm' AND latitude IS NULL;
UPDATE place SET latitude = 21.0340, longitude = 105.8503 WHERE name = 'Phố Cổ Hà Nội' AND latitude IS NULL;
UPDATE place SET latitude = 20.9101, longitude = 107.1839 WHERE name = 'Vịnh Hạ Long' AND latitude IS NULL; -- kiểm tra lại
UPDATE place SET latitude = 20.2528, longitude = 105.9148 WHERE name = 'Tràng An' AND latitude IS NULL;
UPDATE place SET latitude = 22.3033, longitude = 103.7750 WHERE name = 'Fansipan' AND latitude IS NULL;
UPDATE place SET latitude = 21.0359, longitude = 105.8335 WHERE name = 'Chùa Một Cột' AND latitude IS NULL;
UPDATE place SET latitude = 21.0275, longitude = 105.8355 WHERE name = 'Văn Miếu - Quốc Tử Giám' AND latitude IS NULL;
UPDATE place SET latitude = 21.0408, longitude = 105.7983 WHERE name = 'Bảo tàng Dân tộc học Việt Nam' AND latitude IS NULL;
UPDATE place SET latitude = 22.8545, longitude = 106.7227 WHERE name = 'Thác Bản Giốc' AND latitude IS NULL;
UPDATE place SET latitude = 21.0381, longitude = 105.8494 WHERE name = 'Chợ Đồng Xuân' AND latitude IS NULL;

-- MIỀN TRUNG
UPDATE place SET latitude = 16.4698, longitude = 107.5786 WHERE name = 'Đại Nội Huế' AND latitude IS NULL;
UPDATE place SET latitude = 16.4537, longitude = 107.5449 WHERE name = 'Chùa Thiên Mụ' AND latitude IS NULL;
UPDATE place SET latitude = 15.8801, longitude = 108.3380 WHERE name = 'Phố cổ Hội An' AND latitude IS NULL;
UPDATE place SET latitude = 15.9953, longitude = 107.9884 WHERE name = 'Cầu Vàng' AND latitude IS NULL;
UPDATE place SET latitude = 16.0544, longitude = 108.2478 WHERE name = 'Biển Mỹ Khê' AND latitude IS NULL;
UPDATE place SET latitude = 11.9437, longitude = 108.4420 WHERE name = 'Hồ Xuân Hương' AND latitude IS NULL;
UPDATE place SET latitude = 16.0603, longitude = 108.2233 WHERE name = 'Bảo tàng Điêu khắc Chăm' AND latitude IS NULL;
UPDATE place SET latitude = 11.9013, longitude = 108.4491 WHERE name = 'Thác Datanla' AND latitude IS NULL; -- kiểm tra lại
UPDATE place SET latitude = 15.3833, longitude = 109.1167 WHERE name = 'Đảo Lý Sơn' AND latitude IS NULL;
UPDATE place SET latitude = 15.9977, longitude = 107.9881 WHERE name = 'Bà Nà Hills' AND latitude IS NULL;
UPDATE place SET latitude = 10.9425, longitude = 108.2890 WHERE name = 'Đồi cát Mũi Né' AND latitude IS NULL; -- kiểm tra lại

-- MIỀN NAM
UPDATE place SET latitude = 10.7725, longitude = 106.6980 WHERE name = 'Chợ Bến Thành' AND latitude IS NULL;
UPDATE place SET latitude = 11.1437, longitude = 106.4622 WHERE name = 'Địa đạo Củ Chi' AND latitude IS NULL;
UPDATE place SET latitude = 10.0517, longitude = 105.7499 WHERE name = 'Chợ nổi Cái Răng' AND latitude IS NULL;
UPDATE place SET latitude = 10.0500, longitude = 104.0380 WHERE name = 'Bãi Sao' AND latitude IS NULL; -- kiểm tra lại
UPDATE place SET latitude = 11.3835, longitude = 106.1710 WHERE name = 'Núi Bà Đen' AND latitude IS NULL; -- kiểm tra lại
UPDATE place SET latitude = 10.7795, longitude = 106.6921 WHERE name = 'Bảo tàng Chứng tích Chiến tranh' AND latitude IS NULL;
UPDATE place SET latitude = 8.6830, longitude = 106.6090 WHERE name = 'Côn Đảo' AND latitude IS NULL; -- kiểm tra lại
UPDATE place SET latitude = 10.8647, longitude = 106.8031 WHERE name = 'Suối Tiên' AND latitude IS NULL;
UPDATE place SET latitude = 10.7149, longitude = 105.9875 WHERE name = 'Làng nổi Tân Lập' AND latitude IS NULL; -- kiểm tra lại