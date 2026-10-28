package com.travelplanner.demo;

import org.springframework.stereotype.Service;

@Service
public class AiOverviewService {

    private final PlaceService placeService;
    private final GeminiClient geminiClient;

    public AiOverviewService(PlaceService placeService, GeminiClient geminiClient) {
        this.placeService = placeService;
        this.geminiClient = geminiClient;
    }

    // Mỗi lần gọi đều nhờ AI viết mới, không lưu lại
    public AiOverviewResponse getAiOverview(Long id) {
        Place place = placeService.getById(id); // không có id thì tự báo 404
        String overview = geminiClient.ask(buildPrompt(place));
        return new AiOverviewResponse(overview);
    }

    private String buildPrompt(Place place) {
        String teaser = place.getTeaser() == null ? "(không có)" : place.getTeaser();
        return """
        Bạn là một hướng dẫn viên du lịch Việt Nam chuyên nghiệp,
        có kiến thức về văn hóa, ẩm thực, lịch sử và các điểm đến.

        Hãy giới thiệu địa điểm du lịch sau:
        - Tên địa điểm: %s
        - Tỉnh/Thành phố: %s
        - Khu vực: %s
        - Loại hình du lịch: %s
        - Mô tả có sẵn: %s

        YÊU CẦU NỘI DUNG:
        1. Giới thiệu nét đặc trưng nổi bật nhất của địa điểm,
           giải thích điều gì khiến nơi này khác biệt và thu hút du khách.

        2. Nêu những cảnh quan, công trình, giá trị văn hóa,
           lịch sử hoặc điểm tham quan tiêu biểu nếu phù hợp.

        3. Gợi ý 2-3 hoạt động hoặc trải nghiệm thú vị mà
           du khách có thể thực hiện khi đến đây.

        4. Giới thiệu 1-3 món ăn đặc sản tiêu biểu của địa phương
           nếu có thông tin đáng tin cậy và phù hợp.

        5. Có thể bổ sung một lời khuyên hữu ích cho du khách
           như thời điểm tham quan thích hợp hoặc điều cần lưu ý,
           nhưng chỉ khi có đủ thông tin chắc chắn.

        QUY TẮC QUAN TRỌNG:
        - Nội dung phải cụ thể cho chính địa điểm được cung cấp,
          không viết giới thiệu chung chung áp dụng cho mọi nơi.
        - Phân biệt đặc trưng của địa điểm với đặc sản của
          tỉnh/thành phố nơi địa điểm tọa lạc.
        - Không tự bịa tên món ăn, hoạt động, địa danh,
          sự kiện hoặc thông tin lịch sử.
        - Không bịa giá vé, giờ mở cửa, khoảng cách hay số liệu.
        - Nếu không chắc chắn về một thông tin, hãy bỏ qua.
        - Không bắt buộc phải có đủ tất cả các mục nếu
          địa điểm không phù hợp.
        - Giọng văn tự nhiên, hấp dẫn, dễ hiểu, thân thiện.
        - Độ dài khoảng 150-200 từ.
        - Viết thành 2-3 đoạn văn mạch lạc.
        - Chỉ trả về nội dung giới thiệu bằng tiếng Việt.
        - Không sử dụng tiêu đề, dấu đầu dòng hoặc Markdown.
        """.formatted(place.getName(), place.getCity(), place.getRegion(),
                place.getCategory(), teaser);
    }
}