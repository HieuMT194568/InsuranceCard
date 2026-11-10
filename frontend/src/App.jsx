import { Navigate, Route, Routes } from 'react-router-dom';
import { DashboardLayout, GuestOnly, PublicLayout, RequireRole } from './components/layouts';

/*
 * QUY ƯỚC (SRS v2 mục 3.3): file này do Hiếu giữ. Mỗi thành viên chỉ thêm import / route trong
 * khối có tên mình, không sửa route của người khác. Route khách hàng & nhân viên dùng chung đặt
 * trong `sharedRoutes`; route chỉ nhân viên trong `staffRoutes`; trang công khai trong `publicRoutes`.
 */

// ============================================================ HIẾU (M01 xác thực, M02 quản trị & gara, M03 thông báo)
import Login from './pages/public/Login';
import Register from './pages/public/Register';
import VerifyEmail from './pages/public/VerifyEmail';
import ForgotPassword from './pages/public/ForgotPassword';
import ResetPassword from './pages/public/ResetPassword';
import NotFound from './pages/public/NotFound';
import PublicGaragesPage from './pages/public/PublicGaragesPage';
import NotificationsPage from './pages/shared/NotificationsPage';
import SettingsPage from './pages/staff/SettingsPage';
import StaffAccountsPage from './pages/staff/StaffAccountsPage';
import AuditLogsPage from './pages/staff/AuditLogsPage';
import GaragesPage from './pages/staff/GaragesPage';
// ============================================================ QUYÊN (M04 khách hàng & hồ sơ, M05 xe, M06 hỗ trợ)
import Profile from './pages/shared/Profile';
import VehiclesPage from './pages/shared/VehiclesPage';
import CustomersPage from './pages/staff/CustomersPage';
import CustomerFormPage from './pages/staff/CustomerFormPage';
import CustomerDetailPage from './pages/staff/CustomerDetailPage';
// ============================================================ MINH (M07 sản phẩm & báo giá, M08 hợp đồng, M09 khuyến mãi)
import Home from './pages/public/Home';
import ContractsPage from './pages/shared/ContractsPage';
import ContractCreatePage from './pages/shared/ContractCreatePage';
import ContractDetailPage from './pages/shared/ContractDetailPage';
import ProductsPage from './pages/staff/ProductsPage';
// ============================================================ KHANG (M10 thanh toán & báo cáo, M11 vi phạm, M12 giấy chứng nhận)
import CustomerDashboard from './pages/customer/CustomerDashboard';
import StaffDashboard from './pages/staff/StaffDashboard';
import PaymentsPage from './pages/shared/PaymentsPage';
import PunishmentsPage from './pages/shared/PunishmentsPage';
// ============================================================ HOÀNG (M13 tai nạn, M14 bồi thường, M15 giám định)
import AccidentsPage from './pages/shared/AccidentsPage';
import CompensationsPage from './pages/shared/CompensationsPage';
import CompensationRequestPage from './pages/shared/CompensationRequestPage';

/** Trang công khai (không cần đăng nhập). */
const publicRoutes = (
  <>
    {/* === HIẾU === */}
    <Route path="verify-email" element={<VerifyEmail />} />
    <Route path="reset-password" element={<ResetPassword />} />
    <Route path="garages" element={<PublicGaragesPage />} />
    {/* === QUYÊN === */}
    {/* === MINH === (vd: quote, promotions) */}
    {/* === KHANG === (vd: certificate-lookup, verify-certificate/:token) */}
    {/* === HOÀNG === */}
  </>
);

/** Màn hình dùng chung cho khách hàng và nhân viên (hiển thị / quyền khác nhau theo vai trò). */
const sharedRoutes = (
  <>
    {/* === HIẾU === */}
    <Route path="notifications" element={<NotificationsPage />} />
    {/* === QUYÊN === */}
    <Route path="profile" element={<Profile />} />
    <Route path="vehicles" element={<VehiclesPage />} />
    {/* === MINH === */}
    <Route path="contracts" element={<ContractsPage />} />
    <Route path="contracts/new" element={<ContractCreatePage />} />
    <Route path="contracts/:id" element={<ContractDetailPage />} />
    {/* === KHANG === */}
    <Route path="payments" element={<PaymentsPage />} />
    <Route path="punishments" element={<PunishmentsPage />} />
    {/* === HOÀNG === */}
    <Route path="accidents" element={<AccidentsPage />} />
    <Route path="compensations" element={<CompensationsPage />} />
    <Route path="compensations/new" element={<CompensationRequestPage />} />
  </>
);

/** Màn hình chỉ dành cho nhân viên (ADMIN cũng vào được). Trang chỉ ADMIN bọc trong RequireRole roles={['ADMIN']}. */
const staffRoutes = (
  <>
    {/* === HIẾU === */}
    <Route path="garages" element={<GaragesPage />} />
    <Route element={<RequireRole roles={['ADMIN']} />}>
      <Route path="staff-accounts" element={<StaffAccountsPage />} />
      <Route path="audit-logs" element={<AuditLogsPage />} />
      <Route path="settings" element={<SettingsPage />} />
    </Route>
    {/* === QUYÊN === */}
    <Route path="customers" element={<CustomersPage />} />
    <Route path="customers/new" element={<CustomerFormPage />} />
    <Route path="customers/:id" element={<CustomerDetailPage />} />
    <Route path="customers/:id/edit" element={<CustomerFormPage />} />
    {/* === MINH === */}
    <Route path="products" element={<ProductsPage />} />
    {/* === KHANG === */}
    {/* === HOÀNG === */}
  </>
);

export default function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<Home />} />
        <Route element={<GuestOnly />}>
          <Route path="login" element={<Login />} />
          <Route path="register" element={<Register />} />
          <Route path="forgot-password" element={<ForgotPassword />} />
        </Route>
        {publicRoutes}
      </Route>

      <Route path="customer" element={<RequireRole role="CUSTOMER" />}>
        <Route element={<DashboardLayout />}>
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<CustomerDashboard />} />
          {sharedRoutes}
        </Route>
      </Route>

      <Route path="staff" element={<RequireRole roles={['STAFF', 'ADMIN']} />}>
        <Route element={<DashboardLayout />}>
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<StaffDashboard />} />
          {staffRoutes}
          {sharedRoutes}
        </Route>
      </Route>

      <Route path="*" element={<PublicLayout />}>
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}
