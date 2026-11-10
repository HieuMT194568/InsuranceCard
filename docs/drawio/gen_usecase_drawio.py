"""Sinh 2 file draw.io cho use case diagram Phần 1 (Vai trò) và Phần 4 (Cấu hình hệ thống).

Chạy:  python gen_usecase_drawio.py
Kết quả (cùng thư mục với script):
    usecase-1-vai-tro.drawio
    usecase-4-cau-hinh.drawio
Mở bằng https://app.diagrams.net (File > Open from > Device) hoặc draw.io desktop /
extension "Draw.io Integration" của VS Code. Mọi hình đều sửa được trực tiếp.

Nội dung khớp với docs/usecase-1-vai-tro.puml và docs/usecase-4-cau-hinh.puml.
"""

from pathlib import Path
from xml.sax.saxutils import quoteattr

OUT_DIR = Path(__file__).resolve().parent

# ----------------------------------------------------------------- bảng màu
TEXT = "#1E293B"
MUTED = "#64748B"
LINE = "#64748B"
ACTOR_FILL, ACTOR_LINE, ACTOR_TEXT = "#E0E7FF", "#4F46E5", "#312E81"

PKG_COLORS = {
    "pub": ("#F8FAFC", "#CBD5E1"),
    "acc": ("#EFF6FF", "#BFDBFE"),
    "biz": ("#ECFDF5", "#A7F3D0"),
    "adm": ("#FFF7ED", "#FED7AA"),
    "auto": ("#F5F3FF", "#DDD6FE"),
}

FONT = "fontFamily=Segoe UI;"

STYLE_ACTOR = (
    "shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;html=1;outlineConnect=0;"
    f"fillColor={ACTOR_FILL};strokeColor={ACTOR_LINE};strokeWidth=1.5;fontStyle=1;"
    f"fontColor={ACTOR_TEXT};fontSize=12;{FONT}"
)
STYLE_UC = (
    "ellipse;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#475569;strokeWidth=1.2;"
    f"fontColor={TEXT};fontSize=12;{FONT}"
)
STYLE_SYSTEM = (
    "rounded=1;arcSize=1;whiteSpace=wrap;html=1;fillColor=#FFFFFF;strokeColor=#94A3B8;"
    f"strokeWidth=1.5;verticalAlign=top;fontStyle=1;fontSize=15;fontColor=#0F172A;{FONT}"
)
STYLE_TITLE = (
    f"text;html=1;align=center;verticalAlign=middle;fontSize=20;fontStyle=1;fontColor=#0F172A;{FONT}"
)
STYLE_LEGEND = (
    "rounded=1;arcSize=6;whiteSpace=wrap;html=1;align=left;verticalAlign=top;spacingLeft=10;"
    f"spacingTop=4;fillColor=#F8FAFC;strokeColor=#CBD5E1;fontSize=11;fontColor=#334155;{FONT}"
)
EDGE_BASE = (
    f"html=1;endArrow=open;endSize=10;strokeColor={LINE};strokeWidth=1.1;"
    f"fontSize=10;fontColor={MUTED};fontStyle=2;labelBackgroundColor=#FFFFFF;{FONT}"
)
STYLE_ASSOC = "edgeStyle=none;" + EDGE_BASE
STYLE_DASHED = "edgeStyle=none;dashed=1;" + EDGE_BASE
STYLE_GENERAL = (
    f"edgeStyle=none;html=1;endArrow=block;endFill=0;endSize=14;strokeColor={LINE};strokeWidth=1.1;"
)


def pkg_style(kind):
    fill, line = PKG_COLORS[kind]
    return (
        "shape=folder;tabWidth=150;tabHeight=24;tabPosition=left;html=1;whiteSpace=wrap;"
        f"fillColor={fill};strokeColor={line};strokeWidth=1.5;verticalAlign=top;align=left;"
        f"spacingLeft=10;spacingTop=2;fontStyle=1;fontSize=13;fontColor=#334155;{FONT}"
    )


def label(name, sub=None):
    """Tên use case + dòng phụ (mã UC) cỡ nhỏ, màu xám."""
    if not sub:
        return name
    return f'{name}<br><font style="font-size:10px" color="{MUTED}">{sub}</font>'


class Diagram:
    def __init__(self, name, width, height):
        self.name = name
        self.width = width
        self.height = height
        self.cells = []
        self._n = 0

    def _id(self, prefix):
        self._n += 1
        return f"{prefix}{self._n}"

    def vertex(self, value, style, x, y, w, h, cid=None):
        cid = cid or self._id("v")
        self.cells.append(
            f'<mxCell id="{cid}" value={quoteattr(value)} style={quoteattr(style)} vertex="1" parent="1">'
            f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>'
        )
        return cid

    def actor(self, cid, name, cx, cy, stereo=None):
        text = name if not stereo else f'<font style="font-size:10px" color="#6366F1">«{stereo}»</font><br>{name}'
        return self.vertex(text, STYLE_ACTOR, cx - 15, cy - 30, 30, 60, cid)

    def uc(self, cid, name, sub, cx, cy, w=180, h=None):
        h = h or (50 if sub else 40)
        return self.vertex(label(name, sub), STYLE_UC, cx - w / 2, cy - h / 2, w, h, cid)

    def edge(self, src, dst, style, text="", label_pos=0.0):
        cid = self._id("e")
        geo = f'<mxGeometry x="{label_pos}" relative="1" as="geometry"/>' if label_pos else (
            '<mxGeometry relative="1" as="geometry"/>'
        )
        self.cells.append(
            f'<mxCell id="{cid}" value={quoteattr(text)} style={quoteattr(style)} edge="1" parent="1" '
            f'source="{src}" target="{dst}">{geo}</mxCell>'
        )

    def assoc(self, src, dst):
        self.edge(src, dst, STYLE_ASSOC)

    def include(self, src, dst, extra="", label_pos=0.0):
        text = "«include»" + (f"<br>{extra}" if extra else "")
        self.edge(src, dst, STYLE_DASHED, text, label_pos)

    def extend(self, src, dst):
        self.edge(src, dst, STYLE_DASHED, "«extend»")

    def general(self, child, parent):
        self.edge(child, parent, STYLE_GENERAL)

    def xml(self):
        body = "".join(self.cells)
        return (
            '<mxfile host="drawio" type="device">'
            f'<diagram id="{self.name}" name="{self.name}">'
            f'<mxGraphModel dx="1400" dy="900" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" '
            f'arrows="1" fold="1" page="1" pageScale="1" pageWidth="{self.width}" pageHeight="{self.height}" '
            'math="0" shadow="0" background="#FFFFFF">'
            f'<root><mxCell id="0"/><mxCell id="1" parent="0"/>{body}</root>'
            "</mxGraphModel></diagram></mxfile>\n"
        )

    def save(self, filename):
        path = OUT_DIR / filename
        path.write_text(self.xml(), encoding="utf-8")
        print("Saved", path)


# =================================================================== PHẦN 1
def build_phan_1():
    d = Diagram("Phần 1 - Vai trò", 1100, 2850)
    d.vertex(
        'Insurance Card – Use Case Diagram tổng quát<br>'
        f'<font style="font-size:13px;font-weight:normal" color="{MUTED}">Phần 1 · Vai trò</font>',
        STYLE_TITLE, 150, 10, 800, 60,
    )

    SX, SW = 170, 720            # khung hệ thống
    C1, C2 = 330, 650            # tâm cột use case chính / phụ
    PX, PW = 200, 660            # khung package
    STEP = 58

    # (khoá, tên package, kiểu màu, số hàng)
    y = 140
    d.vertex("Hệ thống quản lý bảo hiểm xe máy – Insurance Card", STYLE_SYSTEM, SX, 85, SW, 2595)

    # ---------------- Công khai
    pub_rows = [
        ("UC_Home", "Xem trang chủ", None),
        ("UC_PubProducts", "Xem gói bảo hiểm", None),
        ("UC01", "Đăng ký tài khoản", "UC-01"),
        ("UC02b", "Gửi lại link xác thực", None),
        ("UC02", "Xác thực email", "UC-02"),
        ("UC03", "Đăng nhập", "UC-03"),
        ("UC04", "Quên mật khẩu", "UC-04"),
        ("UC04b", "Đặt lại mật khẩu", None),
    ]
    top = y
    d.vertex("Công khai", pkg_style("pub"), PX, top - 30, PW, len(pub_rows) * STEP + 50)
    pos = {}
    for i, (cid, name, sub) in enumerate(pub_rows):
        cy = top + 25 + i * STEP
        d.uc(cid, name, sub, C1, cy)
        pos[cid] = cy
    d.uc("UC_Mail", "Gửi email", None, C2, pos["UC02b"], w=150)
    guest_y = (pos["UC_Home"] + pos["UC04b"]) / 2
    y = top + len(pub_rows) * STEP + 70

    # ---------------- Tài khoản
    acc_rows = [
        ("UC_Logout", "Đăng xuất", None),
        ("UC05", "Đổi mật khẩu", "UC-05"),
        ("UC22", "Xem dashboard", "UC-22"),
        ("UC06", "Cập nhật hồ sơ", "UC-06"),
    ]
    top = y
    d.vertex("Tài khoản", pkg_style("acc"), PX, top - 30, 300, len(acc_rows) * STEP + 50)
    for i, (cid, name, sub) in enumerate(acc_rows):
        cy = top + 25 + i * STEP
        d.uc(cid, name, sub, C1, cy)
        pos[cid] = cy
    user_y = (pos["UC_Logout"] + pos["UC06"]) / 2
    y = top + len(acc_rows) * STEP + 70

    # ---------------- Nghiệp vụ bảo hiểm
    biz_rows = [
        ("UC08", "Quản lý xe", "UC-08"),
        ("UC_ViewContract", "Xem hợp đồng", None),
        ("UC10", "Tạo hợp đồng", "UC-10"),
        ("UC13", "Gia hạn hợp đồng", "UC-13"),
        ("UC14", "Huỷ hợp đồng", "UC-14"),
        ("UC16", "Khai báo tai nạn", "UC-16"),
        ("UC18", "Yêu cầu bồi thường", "UC-18"),
        ("UC_ViewPunish", "Xem biên bản phạt", None),
        ("UC20pay", "Nộp phạt", "UC-20"),
        ("UC21", "Xem lịch sử giao dịch", "UC-21"),
        ("UC12", "Thanh toán phí", "UC-12"),
    ]
    top = y
    d.vertex("Nghiệp vụ bảo hiểm", pkg_style("biz"), PX, top - 30, 300, len(biz_rows) * STEP + 50)
    for i, (cid, name, sub) in enumerate(biz_rows):
        cy = top + 25 + i * STEP
        d.uc(cid, name, sub, C1, cy)
        pos[cid] = cy
    customer_y = (pos["UC08"] + pos["UC12"]) / 2
    y = top + len(biz_rows) * STEP + 70

    # ---------------- Quản trị (chỉ Nhân viên): (khoá, tên, mã, cột, hàng)
    adm = [
        ("UC12cash", "Thu phí tiền mặt", None, C1, 0),
        ("UC09", "Quản lý sản phẩm", "UC-09", C1, 1.5),
        ("UC07", "Quản lý khách hàng", "UC-07", C1, 3.5),
        ("UC07a", "Tìm kiếm / lọc", None, C2, 1),
        ("UC07b", "Tạo khách hàng", None, C2, 2),
        ("UC07c", "Sửa khách hàng", None, C2, 3),
        ("UC07d", "Khoá / mở khoá", None, C2, 4),
        ("UC07e", "Đặt lại mật khẩu KH", None, C2, 5),
        ("UC07f", "Xoá khách hàng", None, C2, 6),
        ("UC11", "Sửa hợp đồng", "UC-11", C1, 5.5),
        ("UC17", "Xử lý tai nạn", "UC-17", C1, 7),
        ("UC19", "Xử lý bồi thường", "UC-19", C1, 9),
        ("UC19a", "Duyệt", None, C2, 8),
        ("UC19b", "Từ chối", None, C2, 9),
        ("UC19c", "Chi trả", None, C2, 10),
        ("UC20", "Quản lý biên bản phạt", "UC-20", C1, 11.5),
        ("UC20waive", "Miễn phạt", None, C2, 11.5),
        ("UC_Settings", "Cấu hình hệ thống", "xem Phần 4", C1, 13),
    ]
    top = y
    adm_rows = 14
    d.vertex("Quản trị (chỉ Nhân viên)", pkg_style("adm"), PX, top - 30, PW, adm_rows * STEP + 40)
    for cid, name, sub, cx, row in adm:
        cy = top + 25 + row * STEP
        d.uc(cid, name, sub, cx, cy, w=180 if cx == C1 else 160)
        pos[cid] = cy
    staff_y = top + 25 + 6.5 * STEP
    y = top + adm_rows * STEP + 60

    # ---------------- Tự động
    top = y
    d.vertex("Tự động", pkg_style("auto"), PX, top - 30, 300, 110)
    d.uc("UC15", "Cập nhật trạng thái hợp đồng", "UC-15", C1, top + 30, w=200, h=56)
    sched_y = top + 30

    # ---------------- Actor
    AX = 80
    d.actor("Guest", "Khách vãng lai", AX, guest_y)
    d.actor("User", "Người dùng<br>đã đăng nhập", AX, user_y)
    d.actor("Customer", "Khách hàng", AX, customer_y)
    d.actor("Staff", "Nhân viên", AX, staff_y)
    d.actor("Scheduler", "Bộ lập lịch", AX, sched_y, stereo="hệ thống")
    d.actor("Mail", "Hệ thống Email", 990, pos["UC02b"], stereo="hệ thống")

    # ---------------- Quan hệ
    d.general("Customer", "User")
    d.general("Staff", "Customer")

    for cid, *_ in pub_rows:
        if cid != "UC02b":
            d.assoc("Guest", cid)
    d.include("UC01", "UC_Mail")
    d.include("UC02b", "UC_Mail")
    d.include("UC04", "UC_Mail")
    d.extend("UC02b", "UC02")
    d.assoc("UC_Mail", "Mail")

    for cid, *_ in acc_rows:
        d.assoc("User", cid)

    for cid, *_ in biz_rows:
        d.assoc("Customer", cid)
    d.include("UC13", "UC10")
    d.extend("UC18", "UC16")

    for cid in ["UC07", "UC09", "UC11", "UC17", "UC19", "UC20", "UC_Settings"]:
        d.assoc("Staff", cid)
    for sub in ["UC07a", "UC07b", "UC07c", "UC07d", "UC07e", "UC07f"]:
        d.extend(sub, "UC07")
    for sub in ["UC19a", "UC19b", "UC19c"]:
        d.extend(sub, "UC19")
    d.extend("UC20waive", "UC20")
    d.extend("UC12cash", "UC12")

    d.assoc("Scheduler", "UC15")

    d.vertex(
        "<b>Ghi chú</b><br>"
        "• Nhân viên kế thừa mọi use case của Khách hàng và thực hiện trên <b>mọi</b> khách hàng; "
        "Khách hàng chỉ thao tác trên dữ liệu của mình (truy cập dữ liệu người khác =&gt; 404).<br>"
        "• Thanh toán: Khách hàng dùng chuyển khoản / thẻ / ví điện tử; tiền mặt chỉ do Nhân viên thu tại quầy.<br>"
        "• Đăng nhập: sai mật khẩu 5 lần liên tiếp =&gt; tạm khoá 15 phút; tài khoản PENDING / LOCKED không đăng nhập được.<br>"
        "• Bộ lập lịch (mỗi giờ + khi khởi động): ACTIVE quá hạn =&gt; EXPIRED; "
        "PENDING_PAYMENT quá ngày bắt đầu =&gt; CANCELLED.",
        STYLE_LEGEND, 20, 2710, 1040, 110,
    )
    return d


# =================================================================== PHẦN 4
def build_phan_4():
    d = Diagram("Phần 4 - Cấu hình hệ thống", 1200, 1600)
    d.vertex(
        'Insurance Card – Use Case Diagram cấu hình hệ thống<br>'
        f'<font style="font-size:13px;font-weight:normal" color="{MUTED}">'
        "Phần 4 · Cấu hình hệ thống (bảng system_settings)</font>",
        STYLE_TITLE, 200, 10, 800, 60,
    )
    d.vertex("Insurance Card – Cấu hình hệ thống", STYLE_SYSTEM, 170, 85, 1000, 1345)

    C1, C2 = 340, 840
    STEP = 62

    # ---------------- Quản lý cấu hình
    settings = [
        ("S1", "Hạn được gia hạn", "RENEWAL_WINDOW_DAYS · 1–90 · mặc định 30"),
        ("S2", "Số ngày chọn trước ngày bắt đầu", "MAX_START_DATE_ADVANCE_DAYS · 0–365 · mặc định 60"),
        ("S3", "Hạn khai báo tai nạn / bồi thường", "CLAIM_DEADLINE_DAYS · 1–730 · mặc định 365"),
        ("S4", "% hoàn phí khi huỷ hợp đồng", "CANCEL_REFUND_PERCENT · 0–100 · mặc định 80"),
        ("S5", "Hạn nộp phạt mặc định", "PENALTY_DUE_DAYS · 1–180 · mặc định 30"),
        ("S6", "Hạn link xác thực email", "VERIFY_TOKEN_EXPIRY_HOURS · 1–168 giờ · mặc định 24"),
        ("S7", "Hạn link đặt lại mật khẩu", "RESET_TOKEN_EXPIRY_MINUTES · 5–1440 phút · mặc định 30"),
    ]
    top = 170
    rows = len(settings) + 2
    d.vertex("Quản lý cấu hình (màn hình Cấu hình)", pkg_style("adm").replace("tabWidth=150", "tabWidth=290"),
             200, top - 30, 940, rows * STEP + 50)
    for i, (cid, name, sub) in enumerate(settings):
        d.uc(cid, name, sub, C2, top + 25 + i * STEP, w=330, h=52)
    upd_y = top + 25 + 3 * STEP
    d.uc("UC_List", "Xem danh sách<br>tham số cấu hình", None, C1, upd_y - 1.5 * STEP, w=180, h=56)
    d.uc("UC_Update", "Cập nhật<br>tham số cấu hình", None, C1, upd_y, w=180, h=56)
    d.uc("UC_Audit", "Ghi nhận người &amp;<br>thời điểm cập nhật", None, C1 + 220, top + 25 + 7 * STEP,
         w=200, h=56)
    d.uc("UC_Validate", "Kiểm tra giá trị<br>trong khoảng [min, max]", None, C1 + 220, top + 25 + 8 * STEP,
         w=200, h=56)
    staff_y = upd_y - 0.75 * STEP

    # ---------------- Nghiệp vụ áp dụng tham số
    uses = [
        ("UC20", "Lập biên bản phạt", "UC-20", "PENALTY_DUE_DAYS"),
        ("UC10", "Tạo hợp đồng", "UC-10", "MAX_START_DATE_ADVANCE_DAYS"),
        ("UC13", "Gia hạn hợp đồng", "UC-13", "RENEWAL_WINDOW_DAYS"),
        ("UC14", "Huỷ hợp đồng", "UC-14", "CANCEL_REFUND_PERCENT"),
        ("UC16", "Khai báo tai nạn", "UC-16", "CLAIM_DEADLINE_DAYS"),
        ("UC18", "Yêu cầu bồi thường", "UC-18", "CLAIM_DEADLINE_DAYS"),
        ("UC22", "Xem HĐ sắp hết hạn<br>trên dashboard", "UC-22", "RENEWAL_WINDOW_DAYS"),
        ("UC01", "Đăng ký / gửi lại<br>link xác thực", "UC-01, UC-02", "VERIFY_TOKEN_EXPIRY_HOURS"),
        ("UC04", "Quên mật khẩu", "UC-04", "RESET_TOKEN_EXPIRY_MINUTES"),
    ]
    top = 170 + rows * STEP + 90
    d.vertex("Nghiệp vụ áp dụng tham số (có hiệu lực ngay)", pkg_style("biz").replace("tabWidth=150", "tabWidth=330"),
             200, top - 30, 940, len(uses) * STEP + 50)
    upos = {}
    for i, (cid, name, sub, _) in enumerate(uses):
        cy = top + 25 + i * STEP
        multi = "<br>" in name
        d.uc(cid, name, sub, C1, cy, w=190, h=60 if multi else 50)
        upos[cid] = cy
    read_y = (upos["UC20"] + upos["UC04"]) / 2
    d.uc("UC_Read", "Đọc tham số<br>cấu hình", None, C2 + 80, read_y, w=150, h=56)

    # ---------------- Actor
    AX = 80
    d.actor("Staff", "Nhân viên", AX, staff_y)
    d.actor("Customer", "Khách hàng", AX, (upos["UC10"] + upos["UC22"]) / 2)
    d.actor("Guest", "Khách vãng lai", AX, (upos["UC01"] + upos["UC04"]) / 2)

    # ---------------- Quan hệ
    d.general("Staff", "Customer")
    d.assoc("Staff", "UC_List")
    d.assoc("Staff", "UC_Update")
    d.include("UC_Update", "UC_Audit")
    d.include("UC_Update", "UC_Validate")
    for cid, *_ in settings:
        d.general(cid, "UC_Update")

    d.assoc("Staff", "UC20")
    for cid in ["UC10", "UC13", "UC14", "UC16", "UC18", "UC22"]:
        d.assoc("Customer", cid)
    d.assoc("Guest", "UC01")
    d.assoc("Guest", "UC04")
    for cid, _, _, key in uses:
        d.include(cid, "UC_Read", key, label_pos=-0.35)

    d.vertex(
        "<b>Ghi chú</b><br>"
        "• Tiền điều kiện quản lý cấu hình: đăng nhập vai trò STAFF "
        "(chưa đăng nhập =&gt; 401; Khách hàng gọi /api/settings =&gt; 403).<br>"
        "• Nhân viên kế thừa các use case của Khách hàng.<br>"
        "• Kiểm tra giá trị: thiếu giá trị =&gt; 400 \"Vui lòng nhập giá trị\"; "
        "ngoài khoảng =&gt; 400 \"Giá trị phải từ {min} đến {max}\"; khoá không tồn tại =&gt; 404 (TC-S01).<br>"
        "• Giá trị mới có hiệu lực ngay cho các thao tác sau đó (TC-S05).",
        STYLE_LEGEND, 20, 1450, 1150, 100,
    )
    return d


if __name__ == "__main__":
    build_phan_1().save("usecase-1-vai-tro.drawio")
    build_phan_4().save("usecase-4-cau-hinh.drawio")
