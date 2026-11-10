"""Vị trí shape cho 2 class diagram."""

NOTE_MAIN = (
    "Phân quyền (JWT + @PreAuthorize)\n"
    "• Role: CUSTOMER, STAFF. JWT (HS256, 8 giờ) mang claim role -> quyền ROLE_CUSTOMER / ROLE_STAFF.\n"
    "• JwtAuthenticationFilter chỉ nhận token của tài khoản ACTIVE, cấp sau lần đổi mật khẩu gần nhất.\n"
    "• SecurityConfig: /api/auth/** công khai; /api/** phải đăng nhập (401), sai vai trò -> 403.\n"
    "• MeController: updateProfile, customerDashboard = CUSTOMER; updateStaffProfile, staffDashboard = STAFF.\n"
    "• CustomerController, SettingController (cả lớp): chỉ STAFF.\n"
    "• CurrentUser.checkOwnership: khách hàng xem dữ liệu người khác -> 404.\n"
    "• Đăng nhập sai 5 lần -> tạm khoá 15 phút (AppProperties.security)."
)

NOTE_REPO = (
    "Tham số kiểu của JpaRepository<T, ID>:\n"
    "• UserRepository: T = User, ID = Long\n"
    "• CustomerRepository: T = Customer, ID = Long\n"
    "• VerificationTokenRepository: T = VerificationToken, ID = Long"
)

NOTE_DTO = (
    "AuthDtos: các Java record lồng trong lớp final AuthDtos.\n"
    "Request được validate bằng @NotBlank, @Pattern, @AgeBetween, @RawString\n"
    "(mật khẩu không bị trim). AuthResponse trả về JWT + UserInfo."
)

MAIN = dict(
    name="Class Diagram - Phan 1 Vai tro & phan quyen",
    pos={
        # controller
        "AuthController": (60, 90),
        "MeController": (470, 90),
        # config
        "SecurityConfig": (1000, 90),
        "AppProperties": (1720, 90),
        # service
        "AuthService": (60, 400),
        "MailService": (540, 400),
        "SettingService": (540, 560),
        # security
        "CurrentUser": (960, 400),
        "JwtAuthenticationFilter": (1320, 400),
        "AppUserPrincipal": (960, 560),
        "JwtService": (1500, 600),
        # spring
        "OncePerRequestFilter": (2000, 420),
        "JpaRepository<T, ID>": (560, 1110),
        # repository
        "VerificationTokenRepository": (60, 900),
        "UserRepository": (660, 900),
        "CustomerRepository": (1140, 900),
        # domain
        "BaseEntity": (390, 1300),
        "Customer": (100, 1480),
        "User": (380, 1480),
        "VerificationToken": (700, 1480),
        # enums
        "Gender": (128, 1770),
        "Role": (330, 1770),
        "UserStatus": (470, 1770),
        "TokenType": (760, 1770),
    },
    width={"SettingService": 280},
    skip={
        ("AuthService", "AppProperties"), ("MailService", "AppProperties"),
        ("JwtService", "AppProperties"), ("JwtService", "User"),
        ("AuthService", "User"), ("AuthService", "Customer"), ("AuthService", "VerificationToken"),
    },
    routes={
        ("AuthController", "AuthService"): {"from": (240, 245), "to": (240, 400)},
        ("SecurityConfig", "JwtAuthenticationFilter"): {"from": (1500, 260), "to": (1500, 400)},
        ("CurrentUser", "AppUserPrincipal"): {"from": (1110, 496), "to": (1110, 560)},
        ("JwtAuthenticationFilter", "JwtService"): {"from": (1602, 525), "to": (1602, 600)},
        ("AuthService", "MailService"): {"from": (483, 455), "to": (540, 455)},
        ("AuthService", "SettingService"): {"from": (483, 607), "to": (540, 607)},
        ("AuthService", "JwtService"): {"from": (483, 780), "via": [(1602, 780)], "to": (1602, 740)},
        ("AuthService", "UserRepository"): {"from": (430, 795), "via": [(430, 850), (760, 850)], "to": (760, 900)},
        ("AuthService", "CustomerRepository"): {"from": (465, 795), "via": [(465, 838), (1250, 838)], "to": (1250, 900)},
        ("AuthService", "VerificationTokenRepository"): {"from": (270, 795), "to": (270, 900)},
        ("JwtAuthenticationFilter", "UserRepository"): {"from": (1330, 525), "via": [(1330, 850)], "to": (1000, 900)},
        ("JwtAuthenticationFilter", "CustomerRepository"): {"from": (1800, 525), "via": [(1800, 955)], "to": (1697, 955)},
        ("JwtAuthenticationFilter", "OncePerRequestFilter"): {"from": (1933, 445), "to": (2000, 445)},
        ("AppUserPrincipal", "Role"): {"from": (1100, 730), "via": [(1100, 1880), (385, 1880)], "to": (385, 1843)},
        # repository -> JpaRepository (dạng cây)
        ("VerificationTokenRepository", "JpaRepository<T, ID>"): {"from": (330, 996), "via": [(330, 1075), (656, 1075)], "to": (656, 1110)},
        ("UserRepository", "JpaRepository<T, ID>"): {"from": (860, 1041), "via": [(860, 1075), (656, 1075)], "to": (656, 1110)},
        ("CustomerRepository", "JpaRepository<T, ID>"): {"from": (1418, 1011), "via": [(1418, 1075), (656, 1075)], "to": (656, 1110)},
        # domain
        ("User", "BaseEntity"): {"from": (470, 1480), "to": (470, 1425)},
        ("Customer", "BaseEntity"): {"from": (182, 1480), "via": [(182, 1362)], "to": (390, 1362)},
        ("Customer", "User"): {"from": (265, 1540), "to": (380, 1540)},
        ("VerificationToken", "User"): {"from": (700, 1540), "to": (584, 1540)},
        ("Customer", "Gender"): {"from": (182, 1605), "to": (182, 1770)},
        ("User", "Role"): {"from": (385, 1666), "to": (385, 1770)},
        ("User", "UserStatus"): {"from": (525, 1666), "to": (525, 1770)},
        ("VerificationToken", "TokenType"): {"from": (815, 1635), "to": (815, 1770)},
    },
    notes={
        "roles": dict(x=1170, y=1300, w=620, h=200, text=NOTE_MAIN),
        "repo": dict(x=60, y=1110, w=420, h=80, text=NOTE_REPO),
    },
)

DTO = dict(
    name="Class Diagram - Phan 1 AuthDtos",
    pos={
        "AuthController": (60, 300),
        "MeController": (60, 640),
        "LoginRequest": (620, 60),
        "RegisterRequest": (620, 161),
        "TokenRequest": (620, 367),
        "EmailRequest": (620, 453),
        "ResetPasswordRequest": (620, 539),
        "ChangePasswordRequest": (620, 655),
        "AuthResponse": (620, 791),
        "ActionResponse": (620, 907),
        "UserInfo": (860, 760),
        "Gender": (1080, 210),
        "Role": (1080, 760),
        "UserStatus": (1080, 860),
    },
    skip=set(),
    routes={
        ("AuthController", "AuthResponse"): {"from": (420, 455), "to": (620, 820)},
        ("AuthController", "ActionResponse"): {"from": (400, 455), "to": (620, 940)},
        ("AuthResponse", "UserInfo"): {"from": (752, 839), "to": (860, 839)},
        ("RegisterRequest", "Gender"): {"from": (774, 250), "to": (1080, 250)},
    },
    notes={
        "dto": dict(x=60, y=40, w=470, h=80, text=NOTE_DTO),
    },
)

DIAGRAMS = [MAIN, DTO]
