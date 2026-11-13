package com.insurancecard.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancecard.common.ApiException;
import com.insurancecard.model.dto.ChatDtos.ChatRequest;
import com.insurancecard.model.dto.ChatDtos.ChatResponse;
import com.insurancecard.service.ChatbotService;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotServiceImpl.class);

    private static final String SYSTEM_PROMPT = """
            Bạn là Trợ lý ảo AI thông minh của Hệ thống Quản lý Bảo hiểm Xe máy điện tử (Insurance Card).
            Hãy trả lời người dùng một cách lịch sự, thân thiện, ngắn gọn, súc tích bằng tiếng Việt.
            
            Quy định nghiệp vụ cốt lõi của hệ thống:
            1. Các gói bảo hiểm:
               - Xe mô tô 2 bánh dưới 50cc: 60.000 VNĐ/năm.
               - Xe mô tô 2 bánh trên 50cc: 66.000 VNĐ/năm.
               - Xe máy điện: 60.000 VNĐ/năm.
               - Thời hạn bảo hiểm: Từ 1 đến 3 năm (phí = phí/năm x số năm).
            2. Gia hạn hợp đồng:
               - Được phép gia hạn trong vòng 30 ngày trước khi hợp đồng hết hạn.
            3. Huỷ hợp đồng:
               - Nếu hợp đồng chưa từng phát sinh yêu cầu bồi thường: Hoàn lại 80% phí của thời gian còn lại.
               - Nếu đã phát sinh bồi thường: Huỷ không được hoàn phí.
               - Hợp đồng không thể xoá, chỉ có trạng thái Huỷ (CANCELLED).
            4. Khai báo tai nạn:
               - Phải khai báo trong vòng 365 ngày kể từ ngày xảy ra tai nạn.
               - Yêu cầu chụp ảnh hiện trường, mô tả chi tiết sự cố trên hệ thống.
            5. Yêu cầu bồi thường:
               - Chỉ được tạo yêu cầu khi vụ tai nạn đã được nhân viên thẩm định xác minh (VERIFIED).
               - Mức bồi thường tối đa không vượt quá hạn mức chi trả của gói bảo hiểm.
               - Chi trả qua chuyển khoản ngân hàng hoặc nhận tiền mặt tại quầy.
            6. Vi phạm & Nộp phạt:
               - Biên bản vi phạm quy định hợp đồng có hạn nộp 30 ngày kể từ ngày lập.
               - Khách hàng có thể nộp phạt trực tuyến hoặc tại quầy nhân viên.
            7. Giấy chứng nhận:
               - Thẻ bảo hiểm điện tử có mã QR để lực lượng chức năng hoặc CSGT kiểm tra trực tuyến.
            
            Nếu người dùng hỏi ngoài phạm vi bảo hiểm xe máy, hãy trả lời ngắn gọn theo sự hiểu biết tổng quát của AI một cách hữu ích hoặc định hướng họ về bảo hiểm.
            """;

    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.gemini.model:gemini-3.5-flash}")
    private String geminiModel;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public ChatbotServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        String userMsg = request.message().trim();

        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw ApiException.business("GEMINI_KEY_MISSING", "Chưa cấu hình GEMINI_API_KEY!");
        }

        try {
            String reply = callGeminiApi(userMsg);
            if (reply == null || reply.isBlank()) {
                throw ApiException.business("GEMINI_EMPTY", "Gemini AI không trả về nội dung.");
            }
            return new ChatResponse(reply, "GEMINI_AI");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
            throw ApiException.business("GEMINI_ERROR", "Lỗi từ Gemini AI: " + e.getMessage());
        }
    }

    private String callGeminiApi(String userMsg) throws Exception {
        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                geminiModel, geminiApiKey);

        String promptContent = SYSTEM_PROMPT + "\n\nCâu hỏi của khách hàng: " + userMsg;

        Map<String, Object> payload = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", promptContent)
                        ))
                )
        );

        String requestBody = objectMapper.writeValueAsString(payload);

        String jsonResponse = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        return extractGeminiText(jsonResponse);
    }

    private String extractGeminiText(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
        }
        return null;
    }
}
