package com.travelplanner.demo.service;

import com.travelplanner.demo.model.ScheduleItem;
import com.travelplanner.demo.repository.ScheduleItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Module 3: kiểm tra lịch trình trong một ngày.
 *
 * 1. Trùng giờ: hai khung giờ [startA, endA) và [startB, endB) trùng khi
 *        startA < endB  VÀ  startB < endA
 *    Hai mục nối tiếp nhau (mục này kết thúc đúng lúc mục kia bắt đầu) KHÔNG bị tính là trùng.
 * 2. Giờ mở cửa: khung giờ tham quan phải nằm trọn trong [giờ mở cửa, giờ đóng cửa] của địa điểm.
 * 3. Phương tiện: phương tiện phải hợp lý với quãng đường và kịp thời gian di chuyển
 *    giữa hai địa điểm liên tiếp.
 */
@Service
public class ScheduleValidationService {

    private final ScheduleItemRepository itemRepository;

    public ScheduleValidationService(ScheduleItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /** Lấy mục của một ngày từ database rồi trả về id các mục bị trùng giờ. */
    @Transactional(readOnly = true)
    public Set<Long> findConflictIdsByDay(Long dayId) {
        return findConflictIds(itemRepository.findByDay_IdOrderByStartTimeAsc(dayId));
    }

    /** Trả về id của tất cả mục có khung giờ chồng lên một mục khác trong danh sách. */
    public Set<Long> findConflictIds(List<ScheduleItem> items) {
        Set<Long> conflicts = new HashSet<>();
        for (int a = 0; a < items.size(); a++) {
            for (int b = a + 1; b < items.size(); b++) {
                ScheduleItem first = items.get(a);
                ScheduleItem second = items.get(b);
                if (isOverlapping(first, second)) {
                    conflicts.add(first.getId());
                    conflicts.add(second.getId());
                }
            }
        }
        return conflicts;
    }

    /** Hai mục có chồng khung giờ lên nhau không. */
    public boolean isOverlapping(ScheduleItem first, ScheduleItem second) {
        if (first.getStartTime() == null || second.getStartTime() == null) {
            return false;
        }
        int startA = toMinutes(first);
        int endA = startA + durationMinutes(first);
        int startB = toMinutes(second);
        int endB = startB + durationMinutes(second);
        return startA < endB && startB < endA;
    }

    // ---------------- Giờ mở cửa ----------------

    /** Khung giờ tham quan có nằm ngoài giờ mở cửa của địa điểm không (địa điểm không có giờ thì luôn hợp lệ). */
    public boolean isOutsideOpeningHours(ScheduleItem item) {
        if (item.getStartTime() == null || item.getPlace() == null) {
            return false;
        }
        LocalTime open = item.getPlace().getOpenTime();
        LocalTime close = item.getPlace().getCloseTime();
        if (open == null || close == null) {
            return false;
        }
        int start = toMinutes(item);
        int end = start + durationMinutes(item);
        int openMin = open.getHour() * 60 + open.getMinute();
        int closeMin = close.getHour() * 60 + close.getMinute();
        return start < openMin || end > closeMin;
    }

    // ---------------- Phương tiện ----------------

    // Tốc độ trung bình thực tế (km/h) của từng phương tiện
    private static final Map<String, Double> SPEED_KMH = Map.of(
            "BICYCLE", 12.0,
            "MOTORBIKE", 35.0,
            "CAR", 40.0,
            "BUS", 30.0,
            "TRAIN", 55.0,
            "BOAT", 20.0,
            "PLANE", 700.0);

    private static final Map<String, String> TRANSPORT_NAME = Map.of(
            "BICYCLE", "xe đạp",
            "MOTORBIKE", "xe máy",
            "CAR", "ô tô",
            "BUS", "xe buýt",
            "TRAIN", "tàu hỏa",
            "BOAT", "thuyền",
            "PLANE", "máy bay");

    private static final double ROAD_FACTOR = 1.3;          // đường thực tế dài hơn đường chim bay
    private static final int PLANE_OVERHEAD_MINUTES = 120;  // thủ tục sân bay, chờ đợi
    private static final double PLANE_MIN_KM = 100;         // gần hơn mức này không nên đi máy bay
    private static final double BICYCLE_MAX_KM = 50;        // xa hơn mức này không nên đi xe đạp

    /**
     * Kiểm tra phương tiện đã chọn từ địa điểm trước sang địa điểm sau.
     * Trả về câu cảnh báo, hoặc null nếu hợp lý / chưa đủ dữ liệu để kiểm tra.
     */
    public String checkTravel(ScheduleItem previous, ScheduleItem next, Double distanceKm) {
        if (previous == null || next == null || distanceKm == null) {
            return null;
        }
        String transport = previous.getTransport();
        if (transport == null || !SPEED_KMH.containsKey(transport)) {
            return null;
        }
        String name = TRANSPORT_NAME.get(transport);

        if ("PLANE".equals(transport) && distanceKm < PLANE_MIN_KM) {
            return "Đi " + name + " không hợp lý cho quãng đường chỉ ≈ " + formatKm(distanceKm) + " km.";
        }
        if ("BICYCLE".equals(transport) && distanceKm > BICYCLE_MAX_KM) {
            return "Đi " + name + " quá xa cho quãng đường ≈ " + formatKm(distanceKm) + " km.";
        }

        int gap = gapMinutes(previous, next);
        if (gap < 0) {
            return null; // hai mục đã chồng giờ, đã được báo "Trùng giờ"
        }
        int needed = travelMinutes(transport, distanceKm);
        if (needed > gap) {
            return "Đi " + name + " (≈ " + formatKm(distanceKm) + " km) cần khoảng "
                    + formatDuration(needed) + ", nhưng chỉ có " + formatDuration(gap)
                    + " giữa hai địa điểm.";
        }
        return null;
    }

    /** Thời gian di chuyển ước tính (phút, làm tròn lên). */
    public int travelMinutes(String transport, double distanceKm) {
        double speed = SPEED_KMH.get(transport);
        if ("PLANE".equals(transport)) {
            return PLANE_OVERHEAD_MINUTES + (int) Math.ceil(distanceKm / speed * 60);
        }
        return (int) Math.ceil(distanceKm * ROAD_FACTOR / speed * 60);
    }

    /** Khoảng trống (phút) từ lúc mục trước kết thúc đến lúc mục sau bắt đầu; âm nghĩa là chồng giờ. */
    private int gapMinutes(ScheduleItem previous, ScheduleItem next) {
        int previousEnd = toMinutes(previous) + durationMinutes(previous);
        return toMinutes(next) - previousEnd;
    }

    private String formatKm(double km) {
        return km == Math.floor(km) ? String.valueOf((long) km) : String.valueOf(km);
    }

    private String formatDuration(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        if (h == 0) return m + " phút";
        if (m == 0) return h + " giờ";
        return h + " giờ " + m + " phút";
    }

    private int toMinutes(ScheduleItem item) {
        return item.getStartTime().getHour() * 60 + item.getStartTime().getMinute();
    }

    private int durationMinutes(ScheduleItem item) {
        return (int) Math.round(item.getDurationHours() * 60);
    }
}
