// M02 (Hiếu) – SCR-38: Quản lý gara liên kết (UC-43). Management 5 API.
import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Form, Modal, Row, Table } from 'react-bootstrap';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { garageApi } from '../../api/garageApi';
import { useToast } from '../../context/ToastContext';
import { usePaged } from '../../hooks/usePaged';
import { Field, SelectField, TextAreaField } from '../../components/form';
import { ConfirmModal, EmptyRow, Loading, PageHeader, Paging, SearchBox, StatusBadge, SubmitButton } from '../../components/ui';
import { garageSchema } from '../../utils/validation/garage';
import { toFormValues } from '../../utils/validation';
import { applyServerErrors, errorMessage } from '../../utils/errors';
import { LABELS } from '../../utils/format';

const FIELDS = ['garageCode', 'name', 'address', 'province', 'phone', 'email', 'status', 'note'];
const STATUS_OPTIONS = { ACTIVE: 'Đang hợp tác', INACTIVE: 'Ngừng hợp tác' };

function GarageFormModal({ show, garage, onHide, onSaved }) {
  const toast = useToast();
  const [error, setError] = useState(null);
  const form = useForm({ resolver: zodResolver(garageSchema) });

  useEffect(() => {
    if (!show) return;
    setError(null);
    form.reset(garage ? toFormValues(garage, FIELDS) : { ...toFormValues({}, FIELDS), status: 'ACTIVE' });
  }, [show, garage]); // eslint-disable-line react-hooks/exhaustive-deps

  const onSubmit = form.handleSubmit(async (values) => {
    setError(null);
    try {
      const saved = garage ? await garageApi.update(garage.id, values) : await garageApi.create(values);
      toast.success(garage ? 'Đã cập nhật gara' : `Đã thêm gara ${saved.garageCode}`);
      onSaved();
    } catch (e) {
      setError(applyServerErrors(e, form.setError));
    }
  });

  return (
    <Modal show={show} onHide={onHide} size="lg" centered backdrop="static">
      <Form noValidate onSubmit={onSubmit}>
        <Modal.Header closeButton><Modal.Title as="h5">{garage ? `Sửa gara ${garage.garageCode}` : 'Thêm gara liên kết'}</Modal.Title></Modal.Header>
        <Modal.Body>
          {error && <Alert variant="danger">{error}</Alert>}
          <Row>
            <Col md={4}><Field form={form} name="garageCode" label="Mã gara" required maxLength={20} help="VD: GR-HCM-03" /></Col>
            <Col md={8}><Field form={form} name="name" label="Tên gara" required maxLength={150} /></Col>
            <Col md={8}><Field form={form} name="address" label="Địa chỉ" required maxLength={255} /></Col>
            <Col md={4}><Field form={form} name="province" label="Tỉnh / thành phố" required maxLength={50} /></Col>
            <Col md={4}><Field form={form} name="phone" label="Số điện thoại" required maxLength={10} inputMode="numeric" /></Col>
            <Col md={4}><Field form={form} name="email" label="Email" maxLength={100} type="email" /></Col>
            <Col md={4}><SelectField form={form} name="status" label="Trạng thái" required placeholder={null} options={STATUS_OPTIONS} /></Col>
            <Col md={12}><TextAreaField form={form} name="note" label="Ghi chú nội bộ" maxLength={500} rows={2} /></Col>
          </Row>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="light" onClick={onHide}>Đóng</Button>
          <SubmitButton busy={form.formState.isSubmitting}>Lưu</SubmitButton>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}

export default function GaragesPage() {
  const toast = useToast();
  const paged = usePaged(garageApi.search, { q: '', province: '', status: '' });
  const [editing, setEditing] = useState(undefined);
  const [deleting, setDeleting] = useState(null);
  const [busy, setBusy] = useState(false);
  const { data, loading } = paged;

  const confirmDelete = async () => {
    setBusy(true);
    try {
      await garageApi.remove(deleting.id);
      toast.success('Đã xoá gara');
      paged.reload();
    } catch (e) {
      toast.error(errorMessage(e));
    } finally {
      setBusy(false);
      setDeleting(null);
    }
  };

  return (
    <>
      <PageHeader title="Gara liên kết" icon="bi-tools" subtitle="Danh mục gara sửa chữa dùng khi giám định bồi thường">
        <Button onClick={() => setEditing(null)}><i className="bi bi-plus-lg me-1" />Thêm gara</Button>
      </PageHeader>
      <Card>
        <Card.Body>
          <Row className="g-2 mb-3">
            <Col md={5}><SearchBox value={paged.filters.q} onChange={(v) => paged.setFilter('q', v)} placeholder="Mã, tên, địa chỉ hoặc số điện thoại..." /></Col>
            <Col md={4}><SearchBox value={paged.filters.province} onChange={(v) => paged.setFilter('province', v)} placeholder="Tỉnh / thành phố..." maxLength={50} /></Col>
            <Col md={3}>
              <Form.Select value={paged.filters.status} onChange={(e) => paged.setFilter('status', e.target.value)} aria-label="Trạng thái">
                <option value="">Tất cả trạng thái</option>
                {Object.entries(LABELS.garage).map(([k, [l]]) => <option key={k} value={k}>{l}</option>)}
              </Form.Select>
            </Col>
          </Row>
          {paged.error && <Alert variant="danger">{paged.error}</Alert>}
          <Table responsive hover className="mb-0">
            <thead><tr><th>Mã</th><th>Tên gara</th><th>Địa chỉ</th><th>Tỉnh / thành</th><th>Liên hệ</th><th>Trạng thái</th><th className="text-end">Thao tác</th></tr></thead>
            <tbody>
              {loading && !data && <tr><td colSpan={7}><Loading /></td></tr>}
              {data?.content.length === 0 && <EmptyRow colSpan={7} />}
              {data?.content.map((g) => (
                <tr key={g.id}>
                  <td className="fw-medium text-nowrap">{g.garageCode}</td>
                  <td>{g.name}{g.note && <div className="small text-muted text-truncate" style={{ maxWidth: 260 }}>{g.note}</div>}</td>
                  <td>{g.address}</td>
                  <td className="text-nowrap">{g.province}</td>
                  <td className="text-nowrap">{g.phone}{g.email && <div className="small text-muted">{g.email}</div>}</td>
                  <td><StatusBadge type="garage" value={g.status} /></td>
                  <td className="text-end text-nowrap">
                    <Button size="sm" variant="outline-primary" className="me-1" onClick={() => setEditing(g)} title="Sửa"><i className="bi bi-pencil" /></Button>
                    <Button size="sm" variant="outline-danger" onClick={() => setDeleting(g)} title="Xoá"><i className="bi bi-trash" /></Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
          <Paging data={data} onChange={paged.setPage} />
        </Card.Body>
      </Card>
      <GarageFormModal show={editing !== undefined} garage={editing} onHide={() => setEditing(undefined)}
        onSaved={() => { setEditing(undefined); paged.reload(); }} />
      <ConfirmModal show={!!deleting} title="Xoá gara" busy={busy} confirmText="Xoá" onHide={() => setDeleting(null)} onConfirm={confirmDelete}>
        Xoá gara <strong>{deleting?.name}</strong>? Gara đã dùng trong biên bản giám định không thể xoá — hãy chuyển sang "Ngừng hợp tác".
      </ConfirmModal>
    </>
  );
}
