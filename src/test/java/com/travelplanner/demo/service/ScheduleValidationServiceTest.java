package com.travelplanner.demo.service;

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
}
