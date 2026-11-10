// M03 (Hiếu) – SCR-10: danh sách thông báo của người đang đăng nhập (UC-26)
import { useState } from 'react';
import { Alert, Button, Card, Col, Form, ListGroup, Row } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { notificationApi } from '../../api/notificationApi';
import { useToast } from '../../context/ToastContext';
import { usePaged } from '../../hooks/usePaged';
import { Loading, PageHeader, Paging } from '../../components/ui';
import { dateTime } from '../../utils/format';
import { errorMessage } from '../../utils/errors';
import { NOTIFICATION_TYPES } from '../../utils/statusLabels';

export default function NotificationsPage() {
  const toast = useToast();
  const navigate = useNavigate();
  const paged = usePaged(notificationApi.list, { unreadOnly: '' });
  const [busy, setBusy] = useState(false);
  const { data, loading } = paged;

  const open = async (n) => {
    try {
      if (!n.read) await notificationApi.markRead(n.id);
    } catch (e) {
      toast.error(errorMessage(e));
    }
    if (n.link) navigate(n.link);
    else paged.reload();
  };

  const markAll = async () => {
    setBusy(true);
    try {
      const res = await notificationApi.markAllRead();
      toast.success(res.updated > 0 ? `Đã đánh dấu ${res.updated} thông báo là đã đọc` : 'Không có thông báo chưa đọc');
      paged.reload();
    } catch (e) {
      toast.error(errorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <PageHeader title="Thông báo" icon="bi-bell" subtitle="Cập nhật về hợp đồng, bồi thường, vi phạm và hỗ trợ của bạn">
        <Button variant="outline-primary" onClick={markAll} disabled={busy}><i className="bi bi-check2-all me-1" />Đánh dấu tất cả đã đọc</Button>
      </PageHeader>
      <Card>
        <Card.Body>
          <Row className="g-2 mb-3">
            <Col md={4}>
              <Form.Select value={paged.filters.unreadOnly} onChange={(e) => paged.setFilter('unreadOnly', e.target.value)} aria-label="Lọc thông báo">
                <option value="">Tất cả thông báo</option>
                <option value="true">Chỉ thông báo chưa đọc</option>
              </Form.Select>
            </Col>
          </Row>
          {paged.error && <Alert variant="danger">{paged.error}</Alert>}
          {loading && !data && <Loading />}
          {data?.content.length === 0 && <div className="text-center text-muted py-4"><i className="bi bi-inbox me-2" />Không có thông báo</div>}
          <ListGroup variant="flush">
            {data?.content.map((n) => {
              const [typeLabel, icon, variant] = NOTIFICATION_TYPES[n.type] || ['Thông báo', 'bi-info-circle', 'secondary'];
              return (
                <ListGroup.Item key={n.id} action onClick={() => open(n)} className={n.read ? '' : 'bg-primary-subtle'}>
                  <div className="d-flex gap-3">
                    <i className={`bi ${icon} text-${variant} fs-4`} />
                    <div className="flex-grow-1">
                      <div className="d-flex flex-wrap justify-content-between gap-2">
                        <span className={n.read ? '' : 'fw-semibold'}>{n.title}</span>
                        <small className="text-muted">{dateTime(n.createdAt)}</small>
                      </div>
                      <div className="small text-muted">{n.content}</div>
                      <div className="small mt-1">
                        <span className="badge text-bg-light me-2">{typeLabel}</span>
                        {!n.read && <span className="badge text-bg-primary">Mới</span>}
                      </div>
                    </div>
                  </div>
                </ListGroup.Item>
              );
            })}
          </ListGroup>
          <Paging data={data} onChange={paged.setPage} />
          {data?.content.length > 0 && <div className="small text-muted mt-2">Bấm vào thông báo để đánh dấu đã đọc và mở trang liên quan.</div>}
        </Card.Body>
      </Card>
    </>
  );
}
