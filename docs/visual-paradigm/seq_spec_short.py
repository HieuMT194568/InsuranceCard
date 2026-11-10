"""Bản rút gọn của 5 sequence diagram Phần 1 (dùng để trình bày / slide).

Mỗi UC: 6–7 lifeline, 1 fragment alt chính; mỗi nhánh trả kết quả về tới người dùng.
Bản chi tiết: seq_spec.py.
"""
from gen_seq_dsl import ALT, C, N, OPT, R, S

G = "guest"

SHORT = [
    dict(
        name="SD UC-03 Dang nhap (rut gon)",
        lifelines=[
            (G, None, "Khách vãng lai", None),
            ("page", "Login", "loginPage", "boundary"),
            ("ctrl", "AuthController", "", "control"),
            ("svc", "AuthService", "", "control"),
            ("urepo", "UserRepository", "", "entity"),
            ("jwt", "JwtService", "", "control"),
        ],
        steps=[
            C(G, "page", "nhập email, mật khẩu"),
            C("page", "ctrl", "POST /api/auth/login"),
            C("ctrl", "svc", "login(email, password)"),
            C("svc", "urepo", "findByEmailIgnoreCase(email)"),
            R("urepo", "svc", "user"),
            ALT(
                ("sai email / mật khẩu (sai 5 lần -> khoá 15 phút)", [
                    R("svc", "ctrl", "lỗi INVALID_CREDENTIALS"),
                    R("ctrl", "page", "401"),
                    R("page", G, "báo sai email hoặc mật khẩu"),
                ]),
                ("chưa kích hoạt / bị khoá", [
                    R("svc", "ctrl", "lỗi EMAIL_NOT_VERIFIED / ACCOUNT_LOCKED"),
                    R("ctrl", "page", "403"),
                    R("page", G, "báo lỗi"),
                ]),
                ("else", [
                    C("svc", "jwt", "generate(user)"),
                    R("jwt", "svc", "JWT"),
                    R("svc", "ctrl", "AuthResponse"),
                    R("ctrl", "page", "200 AuthResponse"),
                    R("page", G, "vào trang theo vai trò"),
                ]),
            ),
        ],
    ),
    dict(
        name="SD UC-01 Dang ky (rut gon)",
        lifelines=[
            (G, None, "Khách vãng lai", None),
            ("page", "Register", "registerPage", "boundary"),
            ("ctrl", "AuthController", "", "control"),
            ("svc", "AuthService", "", "control"),
            ("urepo", "UserRepository", "", "entity"),
            ("user", "User", "user", "entity"),
            ("mailsvc", "MailService", "", "boundary"),
        ],
        steps=[
            C(G, "page", "nhập thông tin đăng ký"),
            C("page", "ctrl", "POST /api/auth/register"),
            C("ctrl", "svc", "register(r)"),
            C("svc", "urepo", "kiểm tra email, SĐT, CCCD đã tồn tại"),
            R("urepo", "svc", "kết quả"),
            ALT(
                ("thông tin bị trùng", [
                    R("svc", "ctrl", "lỗi DUPLICATE"),
                    R("ctrl", "page", "409"),
                    R("page", G, "báo trường bị trùng"),
                ]),
                ("else", [
                    N("svc", "user", "create(status = PENDING)"),
                    C("svc", "urepo", "save(user)"),
                    C("svc", "mailsvc", "gửi link xác thực"),
                    R("svc", "ctrl", "ActionResponse"),
                    R("ctrl", "page", "201"),
                    R("page", G, "báo kiểm tra email"),
                ]),
            ),
        ],
    ),
    dict(
        name="SD UC-02 Xac thuc email (rut gon)",
        lifelines=[
            (G, None, "Khách vãng lai", None),
            ("page", "VerifyEmail", "verifyPage", "boundary"),
            ("ctrl", "AuthController", "", "control"),
            ("svc", "AuthService", "", "control"),
            ("trepo", "VerificationTokenRepository", "", "entity"),
            ("user", "User", "user", "entity"),
        ],
        steps=[
            C(G, "page", "bấm link xác thực"),
            C("page", "ctrl", "POST /api/auth/verify-email"),
            C("ctrl", "svc", "verifyEmail(token)"),
            C("svc", "trepo", "findByTokenAndType(token, VERIFY_EMAIL)"),
            R("trepo", "svc", "token"),
            ALT(
                ("token không hợp lệ / đã dùng / hết hạn", [
                    R("svc", "ctrl", "lỗi INVALID_TOKEN / TOKEN_EXPIRED"),
                    R("ctrl", "page", "400 / 422"),
                    R("page", G, "báo lỗi, cho gửi lại link"),
                ]),
                ("else", [
                    C("svc", "user", "setStatus(ACTIVE)"),
                    R("svc", "ctrl", "ActionResponse"),
                    R("ctrl", "page", "200"),
                    R("page", G, "kích hoạt thành công"),
                ]),
            ),
        ],
    ),
    dict(
        name="SD UC-04 Quen va dat lai mat khau (rut gon)",
        lifelines=[
            (G, None, "Khách vãng lai", None),
            ("fpage", "ForgotPassword", "forgotPage", "boundary"),
            ("rpage", "ResetPassword", "resetPage", "boundary"),
            ("ctrl", "AuthController", "", "control"),
            ("svc", "AuthService", "", "control"),
            ("trepo", "VerificationTokenRepository", "", "entity"),
            ("mailsvc", "MailService", "", "boundary"),
        ],
        steps=[
            C(G, "fpage", "nhập email"),
            C("fpage", "ctrl", "POST /api/auth/forgot-password"),
            C("ctrl", "svc", "forgotPassword(email)"),
            OPT("email thuộc tài khoản ACTIVE",
                C("svc", "trepo", "save(token RESET_PASSWORD, hạn 30 phút)"),
                C("svc", "mailsvc", "gửi link đặt lại mật khẩu")),
            R("svc", "ctrl", "ActionResponse"),
            R("ctrl", "fpage", "200"),
            R("fpage", G, "báo kiểm tra hộp thư"),
            C(G, "rpage", "mở link, nhập mật khẩu mới"),
            C("rpage", "ctrl", "POST /api/auth/reset-password"),
            C("ctrl", "svc", "resetPassword(r)"),
            C("svc", "trepo", "findByTokenAndType(token, RESET_PASSWORD)"),
            R("trepo", "svc", "token"),
            ALT(
                ("token không hợp lệ / đã dùng / hết hạn", [
                    R("svc", "ctrl", "lỗi"),
                    R("ctrl", "rpage", "400 / 422"),
                    R("rpage", G, "báo lỗi"),
                ]),
                ("else", [
                    S("svc", "đổi mật khẩu, đánh dấu token đã dùng"),
                    R("svc", "ctrl", "ActionResponse"),
                    R("ctrl", "rpage", "200"),
                    R("rpage", G, "thành công, đăng nhập lại"),
                ]),
            ),
        ],
    ),
    dict(
        name="SD UC-05 Doi mat khau (rut gon)",
        lifelines=[
            ("u", None, "Người dùng", None),
            ("page", "Profile", "profilePage", "boundary"),
            ("filter", "JwtAuthenticationFilter", "", "control"),
            ("me", "MeController", "", "control"),
            ("svc", "AuthService", "", "control"),
            ("urepo", "UserRepository", "", "entity"),
            ("jwt", "JwtService", "", "control"),
        ],
        steps=[
            C("u", "page", "nhập mật khẩu hiện tại, mật khẩu mới"),
            C("page", "filter", "POST /api/me/change-password + Bearer JWT"),
            ALT(
                ("JWT không hợp lệ / hết hạn", [
                    R("filter", "page", "401"),
                    R("page", "u", "đăng xuất, về trang đăng nhập"),
                ]),
                ("else", [
                    C("filter", "me", "changePassword(r)"),
                    C("me", "svc", "changePassword(userId, r)"),
                    C("svc", "urepo", "findById(userId)"),
                    R("urepo", "svc", "user"),
                    ALT(
                        ("sai mật khẩu hiện tại / mật khẩu mới không hợp lệ", [
                            R("svc", "me", "lỗi"),
                            R("me", "page", "400"),
                            R("page", "u", "báo lỗi"),
                        ]),
                        ("else", [
                            S("svc", "đổi mật khẩu, passwordChangedAt = now"),
                            C("svc", "jwt", "generate(user)"),
                            R("jwt", "svc", "JWT mới"),
                            R("svc", "me", "AuthResponse"),
                            R("me", "page", "200"),
                            R("page", "u", "đổi mật khẩu thành công"),
                        ]),
                    ),
                ]),
            ),
        ],
    ),
]
