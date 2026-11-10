package com.insurancecard.service;

import java.util.List;

import com.insurancecard.model.dto.SettingDtos.SettingResponse;

public interface SettingService {

    String RENEWAL_WINDOW_DAYS = "RENEWAL_WINDOW_DAYS";
    String MAX_START_DATE_ADVANCE_DAYS = "MAX_START_DATE_ADVANCE_DAYS";
    String CLAIM_DEADLINE_DAYS = "CLAIM_DEADLINE_DAYS";
    String CANCEL_REFUND_PERCENT = "CANCEL_REFUND_PERCENT";
    String PENALTY_DUE_DAYS = "PENALTY_DUE_DAYS";
    String VERIFY_TOKEN_EXPIRY_HOURS = "VERIFY_TOKEN_EXPIRY_HOURS";
    String RESET_TOKEN_EXPIRY_MINUTES = "RESET_TOKEN_EXPIRY_MINUTES";

    int getInt(String key);

    List<SettingResponse> list();

    SettingResponse get(String key);

    SettingResponse update(String key, Integer value);
}
