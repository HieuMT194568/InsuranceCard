package com.insurancecard.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ChatDtos {
    private ChatDtos() {}

    public record ChatRequest(
            @NotBlank(message = "Nội dung câu hỏi không được để trống")
            @Size(max = 1000, message = "Câu hỏi tối đa 1000 ký tự")
            String message
    ) {}

    public record ChatResponse(
            String reply,
            String source
    ) {}
}
