# Insurance Card – Hệ thống bảo hiểm xe máy

Full-stack CRUD theo tài liệu *InsuranceCard_Requirements*:

| Tầng | Công nghệ |
|---|---|
| Frontend | React 19 + **React Bootstrap** + React Router + React Hook Form + Zod (Vite) |
| Backend (API) | **Java 17**, Spring Boot 3.5 (Web, Data JPA, Validation, Security + JWT), Flyway, Swagger UI |
| Database | **MySQL 8** (mặc định) hoặc **SQL Server** (đổi bằng biến môi trường `DB_VENDOR=sqlserver`) |

Đặc tả use case, quy tắc nghiệp vụ và toàn bộ quy tắc validate: [docs/USE_CASES_AND_VALIDATION.md](docs/USE_CASES_AND_VALIDATION.md).

**SRS v2.0 (môn SWD392 – chia 15 module cho 5 thành viên):** [docs/SRS_InsuranceCard_v2.docx](docs/SRS_InsuranceCard_v2.docx). Trước khi code, mỗi người đọc mục 1–4 và chương của mình; quy ước làm việc nhóm tóm tắt ở mục 7 bên dưới.

## 1. Chạy dự án

Yêu cầu: JDK 17+, Node.js 20+, MySQL 8 đang chạy (hoặc SQL Server). Maven **không cần cài** (dùng `mvnw`).

### Backend – http://localhost:8080

Lần đầu: khai báo tài khoản MySQL của máy bạn (file này không được đẩy lên git):

```bash
cp backend/src/main/resources/application-local.example.yml backend/src/main/resources/application-local.yml
```

Sửa `DB_USERNAME` / `DB_PASSWORD` trong file vừa tạo (hoặc đặt biến môi trường cùng tên), rồi chạy:

```bash
cd backend
./mvnw spring-boot:run
```

- Mặc định kết nối `jdbc:mysql://localhost:3306/insurance_card` (đổi bằng `DB_URL`).
- Lần chạy đầu: tự tạo database, Flyway tạo bảng, rồi nạp dữ liệu demo.
- Swagger UI: http://localhost:8080/swagger-ui.html

### Frontend – http://localhost:5173

```bash
cd frontend
npm install
npm run dev
```

Vite proxy `/api` sang `http://localhost:8080`.

### Chạy với SQL Server

1. Chạy `database/sqlserver/00_create_database.sql` (tạo database `InsuranceCard`).
2. Bật TCP/IP cổng 1433 và SQL Server Authentication.
3. Chạy backend:

```powershell
$env:DB_VENDOR="sqlserver"; $env:DB_USERNAME="sa"; $env:DB_PASSWORD="<mật khẩu>"; ./mvnw spring-boot:run
```

Flyway dùng `db/migration/sqlserver` (NVARCHAR, DATETIME2, IDENTITY). Nếu muốn tạo bảng thủ công, chạy thêm `database/sqlserver/01_schema_and_settings.sql`.

## 2. Tài khoản demo

| Vai trò | Email | Mật khẩu |
|---|---|---|
| Quản trị viên (ADMIN) | `admin@insurancecard.vn` | `Admin@123` |
| Nhân viên | `staff@insurancecard.vn` | `Staff@123` |
| Khách hàng (có đủ dữ liệu mẫu) | `customer@insurancecard.vn` | `Customer@123` |
| Khách hàng | `lan.nguyen@gmail.com` | `Customer@123` |
| Chưa xác thực email | `pending.user@gmail.com` | `Customer@123` |
| Bị khoá | `locked.user@gmail.com` | `Customer@123` |

Email ở **chế độ demo** (`MAIL_ENABLED=false`): link xác thực / đặt lại mật khẩu được ghi log và hiển thị ngay trên giao diện.
Gửi mail thật: đặt `MAIL_ENABLED=true`, `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`.

## 3. Kiểm thử tự động

Bộ 241 case API (xác thực, phân quyền, CRUD, validate, quy tắc nghiệp vụ), chạy khi backend đang chạy, chạy lặp lại được:

```bash
node tests/api-test.mjs
```

Test của từng module mới đặt ở `tests/<module>.test.mjs`, ví dụ phần của Hiếu (vai trò ADMIN, tài khoản nhân viên, nhật ký, gara, thông báo):

```bash
node tests/hieu.test.mjs
```

## 4. Chức năng

**Chung:** trang chủ + gói bảo hiểm, đăng ký (xác thực email), đăng nhập, quên/đặt lại mật khẩu, đổi mật khẩu, phân quyền theo vai trò.

**Khách hàng** (dashboard): hồ sơ; CRUD xe; mua bảo hiểm; thanh toán; gia hạn; huỷ (hoàn phí); khai báo tai nạn; yêu cầu bồi thường; nộp phạt; lịch sử thanh toán, tai nạn, bồi thường, vi phạm, hợp đồng.

**Nhân viên** (dashboard): CRUD khách hàng (khoá/mở khoá, đặt lại mật khẩu); CRUD xe; tạo/sửa/gia hạn/huỷ hợp đồng, thu tiền; xử lý tai nạn; duyệt/từ chối/chi trả bồi thường; lập/sửa/xoá/miễn/thu tiền phạt; CRUD sản phẩm bảo hiểm; CRUD gara liên kết; xem giao dịch.

**Quản trị viên (ADMIN – mới ở v2)**: mọi chức năng của nhân viên + quản lý tài khoản nhân viên, nhật ký hệ thống, cấu hình hệ thống.

**Mới ở v2 cho mọi người dùng:** chuông thông báo + trang thông báo; nhắc hợp đồng sắp hết hạn (08:00 hằng ngày); trang công khai "Gara liên kết".

## 5. Cấu trúc thư mục

```
backend/                     Mô hình MVC (View = frontend React, giao tiếp JSON qua /api)
  src/main/java/com/insurancecard/
    controller/    (C) @RestController: nhận request, validate DTO, gọi service, trả JSON
    model/         (M)
      entity/        Entity JPA (bảng dữ liệu)
      enums/         Trạng thái, vai trò, loại...
      dto/           Request/Response + annotation validate (dữ liệu trả cho View)
    service/       Interface nghiệp vụ (controller chỉ phụ thuộc interface)
      impl/          Cài đặt nghiệp vụ + validate quy tắc (@Service, @Transactional)
    repository/    Spring Data JPA (truy cập CSDL)
    mapper/        DtoMapper: entity -> DTO (không bao giờ trả passwordHash)
    scheduler/     Tự động cập nhật trạng thái hợp đồng, nhắc hợp đồng sắp hết hạn
    config/        Security, Jackson (trim chuỗi), dữ liệu demo, Swagger
      seed/          Seeder dữ liệu demo của từng module (AdminSeeder, GarageSeeder...)
    security/      JWT filter, người dùng hiện tại
    validation/    Regex dùng chung, @AgeBetween, @RawString
    common/        ApiException, GlobalExceptionHandler, phân trang, sinh mã, chuẩn hoá chuỗi
  src/main/resources/db/migration/{mysql,sqlserver}/   Flyway
frontend/src/
  api/             axios client + services.js (module v1) + <module>Api.js (module mới)
  context/         AuthContext (JWT), ToastContext
  components/      Layout, menu.js (menu theo khối từng người), form field, picker, modal dùng chung
  pages/           public / customer / staff / shared
  utils/           validation.js + validation/<module>.js (zod – khớp backend), format, statusLabels, errors
database/          Script SQL độc lập cho MySQL và SQL Server
docs/              Đặc tả use case & validate
tests/             Bộ test API tự động
```

## 6. API chính

| Nhóm | Endpoint |
|---|---|
| Auth | `POST /api/auth/{login,register,verify-email,resend-verification,forgot-password,reset-password}` |
| Cá nhân | `GET /api/me`, `PUT /api/me/profile`, `PUT /api/me/staff-profile`, `POST /api/me/change-password`, `GET /api/dashboard/{customer,staff}` |
| Khách hàng | `GET/POST /api/customers`, `GET/PUT/DELETE /api/customers/{id}`, `PATCH /{id}/status`, `POST /{id}/set-password` |
| Xe | `GET/POST /api/vehicles`, `GET/PUT/DELETE /api/vehicles/{id}` |
| Sản phẩm | `GET /api/products/public`, `GET/POST /api/products`, `GET/PUT/DELETE /api/products/{id}` |
| Hợp đồng | `GET/POST /api/contracts`, `GET/PUT /api/contracts/{id}`, `POST /{id}/pay`, `/{id}/renew`, `/{id}/cancel` |
| Giao dịch | `GET /api/payments`, `GET /api/payments/{id}` (chỉ đọc: giao dịch sinh từ nghiệp vụ) |
| Tai nạn | `GET/POST /api/accidents`, `GET/PUT/DELETE /api/accidents/{id}`, `POST /{id}/resolve` |
| Bồi thường | `GET/POST /api/compensations`, `GET/PUT/DELETE /api/compensations/{id}` (sửa/rút khi chờ duyệt), `POST /{id}/approve`, `/{id}/reject`, `/{id}/payout` |
| Phạt | `GET/POST /api/punishments`, `GET/PUT/DELETE /api/punishments/{id}`, `POST /{id}/pay`, `/{id}/waive` |
| Cấu hình (ADMIN) | `GET /api/settings`, `GET/PUT /api/settings/{key}` |
| Tài khoản nhân viên (ADMIN) | `GET/POST /api/staff-accounts`, `GET/PUT/DELETE /api/staff-accounts/{id}` |
| Nhật ký hệ thống (ADMIN) | `GET /api/audit-logs` (q, action, entityType, from, to) |
| Gara liên kết | `GET/POST /api/garages`, `GET/PUT/DELETE /api/garages/{id}` (nhân viên), `GET /api/public/garages` (công khai) |
| Thông báo | `GET /api/notifications`, `GET /api/notifications/unread-count`, `POST /api/notifications/{id}/read`, `POST /api/notifications/read-all` |

Danh sách đều hỗ trợ `q` (tìm kiếm), lọc trạng thái, `page` (từ 0) và `size` (1–100).

API công khai (không cần đăng nhập) của mọi module đặt dưới `/api/public/**`.

## 7. Làm việc nhóm theo SRS v2

| Thành viên | Module | Dải Flyway | Seeder `@Order` |
|---|---|---|---|
| Hiếu | M01 Xác thực, M02 Quản trị hệ thống & gara, M03 Thông báo | V10 – V19 | 10 – 19 |
| Quyên | M04 Khách hàng, M05 Xe, M06 Hỗ trợ khách hàng | V20 – V29 | 20 – 29 |
| Minh | M07 Sản phẩm & báo giá, M08 Hợp đồng, M09 Khuyến mãi | V30 – V39 | 30 – 39 |
| Khang | M10 Thanh toán & báo cáo, M11 Vi phạm, M12 Giấy chứng nhận | V40 – V49 | 40 – 49 |
| Hoàng | M13 Tai nạn, M14 Bồi thường, M15 Giám định | V50 – V59 | 50 – 59 |

- **Nhánh:** `feature/<ten>-<module>` → Pull Request vào `main`. Commit có mã module, ví dụ `[M06] Thêm API sửa yêu cầu hỗ trợ`.
- **Migration:** chỉ tạo file trong dải của mình, viết cả `mysql/` và `sqlserver/`, không sửa file đã merge (`spring.flyway.out-of-order` đã bật).
- **File dùng chung có khối theo tên:** `frontend/src/App.jsx` (import + route), `components/menu.js` (menu), `utils/statusLabels.js` (nhãn trạng thái). Chỉ thêm dòng trong khối của mình.
- **Module mới:** API frontend ở `api/<module>Api.js`, schema zod ở `utils/validation/<module>.js`, mapper ở `mapper/<Module>Mapper.java`, seed ở `config/seed/<Module>Seeder.java`, test ở `tests/<module>.test.mjs`.
- **Ghi dữ liệu của người khác** chỉ qua Service của họ. Các hàm dùng chung đã có sẵn:
  - `AuditLogService.record(action, entityType, entityId, description)` – ghi nhật ký (M02, Hiếu)
  - `NotificationService.notify(userId, type, title, content, link, refKey)` – tạo thông báo (M03, Hiếu)
  - `GarageService.find(id)` – lấy gara cho biên bản giám định (M02, Hiếu)
  - `ContractService.findExpiringWithoutRenewal(from, to)` – hợp đồng sắp hết hạn (M08, Minh)
  - `CertificateService.issueFor / revokeByContract / findIdByContract` – **bản rỗng**, Khang thay bằng logic thật (M12)
- **Phân quyền:** `ADMIN > STAFF` (RoleHierarchy) – API `hasRole('STAFF')` tự cho ADMIN dùng; route `/staff/*` của frontend cũng vậy.
