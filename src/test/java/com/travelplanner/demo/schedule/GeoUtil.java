package com.travelplanner.demo.schedule;

final class GeoUtil {
    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtil() {
    }

    /** Khoảng cách đường chim bay (km, làm tròn 1 chữ số). Thiếu tọa độ thì trả null. */
    static Double distanceKm(Number lat1, Number lon1, Number lat2, Number lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return null;
        double a1 = lat1.doubleValue(), o1 = lon1.doubleValue();
        double a2 = lat2.doubleValue(), o2 = lon2.doubleValue();
        if ((a1 == 0 && o1 == 0) || (a2 == 0 && o2 == 0)) return null;   // chưa có tọa độ
        double dLat = Math.toRadians(a2 - a1);
        double dLon = Math.toRadians(o2 - o1);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a1)) * Math.cos(Math.toRadians(a2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double km = EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return Math.round(km * 10) / 10.0;
    }
}