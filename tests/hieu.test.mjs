/**
 * Test API cho phần của Hiếu (SRS v2): vai trò ADMIN, M02 (tài khoản nhân viên, nhật ký, gara), M03 (thông báo).
 * Chạy:  node tests/hieu.test.mjs      (backend phải đang chạy ở http://localhost:8080)
 * Chạy lặp lại được: dữ liệu test sinh ngẫu nhiên.
 */
const BASE = process.env.API_URL || 'http://localhost:8080/api';

let passed = 0;
let failed = 0;
const failures = [];

async function api(method, path, body, token) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;
  const res = await fetch(BASE + path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  return { status: res.status, data };
}

function check(id, name, cond, detail) {
  if (cond) {
    passed++;
    console.log(`  \x1b[32mPASS\x1b[0m ${id} ${name}`);
  } else {
    failed++;
    failures.push(`${id} ${name}`);
    console.log(`  \x1b[31mFAIL\x1b[0m ${id} ${name}`);
    if (detail !== undefined) console.log('       ->', JSON.stringify(detail).slice(0, 600));
  }
}

function expect(id, name, res, status, fields = []) {
  const fe = (res.data && res.data.fieldErrors) || {};
  check(id, `${name} -> ${status}${fields.length ? ' [' + fields.join(', ') + ']' : ''}`,
    res.status === status && fields.every((f) => fe[f]), res);
  return res.data;
}

const section = (t) => console.log(`\n\x1b[36m== ${t} ==\x1b[0m`);
const rnd = (n) => Array.from({ length: n }, () => Math.floor(Math.random() * 10)).join('');
const uniq = Date.now().toString(36);
const phone = () => '09' + rnd(8);
const pad = (n) => String(n).padStart(2, '0');
const day = (offset = 0) => {
  const d = new Date();
  d.setDate(d.getDate() + offset);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

async function loginRes(email, password) {
  return api('POST', '/auth/login', { email, password });
}

async function main() {
  console.log(`API: ${BASE}`);

  // ================================================================ VAI TRÒ ADMIN
  section('H1. Vai trò ADMIN (M01)');
  let r = await loginRes('admin@insurancecard.vn', 'Admin@123');
  const adminLogin = expect('TC-H01', 'Quản trị viên mặc định đăng nhập', r, 200);
  check('TC-H01b', 'Vai trò trả về là ADMIN', adminLogin?.user?.role === 'ADMIN', adminLogin?.user);
  const admin = adminLogin?.accessToken;
  const adminId = adminLogin?.user?.id;
  const staff = (await loginRes('staff@insurancecard.vn', 'Staff@123')).data?.accessToken;
  const custLogin = (await loginRes('customer@insurancecard.vn', 'Customer@123')).data;
  const cust = custLogin?.accessToken;
  check('TC-H02', 'Có token nhân viên và khách hàng demo', !!staff && !!cust);

  r = await api('GET', '/customers?size=1', undefined, admin);
  expect('TC-H03', 'ADMIN dùng được API của nhân viên (RoleHierarchy)', r, 200);
  r = await api('GET', '/dashboard/staff', undefined, admin);
  expect('TC-H04', 'ADMIN xem dashboard nhân viên', r, 200);
  r = await api('GET', '/staff-accounts', undefined, staff);
  expect('TC-H05', 'Nhân viên thường gọi API quản trị', r, 403);
  r = await api('GET', '/staff-accounts', undefined, cust);
  expect('TC-H06', 'Khách hàng gọi API quản trị', r, 403);
  r = await api('GET', '/staff-accounts');
  expect('TC-H07', 'Chưa đăng nhập gọi API quản trị', r, 401);

  // ================================================================ TÀI KHOẢN NHÂN VIÊN
  section('H2. Quản lý tài khoản nhân viên (UC-24)');
  r = await api('POST', '/staff-accounts', {}, admin);
  expect('TC-H10', 'Tạo tài khoản bỏ trống', r, 400, ['fullName', 'email', 'phone', 'role']);
  r = await api('POST', '/staff-accounts', { fullName: 'Ng Van 1', email: 'sai-email', phone: '123', role: 'STAFF' }, admin);
  expect('TC-H11', 'Sai định dạng họ tên / email / SĐT', r, 400, ['fullName', 'email', 'phone']);
  r = await api('POST', '/staff-accounts', { fullName: 'Nhân Viên Thử', email: `nv.${uniq}@example.com`, phone: phone(), role: 'CUSTOMER' }, admin);
  expect('TC-H12', 'Tạo tài khoản vai trò CUSTOMER', r, 400, ['role']);

  const email1 = `nv.${uniq}.${rnd(4)}@example.com`;
  const phone1 = phone();
  r = await api('POST', '/staff-accounts', { fullName: '  Nhân   Viên Kiểm Thử ', email: email1.toUpperCase(), phone: phone1, role: 'STAFF' }, admin);
  const created1 = expect('TC-H13', 'Tạo tài khoản nhân viên hợp lệ', r, 201);
  check('TC-H13b', 'Chuẩn hoá email chữ thường, gộp khoảng trắng họ tên, ACTIVE, có mật khẩu tạm',
    created1?.account?.email === email1 && created1?.account?.fullName === 'Nhân Viên Kiểm Thử'
      && created1?.account?.status === 'ACTIVE' && /^\S{10}$/.test(created1?.temporaryPassword || ''), created1);
  const staff1Id = created1?.account?.id;

  r = await api('POST', '/staff-accounts', { fullName: 'Trùng Lặp', email: email1, phone: phone1, role: 'ADMIN' }, admin);
  expect('TC-H14', 'Trùng email và SĐT (báo cả hai)', r, 409, ['email', 'phone']);

  r = await loginRes(email1, created1?.temporaryPassword);
  const staff1 = expect('TC-H15', 'Nhân viên mới đăng nhập bằng mật khẩu tạm', r, 200)?.accessToken;

  r = await api('GET', `/staff-accounts?q=${encodeURIComponent(email1)}`, undefined, admin);
  const found = expect('TC-H16', 'Tìm kiếm theo email', r, 200);
  check('TC-H16b', 'Kết quả có tài khoản vừa tạo', found?.content?.some((a) => a.id === staff1Id), found);
  r = await api('GET', '/staff-accounts?role=ADMIN', undefined, admin);
  check('TC-H17', 'Lọc vai trò ADMIN chỉ trả ADMIN', r.status === 200 && r.data.content.every((a) => a.role === 'ADMIN'), r.data);
  r = await api('GET', '/staff-accounts?role=CUSTOMER', undefined, admin);
  expect('TC-H18', 'Lọc vai trò CUSTOMER', r, 400, ['role']);

  r = await api('GET', `/staff-accounts/${staff1Id}`, undefined, admin);
  expect('TC-H19', 'Xem chi tiết tài khoản', r, 200);
  r = await api('GET', `/staff-accounts/${custLogin?.user?.id}`, undefined, admin);
  expect('TC-H20', 'Xem tài khoản khách hàng qua API nhân viên', r, 404);

  r = await api('PUT', `/staff-accounts/${staff1Id}`, { fullName: 'Nhân Viên Đã Sửa', phone: phone1, role: 'STAFF', status: 'PENDING' }, admin);
  expect('TC-H21', 'Đặt trạng thái PENDING', r, 400, ['status']);
  r = await api('PUT', `/staff-accounts/${staff1Id}`, { fullName: 'Nhân Viên Đã Sửa', phone: phone1, role: 'STAFF', status: 'LOCKED' }, admin);
  const locked = expect('TC-H22', 'Khoá tài khoản nhân viên', r, 200);
  check('TC-H22b', 'Trạng thái LOCKED, đã đổi tên', locked?.status === 'LOCKED' && locked?.fullName === 'Nhân Viên Đã Sửa', locked);
  r = await api('GET', '/me', undefined, staff1);
  expect('TC-H23', 'Token của tài khoản bị khoá không dùng được nữa', r, 401);
  r = await loginRes(email1, created1?.temporaryPassword);
  expect('TC-H24', 'Tài khoản bị khoá đăng nhập', r, 403);

  r = await api('PUT', `/staff-accounts/${adminId}`, { fullName: 'Quản Trị Viên', phone: adminLogin?.user?.phone, role: 'ADMIN', status: 'LOCKED' }, admin);
  expect('TC-H25', 'Tự khoá chính mình', r, 422);
  r = await api('PUT', `/staff-accounts/${adminId}`, { fullName: 'Quản Trị Viên', phone: adminLogin?.user?.phone, role: 'STAFF', status: 'ACTIVE' }, admin);
  expect('TC-H26', 'Tự hạ quyền chính mình', r, 422);
  r = await api('DELETE', `/staff-accounts/${adminId}`, undefined, admin);
  expect('TC-H27', 'Tự xoá chính mình', r, 422);

  r = await api('DELETE', `/staff-accounts/${staff1Id}`, undefined, admin);
  expect('TC-H28', 'Xoá tài khoản đã đăng nhập (đã phát sinh dữ liệu)', r, 409);

  const email2 = `nv2.${uniq}.${rnd(4)}@example.com`;
  r = await api('POST', '/staff-accounts', { fullName: 'Nhân Viên Xoá Được', email: email2, phone: phone(), role: 'STAFF' }, admin);
  const created2 = expect('TC-H29', 'Tạo tài khoản chưa dùng', r, 201);
  r = await api('DELETE', `/staff-accounts/${created2?.account?.id}`, undefined, admin);
  expect('TC-H30', 'Xoá tài khoản chưa phát sinh dữ liệu', r, 204);
  r = await api('GET', `/staff-accounts/${created2?.account?.id}`, undefined, admin);
  expect('TC-H31', 'Xem tài khoản đã xoá', r, 404);

  // ================================================================ NHẬT KÝ
  section('H3. Nhật ký hệ thống (UC-25)');
  await loginRes(email2, 'Sai@12345'); // email đã xoá -> LOGIN_FAILED (user null)
  await loginRes('staff@insurancecard.vn', 'Sai@12345'); // sai mật khẩu -> LOGIN_FAILED
  await api('PUT', '/settings/RENEWAL_WINDOW_DAYS', { value: 30 }, admin); // UPDATE SETTING
  r = await api('GET', '/audit-logs', undefined, staff);
  expect('TC-H40', 'Nhân viên thường xem nhật ký', r, 403);
  r = await api('GET', '/audit-logs?action=LOGIN_FAILED&q=staff@insurancecard.vn', undefined, admin);
  const failedLogs = expect('TC-H41', 'Lọc nhật ký đăng nhập sai', r, 200);
  check('TC-H41b', 'Có bản ghi đăng nhập sai của staff', failedLogs?.content?.some((a) => a.action === 'LOGIN_FAILED' && a.userEmail === 'staff@insurancecard.vn'), failedLogs);
  r = await api('GET', `/audit-logs?q=${encodeURIComponent(email2)}`, undefined, admin);
  check('TC-H42', 'Nhật ký có tạo / xoá tài khoản và đăng nhập sai bằng email không tồn tại',
    r.status === 200 && ['CREATE', 'DELETE', 'LOGIN_FAILED'].every((a) => r.data.content.some((x) => x.action === a)), r.data);
  r = await api('GET', '/audit-logs?entityType=setting', undefined, admin);
  check('TC-H43', 'Nhật ký sửa cấu hình (lọc đối tượng không phân biệt hoa thường)',
    r.status === 200 && r.data.content.some((x) => x.entityType === 'SETTING' && x.userId === adminId), r.data);
  r = await api('GET', `/audit-logs?from=${day(0)}&to=${day(-1)}`, undefined, admin);
  expect('TC-H44', 'Từ ngày sau đến ngày', r, 400, ['to']);
  r = await api('GET', `/audit-logs?from=${day(-400)}&to=${day(0)}`, undefined, admin);
  expect('TC-H45', 'Khoảng ngày quá 366 ngày', r, 400, ['to']);
  r = await api('GET', '/audit-logs?action=KHONG_CO', undefined, admin);
  expect('TC-H46', 'Hành động không hợp lệ', r, 400);

  // ================================================================ GARA
  section('H4. Gara liên kết (UC-43, UC-44)');
  r = await api('GET', '/public/garages');
  const pub = expect('TC-H50', 'Danh sách gara công khai không cần đăng nhập', r, 200);
  check('TC-H50b', 'Công khai không lộ ghi chú / trạng thái', pub?.content?.every((g) => !('note' in g) && !('status' in g)), pub);
  r = await api('POST', '/garages', {}, staff);
  expect('TC-H51', 'Tạo gara bỏ trống', r, 400, ['garageCode', 'name', 'address', 'province', 'phone', 'status']);
  const garage = { garageCode: `gr-${rnd(6)}`, name: 'Gara Kiểm Thử', address: '1 Đường Thử, Quận 1', province: 'TP. Hồ Chí Minh', phone: phone(), email: '', status: 'ACTIVE', note: 'Ghi chú nội bộ' };
  r = await api('POST', '/garages', { ...garage, garageCode: 'a b' }, staff);
  expect('TC-H52', 'Mã gara sai định dạng', r, 400, ['garageCode']);
  r = await api('POST', '/garages', { ...garage, email: 'khong-phai-email' }, staff);
  expect('TC-H53', 'Email gara sai định dạng', r, 400, ['email']);
  r = await api('POST', '/garages', garage, cust);
  expect('TC-H54', 'Khách hàng tạo gara', r, 403);
  r = await api('POST', '/garages', garage, staff);
  const g1 = expect('TC-H55', 'Tạo gara hợp lệ', r, 201);
  check('TC-H55b', 'Mã gara viết hoa, email rỗng -> null', g1?.garageCode === garage.garageCode.toUpperCase() && g1?.email === null, g1);
  r = await api('POST', '/garages', { ...garage, garageCode: garage.garageCode.toUpperCase() }, admin);
  expect('TC-H56', 'Trùng mã gara (không phân biệt hoa thường)', r, 409, ['garageCode']);
  r = await api('GET', `/public/garages?q=${encodeURIComponent(g1?.garageCode)}`);
  check('TC-H57', 'Gara ACTIVE hiện ở danh sách công khai', r.status === 200 && r.data.content.some((g) => g.id === g1?.id), r.data);
  r = await api('PUT', `/garages/${g1?.id}`, { ...garage, status: 'INACTIVE' }, staff);
  expect('TC-H58', 'Chuyển gara sang ngừng hợp tác', r, 200);
  r = await api('GET', `/public/garages?q=${encodeURIComponent(g1?.garageCode)}`);
  check('TC-H59', 'Gara ngừng hợp tác không hiện ở danh sách công khai', r.status === 200 && !r.data.content.some((g) => g.id === g1?.id), r.data);
  r = await api('GET', '/garages?status=INACTIVE&province=hồ chí minh', undefined, staff);
  check('TC-H60', 'Lọc theo trạng thái và tỉnh', r.status === 200 && r.data.content.some((g) => g.id === g1?.id), r.data);
  r = await api('DELETE', `/garages/${g1?.id}`, undefined, staff);
  expect('TC-H61', 'Xoá gara chưa dùng', r, 204);
  r = await api('GET', `/garages/${g1?.id}`, undefined, staff);
  expect('TC-H62', 'Xem gara đã xoá', r, 404);

  // ================================================================ THÔNG BÁO
  section('H5. Thông báo (UC-26)');
  r = await api('GET', '/notifications/unread-count');
  expect('TC-H70', 'Chưa đăng nhập xem thông báo', r, 401);
  r = await api('GET', '/notifications/unread-count', undefined, cust);
  const cnt = expect('TC-H71', 'Đếm thông báo chưa đọc', r, 200);
  check('TC-H71b', 'count là số', typeof cnt?.count === 'number', cnt);
  r = await api('GET', '/notifications?unreadOnly=false&size=5', undefined, cust);
  const list = expect('TC-H72', 'Danh sách thông báo của mình', r, 200);
  r = await api('POST', '/notifications/999999999/read', undefined, cust);
  expect('TC-H73', 'Đánh dấu thông báo không tồn tại / của người khác', r, 404);
  if (list?.content?.length) {
    const n = list.content[0];
    r = await api('POST', `/notifications/${n.id}/read`, undefined, staff);
    expect('TC-H74', 'Nhân viên đánh dấu thông báo của khách', r, 404);
    r = await api('POST', `/notifications/${n.id}/read`, undefined, cust);
    const read = expect('TC-H75', 'Đánh dấu đã đọc', r, 200);
    check('TC-H75b', 'read = true, có readAt', read?.read === true && !!read?.readAt, read);
  }
  r = await api('POST', '/notifications/read-all', undefined, cust);
  check('TC-H76', 'Đánh dấu tất cả đã đọc', r.status === 200 && typeof r.data.updated === 'number', r.data);
  r = await api('GET', '/notifications/unread-count', undefined, cust);
  check('TC-H77', 'Sau khi đọc hết, số chưa đọc = 0', r.status === 200 && r.data.count === 0, r.data);

  console.log(`\n\x1b[1mKết quả: ${passed} PASS, ${failed} FAIL\x1b[0m`);
  if (failed) {
    console.log('Các case lỗi:\n - ' + failures.join('\n - '));
    process.exitCode = 1;
  }
}

main().catch((e) => {
  console.error(e);
  process.exitCode = 1;
});
