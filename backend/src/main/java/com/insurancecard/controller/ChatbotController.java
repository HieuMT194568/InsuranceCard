package com.insurancecard.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurancecard.model.dto.ChatDtos.ChatRequest;
import com.insurancecard.model.dto.ChatDtos.ChatResponse;
import com.insurancecard.service.ChatbotService;

import jakarta.validation.Valid;

/**
 * API Chatbot AI phục vụ tư vấn bảo hiểm cho mọi đối tượng người dùng (công khai).
 */
@RestController
@RequestMapping("/api/public/chat")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatbotService.chat(request);
    }
}
