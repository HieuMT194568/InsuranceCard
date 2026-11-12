package com.insurancecard.service.impl;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            
            Nếu người dùng hỏi ngoài phạm vi bảo hiểm xe máy, hãy lịch sự từ chối và hướng dẫn họ về các chủ đề bảo hiểm xe máy.
            """;

    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.gemini.model:gemini-1.5-flash}")
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

        // 1. Thử gọi Google Gemini API nếu có API Key
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String reply = callGeminiApi(userMsg);
                if (reply != null && !reply.isBlank()) {
                    return new ChatResponse(reply, "GEMINI_AI");
                }
            } catch (Exception e) {
                log.warn("Gemini API call failed, falling back to Knowledge Base: {}", e.getMessage());
            }
        }

        // 2. Chế độ dự phòng (Knowledge-base) đảm bảo hệ thống luôn trả lời thông minh
        String fallbackReply = generateKnowledgeBaseReply(userMsg);
        return new ChatResponse(fallbackReply, "KNOWLEDGE_BASE");
    }

    private String callGeminiApi(String userMsg) {
        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                geminiModel, geminiApiKey);

        String promptContent = SYSTEM_PROMPT + "\n\nCâu hỏi của khách hàng: " + userMsg;

        String requestBody = """
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": %s }
                      ]
                    }
                  ]
                }
                """.formatted(objectMapper.valueToTree(promptContent).toString());

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

    private String generateKnowledgeBaseReply(String msg) {
        String lower = msg.toLowerCase(Locale.ROOT);

        if (lower.contains("chào") || lower.contains("hi") || lower.contains("hello") || lower.contains("bạn là ai")) {
            return "Xin chào! Tôi là Trợ lý AI của hệ thống Bảo hiểm Xe máy Insurance Card. Tôi có thể hỗ trợ bạn tìm hiểu về các gói bảo hiểm, quy trình khai báo tai nạn, thủ tục bồi thường hoặc các chính sách hoàn phí. Bạn cần hỗ trợ gì hôm nay?";
        }
        if (lower.contains("giá") || lower.contains("phí") || lower.contains("bao nhiêu") || lower.contains("gói")) {
            return "Hiện tại hệ thống cung cấp 3 nhóm gói bảo hiểm xe máy điện tử:\n" +
                   "• Xe mô tô dưới 50cc: 60.000 đ/năm\n" +
                   "• Xe mô tô trên 50cc: 66.000 đ/năm\n" +
                   "• Xe máy điện: 60.000 đ/năm\n" +
                   "Thời hạn linh hoạt từ 1 đến 3 năm (Phí = Phí/năm x Số năm). Hợp đồng mới được cấp mã QR và thẻ bảo hiểm điện tử xem trực tiếp trên ứng dụng.";
        }
        if (lower.contains("tai nạn") || lower.contains("ngã xe") || lower.contains("va chạm") || lower.contains("sự cố")) {
            return "Khi xảy ra sự cố tai nạn, bạn cần lưu ý:\n" +
                   "1. Giữ an toàn và chụp ảnh hiện trường, giấy tờ liên quan.\n" +
                   "2. Đăng nhập hệ thống và vào mục 'Khai báo tai nạn' trong vòng 365 ngày kể từ ngày xảy ra sự cố.\n" +
                   "3. Nhân viên bảo hiểm sẽ tiến hành thẩm định và xác minh (VERIFIED) để bạn có thể gửi yêu cầu bồi thường.";
        }
        if (lower.contains("bồi thường") || lower.contains("nhận tiền") || lower.contains("chi trả") || lower.contains("hạn mức")) {
            return "Quy trình xử lý bồi thường bảo hiểm:\n" +
                   "1. Vụ tai nạn của bạn phải được nhân viên xác minh hợp lệ (VERIFIED).\n" +
                   "2. Gửi yêu cầu bồi thường với số tiền ước tính và bằng chứng thiệt hại.\n" +
                   "3. Sau khi được duyệt (APPROVED), công ty sẽ chi trả tiền qua chuyển khoản hoặc tiền mặt tại quầy và ghi nhận giao dịch vào lịch sử thanh toán.";
        }
        if (lower.contains("gia hạn")) {
            return "Hợp đồng bảo hiểm có thể được gia hạn khi thời hạn còn lại từ 30 ngày trở xuống. Khi gia hạn, hợp đồng mới sẽ tự động nối tiếp ngày hết hạn cũ giúp bảo hiểm của bạn không bị gián đoạn.";
        }
        if (lower.contains("huỷ") || lower.contains("hủy") || lower.contains("hoàn tiền") || lower.contains("hoàn phí")) {
            return "Chính sách huỷ hợp đồng:\n" +
                   "• Hợp đồng đang hiệu lực và chưa từng phát sinh bồi thường: Bạn được hoàn phí 80% tính theo số ngày còn lại của hợp đồng.\n" +
                   "• Hợp đồng đã có chi trả bồi thường: Sẽ huỷ nhưng không được hoàn phí.\n" +
                   "Lưu ý: Hợp đồng là chứng từ pháp lý nên hệ thống không cho phép xoá, chỉ chuyển trạng thái sang Đã huỷ (CANCELLED).";
        }
        if (lower.contains("phạt") || lower.contains("vi phạm") || lower.contains("biên bản")) {
            return "Nếu có biên bản vi phạm phát sinh, khách hàng có 30 ngày để nộp phạt. Bạn có thể thanh toán trực tuyến qua mục 'Vi phạm & Phạt' trên hệ thống hoặc nộp trực tiếp tại quầy làm việc.";
        }
        if (lower.contains("gara") || lower.contains("sửa xe") || lower.contains("bảo dưỡng")) {
            return "Hệ thống có liên kết với mạng lưới Gara đối tác trên toàn quốc. Bạn có thể tra cứu danh sách Gara liên kết tại trang 'Gara đối tác' để chọn nơi sửa chữa và thẩm định xe gần nhất.";
        }

        return "Cảm ơn câu hỏi của bạn. Về vấn đề này, bạn có thể tham khảo mục Các gói bảo hiểm, Khai báo tai nạn hoặc liên hệ trực tiếp nhân viên tại quầy hỗ trợ để được hướng dẫn chi tiết nhất.";
    }
}
