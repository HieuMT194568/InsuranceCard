package com.insurancecard.service;

import com.insurancecard.model.dto.ChatDtos.ChatRequest;
import com.insurancecard.model.dto.ChatDtos.ChatResponse;

public interface ChatbotService {
    ChatResponse chat(ChatRequest request);
}
