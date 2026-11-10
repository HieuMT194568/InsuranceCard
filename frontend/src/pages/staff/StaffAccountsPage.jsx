// M02 (Hiếu) – SCR-08: Quản lý tài khoản nhân viên / quản trị viên (UC-24, chỉ ADMIN)
import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Form, InputGroup, Modal, Row, Table } from 'react-bootstrap';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { staffAccountApi } from '../../api/staffAccountApi';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { usePaged } from '../../hooks/usePaged';
import { Field, SelectField } from '../../components/form';
import { ConfirmModal, EmptyRow, Loading, PageHeader, Paging, SearchBox, StatusBadge, SubmitButton } from '../../components/ui';
import { staffAccountCreateSchema, staffAccountUpdateSchema } from '../../utils/validation/staffAccount';
import { toFormValues } from '../../utils/validation';
import { applyServerErrors, errorMessage } from '../../utils/errors';
import { date } from '../../utils/format';

const ROLE_OPTIONS = { STAFF: 'Nhân viên', ADMIN: 'Quản trị viên' };
const STATUS_OPTIONS = { ACTIVE: 'Hoạt động', LOCKED: 'Bị khoá' };

function StaffFormModal({ show, account, onHide, onSaved }) {
  const toast = useToast();
  const [error, setError] = useState(null);
  const isEdit = !!account;
  const form = useForm({ resolver: zodResolver(isEdit ? staffAccountUpdateSchema : staffAccountCreateSchema) });

  useEffect(() => {
    if (!show) return;
    setError(null);
    form.reset(isEdit
      ? toFormValues(account, ['fullName', 'phone', 'role', 'status'])
      : { fullName: '', email: '', phone: '', role: 'STAFF' });
  }, [show, account]); // eslint-disable-line react-hooks/exhaustive-deps

  const onSubmit = form.handleSubmit(async (values) => {
    setError(null);
    try {
      if (isEdit) {
        await staffAccountApi.update(account.id, values);
        toast.success('Đã cập nhật tài khoản');
        onSaved(null);
      } else {
        const created = await staffAccountApi.create(values);
        onSaved(created);
      }
    } catch (e) {
      setError(applyServerErrors(e, form.setError));
    }
  });

  return (
    <Modal show={show} onHide={onHide} centered backdrop="static">
      <Form noValidate onSubmit={onSubmit}>
        <Modal.Header closeButton><Modal.Title as="h5">{isEdit ? `Sửa tài khoản ${account.email}` : 'Thêm tài khoản nhân viên'}</Modal.Title></Modal.Header>
        <Modal.Body>
          {error && <Alert variant="danger">{error}</Alert>}
          <Field form={form} name="fullName" label="Họ tên" required maxLength={100} />
          {!isEdit && <Field form={form} name="email" label="Email đăng nhập" required maxLength={100} type="email" />}
          {isEdit && (
            <Form.Group className="mb-3">
              <Form.Label>Email đăng nhập</Form.Label>
              <Form.Control value={account.email} disabled readOnly />
              <Form.Text muted>Không sửa được email (định danh đăng nhập).</Form.Text>
            </Form.Group>
          )}
          <Field form={form} name="phone" label="Số điện thoại" required maxLength={10} inputMode="numeric" />
          <Row>
            <Col><SelectField form={form} name="role" label="Vai trò" required placeholder={null} options={ROLE_OPTIONS} /></Col>
            {isEdit && <Col><SelectField form={form} name="status" label="Trạng thái" required placeholder={null} options={STATUS_OPTIONS} /></Col>}
          </Row>
          {!isEdit && <Alert variant="info" className="small py-2 mb-0">Tài khoản được kích hoạt ngay với mật khẩu tạm (hiển thị 1 lần sau khi tạo và gửi qua email).</Alert>}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="light" onClick={onHide}>Đóng</Button>
          <SubmitButton busy={form.formState.isSubmitting}>Lưu</SubmitButton>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}

function TempPasswordModal({ created, onHide }) {
  const toast = useToast();
  if (!created) return null;
  const copy = async () => {
    try {
      await navigator.clipboard.writeText(created.temporaryPassword);
      toast.success('Đã sao chép mật khẩu tạm');
    } catch {
      toast.error('Không sao chép được, hãy chép thủ công');
    }
  };
  return (
    <Modal show onHide={onHide} centered>
      <Modal.Header closeButton><Modal.Title as="h5">Đã tạo tài khoản</Modal.Title></Modal.Header>
      <Modal.Body>
        <p className="mb-2">Tài khoản <strong>{created.account.email}</strong> đã được tạo. Mật khẩu tạm chỉ hiển thị <strong>1 lần</strong>:</p>
        <InputGroup>
          <Form.Control value={created.temporaryPassword} readOnly className="font-monospace" />
          <Button variant="outline-primary" onClick={copy}><i className="bi bi-clipboard me-1" />Sao chép</Button>
        </InputGroup>
        <Form.Text muted>Yêu cầu nhân viên đổi mật khẩu ngay sau lần đăng nhập đầu tiên.</Form.Text>
      </Modal.Body>
      <Modal.Footer><Button onClick={onHide}>Đã lưu mật khẩu</Button></Modal.Footer>
    </Modal>
  );
}

export default function StaffAccountsPage() {
  const { user } = useAuth();
  const toast = useToast();
  const paged = usePaged(staffAccountApi.search, { q: '', role: '', status: '' });
  const [editing, setEditing] = useState(undefined);
  const [created, setCreated] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [busy, setBusy] = useState(false);
  const { data, loading } = paged;

  const confirmDelete = async () => {
    setBusy(true);
    try {
      await staffAccountApi.remove(deleting.id);
      toast.success('Đã xoá tài khoản');
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
      <PageHeader title="Tài khoản nhân viên" icon="bi-person-badge" subtitle="Quản lý tài khoản nhân viên và quản trị viên (chỉ quản trị viên)">
        <Button onClick={() => setEditing(null)}><i className="bi bi-plus-lg me-1" />Thêm tài khoản</Button>
      </PageHeader>
      <Card>
        <Card.Body>
          <Row className="g-2 mb-3">
            <Col md={6}><SearchBox value={paged.filters.q} onChange={(v) => paged.setFilter('q', v)} placeholder="Họ tên, email hoặc số điện thoại..." /></Col>
            <Col md={3}>
              <Form.Select value={paged.filters.role} onChange={(e) => paged.setFilter('role', e.target.value)} aria-label="Vai trò">
                <option value="">Tất cả vai trò</option>
                {Object.entries(ROLE_OPTIONS).map(([k, l]) => <option key={k} value={k}>{l}</option>)}
              </Form.Select>
            </Col>
            <Col md={3}>
              <Form.Select value={paged.filters.status} onChange={(e) => paged.setFilter('status', e.target.value)} aria-label="Trạng thái">
                <option value="">Tất cả trạng thái</option>
                {Object.entries(STATUS_OPTIONS).map(([k, l]) => <option key={k} value={k}>{l}</option>)}
              </Form.Select>
            </Col>
          </Row>
          {paged.error && <Alert variant="danger">{paged.error}</Alert>}
          <Table responsive hover className="mb-0">
            <thead><tr><th>Họ tên</th><th>Email</th><th>Số điện thoại</th><th>Vai trò</th><th>Trạng thái</th><th>Ngày tạo</th><th className="text-end">Thao tác</th></tr></thead>
            <tbody>
              {loading && !data && <tr><td colSpan={7}><Loading /></td></tr>}
              {data?.content.length === 0 && <EmptyRow colSpan={7} />}
              {data?.content.map((a) => (
                <tr key={a.id}>
                  <td className="fw-medium">{a.fullName}{a.id === user.id && <span className="badge text-bg-light ms-2">Bạn</span>}</td>
                  <td>{a.email}</td>
                  <td>{a.phone}</td>
                  <td><StatusBadge type="role" value={a.role} /></td>
                  <td><StatusBadge type="user" value={a.status} /></td>
                  <td className="text-nowrap">{date(a.createdAt)}</td>
                  <td className="text-end text-nowrap">
                    <Button size="sm" variant="outline-primary" className="me-1" onClick={() => setEditing(a)} title="Sửa / khoá"><i className="bi bi-pencil" /></Button>
                    <Button size="sm" variant="outline-danger" onClick={() => setDeleting(a)} title="Xoá" disabled={a.id === user.id}><i className="bi bi-trash" /></Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
          <Paging data={data} onChange={paged.setPage} />
        </Card.Body>
      </Card>
      <StaffFormModal show={editing !== undefined} account={editing} onHide={() => setEditing(undefined)}
        onSaved={(res) => { setEditing(undefined); if (res) setCreated(res); paged.reload(); }} />
      <TempPasswordModal created={created} onHide={() => setCreated(null)} />
      <ConfirmModal show={!!deleting} title="Xoá tài khoản" busy={busy} confirmText="Xoá" onHide={() => setDeleting(null)} onConfirm={confirmDelete}>
        Xoá tài khoản <strong>{deleting?.email}</strong>? Tài khoản đã đăng nhập hoặc đã xử lý hồ sơ không xoá được — hãy khoá thay vì xoá.
      </ConfirmModal>
    </>
  );
}
