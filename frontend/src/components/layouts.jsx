import { useState } from 'react';
import { Container, Nav, Navbar, NavDropdown, Offcanvas } from 'react-bootstrap';
import { Link, Navigate, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { menuFor } from './menu';
import NotificationBell from './NotificationBell';

const ROLE_LABELS = { CUSTOMER: 'Khách hàng', STAFF: 'Nhân viên', ADMIN: 'Quản trị viên' };

function Brand() {
  return (
    <Navbar.Brand as={Link} to="/" className="fw-bold">
      <i className="bi bi-shield-check text-primary me-1" />InsuranceCard
    </Navbar.Brand>
  );
}

export function PublicLayout() {
  const { user, basePath } = useAuth();
  return (
    <>
      <Navbar bg="white" expand="md" className="border-bottom sticky-top">
        <Container>
          <Brand />
          <Navbar.Toggle aria-controls="public-nav" />
          <Navbar.Collapse id="public-nav">
            <Nav className="ms-auto align-items-md-center gap-md-2">
              <Nav.Link as={NavLink} to="/" end>Trang chủ</Nav.Link>
              <Nav.Link as={NavLink} to="/garages">Gara liên kết</Nav.Link>
              {user ? (
                <Link to={`${basePath}/dashboard`} className="btn btn-primary btn-sm">
                  <i className="bi bi-speedometer2 me-1" />Vào trang quản lý
                </Link>
              ) : (
                <>
                  <Nav.Link as={NavLink} to="/login">Đăng nhập</Nav.Link>
                  <Link to="/register" className="btn btn-primary btn-sm">Đăng ký</Link>
                </>
              )}
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
      <Outlet />
      <footer className="border-top bg-white py-4 mt-5">
        <Container className="text-muted small d-flex flex-wrap justify-content-between gap-2">
          <span>© {new Date().getFullYear()} InsuranceCard – Bảo hiểm xe máy trực tuyến</span>
          <span>Hotline: 1900 1234 · hotro@insurancecard.vn</span>
        </Container>
      </footer>
    </>
  );
}

/** Menu lấy từ components/menu.js (mỗi thành viên thêm mục trong khối của mình). */
function SideMenu({ role, basePath, onNavigate }) {
  return (
    <Nav className="flex-column p-2">
      {menuFor(role).map((group) => (
        <div key={group.heading}>
          <div className="sidebar-heading">{group.heading}</div>
          {group.items.map((m) => (
            <Nav.Link key={m.to} as={NavLink} to={`${basePath}/${m.to}`} end={m.end} onClick={onNavigate}>
              <i className={`bi ${m.icon}`} />{m.label}
            </Nav.Link>
          ))}
        </div>
      ))}
    </Nav>
  );
}

export function DashboardLayout() {
  const { user, basePath, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const doLogout = () => { logout(); navigate('/login'); };

  return (
    <>
      <Navbar bg="white" className="border-bottom sticky-top" style={{ height: 56 }}>
        <Container fluid>
          <button type="button" className="btn btn-link text-dark d-lg-none me-1 p-1" onClick={() => setOpen(true)} aria-label="Mở menu">
            <i className="bi bi-list fs-4" />
          </button>
          <Brand />
          <span className="badge bg-primary-subtle text-primary d-none d-sm-inline">
            {ROLE_LABELS[user.role] || user.role}
          </span>
          <Nav className="ms-auto align-items-center">
            <NotificationBell />
            <NavDropdown align="end" title={<span><i className="bi bi-person-circle me-1" />{user.fullName}</span>}>
              <NavDropdown.Item as={Link} to={`${basePath}/profile`}><i className="bi bi-person me-2" />Hồ sơ cá nhân</NavDropdown.Item>
              <NavDropdown.Item as={Link} to="/"><i className="bi bi-house me-2" />Trang chủ</NavDropdown.Item>
              <NavDropdown.Divider />
              <NavDropdown.Item onClick={doLogout} className="text-danger"><i className="bi bi-box-arrow-right me-2" />Đăng xuất</NavDropdown.Item>
            </NavDropdown>
          </Nav>
        </Container>
      </Navbar>
      <div className="d-flex">
        <aside className="sidebar d-none d-lg-block flex-shrink-0">
          <SideMenu role={user.role} basePath={basePath} />
        </aside>
        <Offcanvas show={open} onHide={() => setOpen(false)} style={{ width: 270 }}>
          <Offcanvas.Header closeButton><Offcanvas.Title>Menu</Offcanvas.Title></Offcanvas.Header>
          <Offcanvas.Body className="p-0">
            <SideMenu role={user.role} basePath={basePath} onNavigate={() => setOpen(false)} />
          </Offcanvas.Body>
        </Offcanvas>
        <main className="content-area flex-grow-1 p-3 p-md-4">
          <Outlet />
        </main>
      </div>
    </>
  );
}

/**
 * Chặn truy cập theo vai trò: chưa đăng nhập -> /login; sai vai trò -> dashboard của mình.
 * `role` hoặc `roles` (mảng). ADMIN kế thừa STAFF: route cho STAFF thì ADMIN cũng vào được.
 */
export function RequireRole({ role, roles }) {
  const { user, basePath } = useAuth();
  const location = useLocation();
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
  const allowed = roles || [role];
  const ok = allowed.includes(user.role) || (user.role === 'ADMIN' && allowed.includes('STAFF'));
  if (!ok) return <Navigate to={`${basePath}/dashboard`} replace />;
  return <Outlet />;
}

/** Trang chỉ dành cho khách chưa đăng nhập (login/register). */
export function GuestOnly() {
  const { user, basePath } = useAuth();
  if (user) return <Navigate to={`${basePath}/dashboard`} replace />;
  return <Outlet />;
}
