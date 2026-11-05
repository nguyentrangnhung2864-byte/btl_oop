package com.travelplanner.demo.service;

import com.travelplanner.demo.model.Place;
import com.travelplanner.demo.model.ScheduleItem;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleValidationServiceTest {

    // Các hàm kiểm tra không dùng repository nên truyền null cho đơn giản
    private final ScheduleValidationService service = new ScheduleValidationService(null);

    private ScheduleItem item(long id, String start, double hours) {
        ScheduleItem item = new ScheduleItem();
        item.setId(id);
        item.setStartTime(LocalTime.parse(start));
        item.setDurationHours(hours);
        return item;
    }

    @Test
    void khongCoMucNaoThiKhongTrung() {
        assertTrue(service.findConflictIds(List.of()).isEmpty());
    }

    @Test
    void motMucDuyNhatKhongTrung() {
        assertTrue(service.findConflictIds(List.of(item(1, "08:00", 2))).isEmpty());
    }

    @Test
    void haiMucChongGioThiCaHaiBiDanhDau() {
        // 08:00-10:00 và 09:00-10:30
        Set<Long> result = service.findConflictIds(List.of(item(1, "08:00", 2), item(2, "09:00", 1.5)));
        assertEquals(Set.of(1L, 2L), result);
    }

    @Test
    void haiMucNoiTiepNhauKhongBiTinhLaTrung() {
        // 08:00-10:00 và 10:00-11:00: biên chạm nhau
        assertTrue(service.findConflictIds(List.of(item(1, "08:00", 2), item(2, "10:00", 1))).isEmpty());
    }

    @Test
    void haiMucCachNhauKhongTrung() {
        assertTrue(service.findConflictIds(List.of(item(1, "08:00", 1), item(2, "13:00", 1))).isEmpty());
    }

    @Test
    void mucNamTronTrongMucKhacThiTrung() {
        // 08:00-12:00 chứa 09:00-10:00
        Set<Long> result = service.findConflictIds(List.of(item(1, "08:00", 4), item(2, "09:00", 1)));
        assertEquals(Set.of(1L, 2L), result);
    }

    @Test
    void haiMucCungGioBatDauThiTrung() {
        Set<Long> result = service.findConflictIds(List.of(item(1, "09:00", 1), item(2, "09:00", 2)));
        assertEquals(Set.of(1L, 2L), result);
    }

    @Test
    void chiDanhDauDungNhungMucBiTrung() {
        // Mục 1 và 2 trùng nhau, mục 3 độc lập
        Set<Long> result = service.findConflictIds(List.of(
                item(1, "08:00", 2), item(2, "09:30", 1), item(3, "14:00", 1)));
        assertEquals(Set.of(1L, 2L), result);
        assertFalse(result.contains(3L));
    }

    @Test
    void thoiLuongLeNuaGioDuocTinhDung() {
        // 08:00-09:30 và 09:15-10:00 chồng nhau 15 phút
        assertEquals(Set.of(1L, 2L),
                service.findConflictIds(List.of(item(1, "08:00", 1.5), item(2, "09:15", 0.75))));
        // 08:00-09:30 và 09:30-10:00 chỉ chạm biên
        assertTrue(service.findConflictIds(List.of(item(1, "08:00", 1.5), item(2, "09:30", 0.5))).isEmpty());
    }

    @Test
    void mucThieuGioBatDauBiBoQua() {
        ScheduleItem noTime = new ScheduleItem();
        noTime.setId(9L);
        noTime.setDurationHours(2);
        assertFalse(service.isOverlapping(noTime, item(1, "08:00", 2)));
    }

    // ---------- Giờ mở cửa ----------

    private ScheduleItem itemAt(String start, double hours, String open, String close) {
        ScheduleItem item = item(1, start, hours);
        Place place = new Place();
        if (open != null) {
            place.setOpenTime(LocalTime.parse(open));
            place.setCloseTime(LocalTime.parse(close));
        }
        item.setPlace(place);
        return item;
    }

    @Test
    void trongGioMoCuaThiHopLe() {
        assertFalse(service.isOutsideOpeningHours(itemAt("08:00", 2, "07:00", "17:00")));
    }

    @Test
    void ketThucDungGioDongCuaVanHopLe() {
        assertFalse(service.isOutsideOpeningHours(itemAt("15:00", 2, "07:00", "17:00")));
    }

    @Test
    void denSomHonGioMoCuaThiCanhBao() {
        assertTrue(service.isOutsideOpeningHours(itemAt("06:00", 1, "07:00", "17:00")));
    }

    @Test
    void ketThucMuonHonGioDongCuaThiCanhBao() {
        assertTrue(service.isOutsideOpeningHours(itemAt("16:00", 2, "07:00", "17:00")));
    }

    @Test
    void diaDiemKhongCoGioThiKhongBaoLoi() {
        assertFalse(service.isOutsideOpeningHours(itemAt("03:00", 2, null, null)));
    }

    // ---------- Phương tiện ----------

    private ScheduleItem withTransport(String start, double hours, String transport) {
        ScheduleItem item = item(1, start, hours);
        item.setTransport(transport);
        return item;
    }

    @Test
    void khongKipDiChuyenThiCanhBao() {
        // Ô tô 30 km cần khoảng 59 phút nhưng chỉ có 10 phút
        String warning = service.checkTravel(withTransport("08:00", 1, "CAR"), item(2, "09:10", 1), 30.0);
        assertNotNull(warning);
    }

    @Test
    void duThoiGianDiChuyenThiKhongCanhBao() {
        assertNull(service.checkTravel(withTransport("08:00", 1, "CAR"), item(2, "11:00", 1), 30.0));
    }

    @Test
    void mayBayChoQuangDuongNganThiCanhBao() {
        assertNotNull(service.checkTravel(withTransport("08:00", 1, "PLANE"), item(2, "15:00", 1), 40.0));
    }

    @Test
    void xeDapChoQuangDuongXaThiCanhBao() {
        assertNotNull(service.checkTravel(withTransport("08:00", 1, "BICYCLE"), item(2, "20:00", 1), 80.0));
    }

    @Test
    void mayBayDuongDaiNhungGapNganThiCanhBao() {
        // 1100 km: 2 giờ thủ tục + ~1 giờ 35 phút bay, nhưng chỉ có 1 giờ 30 phút
        assertNotNull(service.checkTravel(withTransport("08:00", 1, "PLANE"), item(2, "10:30", 1), 1100.0));
    }

    @Test
    void chuaChonPhuongTienHoacThieuKhoangCachThiBoQua() {
        assertNull(service.checkTravel(withTransport("08:00", 1, null), item(2, "09:01", 1), 300.0));
        assertNull(service.checkTravel(withTransport("08:00", 1, "CAR"), item(2, "09:01", 1), null));
    }

    @Test
    void haiMucDaTrungGioThiKhongBaoThemLoiPhuongTien() {
        assertNull(service.checkTravel(withTransport("08:00", 2, "CAR"), item(2, "09:00", 1), 30.0));
    }
}
