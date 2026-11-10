package com.insurancecard.service;

import com.insurancecard.model.dto.AuthDtos.ActionResponse;
import com.insurancecard.model.dto.AuthDtos.AuthResponse;
import com.insurancecard.model.dto.AuthDtos.ChangePasswordRequest;
import com.insurancecard.model.dto.AuthDtos.LoginRequest;
import com.insurancecard.model.dto.AuthDtos.RegisterRequest;
import com.insurancecard.model.dto.AuthDtos.ResetPasswordRequest;
import com.insurancecard.model.entity.User;

public interface AuthService {

    ActionResponse register(RegisterRequest r);

    ActionResponse verifyEmail(String token);

    /** Luôn trả cùng một thông báo để không lộ email nào đã đăng ký. */
    ActionResponse resendVerification(String rawEmail);

    AuthResponse login(LoginRequest r);

    AuthResponse buildAuthResponse(User user);

    ActionResponse forgotPassword(String rawEmail);

    ActionResponse resetPassword(ResetPasswordRequest r);

    /** Đổi mật khẩu: các phiên đăng nhập cũ bị vô hiệu, trả về token mới cho phiên hiện tại. */
    AuthResponse changePassword(Long userId, ChangePasswordRequest r);

    void applyNewPassword(User user, String rawPassword);
}
