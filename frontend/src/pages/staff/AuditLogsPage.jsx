// M02 (Hiếu) – SCR-09: Nhật ký hệ thống (UC-25, chỉ ADMIN, chỉ đọc)
import { Alert, Card, Col, Form, Row, Table } from 'react-bootstrap';
import { auditLogApi } from '../../api/auditLogApi';
import { usePaged } from '../../hooks/usePaged';
import { EmptyRow, Loading, PageHeader, Paging, SearchBox, StatusBadge } from '../../components/ui';
import { LABELS, dateTime } from '../../utils/format';

/** Loại đối tượng hay gặp (mục 10.3 SRS); module mới ghi thêm loại khác vẫn tìm được bằng ô từ khoá. */
const ENTITY_TYPES = {
  USER: 'Tài khoản',
  CUSTOMER: 'Khách hàng',
  CONTRACT: 'Hợp đồng',
  PROMOTION: 'Khuyến mãi',
  PUNISHMENT: 'Vi phạm',
  ACCIDENT: 'Tai nạn',
  COMPENSATION: 'Bồi thường',
  ASSESSMENT: 'Giám định',
  SETTING: 'Cấu hình',
};

export default function AuditLogsPage() {
  const paged = usePaged(auditLogApi.search, { q: '', action: '', entityType: '', from: '', to: '' }, 20);
  const { data, loading } = paged;

  return (
    <>
      <PageHeader title="Nhật ký hệ thống" icon="bi-journal-text" subtitle="Các thao tác quan trọng: đăng nhập, thêm / sửa / xoá, đổi trạng thái, thanh toán" />
      <Card>
        <Card.Body>
          <Row className="g-2 mb-3">
            <Col lg={4}><SearchBox value={paged.filters.q} onChange={(v) => paged.setFilter('q', v)} placeholder="Mô tả, email hoặc tên người thực hiện..." /></Col>
            <Col sm={6} lg={2}>
              <Form.Select value={paged.filters.action} onChange={(e) => paged.setFilter('action', e.target.value)} aria-label="Hành động">
                <option value="">Mọi hành động</option>
                {Object.entries(LABELS.auditAction).map(([k, [l]]) => <option key={k} value={k}>{l}</option>)}
              </Form.Select>
            </Col>
            <Col sm={6} lg={2}>
              <Form.Select value={paged.filters.entityType} onChange={(e) => paged.setFilter('entityType', e.target.value)} aria-label="Đối tượng">
                <option value="">Mọi đối tượng</option>
                {Object.entries(ENTITY_TYPES).map(([k, l]) => <option key={k} value={k}>{l}</option>)}
              </Form.Select>
            </Col>
            <Col sm={6} lg={2}>
              <Form.Control type="date" value={paged.filters.from} onChange={(e) => paged.setFilter('from', e.target.value)} aria-label="Từ ngày" title="Từ ngày" />
            </Col>
            <Col sm={6} lg={2}>
              <Form.Control type="date" value={paged.filters.to} onChange={(e) => paged.setFilter('to', e.target.value)} aria-label="Đến ngày" title="Đến ngày" />
            </Col>
          </Row>
          {paged.error && <Alert variant="danger">{paged.error}</Alert>}
          <Table responsive hover size="sm" className="mb-0">
            <thead><tr><th>Thời gian</th><th>Người thực hiện</th><th>Hành động</th><th>Đối tượng</th><th>Mô tả</th><th>IP</th></tr></thead>
            <tbody>
              {loading && !data && <tr><td colSpan={6}><Loading /></td></tr>}
              {data?.content.length === 0 && <EmptyRow colSpan={6} />}
              {data?.content.map((a) => (
                <tr key={a.id}>
                  <td className="text-nowrap">{dateTime(a.createdAt)}</td>
                  <td>{a.userEmail ? <>{a.userFullName}<div className="small text-muted">{a.userEmail}</div></> : <span className="text-muted">Hệ thống / không xác định</span>}</td>
                  <td><StatusBadge type="auditAction" value={a.action} /></td>
                  <td className="text-nowrap">{ENTITY_TYPES[a.entityType] || a.entityType}{a.entityId ? ` #${a.entityId}` : ''}</td>
                  <td>{a.description}</td>
                  <td className="small text-muted">{a.ipAddress || '—'}</td>
                </tr>
              ))}
            </tbody>
          </Table>
          <Paging data={data} onChange={paged.setPage} />
        </Card.Body>
      </Card>
    </>
  );
}
