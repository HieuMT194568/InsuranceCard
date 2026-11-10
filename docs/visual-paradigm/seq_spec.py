"""Phần 1 – UC model + sequence diagram cho nhóm use case xác thực / phân quyền.

Bước 1  Develop UC model         -> ACTORS, USE_CASES (mô tả: luồng chính + luồng thay thế), UC_LINKS
Bước 2a Boundary objects         -> lifeline «boundary» (trang React, MailService – proxy tới hệ thống Email)
Bước 2b Internal software objects-> lifeline «control» (controller, service, filter) và «entity» (entity, repository)
Bước 3  Message sequence         -> C / R / N / S trong SEQUENCES
Bước 4  Alternative sequences    -> ALT / OPT
"""
from gen_seq_dsl import ALT, C, N, OPT, R, S

# ====================================================================== 1. UC MODEL
ACTORS = {
    "guest": dict(name="Khách vãng lai", doc="Người chưa đăng nhập."),
    "user": dict(name="Người dùng đã đăng nhập", doc="Khách hàng (role CUSTOMER) hoặc Nhân viên (role STAFF) có JWT hợp lệ – xem use case diagram Phần 1 (Vai trò)."),
    "mail": dict(name="Hệ thống Email", doc="Máy chủ SMTP (app.mail.enabled = true); chế độ demo chỉ ghi log."),
}

USE_CASES = {
    "UC01": dict(name="UC-01 Đăng ký tài khoản", doc="""Tác nhân: Khách vãng lai. Tiền điều kiện: chưa đăng nhập.
Luồng chính:
1. Khách mở trang Đăng ký, nhập họ tên, email, SĐT, mật khẩu, nhập lại mật khẩu, CCCD, ngày sinh, giới tính, địa chỉ.
2. Hệ thống kiểm tra định dạng từng trường (frontend zod + @Valid RegisterRequest).
3. Hệ thống kiểm tra mật khẩu nhập lại khớp và email / SĐT / CCCD chưa tồn tại.
4. Hệ thống tạo User (role CUSTOMER, status PENDING, mật khẩu băm BCrypt) và hồ sơ Customer.
5. Hệ thống tạo token VERIFY_EMAIL (hạn VERIFY_TOKEN_EXPIRY_HOURS = 24 giờ) và gửi link xác thực qua email.
6. Hệ thống báo đăng ký thành công, yêu cầu kiểm tra email.
Luồng thay thế:
2a. Sai định dạng / thiếu trường: 400 VALIDATION_ERROR, báo lỗi từng trường.
3a. Mật khẩu nhập lại không khớp: 400 (confirmPassword).
3b. Email / SĐT / CCCD đã tồn tại: 409 DUPLICATE, báo tất cả trường trùng cùng lúc.
5a. Chế độ demo (app.mail.enabled = false): không gửi email thật, trả devLink."""),
    "UC02": dict(name="UC-02 Xác thực email", doc="""Tác nhân: Khách vãng lai. Tiền điều kiện: đã đăng ký, có link xác thực.
Luồng chính:
1. Khách bấm link /verify-email?token=...
2. Hệ thống tìm token loại VERIFY_EMAIL.
3. Hệ thống kiểm tra token chưa dùng, còn hạn và tài khoản đang PENDING.
4. Hệ thống chuyển tài khoản sang ACTIVE, đánh dấu token đã dùng.
5. Hệ thống báo kích hoạt thành công; khách có thể đăng nhập.
Luồng thay thế:
2a. Token không tồn tại: 400 INVALID_TOKEN.
3a. Token đã dùng và tài khoản đã ACTIVE: 200 "đã kích hoạt trước đó".
3b. Token đã dùng, tài khoản chưa ACTIVE: 422 TOKEN_USED.
3c. Token hết hạn: 422 TOKEN_EXPIRED -> khách nhập email để gửi lại link (UC Gửi lại link xác thực).
3d. Tài khoản không ở trạng thái PENDING: 422 INVALID_STATUS."""),
    "UC02b": dict(name="Gửi lại link xác thực", doc="""Mở rộng UC-02 (và màn hình Đăng nhập khi gặp EMAIL_NOT_VERIFIED).
Hệ thống luôn trả cùng một thông báo (không lộ email); nếu email tồn tại và đang PENDING thì vô hiệu link cũ, tạo token mới và gửi email."""),
    "UC03": dict(name="UC-03 Đăng nhập", doc="""Tác nhân: Khách vãng lai. Tiền điều kiện: có tài khoản.
Luồng chính:
1. Khách nhập email, mật khẩu, bấm Đăng nhập.
2. Hệ thống tìm tài khoản theo email (không phân biệt hoa thường).
3. Hệ thống kiểm tra tài khoản không bị tạm khoá, mật khẩu đúng (BCrypt), trạng thái ACTIVE.
4. Hệ thống đặt lại số lần sai, sinh JWT (HS256, 8 giờ, claim role).
5. Hệ thống trả AuthResponse (token + UserInfo); giao diện lưu phiên và chuyển tới trang theo vai trò (/customer hoặc /staff).
Luồng thay thế:
1a. Bỏ trống email / mật khẩu: 400.
2a. Không có tài khoản: 401 INVALID_CREDENTIALS (thông báo chung).
3a. Đang bị tạm khoá (lockoutUntil > now): 403 TEMPORARILY_LOCKED.
3b. Sai mật khẩu: tăng failedLoginCount; đủ 5 lần -> lockoutUntil = now + 15 phút; 401 INVALID_CREDENTIALS.
3c. Tài khoản PENDING: 403 EMAIL_NOT_VERIFIED, hiện nút Gửi lại link xác thực.
3d. Tài khoản LOCKED: 403 ACCOUNT_LOCKED."""),
    "UC04": dict(name="UC-04 Quên & đặt lại mật khẩu", doc="""Tác nhân: Khách vãng lai, Hệ thống Email.
Luồng chính:
1. Khách nhập email ở trang Quên mật khẩu.
2. Nếu email thuộc tài khoản ACTIVE: hệ thống vô hiệu link cũ, tạo token RESET_PASSWORD (hạn RESET_TOKEN_EXPIRY_MINUTES = 30 phút), gửi email.
3. Hệ thống luôn trả cùng một thông báo.
4. Khách bấm link /reset-password?token=..., nhập mật khẩu mới + nhập lại.
5. Hệ thống kiểm tra token, đổi mật khẩu (BCrypt), ghi passwordChangedAt (mọi JWT cũ hết hiệu lực), đánh dấu token đã dùng.
6. Hệ thống báo thành công; khách đăng nhập lại bằng mật khẩu mới.
Luồng thay thế:
2a. Email không tồn tại / không ACTIVE: không tạo link, vẫn trả cùng thông báo.
4a. Mật khẩu mới yếu / nhập lại không khớp: 400.
5a. Token không tồn tại: 400 INVALID_TOKEN; đã dùng: 422 TOKEN_USED; hết hạn: 422 TOKEN_EXPIRED.
5b. Tài khoản không ACTIVE: 422 ACCOUNT_LOCKED."""),
    "UC05": dict(name="UC-05 Đổi mật khẩu", doc="""Tác nhân: Người dùng đã đăng nhập (Khách hàng / Nhân viên).
Luồng chính:
1. Người dùng mở Hồ sơ, nhập mật khẩu hiện tại, mật khẩu mới, nhập lại.
2. JwtAuthenticationFilter xác thực JWT (chữ ký, hạn, tài khoản ACTIVE, cấp sau lần đổi mật khẩu gần nhất).
3. Hệ thống kiểm tra mật khẩu hiện tại đúng, mật khẩu mới khớp và khác mật khẩu cũ.
4. Hệ thống đổi mật khẩu, ghi passwordChangedAt -> mọi token cũ bị vô hiệu.
5. Hệ thống trả token mới cho phiên hiện tại; giao diện lưu lại phiên.
Luồng thay thế:
2a. Token sai / hết hạn / bị vô hiệu: 401 UNAUTHORIZED, chuyển về trang đăng nhập.
3a. Sai mật khẩu hiện tại: 400 currentPassword.
3b. Nhập lại không khớp: 400 confirmPassword.
3c. Mật khẩu mới trùng mật khẩu cũ: 400 newPassword."""),
    "MAIL": dict(name="Gửi email", doc="MailService gửi email qua SMTP; chế độ demo chỉ ghi log và trả devLink."),
}

# (kind, from, to): assoc actor–UC, gen (con, cha), include (base, included), extend (extension, base)
UC_LINKS = [
    ("assoc", "guest", "UC01"),
    ("assoc", "guest", "UC02"),
    ("assoc", "guest", "UC03"),
    ("assoc", "guest", "UC04"),
    ("assoc", "user", "UC05"),
    ("include", "UC01", "MAIL"),
    ("include", "UC04", "MAIL"),
    ("include", "UC02b", "MAIL"),
    ("extend", "UC02b", "UC02"),
    ("assoc", "MAIL", "mail"),
]

UC_LAYOUT = dict(
    name="Use Case Diagram - Phan 1 Xac thuc",
    system_name="Insurance Card – Xác thực & phân quyền",
    system=(240, 30, 640, 560),
    pos={
        "guest": (100, 220), "user": (100, 470),
        "mail": (970, 300),
        "UC01": (300, 80), "UC02": (300, 190), "UC03": (300, 300), "UC04": (300, 400), "UC05": (300, 500),
        "UC02b": (600, 60), "MAIL": (640, 300),
    },
    size={"UC04": (190, 60), "UC02b": (180, 60), "MAIL": (150, 50)},
)

# ====================================================================== 2–4. SEQUENCE
# lifeline: (key, class trong class_spec | None = actor, tên instance, stereotype)
G = "guest"

SEQUENCES = [
    # ------------------------------------------------------------------ UC-03 Đăng nhập
    dict(
        name="SD UC-03 Dang nhap",
        lifelines=[
            (G, None, "Khách vãng lai", None),
            ("page", "Login", "loginPage", "boundary"),
            ("ctrl", "AuthController", "", "control"),
            ("svc", "AuthService", "", "control"),
            ("urepo", "UserRepository", "", "entity"),
            ("user", "User", "user", "entity"),
            ("enc", "PasswordEncoder", "", None),
            ("jwt", "JwtService", "", "control"),
            ("crepo", "CustomerRepository", "", "entity"),
        ],
        steps=[
            C(G, "page", "nhập email, mật khẩu; bấm Đăng nhập"),
            S("page", "validate form (zod)"),
            C("page", "ctrl", "POST /api/auth/login (LoginRequest)"),
            C("ctrl", "svc", "login(r)"),
            C("svc", "urepo", "findByEmailIgnoreCase(email)"),
            R("urepo", "svc", "Optional<User>"),
            ALT(
                ("không có tài khoản", [R("svc", "ctrl", "ApiException 401 INVALID_CREDENTIALS")]),
                ("lockoutUntil > now", [R("svc", "ctrl", "ApiException 403 TEMPORARILY_LOCKED")]),
                ("else", [
                    C("svc", "enc", "matches(password, passwordHash)"),
                    R("enc", "svc", "ok : boolean"),
                    ALT(
                        ("ok = false", [
                            C("svc", "user", "setFailedLoginCount(failed + 1)"),
                            OPT("failed >= 5", C("svc", "user", "setLockoutUntil(now + 15 phút)")),
                            R("svc", "ctrl", "ApiException 401 INVALID_CREDENTIALS"),
                        ]),
                        ("status = PENDING", [R("svc", "ctrl", "ApiException 403 EMAIL_NOT_VERIFIED")]),
                        ("status = LOCKED", [R("svc", "ctrl", "ApiException 403 ACCOUNT_LOCKED")]),
                        ("else", [
                            C("svc", "user", "setFailedLoginCount(0), setLockoutUntil(null)"),
                            S("svc", "buildAuthResponse(user)"),
                            C("svc", "jwt", "generate(user)"),
                            R("jwt", "svc", "accessToken (JWT 8 giờ, claim role)"),
                            OPT("role = CUSTOMER",
                                C("svc", "crepo", "findByUserId(userId)"),
                                R("crepo", "svc", "Customer")),
                            R("svc", "ctrl", "AuthResponse(token, UserInfo)"),
                        ]),
                    ),
                ]),
            ),
            R("ctrl", "page", "200 AuthResponse | 401 / 403 ErrorResponse"),
            ALT(
                ("200 OK", [
                    S("page", "saveSession(auth): lưu token, user vào localStorage"),
                    R("page", G, "chuyển tới /customer hoặc /staff theo role"),
                ]),
                ("code = EMAIL_NOT_VERIFIED", [R("page", G, "báo lỗi + nút Gửi lại link xác thực")]),
                ("else", [R("page", G, "hiển thị thông báo lỗi")]),
            ),
        ],
    ),
    # ------------------------------------------------------------------ UC-01 Đăng ký
    dict(
        name="SD UC-01 Dang ky",
        lifelines=[
            (G, None, "Khách vãng lai", None),
            ("page", "Register", "registerPage", "boundary"),
            ("ctrl", "AuthController", "", "control"),
            ("svc", "AuthService", "", "control"),
            ("urepo", "UserRepository", "", "entity"),
            ("crepo", "CustomerRepository", "", "entity"),
            ("enc", "PasswordEncoder", "", None),
            ("user", "User", "user", "entity"),
            ("cus", "Customer", "customer", "entity"),
            ("set", "SettingService", "", "control"),
            ("trepo", "VerificationTokenRepository", "", "entity"),
            ("tok", "VerificationToken", "token", "entity"),
            ("mailsvc", "MailService", "", "boundary"),
            ("mail", None, "Hệ thống Email", None),
        ],
        steps=[
            C(G, "page", "nhập thông tin; bấm Đăng ký"),
            S("page", "validate form (zod)"),
            C("page", "ctrl", "POST /api/auth/register (RegisterRequest)"),
            ALT(
                ("@Valid RegisterRequest sai định dạng", [R("ctrl", "page", "400 VALIDATION_ERROR (fieldErrors)")]),
                ("else", [
                    C("ctrl", "svc", "register(r)"),
                    ALT(
                        ("password ≠ confirmPassword", [R("svc", "ctrl", "ApiException 400 confirmPassword")]),
                        ("else", [
                            C("svc", "urepo", "existsByEmailIgnoreCase(email), existsByPhone(phone)"),
                            R("urepo", "svc", "boolean"),
                            C("svc", "crepo", "existsByIdNumber(idNumber)"),
                            R("crepo", "svc", "boolean"),
                            ALT(
                                ("có email / SĐT / CCCD trùng", [R("svc", "ctrl", "ApiException 409 DUPLICATE (mọi trường trùng)")]),
                                ("else", [
                                    C("svc", "enc", "encode(password)"),
                                    R("enc", "svc", "passwordHash"),
                                    N("svc", "user", "create(role = CUSTOMER, status = PENDING)"),
                                    C("svc", "urepo", "save(user)"),
                                    N("svc", "cus", "create(user, idNumber, dateOfBirth, gender, address)"),
                                    C("svc", "crepo", "save(customer)"),
                                    S("svc", "issueVerificationLink(user)"),
                                    C("svc", "set", "getInt(VERIFY_TOKEN_EXPIRY_HOURS)"),
                                    R("set", "svc", "24"),
                                    N("svc", "tok", "create(user, VERIFY_EMAIL, expiresAt = now + 24h)"),
                                    C("svc", "trepo", "save(token)"),
                                    C("svc", "mailsvc", "send(email, \"Kích hoạt tài khoản\", link)"),
                                    OPT("app.mail.enabled = true",
                                        C("mailsvc", "mail", "gửi email kích hoạt (SMTP)")),
                                    R("svc", "ctrl", "ActionResponse(message, devLink)"),
                                ]),
                            ),
                        ]),
                    ),
                    R("ctrl", "page", "201 ActionResponse | 400 / 409 ErrorResponse"),
                ]),
            ),
            ALT(
                ("201 Created", [R("page", G, "báo kiểm tra email (+ link xác thực ở chế độ demo)")]),
                ("else", [R("page", G, "hiển thị lỗi từng trường")]),
            ),
        ],
    ),
    # ------------------------------------------------------------------ UC-02 Xác thực email
    dict(
        name="SD UC-02 Xac thuc email",
        lifelines=[
            (G, None, "Khách vãng lai", None),
            ("page", "VerifyEmail", "verifyPage", "boundary"),
            ("ctrl", "AuthController", "", "control"),
            ("svc", "AuthService", "", "control"),
            ("trepo", "VerificationTokenRepository", "", "entity"),
            ("tok", "VerificationToken", "token", "entity"),
            ("user", "User", "user", "entity"),
            ("urepo", "UserRepository", "", "entity"),
            ("mailsvc", "MailService", "", "boundary"),
        ],
        steps=[
            C(G, "page", "bấm link /verify-email?token=..."),
            C("page", "ctrl", "POST /api/auth/verify-email (TokenRequest)"),
            C("ctrl", "svc", "verifyEmail(token)"),
            C("svc", "trepo", "findByTokenAndType(token, VERIFY_EMAIL)"),
            R("trepo", "svc", "Optional<VerificationToken>"),
            ALT(
                ("không tìm thấy", [R("svc", "ctrl", "ApiException 400 INVALID_TOKEN")]),
                ("else", [
                    C("svc", "tok", "getUser(), getUsedAt(), isUsable(now)"),
                    R("tok", "svc", "user, usedAt, usable"),
                    ALT(
                        ("usedAt ≠ null", [
                            ALT(
                                ("user.status = ACTIVE", [R("svc", "ctrl", "ActionResponse \"đã kích hoạt trước đó\"")]),
                                ("else", [R("svc", "ctrl", "ApiException 422 TOKEN_USED")]),
                            ),
                        ]),
                        ("usable = false (hết hạn)", [R("svc", "ctrl", "ApiException 422 TOKEN_EXPIRED")]),
                        ("user.status ≠ PENDING", [R("svc", "ctrl", "ApiException 422 INVALID_STATUS")]),
                        ("else", [
                            C("svc", "user", "setStatus(ACTIVE)"),
                            C("svc", "tok", "setUsedAt(now)"),
                            R("svc", "ctrl", "ActionResponse \"Kích hoạt thành công\""),
                        ]),
                    ),
                ]),
            ),
            R("ctrl", "page", "200 ActionResponse | 400 / 422 ErrorResponse"),
            ALT(
                ("200 OK", [R("page", G, "báo thành công + nút Đăng nhập")]),
                ("code = TOKEN_EXPIRED", [
                    R("page", G, "báo link hết hạn, hiện ô nhập email"),
                    C(G, "page", "nhập email; bấm Gửi lại link"),
                    C("page", "ctrl", "POST /api/auth/resend-verification (EmailRequest)"),
                    C("ctrl", "svc", "resendVerification(email)"),
                    C("svc", "urepo", "findByEmailIgnoreCase(email)"),
                    R("urepo", "svc", "Optional<User>"),
                    OPT("user tồn tại và status = PENDING",
                        S("svc", "issueVerificationLink(user): vô hiệu link cũ, tạo token mới"),
                        C("svc", "mailsvc", "send(email, \"Kích hoạt tài khoản\", link)")),
                    R("svc", "ctrl", "ActionResponse (luôn cùng thông báo)"),
                    R("ctrl", "page", "200 ActionResponse"),
                    R("page", G, "báo đã gửi lại link"),
                ]),
                ("else", [R("page", G, "báo link không hợp lệ")]),
            ),
        ],
    ),
    # ------------------------------------------------------------------ UC-04 Quên & đặt lại mật khẩu
    dict(
        name="SD UC-04 Quen va dat lai mat khau",
        lifelines=[
            (G, None, "Khách vãng lai", None),
            ("fpage", "ForgotPassword", "forgotPage", "boundary"),
            ("rpage", "ResetPassword", "resetPage", "boundary"),
            ("ctrl", "AuthController", "", "control"),
            ("svc", "AuthService", "", "control"),
            ("urepo", "UserRepository", "", "entity"),
            ("set", "SettingService", "", "control"),
            ("trepo", "VerificationTokenRepository", "", "entity"),
            ("tok", "VerificationToken", "token", "entity"),
            ("user", "User", "user", "entity"),
            ("enc", "PasswordEncoder", "", None),
            ("mailsvc", "MailService", "", "boundary"),
            ("mail", None, "Hệ thống Email", None),
        ],
        steps=[
            C(G, "fpage", "nhập email; bấm Gửi"),
            C("fpage", "ctrl", "POST /api/auth/forgot-password (EmailRequest)"),
            C("ctrl", "svc", "forgotPassword(email)"),
            C("svc", "urepo", "findByEmailIgnoreCase(email)"),
            R("urepo", "svc", "Optional<User>"),
            OPT("user tồn tại và status = ACTIVE",
                C("svc", "set", "getInt(RESET_TOKEN_EXPIRY_MINUTES)"),
                R("set", "svc", "30"),
                C("svc", "trepo", "findByUserIdAndTypeAndUsedAtIsNull(userId, RESET_PASSWORD)"),
                R("trepo", "svc", "link cũ -> setUsedAt(now)"),
                N("svc", "tok", "create(user, RESET_PASSWORD, expiresAt = now + 30')"),
                C("svc", "trepo", "save(token)"),
                C("svc", "mailsvc", "send(email, \"Đặt lại mật khẩu\", link)"),
                OPT("app.mail.enabled = true", C("mailsvc", "mail", "gửi email đặt lại mật khẩu (SMTP)"))),
            R("svc", "ctrl", "ActionResponse (luôn cùng thông báo)"),
            R("ctrl", "fpage", "200 ActionResponse"),
            R("fpage", G, "báo kiểm tra hộp thư"),
            C(G, "rpage", "bấm link /reset-password?token=...; nhập mật khẩu mới"),
            S("rpage", "validate form (zod)"),
            C("rpage", "ctrl", "POST /api/auth/reset-password (ResetPasswordRequest)"),
            C("ctrl", "svc", "resetPassword(r)"),
            ALT(
                ("newPassword ≠ confirmPassword", [R("svc", "ctrl", "ApiException 400 confirmPassword")]),
                ("else", [
                    C("svc", "trepo", "findByTokenAndType(token, RESET_PASSWORD)"),
                    R("trepo", "svc", "Optional<VerificationToken>"),
                    ALT(
                        ("không tìm thấy", [R("svc", "ctrl", "ApiException 400 INVALID_TOKEN")]),
                        ("usedAt ≠ null", [R("svc", "ctrl", "ApiException 422 TOKEN_USED")]),
                        ("token hết hạn", [R("svc", "ctrl", "ApiException 422 TOKEN_EXPIRED")]),
                        ("user.status ≠ ACTIVE", [R("svc", "ctrl", "ApiException 422 ACCOUNT_LOCKED")]),
                        ("else", [
                            S("svc", "applyNewPassword(user, newPassword)"),
                            C("svc", "enc", "encode(newPassword)"),
                            R("enc", "svc", "passwordHash"),
                            C("svc", "user", "setPasswordHash(hash), setPasswordChangedAt(now)"),
                            C("svc", "tok", "setUsedAt(now)"),
                            R("svc", "ctrl", "ActionResponse \"Đặt lại mật khẩu thành công\""),
                        ]),
                    ),
                ]),
            ),
            R("ctrl", "rpage", "200 ActionResponse | 400 / 422 ErrorResponse"),
            ALT(
                ("200 OK", [R("rpage", G, "báo thành công, chuyển tới Đăng nhập")]),
                ("else", [R("rpage", G, "hiển thị lỗi")]),
            ),
        ],
    ),
    # ------------------------------------------------------------------ UC-05 Đổi mật khẩu (kèm kiểm tra JWT)
    dict(
        name="SD UC-05 Doi mat khau",
        lifelines=[
            ("u", None, "Người dùng", None),
            ("page", "Profile", "profilePage", "boundary"),
            ("filter", "JwtAuthenticationFilter", "", "control"),
            ("jwt", "JwtService", "", "control"),
            ("me", "MeController", "", "control"),
            ("cur", "CurrentUser", "", "control"),
            ("svc", "AuthService", "", "control"),
            ("urepo", "UserRepository", "", "entity"),
            ("user", "User", "user", "entity"),
            ("enc", "PasswordEncoder", "", None),
        ],
        steps=[
            C("u", "page", "nhập mật khẩu hiện tại, mới, nhập lại; bấm Đổi"),
            S("page", "validate form (zod)"),
            C("page", "filter", "POST /api/me/change-password + Authorization: Bearer JWT"),
            C("filter", "jwt", "parse(token)"),
            R("jwt", "filter", "claims | null"),
            OPT("claims ≠ null",
                C("filter", "urepo", "findById(claims.sub)"),
                R("urepo", "filter", "user")),
            ALT(
                ("claims = null | user không ACTIVE | iat < passwordChangedAt", [
                    R("filter", "page", "401 UNAUTHORIZED"),
                    R("page", "u", "đăng xuất, chuyển tới /login?expired=1"),
                ]),
                ("else", [
                    S("filter", "setAuthentication(AppUserPrincipal)"),
                    C("filter", "me", "changePassword(ChangePasswordRequest)"),
                    C("me", "cur", "get()"),
                    R("cur", "me", "AppUserPrincipal"),
                    C("me", "svc", "changePassword(userId, r)"),
                    C("svc", "urepo", "findById(userId)"),
                    R("urepo", "svc", "user"),
                    C("svc", "enc", "matches(currentPassword, passwordHash)"),
                    R("enc", "svc", "ok : boolean"),
                    ALT(
                        ("ok = false", [R("svc", "me", "ApiException 400 currentPassword")]),
                        ("newPassword ≠ confirmPassword", [R("svc", "me", "ApiException 400 confirmPassword")]),
                        ("newPassword = currentPassword", [R("svc", "me", "ApiException 400 newPassword")]),
                        ("else", [
                            S("svc", "applyNewPassword(user, newPassword)"),
                            C("svc", "enc", "encode(newPassword)"),
                            R("enc", "svc", "passwordHash"),
                            C("svc", "user", "setPasswordHash(hash), setPasswordChangedAt(now)"),
                            S("svc", "buildAuthResponse(user)"),
                            C("svc", "jwt", "generate(user)"),
                            R("jwt", "svc", "JWT mới"),
                            R("svc", "me", "AuthResponse"),
                        ]),
                    ),
                    R("me", "page", "200 AuthResponse | 400 ErrorResponse"),
                    ALT(
                        ("200 OK", [
                            S("page", "saveSession(auth): lưu token mới"),
                            R("page", "u", "báo đổi mật khẩu thành công (phiên khác bị đăng xuất)"),
                        ]),
                        ("else", [R("page", "u", "hiển thị lỗi từng trường")]),
                    ),
                ]),
            ),
        ],
    ),
]

# Bản rút gọn (trình bày / slide)
from seq_spec_short import SHORT  # noqa: E402

SEQUENCES += SHORT
