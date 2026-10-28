package com.travelplanner.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSeeder implements CommandLineRunner {

    private final PlaceRepository placeRepository;
    private final DataSource dataSource;

    public DataSeeder(PlaceRepository placeRepository, DataSource dataSource) {
        this.placeRepository = placeRepository;
        this.dataSource = dataSource;
    }

    // Chạy 1 lần mỗi khi project khởi động xong
    @Override
    public void run(String... args) {
        // Bảng trống thì nạp 30 địa điểm mẫu
        if (placeRepository.count() == 0) {
            runScript("data.sql");
        }
        // Địa điểm nào chưa có tọa độ thì bổ sung (không ghi đè tọa độ đã có)
        if (placeRepository.existsByLatitudeIsNull()) {
            runScript("place-coordinates.sql");
        }
    }

    private void runScript(String fileName) {
        ResourceDatabasePopulator populator =
                new ResourceDatabasePopulator(new ClassPathResource(fileName));
        populator.setSqlScriptEncoding("UTF-8");
        populator.execute(dataSource);
    }
}