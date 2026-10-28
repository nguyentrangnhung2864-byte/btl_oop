package com.travelplanner.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(@Value("${gemini.api-key:}") String apiKey,
                        @Value("${gemini.model}") String model) {
        // Chờ kết nối tối đa 5 giây, chờ AI trả lời tối đa 60 giây
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(60));

        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .requestFactory(factory)
                .build();
        this.apiKey = apiKey;
        this.model = model;
    }

    // Gửi một câu hỏi, nhận về câu trả lời dạng chữ
    public String ask(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Chưa cấu hình khóa API cho AI");
        }

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

        ResponseEntity<byte[]> response;
        try {
            response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(byte[].class); // đọc thô, không phụ thuộc vào kiểu dữ liệu phản hồi
        } catch (RestClientResponseException e) {
            // Google trả mã lỗi (400, 403, 404, 429...): chi tiết chỉ ghi vào log của server
            log.error("Gemini trả lỗi {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 429) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "AI đang quá tải hoặc đã hết lượt dùng, vui lòng thử lại sau");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Không gọi được dịch vụ AI, vui lòng thử lại sau");
        } catch (RestClientException e) {
            log.error("Không kết nối được Gemini: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Không gọi được dịch vụ AI, vui lòng thử lại sau");
        }

        byte[] bytes = response.getBody();
        String json = bytes == null ? "" : new String(bytes, StandardCharsets.UTF_8);

        String result = extractText(json);
        if (result.isEmpty()) {
            // Ghi lại phản hồi thật để biết chuyện gì xảy ra (khóa API không nằm trong phản hồi)
            log.error("Gemini trả về dữ liệu không có nội dung. Mã {}, Content-Type {}, nội dung: {}",
                    response.getStatusCode().value(),
                    response.getHeaders().getContentType(),
                    shorten(json));
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI không trả về nội dung");
        }
        return result;
    }

    // Đọc JSON thô, lấy chữ trong candidates[0].content.parts[].text
    private String extractText(String json) {
        if (json == null || json.isBlank()) {
            return "";
        }
        try {
            Map<String, Object> root = JsonParserFactory.getJsonParser().parseMap(json);
            StringBuilder text = new StringBuilder();
            if (root.get("candidates") instanceof List<?> candidates && !candidates.isEmpty()
                    && candidates.get(0) instanceof Map<?, ?> candidate
                    && candidate.get("content") instanceof Map<?, ?> content
                    && content.get("parts") instanceof List<?> parts) {
                for (Object item : parts) {
                    if (item instanceof Map<?, ?> part && part.get("text") instanceof String t) {
                        text.append(t);
                    }
                }
            }
            return text.toString().trim();
        } catch (RuntimeException e) {
            log.error("Không đọc được JSON từ Gemini: {}", e.getMessage());
            return "";
        }
    }

    private String shorten(String s) {
        if (s == null || s.isBlank()) {
            return "(rỗng)";
        }
        return s.length() > 500 ? s.substring(0, 500) + "..." : s;
    }
}