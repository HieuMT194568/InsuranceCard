// M03 (Hiếu) – SCR-06: chuông thông báo trên thanh tiêu đề (số chưa đọc + 5 thông báo mới nhất)
import { useCallback, useEffect, useState } from 'react';
import { Badge, Dropdown, Spinner } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { notificationApi } from '../api/notificationApi';
import { useAuth } from '../context/AuthContext';
import { dateTime } from '../utils/format';
import { NOTIFICATION_TYPES } from '../utils/statusLabels';

const POLL_MS = 60_000;

export default function NotificationBell() {
  const { basePath } = useAuth();
  const navigate = useNavigate();
  const [count, setCount] = useState(0);
  const [items, setItems] = useState(null);
  const [loading, setLoading] = useState(false);

  const loadCount = useCallback(async () => {
    try {
      const res = await notificationApi.unreadCount();
      setCount(res.count);
    } catch {
      // bỏ qua: chuông không được làm hỏng trang
    }
  }, []);

  useEffect(() => {
    loadCount();
    const t = setInterval(loadCount, POLL_MS);
    return () => clearInterval(t);
  }, [loadCount]);

  const onToggle = async (open) => {
    if (!open) return;
    setLoading(true);
    try {
      const page = await notificationApi.list({ page: 0, size: 5 });
      setItems(page.content);
    } catch {
      setItems([]);
    } finally {
      setLoading(false);
    }
  };

  const openItem = async (n) => {
    try {
      if (!n.read) {
        await notificationApi.markRead(n.id);
        loadCount();
      }
    } catch {
      // vẫn điều hướng
    }
    navigate(n.link || `${basePath}/notifications`);
  };

  const markAll = async () => {
    await notificationApi.markAllRead();
    setItems((list) => list?.map((n) => ({ ...n, read: true })));
    setCount(0);
  };

  return (
    <Dropdown align="end" onToggle={onToggle} className="me-2">
      <Dropdown.Toggle variant="link" className="text-dark position-relative px-2 no-caret" aria-label="Thông báo">
        <i className="bi bi-bell fs-5" />
        {count > 0 && (
          <Badge bg="danger" pill className="position-absolute top-0 start-100 translate-middle" style={{ fontSize: '0.65rem' }}>
            {count > 99 ? '99+' : count}
          </Badge>
        )}
      </Dropdown.Toggle>
      <Dropdown.Menu style={{ width: 340 }} className="py-0">
        <div className="d-flex justify-content-between align-items-center px-3 py-2 border-bottom">
          <strong>Thông báo</strong>
          {count > 0 && <button type="button" className="btn btn-link btn-sm p-0" onClick={markAll}>Đánh dấu tất cả đã đọc</button>}
        </div>
        {loading && <div className="text-center text-muted py-3"><Spinner animation="border" size="sm" /></div>}
        {!loading && items?.length === 0 && <div className="text-center text-muted small py-3">Chưa có thông báo</div>}
        {!loading && items?.map((n) => {
          const [, icon, variant] = NOTIFICATION_TYPES[n.type] || ['', 'bi-info-circle', 'secondary'];
          return (
            <Dropdown.Item key={n.id} onClick={() => openItem(n)} className={`py-2 text-wrap ${n.read ? '' : 'bg-primary-subtle'}`}>
              <div className="d-flex gap-2">
                <i className={`bi ${icon} text-${variant} fs-5`} />
                <div className="small">
                  <div className={n.read ? '' : 'fw-semibold'}>{n.title}</div>
                  <div className="text-muted text-truncate" style={{ maxWidth: 260 }}>{n.content}</div>
                  <div className="text-muted" style={{ fontSize: '0.75rem' }}>{dateTime(n.createdAt)}</div>
                </div>
              </div>
            </Dropdown.Item>
          );
        })}
        <div className="border-top text-center py-2">
          <Link to={`${basePath}/notifications`} className="small">Xem tất cả thông báo</Link>
        </div>
      </Dropdown.Menu>
    </Dropdown>
  );
}
