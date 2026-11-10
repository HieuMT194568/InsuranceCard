# Phần 1 – Từ use case tới sequence diagram (nhóm xác thực / phân quyền)

Mô hình nằm trong project Visual Paradigm `docs/visual-paradigm/InsuranceCard-Phan1-UC-Sequence-v2.vpp`
(hoặc import `docs/visual-paradigm/phan1-vai-tro.xml` vào project mới). Nội dung sinh từ
`seq_spec.py` (bản chi tiết) và `seq_spec_short.py` (bản rút gọn) bằng `python gen_vp.py`.

| Diagram trong VP | Nội dung |
|---|---|
| Use Case Diagram - Phan 1 Xac thuc | Bước 1 – UC model; mô tả từng UC nằm ở **Documentation** của use case |
| SD UC-01 Dang ky | Đăng ký tài khoản |
| SD UC-02 Xac thuc email | Xác thực email + gửi lại link |
| SD UC-03 Dang nhap | Đăng nhập (khoá tạm sau 5 lần sai) |
| SD UC-04 Quen va dat lai mat khau | Quên mật khẩu + đặt lại mật khẩu |
| SD UC-05 Doi mat khau | Đổi mật khẩu, kèm bước JwtAuthenticationFilter kiểm tra JWT |
| SD UC-0x … (rut gon) | Bản rút gọn của 5 sơ đồ trên (6–7 lifeline, 1 fragment alt chính) – dùng để trình bày |
| Class Diagram - Phan 1 … | Class diagram (lifeline của sequence tham chiếu tới các class này) |

---

## Bước 1 – Develop UC model

Actor: **Khách vãng lai**, **Người dùng đã đăng nhập** (Khách hàng / Nhân viên), **Hệ thống Email** (actor phụ).

| UC | Actor | Quan hệ |
|---|---|---|
| UC-01 Đăng ký tài khoản | Khách vãng lai | «include» Gửi email |
| UC-02 Xác thực email | Khách vãng lai | được «extend» bởi Gửi lại link xác thực |
| UC-03 Đăng nhập | Khách vãng lai | |
| UC-04 Quên & đặt lại mật khẩu | Khách vãng lai | «include» Gửi email |
| UC-05 Đổi mật khẩu | Người dùng đã đăng nhập | |
| Gửi email | Hệ thống Email | |

### UC-03 Đăng nhập (ví dụ đầy đủ; các UC khác xem Documentation trong VP)

**Luồng chính**
1. Khách nhập email, mật khẩu, bấm Đăng nhập.
2. Hệ thống tìm tài khoản theo email (không phân biệt hoa thường).
3. Hệ thống kiểm tra tài khoản không bị tạm khoá, mật khẩu đúng (BCrypt), trạng thái ACTIVE.
4. Hệ thống đặt lại số lần sai, sinh JWT (HS256, 8 giờ, claim `role`).
5. Hệ thống trả `AuthResponse`; giao diện lưu phiên và chuyển tới `/customer` hoặc `/staff` theo vai trò.

**Luồng thay thế**
- 2a. Không có tài khoản → 401 `INVALID_CREDENTIALS`.
- 3a. Đang tạm khoá → 403 `TEMPORARILY_LOCKED`.
- 3b. Sai mật khẩu → tăng `failedLoginCount`; đủ 5 lần → `lockoutUntil = now + 15 phút`; 401.
- 3c. Tài khoản PENDING → 403 `EMAIL_NOT_VERIFIED` (hiện nút gửi lại link).
- 3d. Tài khoản LOCKED → 403 `ACCOUNT_LOCKED`.

---

## Bước 2 – Xác định đối tượng

### 2a. Boundary objects

| Đối tượng | Lớp | Giao tiếp với |
|---|---|---|
| loginPage, registerPage, verifyPage, forgotPage, resetPage, profilePage | Trang React `Login`, `Register`, `VerifyEmail`, `ForgotPassword`, `ResetPassword`, `Profile` (`frontend/src/pages`) | Actor người dùng |
| : MailService | `MailService` | Actor Hệ thống Email (proxy SMTP) |

### 2b. Internal software objects

| Loại | Đối tượng |
|---|---|
| «control» – điều phối, nghiệp vụ | `AuthController`, `MeController`, `AuthService`, `JwtService`, `JwtAuthenticationFilter`, `CurrentUser`, `SettingService` |
| «entity» – dữ liệu | `User`, `Customer`, `VerificationToken` và các repository `UserRepository`, `CustomerRepository`, `VerificationTokenRepository` |
| Tiện ích (thư viện) | `PasswordEncoder` (BCrypt) |

Trong VP các lifeline hiển thị biểu tượng boundary / control / entity theo stereotype.

---

## Bước 3 – Trình tự thông điệp (luồng chính UC-03)

```
1      Khách -> loginPage           : nhập email, mật khẩu; bấm Đăng nhập
1.1    loginPage -> loginPage       : validate form (zod)
1.2    loginPage -> AuthController  : POST /api/auth/login (LoginRequest)
1.2.1  AuthController -> AuthService: login(r)
1.2.1.1  AuthService -> UserRepository : findByEmailIgnoreCase(email)  --> Optional<User>
1.2.1.2  AuthService -> PasswordEncoder: matches(password, passwordHash) --> ok
1.2.1.5  AuthService -> user        : setFailedLoginCount(0), setLockoutUntil(null)
1.2.1.6  AuthService -> AuthService : buildAuthResponse(user)
1.2.1.7  AuthService -> JwtService  : generate(user)                   --> accessToken
1.2.1.8  AuthService -> CustomerRepository : findByUserId(userId)      --> Customer   [role = CUSTOMER]
         AuthService --> AuthController : AuthResponse
         AuthController --> loginPage   : 200 AuthResponse
1.3    loginPage -> loginPage       : saveSession(auth)
       loginPage --> Khách          : chuyển tới /customer hoặc /staff
```

Số thứ tự lồng nhau (1.2.1.x) thể hiện thông điệp được gửi trong lúc đối tượng đang xử lý thông điệp cha.

---

## Bước 4 – Luồng thay thế (combined fragment)

| UC | Fragment |
|---|---|
| UC-01 | `alt` @Valid sai định dạng (400) / mật khẩu nhập lại không khớp (400) / trùng email-SĐT-CCCD (409) / thành công; `opt` gửi SMTP khi `app.mail.enabled = true` |
| UC-02 | `alt` token không tồn tại (400) / đã dùng (`alt` lồng: đã ACTIVE → 200, chưa → 422) / hết hạn (422 → gửi lại link) / không PENDING (422) / thành công |
| UC-03 | `alt` không có tài khoản / tạm khoá / `alt` lồng: sai mật khẩu (`opt` khoá 15 phút) / PENDING / LOCKED / thành công (`opt` role = CUSTOMER) |
| UC-04 | `opt` chỉ tạo link khi user ACTIVE (luôn trả cùng thông báo); `alt` token không tồn tại / đã dùng / hết hạn / user không ACTIVE / thành công |
| UC-05 | `alt` JWT không hợp lệ (401) / hợp lệ; `alt` lồng: sai mật khẩu hiện tại / nhập lại không khớp / trùng mật khẩu cũ / thành công (token mới) |
