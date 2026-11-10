"""Phần 1 – Vai trò / phân quyền: class model lấy từ backend/src/main/java/com/insurancecard."""

# Màu nền theo package (shape fill)
COLORS = {
    "controller": (224, 242, 254),
    "service": (220, 252, 231),
    "repository": (243, 232, 255),
    "security": (255, 228, 230),
    "config": (254, 243, 199),
    "domain": (255, 237, 213),
    "domain.enums": (254, 249, 195),
    "dto.AuthDtos": (207, 250, 254),
    "spring.web": (241, 245, 249),
    "spring.data": (241, 245, 249),
    "spring.crypto": (241, 245, 249),
    "frontend.pages": (224, 242, 254),
}

# key -> spec. Member syntax:
#   attribute: "<vis>name: Type"         (vis: + - # ~)
#   operation: "<vis>name(p: T, q: U): R"   prefix "$" = static, "/" = abstract
#   enum literal: plain NAME
CLASSES = {
    # ------------------------------------------------------------------ controller
    "AuthController": dict(pkg="controller", stereo="RestController", attrs=[
        "-authService: AuthService",
    ], ops=[
        "+login(request: LoginRequest): AuthResponse",
        "+register(request: RegisterRequest): ActionResponse",
        "+verifyEmail(request: TokenRequest): ActionResponse",
        "+resendVerification(request: EmailRequest): ActionResponse",
        "+forgotPassword(request: EmailRequest): ActionResponse",
        "+resetPassword(request: ResetPasswordRequest): ActionResponse",
    ]),
    "MeController": dict(pkg="controller", stereo="RestController", attrs=[
        "-currentUser: CurrentUser",
        "-customerService: CustomerService",
        "-authService: AuthService",
        "-dashboardService: DashboardService",
    ], ops=[
        "+me(): ProfileResponse",
        "+updateProfile(request: ProfileUpdateRequest): ProfileResponse",
        "+updateStaffProfile(request: StaffProfileUpdateRequest): ProfileResponse",
        "+changePassword(request: ChangePasswordRequest): AuthResponse",
        "+customerDashboard(): CustomerDashboard",
        "+staffDashboard(): StaffDashboard",
    ]),
    # ------------------------------------------------------------------ config
    "SecurityConfig": dict(pkg="config", stereo="Configuration", attrs=[
        "-jwtFilter: JwtAuthenticationFilter",
        "-props: AppProperties",
        "-objectMapper: ObjectMapper",
    ], ops=[
        "+filterChain(http: HttpSecurity): SecurityFilterChain",
        "+passwordEncoder(): PasswordEncoder",
        "+jwtFilterRegistration(filter: JwtAuthenticationFilter): FilterRegistrationBean<JwtAuthenticationFilter>",
        "-writeError(req: HttpServletRequest, res: HttpServletResponse, status: int, code: String, message: String): void",
        "-corsSource(): CorsConfigurationSource",
    ]),
    "AppProperties": dict(pkg="config", stereo="record", attrs=[
        "+jwt: Jwt",
        "+cors: Cors",
        "+frontendUrl: String",
        "+mail: Mail",
        "+security: Security",
        "+seed: Seed",
    ], ops=[]),
    # ------------------------------------------------------------------ service
    "AuthService": dict(pkg="service", stereo="Service", attrs=[
        "-RANDOM: SecureRandom {static}",
        "-userRepository: UserRepository",
        "-customerRepository: CustomerRepository",
        "-tokenRepository: VerificationTokenRepository",
        "-passwordEncoder: PasswordEncoder",
        "-jwtService: JwtService",
        "-mailService: MailService",
        "-settingService: SettingService",
        "-props: AppProperties",
    ], ops=[
        "+register(r: RegisterRequest): ActionResponse",
        "+verifyEmail(token: String): ActionResponse",
        "+resendVerification(rawEmail: String): ActionResponse",
        "-issueVerificationLink(user: User): String",
        "+login(r: LoginRequest): AuthResponse",
        "$-invalidCredentials(): ApiException",
        "+buildAuthResponse(user: User): AuthResponse",
        "+forgotPassword(rawEmail: String): ActionResponse",
        "+resetPassword(r: ResetPasswordRequest): ActionResponse",
        "+changePassword(userId: Long, r: ChangePasswordRequest): AuthResponse",
        "+applyNewPassword(user: User, rawPassword: String): void",
        "-createToken(user: User, type: TokenType, validity: Duration): String",
        "$+normalizeEmail(email: String): String",
        "$+normalizeName(name: String): String",
    ]),
    "MailService": dict(pkg="service", stereo="Service", attrs=[
        "-props: AppProperties",
        "-mailSender: ObjectProvider<JavaMailSender>",
    ], ops=[
        "+isDemoMode(): boolean",
        "+send(to: String, subject: String, body: String): void",
    ]),
    "SettingService": dict(pkg="service", stereo="Service", attrs=[
        "+VERIFY_TOKEN_EXPIRY_HOURS: String {static}",
        "+RESET_TOKEN_EXPIRY_MINUTES: String {static}",
    ], ops=[
        "+getInt(key: String): int",
    ]),
    # ------------------------------------------------------------------ security
    "JwtAuthenticationFilter": dict(pkg="security", stereo="Component", attrs=[
        "-jwtService: JwtService",
        "-userRepository: UserRepository",
        "-customerRepository: CustomerRepository",
    ], ops=[
        "#doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain): void",
        "-authenticate(claims: Claims): void",
    ]),
    "JwtService": dict(pkg="security", stereo="Service", attrs=[
        "-key: SecretKey",
        "-expirationMinutes: long",
    ], ops=[
        "+JwtService(props: AppProperties)",
        "+generate(user: User): String",
        "+getExpirationSeconds(): long",
        "+parse(token: String): Claims",
    ]),
    "CurrentUser": dict(pkg="security", stereo="Component", attrs=[], ops=[
        "+get(): AppUserPrincipal",
        "+scopeCustomerId(requestedCustomerId: Long): Long",
        "+checkOwnership(ownerCustomerId: Long): void",
    ]),
    "AppUserPrincipal": dict(pkg="security", stereo="record", attrs=[
        "+userId: Long",
        "+email: String",
        "+fullName: String",
        "+role: Role",
        "+customerId: Long",
    ], ops=[
        "+authorities(): Collection<? extends GrantedAuthority>",
        "+isStaff(): boolean",
        "+isCustomer(): boolean",
    ]),
    # ------------------------------------------------------------------ repository
    "UserRepository": dict(pkg="repository", stereo="Interface", attrs=[], ops=[
        "/+findByEmailIgnoreCase(email: String): Optional<User>",
        "/+existsByEmailIgnoreCase(email: String): boolean",
        "/+existsByEmailIgnoreCaseAndIdNot(email: String, id: Long): boolean",
        "/+existsByPhone(phone: String): boolean",
        "/+existsByPhoneAndIdNot(phone: String, id: Long): boolean",
        "/+countByRoleAndStatus(role: Role, status: UserStatus): long",
    ]),
    "CustomerRepository": dict(pkg="repository", stereo="Interface", attrs=[], ops=[
        "/+findByUserId(userId: Long): Optional<Customer>",
        "/+existsByIdNumber(idNumber: String): boolean",
        "/+existsByIdNumberAndIdNot(idNumber: String, id: Long): boolean",
        "/+search(q: String, customerId: Long, status: UserStatus, pageable: Pageable): Page<Customer>",
    ]),
    "VerificationTokenRepository": dict(pkg="repository", stereo="Interface", attrs=[], ops=[
        "/+findByTokenAndType(token: String, type: TokenType): Optional<VerificationToken>",
        "/+findByUserIdAndTypeAndUsedAtIsNull(userId: Long, type: TokenType): List<VerificationToken>",
        "/+deleteByUserId(userId: Long): void",
    ]),
    # ------------------------------------------------------------------ domain
    "BaseEntity": dict(pkg="domain", stereo="MappedSuperclass", abstract=True, attrs=[
        "-id: Long",
        "-createdAt: LocalDateTime",
        "-updatedAt: LocalDateTime",
    ], ops=[
        "#onCreate(): void",
        "#onUpdate(): void",
    ]),
    "User": dict(pkg="domain", stereo="Entity", attrs=[
        "-email: String",
        "-passwordHash: String",
        "-fullName: String",
        "-phone: String",
        "-role: Role",
        "-status: UserStatus",
        "-failedLoginCount: int",
        "-lockoutUntil: LocalDateTime",
        "-passwordChangedAt: LocalDateTime",
    ], ops=[]),
    "Customer": dict(pkg="domain", stereo="Entity", attrs=[
        "-idNumber: String",
        "-dateOfBirth: LocalDate",
        "-gender: Gender",
        "-address: String",
    ], ops=[
        "+getCustomerCode(): String",
    ]),
    "VerificationToken": dict(pkg="domain", stereo="Entity", attrs=[
        "-id: Long",
        "-token: String",
        "-type: TokenType",
        "-expiresAt: LocalDateTime",
        "-usedAt: LocalDateTime",
        "-createdAt: LocalDateTime",
    ], ops=[
        "+isUsable(now: LocalDateTime): boolean",
    ]),
    # ------------------------------------------------------------------ enums
    "Role": dict(pkg="domain.enums", stereo="enumeration", literals=["CUSTOMER", "STAFF"]),
    "UserStatus": dict(pkg="domain.enums", stereo="enumeration", literals=["PENDING", "ACTIVE", "LOCKED"]),
    "TokenType": dict(pkg="domain.enums", stereo="enumeration", literals=["VERIFY_EMAIL", "RESET_PASSWORD"]),
    "Gender": dict(pkg="domain.enums", stereo="enumeration", literals=["MALE", "FEMALE", "OTHER"]),
    # ------------------------------------------------------------------ Spring (bên ngoài)
    "OncePerRequestFilter": dict(pkg="spring.web", abstract=True, attrs=[], ops=[]),
    "JpaRepository<T, ID>": dict(pkg="spring.data", stereo="Interface", attrs=[], ops=[
        "/+findById(id: ID): Optional<T>",
        "/+save(entity: T): T",
    ]),
    "PasswordEncoder": dict(pkg="spring.crypto", stereo="Interface", attrs=[], ops=[
        "/+encode(rawPassword: CharSequence): String",
        "/+matches(rawPassword: CharSequence, encodedPassword: String): boolean",
    ]),
    # ------------------------------------------------------------------ frontend (boundary – trang React)
    "Login": dict(pkg="frontend.pages", stereo="boundary", attrs=[], ops=[
        "+onSubmit(email: string, password: string): void", "+resend(): void"]),
    "Register": dict(pkg="frontend.pages", stereo="boundary", attrs=[], ops=["+onSubmit(values: RegisterForm): void"]),
    "VerifyEmail": dict(pkg="frontend.pages", stereo="boundary", attrs=[], ops=["+verify(token: string): void", "+resend(email: string): void"]),
    "ForgotPassword": dict(pkg="frontend.pages", stereo="boundary", attrs=[], ops=["+onSubmit(email: string): void"]),
    "ResetPassword": dict(pkg="frontend.pages", stereo="boundary", attrs=[], ops=["+onSubmit(values: ResetForm): void"]),
    "Profile": dict(pkg="frontend.pages", stereo="boundary", attrs=[], ops=["+changePassword(values: ChangePasswordForm): void"]),
    # ------------------------------------------------------------------ DTO (AuthDtos – record lồng)
    "LoginRequest": dict(pkg="dto.AuthDtos", stereo="record", attrs=[
        "+email: String", "+password: String",
    ], ops=[]),
    "RegisterRequest": dict(pkg="dto.AuthDtos", stereo="record", attrs=[
        "+fullName: String", "+email: String", "+phone: String", "+password: String",
        "+confirmPassword: String", "+idNumber: String", "+dateOfBirth: LocalDate",
        "+gender: Gender", "+address: String",
    ], ops=[]),
    "TokenRequest": dict(pkg="dto.AuthDtos", stereo="record", attrs=["+token: String"], ops=[]),
    "EmailRequest": dict(pkg="dto.AuthDtos", stereo="record", attrs=["+email: String"], ops=[]),
    "ResetPasswordRequest": dict(pkg="dto.AuthDtos", stereo="record", attrs=[
        "+token: String", "+newPassword: String", "+confirmPassword: String",
    ], ops=[]),
    "ChangePasswordRequest": dict(pkg="dto.AuthDtos", stereo="record", attrs=[
        "+currentPassword: String", "+newPassword: String", "+confirmPassword: String",
    ], ops=[]),
    "UserInfo": dict(pkg="dto.AuthDtos", stereo="record", attrs=[
        "+id: Long", "+email: String", "+fullName: String", "+phone: String", "+role: Role",
        "+status: UserStatus", "+customerId: Long", "+customerCode: String",
    ], ops=[]),
    "AuthResponse": dict(pkg="dto.AuthDtos", stereo="record", attrs=[
        "+accessToken: String", "+tokenType: String", "+expiresIn: long",
    ], ops=[]),
    "ActionResponse": dict(pkg="dto.AuthDtos", stereo="record", attrs=[
        "+message: String", "+devLink: String",
    ], ops=[]),
}

# (kind, from, to, extra)
#   gen:  from = lớp con, to = lớp cha               (vẽ mũi tam giác ở lớp cha)
#   assoc: from -> to điều hướng; extra = (role, multFrom, multTo)
#   dep:  from ..> to; extra = nhãn stereotype hoặc None
RELATIONS = [
    ("gen", "User", "BaseEntity", None),
    ("gen", "Customer", "BaseEntity", None),
    ("gen", "JwtAuthenticationFilter", "OncePerRequestFilter", None),
    ("gen", "UserRepository", "JpaRepository<T, ID>", None),
    ("gen", "CustomerRepository", "JpaRepository<T, ID>", None),
    ("gen", "VerificationTokenRepository", "JpaRepository<T, ID>", None),

    ("assoc", "Customer", "User", ("user", "0..1", "1")),
    ("assoc", "VerificationToken", "User", ("user", "*", "1")),
    ("assoc", "User", "Role", (None, None, "1")),
    ("assoc", "User", "UserStatus", (None, None, "1")),
    ("assoc", "Customer", "Gender", (None, None, "1")),
    ("assoc", "VerificationToken", "TokenType", (None, None, "1")),
    ("assoc", "AppUserPrincipal", "Role", (None, None, "1")),

    ("assoc", "AuthController", "AuthService", (None, None, None)),
    ("assoc", "MeController", "AuthService", (None, None, None)),
    ("assoc", "MeController", "CurrentUser", (None, None, None)),
    ("assoc", "SecurityConfig", "JwtAuthenticationFilter", (None, None, None)),
    ("assoc", "SecurityConfig", "AppProperties", (None, None, None)),
    ("assoc", "AuthService", "UserRepository", (None, None, None)),
    ("assoc", "AuthService", "CustomerRepository", (None, None, None)),
    ("assoc", "AuthService", "VerificationTokenRepository", (None, None, None)),
    ("assoc", "AuthService", "JwtService", (None, None, None)),
    ("assoc", "AuthService", "MailService", (None, None, None)),
    ("assoc", "AuthService", "SettingService", (None, None, None)),
    ("assoc", "AuthService", "AppProperties", (None, None, None)),
    ("assoc", "MailService", "AppProperties", (None, None, None)),
    ("assoc", "JwtAuthenticationFilter", "JwtService", (None, None, None)),
    ("assoc", "JwtAuthenticationFilter", "UserRepository", (None, None, None)),
    ("assoc", "JwtAuthenticationFilter", "CustomerRepository", (None, None, None)),

    ("dep", "CurrentUser", "AppUserPrincipal", "use"),
    ("dep", "JwtAuthenticationFilter", "AppUserPrincipal", "create"),
    ("dep", "JwtService", "AppProperties", "use"),
    ("dep", "JwtService", "User", "use"),
    ("dep", "AuthService", "User", "create"),
    ("dep", "AuthService", "Customer", "create"),
    ("dep", "AuthService", "VerificationToken", "create"),

    # DTO diagram
    ("assoc", "AuthResponse", "UserInfo", ("user", None, "1")),
    ("assoc", "UserInfo", "Role", (None, None, "1")),
    ("assoc", "UserInfo", "UserStatus", (None, None, "1")),
    ("assoc", "RegisterRequest", "Gender", (None, None, "1")),
    ("dep", "AuthController", "LoginRequest", "use"),
    ("dep", "AuthController", "RegisterRequest", "use"),
    ("dep", "AuthController", "TokenRequest", "use"),
    ("dep", "AuthController", "EmailRequest", "use"),
    ("dep", "AuthController", "ResetPasswordRequest", "use"),
    ("dep", "MeController", "ChangePasswordRequest", "use"),
    ("dep", "AuthController", "AuthResponse", "use"),
    ("dep", "MeController", "AuthResponse", "use"),
    ("dep", "AuthController", "ActionResponse", "use"),
]
