"""Sinh file XML (định dạng "simple XML" của Visual Paradigm) cho Phần 1 – Vai trò / phân quyền.

Gồm:
  * Class diagram (class_spec.py + class_layout.py) – lấy từ mã nguồn backend com.insurancecard.
  * Use case diagram + 5 sequence diagram cho UC-01..UC-05 (seq_spec.py, bộ sinh gen_seq.py).

Chạy:   python gen_vp.py            -> phan1-vai-tro.xml (cùng thư mục)
Mở trong Visual Paradigm:
  - Mở thẳng InsuranceCard-Phan1-UC-Sequence.vpp (đã import sẵn), hoặc
  - Project mới > Project > Import > XML... > chọn phan1-vai-tro.xml, hoặc dòng lệnh:
      "<VP>/scripts/ImportXML.bat" -project <đường dẫn>.vpp -file phan1-vai-tro.xml
"""
import re
import sys
from pathlib import Path
from xml.sax.saxutils import quoteattr

from class_spec import CLASSES, COLORS, RELATIONS
from class_layout import DIAGRAMS
import gen_seq

OUT = Path(sys.argv[1]) if len(sys.argv) > 1 and not sys.argv[1].startswith("--") else Path(__file__).resolve().parent / "phan1-vai-tro.xml"

# ---------------------------------------------------------------- ids
_n = 0


def nid(p):
    global _n
    _n += 1
    return f"{p}{_n:05d}"


def A(**kw):
    return " ".join(f"{k}={quoteattr(str(v))}" for k, v in kw.items() if v is not None)


out = []


def w(depth, s):
    out.append("\t" * depth + s)


# ---------------------------------------------------------------- parse members
VIS = {"+": "public", "-": "private", "#": "protected", "~": "package"}


def split_top(s):
    parts, depth, cur = [], 0, ""
    for ch in s:
        if ch in "<(":
            depth += 1
        elif ch in ">)":
            depth -= 1
        if ch == "," and depth == 0:
            parts.append(cur.strip())
            cur = ""
        else:
            cur += ch
    if cur.strip():
        parts.append(cur.strip())
    return parts


def parse_attr(s):
    static = s.endswith("{static}")
    s = s.replace("{static}", "").strip()
    vis = VIS[s[0]]
    name, typ = [x.strip() for x in s[1:].split(":", 1)]
    return dict(vis=vis, name=name, type=typ, static=static)


def parse_op(s):
    static = abstract = False
    while s[0] in "$/":
        static |= s[0] == "$"
        abstract |= s[0] == "/"
        s = s[1:]
    vis = VIS[s[0]]
    m = re.match(r"(\w+)\((.*)\)\s*(?::\s*(.+))?$", s[1:])
    name, params, ret = m.group(1), m.group(2), m.group(3)
    ps = []
    for p in split_top(params):
        pn, pt = [x.strip() for x in p.split(":", 1)]
        ps.append((pn, pt))
    return dict(vis=vis, name=name, params=ps, ret=ret, static=static, abstract=abstract)


def op_text(o):
    return f"+{o['name']}(" + ", ".join(f"{a} : {b}" for a, b in o["params"]) + ")" + (f" : {o['ret']}" if o["ret"] else "")


for k, c in CLASSES.items():
    c.setdefault("attrs", [])
    c.setdefault("ops", [])
    c.setdefault("literals", [])
    c.setdefault("abstract", False)
    c.setdefault("stereo", None)
    c["A"] = [parse_attr(a) for a in c["attrs"]]
    c["O"] = [parse_op(o) for o in c["ops"]]
    c["id"] = nid("C")

# ---------------------------------------------------------------- sizing (font Dialog 11)
CHAR = 5.6
BOLD = 6.9
ROW = 15


def size(c, name):
    texts = [f"-{a['name']} : {a['type']}" for a in c["A"]] + [op_text(o) for o in c["O"]] + c["literals"]
    wd = max([len(t) * CHAR + 14 for t in texts] + [len(name) * BOLD + 24, (len(c["stereo"] or "") + 4) * CHAR + 20, 110])
    h = 20 + (14 if c["stereo"] else 0)
    comps = [len(c["literals"])] if c["literals"] else [len(c["A"]), len(c["O"])]
    for n in comps:
        h += 7 + ROW * n if n else 8
    return int(round(wd)), int(round(h + 2))


for k, c in CLASSES.items():
    c["W"], c["H"] = size(c, k)

# ---------------------------------------------------------------- stereotypes
STEREOS = {}


def stereo_id(name, base):
    key = (name, base)
    if key not in STEREOS:
        STEREOS[key] = nid("ST")
    return STEREOS[key]


for c in CLASSES.values():
    if c["stereo"]:
        c["stid"] = stereo_id(c["stereo"], "Class")
for st in ("boundary", "control", "entity"):
    stereo_id(st, "InteractionLifeLine")

# ---------------------------------------------------------------- relationships (model)
rels = []
for kind, f, t, extra in RELATIONS:
    r = dict(kind=kind, f=f, t=t, extra=extra, id=nid("R"))
    if kind == "assoc":
        r["e1"], r["e2"] = nid("AE"), nid("AE")
    if kind == "dep" and extra:
        r["stid"] = stereo_id(extra, "Dependency")
    rels.append(r)

# ---------------------------------------------------------------- packages (model)
PKG_TREE = {
    "com.insurancecard": ["controller", "service", "repository", "security", "config", "domain", "dto"],
    "domain": ["domain.enums"],
    "dto": ["dto.AuthDtos"],
}
PKG_IDS = {p: nid("P") for p in ["com.insurancecard", "controller", "service", "repository", "security", "config",
                                  "domain", "domain.enums", "dto", "dto.AuthDtos", "spring.web", "spring.data",
                                  "frontend.pages", "spring.crypto"]}
PKG_LABEL = {"com.insurancecard": "com.insurancecard", "domain.enums": "enums", "dto.AuthDtos": "AuthDtos",
             "spring.web": "org.springframework.web.filter",
             "spring.data": "org.springframework.data.jpa.repository",
             "frontend.pages": "frontend/src/pages (React)",
             "spring.crypto": "org.springframework.security.crypto.password"}


def pkg_label(p):
    return PKG_LABEL.get(p, p)


# ---------------------------------------------------------------- write models
w(0, '<?xml version="1.0" encoding="UTF-8"?>')
w(0, '<Project Name="InsuranceCard - Phan 1 Vai tro" UmlVersion="2.x" Xml_structure="simple" '
     'xmlns="http://www.visual-paradigm.com/product/vpuml/modelexporter">')
w(1, "<Models>")
for (name, base), sid in STEREOS.items():
    w(2, f"<Stereotype {A(Id=sid, Name=name, BaseType=base)}/>")


def write_class(d, key):
    c = CLASSES[key]
    w(d, f"<Class {A(Id=c['id'], Name=key, Abstract=str(c['abstract']).lower(), Visibility='public')}>")
    if c["stereo"]:
        w(d + 1, "<Stereotypes>")
        w(d + 2, f"<Stereotype {A(Idref=c['stid'], Name=c['stereo'])}/>")
        w(d + 1, "</Stereotypes>")
    w(d + 1, "<ModelChildren>")
    for lit in c["literals"]:
        w(d + 2, f"<EnumerationLiteral {A(Id=nid('L'), Name=lit)}/>")
    for a in c["A"]:
        w(d + 2, f"<Attribute {A(Id=nid('A'), Name=a['name'], Visibility=a['vis'], Type=a['type'], Scope='classifier' if a['static'] else 'instance')}/>")
    for o in c["O"]:
        attrs = A(Id=nid("O"), Name=o["name"], Visibility=o["vis"], ReturnType=o["ret"] or "",
                  Scope="classifier" if o["static"] else "instance", Abstract=str(o["abstract"]).lower())
        if o["params"]:
            w(d + 2, f"<Operation {attrs}>")
            w(d + 3, "<ModelChildren>")
            for pn, pt in o["params"]:
                w(d + 4, f"<Parameter {A(Id=nid('PA'), Name=pn, Type=pt, Direction='in')}/>")
            w(d + 3, "</ModelChildren>")
            w(d + 2, "</Operation>")
        else:
            w(d + 2, f"<Operation {attrs}/>")
    w(d + 1, "</ModelChildren>")
    w(d, "</Class>")


def write_pkg(d, p):
    w(d, f"<Package {A(Id=PKG_IDS[p], Name=pkg_label(p))}>")
    w(d + 1, "<ModelChildren>")
    for sub in PKG_TREE.get(p, []):
        write_pkg(d + 2, sub)
    for k, c in CLASSES.items():
        if c["pkg"] == p:
            write_class(d + 2, k)
    w(d + 1, "</ModelChildren>")
    w(d, "</Package>")


write_pkg(2, "com.insurancecard")
write_pkg(2, "spring.web")
write_pkg(2, "spring.data")
write_pkg(2, "spring.crypto")
write_pkg(2, "frontend.pages")

# notes (model)
NOTE_IDS = {}
for dg in DIAGRAMS:
    for nkey, n in dg.get("notes", {}).items():
        NOTE_IDS[(dg["name"], nkey)] = nid("N")
        w(2, f"<NOTE {A(Id=NOTE_IDS[(dg['name'], nkey)], Name='', Documentation_plain=n['text'])}/>")

# use case + interaction (frame) cho sequence diagram
CTX = gen_seq.Ctx(w, A, nid, CLASSES, stereo_id)
gen_seq.models(CTX)

# relationships
w(2, f"<ModelRelationshipContainer {A(Id=nid('RC'), Name='relationships')}>")
w(3, "<ModelChildren>")
for kind, tag in [("gen", "Generalization"), ("assoc", "Association"), ("dep", "Dependency")]:
    w(4, f"<ModelRelationshipContainer {A(Id=nid('RC'), Name=tag)}>")
    w(5, "<ModelChildren>")
    for r in rels:
        if r["kind"] != kind:
            continue
        cf, ct = CLASSES[r["f"]], CLASSES[r["t"]]
        if kind == "gen":
            # VP: From = lớp cha (general), To = lớp con (specific)
            w(6, f"<Generalization {A(Id=r['id'], From=ct['id'], To=cf['id'], Name=r['extra'])}/>")
        elif kind == "dep":
            if r.get("stid"):
                w(6, f"<Dependency {A(Id=r['id'], From=cf['id'], To=ct['id'])}>")
                w(7, "<Stereotypes>")
                w(8, f"<Stereotype {A(Idref=r['stid'], Name=r['extra'])}/>")
                w(7, "</Stereotypes>")
                w(6, "</Dependency>")
            else:
                w(6, f"<Dependency {A(Id=r['id'], From=cf['id'], To=ct['id'])}/>")
        else:
            role, mf, mt = r["extra"]
            w(6, f"<Association {A(Id=r['id'], Direction='From To', EndRelationshipFromMetaModelElement=cf['id'], EndRelationshipToMetaModelElement=ct['id'])}>")
            w(7, "<FromEnd>")
            w(8, f"<AssociationEnd {A(Id=r['e1'], EndModelElement=cf['id'], Multiplicity=mf or 'Unspecified', Navigable='Unspecified', AggregationKind='None', Visibility='Unspecified')}>")
            w(9, f"<Type><Class {A(Idref=cf['id'], Name=r['f'])}/></Type>")
            w(8, "</AssociationEnd>")
            w(7, "</FromEnd>")
            w(7, "<ToEnd>")
            w(8, f"<AssociationEnd {A(Id=r['e2'], EndModelElement=ct['id'], Name=role, Multiplicity=mt or 'Unspecified', Navigable='Navigable', AggregationKind='None', Visibility='private' if role else 'Unspecified')}>")
            w(9, f"<Type><Class {A(Idref=ct['id'], Name=r['t'])}/></Type>")
            w(8, "</AssociationEnd>")
            w(7, "</ToEnd>")
            w(6, "</Association>")
    w(5, "</ModelChildren>")
    w(4, "</ModelRelationshipContainer>")
gen_seq.relationships(CTX)
w(3, "</ModelChildren>")
w(2, "</ModelRelationshipContainer>")
w(1, "</Models>")


# ---------------------------------------------------------------- diagrams
def rgb(t):
    return f"rgb({t[0]}, {t[1]}, {t[2]})"


def fill(d, color):
    w(d, f'<FillColor Color="{rgb(color)}" Style="1" Transparency="0" Type="1"/>')


def clip(rect, p, q):
    """Điểm trên biên rect theo hướng từ tâm p tới q."""
    x, y, wd, h = rect
    cx, cy = p
    dx, dy = q[0] - cx, q[1] - cy
    if dx == 0 and dy == 0:
        return p
    tx = (wd / 2) / abs(dx) if dx else float("inf")
    ty = (h / 2) / abs(dy) if dy else float("inf")
    t = min(tx, ty)
    return (cx + dx * t, cy + dy * t)


w(1, "<Diagrams>")
for dg in DIAGRAMS:
    pos = dg["pos"]
    rects = {}
    for k, (x, y) in pos.items():
        c = CLASSES[k]
        wd = dg.get("width", {}).get(k, c["W"])
        rects[k] = (x, y, wd, c["H"])
    shape_ids = {k: nid("S") for k in pos}

    w(2, f"<ClassDiagram {A(Id=nid('D'), Name=dg['name'], ConnectorStyle='0')}>")
    w(3, "<Shapes>")

    def class_shape(d, k):
        x, y, wd, h = rects[k]
        c = CLASSES[k]
        color = COLORS[c["pkg"]]
        w(d, f"<Class {A(Id=shape_ids[k], Model=c['id'], MetaModelElement=c['id'], Name=k, X=int(x), Y=int(y), Width=int(wd), Height=int(h), RequestResetCaption='true', Background=rgb(color))}>")
        fill(d + 1, color)
        w(d, "</Class>")

    # package frames
    groups = {}
    for k in pos:
        groups.setdefault(CLASSES[k]["pkg"], []).append(k)
    for p, keys in groups.items():
        frame = dg.get("frames", {}).get(p)
        if frame is None:
            x0 = min(rects[k][0] for k in keys) - 20
            y0 = min(rects[k][1] for k in keys) - 50
            x1 = max(rects[k][0] + rects[k][2] for k in keys) + 20
            y1 = max(rects[k][1] + rects[k][3] for k in keys) + 20
        else:
            x0, y0, x1, y1 = frame
        label = dg.get("pkg_labels", {}).get(p, pkg_label(p))
        model_id = PKG_IDS[p]
        w(3, f"<Package {A(Id=nid('S'), Model=model_id, MetaModelElement=model_id, Name=label, X=int(x0), Y=int(y0), Width=int(x1 - x0), Height=int(y1 - y0), RequestResetCaption='true', Background='rgb(255, 255, 255)')}>")
        fill(4, (250, 250, 250))
        w(4, "<DiagramElementChildren>")
        for k in keys:
            class_shape(5, k)
        w(4, "</DiagramElementChildren>")
        w(3, "</Package>")
    for nkey, n in dg.get("notes", {}).items():
        mid = NOTE_IDS[(dg["name"], nkey)]
        w(3, f"<NOTE {A(Id=nid('S'), Model=mid, MetaModelElement=mid, X=n['x'], Y=n['y'], Width=n['w'], Height=n['h'], Background='rgb(255, 253, 231)')}>")
        fill(4, (255, 253, 231))
        w(3, "</NOTE>")
    w(3, "</Shapes>")

    w(3, "<Connectors>")
    for r in rels:
        if r["f"] not in pos or r["t"] not in pos:
            continue
        if (r["f"], r["t"]) in dg.get("skip", set()):
            continue
        rf, rt = rects[r["f"]], rects[r["t"]]
        cf = (rf[0] + rf[2] / 2, rf[1] + rf[3] / 2)
        ct = (rt[0] + rt[2] / 2, rt[1] + rt[3] / 2)
        # điểm neo tuỳ chỉnh: "from"/"to" = (x, y) tuyệt đối hoặc tỉ lệ (0..1) trên shape; "via" = waypoints
        route = dg.get("routes", {}).get((r["f"], r["t"]), {})

        def anchor(rect, v):
            if max(v) <= 1.0:
                return (rect[0] + rect[2] * v[0], rect[1] + rect[3] * v[1])
            return v

        if "from" in route:
            cf = anchor(rf, route["from"])
        if "to" in route:
            ct = anchor(rt, route["to"])
        mids = route.get("via", [])
        p1 = clip(rf, (rf[0] + rf[2] / 2, rf[1] + rf[3] / 2), mids[0] if mids else ct) if "from" not in route else cf
        p2 = clip(rt, (rt[0] + rt[2] / 2, rt[1] + rt[3] / 2), mids[-1] if mids else cf) if "to" not in route else ct
        pts = [p1] + list(mids) + [p2]
        tag = {"gen": "Generalization", "assoc": "Association", "dep": "Dependency"}[r["kind"]]
        sf, st = shape_ids[r["f"]], shape_ids[r["t"]]
        if r["kind"] == "gen":  # From = lớp cha
            sf, st = st, sf
            pts = pts[::-1]
        w(4, f"<{tag} {A(Id=nid('CN'), Model=r['id'], MetaModelElement=r['id'], From=sf, To=st, RequestResetCaption='true')}>")
        w(5, "<Points>")
        for (px, py) in pts:
            w(6, f'<Point X="{px:.1f}" Y="{py:.1f}"/>')
        w(5, "</Points>")
        w(4, f"</{tag}>")
    w(3, "</Connectors>")
    w(2, "</ClassDiagram>")
gen_seq.diagrams(CTX)
w(1, "</Diagrams>")
w(0, "</Project>")

OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text("\n".join(out), encoding="utf-8")
if "--sizes" in sys.argv:
    for k, c in CLASSES.items():
        print(f"{k:32s} {c['W']:4d} x {c['H']:4d}")
print("written", OUT)
