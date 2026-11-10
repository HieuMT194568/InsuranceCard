"""Sinh use case diagram + sequence diagram (Visual Paradigm simple XML) cho Phần 1.

Được gọi từ gen_vp.py: models(...) ghi model element, diagrams(...) ghi diagram.
Nội dung (mô tả UC, lifeline, thông điệp, luồng thay thế) nằm trong seq_spec.py.
"""
from seq_spec import ACTORS, USE_CASES, UC_LINKS, UC_LAYOUT, SEQUENCES

# ------------------------------------------------------------------ hằng số bố cục
TOP = 40            # y đầu lifeline
HEAD = 50           # chiều cao phần đầu lifeline (hộp tên)
FIRST_MSG = 125     # y thông điệp đầu tiên
STEP = 34           # khoảng cách giữa 2 thông điệp
SELF_H = 18         # chiều cao vòng self message
ACT_W = 12          # bề rộng activation
FRAG_HEAD = 40      # khoảng trống đầu fragment (nhãn alt + guard operand đầu)
OPER_HEAD = 26      # khoảng trống đầu operand tiếp theo (guard)
CHAR = 6.0          # bề rộng ước lượng 1 ký tự (font Dialog 11)


# ------------------------------------------------------------------ tiện ích
def _iter_msgs(steps):
    for s in steps:
        if s[0] in ("alt", "opt", "loop"):
            for _, sub in s[1]:
                yield from _iter_msgs(sub)
        else:
            yield s


def _depth(steps):
    d = 0
    for s in steps:
        if s[0] in ("alt", "opt", "loop"):
            d = max(d, 1 + max(_depth(sub) for _, sub in s[1]))
    return d


class Ctx:
    """Những thứ dùng chung với gen_vp.py."""

    def __init__(self, w, A, nid, classes, stereo_id):
        self.w, self.A, self.nid, self.classes, self.stereo_id = w, A, nid, classes, stereo_id


# ------------------------------------------------------------------ use case model
def _uc_models(ctx):
    w, A, nid = ctx.w, ctx.A, ctx.nid
    for a in ACTORS.values():
        a["id"] = nid("UA")
        w(2, f"<Actor {A(Id=a['id'], Name=a['name'], Documentation_plain=a.get('doc', ''))}/>")
    UC_LAYOUT["system_id"] = nid("SY")
    w(2, f"<System {A(Id=UC_LAYOUT['system_id'], Name=UC_LAYOUT['system_name'])}>")
    w(3, "<ModelChildren>")
    for uc in USE_CASES.values():
        uc["id"] = nid("UC")
        w(4, f"<UseCase {A(Id=uc['id'], Name=uc['name'], Documentation_plain=uc.get('doc', ''))}/>")
    w(3, "</ModelChildren>")
    w(2, "</System>")


def _uc_relationships(ctx):
    w, A, nid = ctx.w, ctx.A, ctx.nid

    def ref(k):
        return ACTORS[k]["id"] if k in ACTORS else USE_CASES[k]["id"]

    def tag(k):
        return "Actor" if k in ACTORS else "UseCase"

    for link in UC_LINKS:
        kind, f, t = link[:3]
        link_id = nid("UR")
        link_ids.append(link_id)
        if kind == "assoc":
            e1, e2 = nid("AE"), nid("AE")
            w(6, f"<Association {A(Id=link_id, EndRelationshipFromMetaModelElement=ref(f), EndRelationshipToMetaModelElement=ref(t))}>")
            for end_tag, eid, k in (("FromEnd", e1, f), ("ToEnd", e2, t)):
                w(7, f"<{end_tag}>")
                w(8, f"<AssociationEnd {A(Id=eid, EndModelElement=ref(k), Multiplicity='Unspecified', Navigable='Unspecified', AggregationKind='None')}>")
                w(9, f"<Type><{tag(k)} {A(Idref=ref(k), Name=k)}/></Type>")
                w(8, "</AssociationEnd>")
                w(7, f"</{end_tag}>")
            w(6, "</Association>")
        elif kind == "gen":   # f = con, t = cha; VP: From = cha
            w(6, f"<Generalization {A(Id=link_id, From=ref(t), To=ref(f))}/>")
        elif kind == "include":   # f «include» t (VP: From = UC cơ sở, mũi tên ở To)
            w(6, f"<Include {A(Id=link_id, From=ref(f), To=ref(t), Name='«include»')}/>")
        elif kind == "extend":    # f «extend» t (VP vẽ mũi tên ở From -> From = UC cơ sở t)
            w(6, f"<Extend {A(Id=link_id, From=ref(t), To=ref(f), Name='«extend»')}/>")


link_ids = []


# ------------------------------------------------------------------ sequence layout
def _layout(sd):
    """Tính toạ độ: x tâm lifeline, y từng thông điệp, khung fragment, activation."""
    lls = sd["lifelines"]
    keys = [l[0] for l in lls]
    idx = {k: i for i, k in enumerate(keys)}

    # bề rộng nhãn lifeline
    labels = {}
    for key, cls, inst, stereo in lls:
        if cls is None:
            labels[key] = inst
        else:
            labels[key] = f"{inst} : {cls}"
    widths = {k: max(110, int(len(labels[k]) * CHAR + 30)) for k in keys}

    # khoảng cách tâm 2 lifeline liền kề: đủ chỗ cho nhãn thông điệp dài nhất giữa chúng
    need = [0] * (len(keys) - 1)
    for m in _iter_msgs(sd["steps"]):
        kind, f, t, text = m
        if kind == "self":
            i = idx[f]
            if i < len(need):
                need[i] = max(need[i], len(text) * CHAR + 50)
            continue
        a, b = sorted((idx[f], idx[t]))
        per = (len(text) * CHAR + 50) / (b - a)
        for i in range(a, b):
            need[i] = max(need[i], per)

    # guard của fragment nằm ở góc trái; activation/nhãn của lifeline bên trong sẽ che chữ,
    # nên chừa khoảng trống bên trái lifeline đầu tiên của fragment.
    def guard_w(operands):
        return max(len(f"[{g}]") * CHAR + 16 for g, _ in operands)

    def involved(operands):
        inv = set()
        for _, sub in operands:
            for m in _iter_msgs(sub):
                inv |= {m[1], m[2]}
        return inv

    left_margin = 60.0

    def reserve(steps, level):
        nonlocal left_margin
        for s in steps:
            if s[0] in ("alt", "opt", "loop"):
                inner = 1 + max(_depth(sub) for _, sub in s[1])
                lo = min(idx[k] for k in involved(s[1]))
                room = guard_w(s[1]) + 36 + 14 * (level + inner)
                if lo == 0:
                    left_margin = max(left_margin, room - widths[keys[0]] / 2 + 10)
                else:
                    need[lo - 1] = max(need[lo - 1], room + 20)
                for _, sub in s[1]:
                    reserve(sub, level + 1)

    reserve(sd["steps"], 0)
    cx = {keys[0]: left_margin + widths[keys[0]] / 2}
    for i in range(1, len(keys)):
        gap = max(need[i - 1], (widths[keys[i - 1]] + widths[keys[i]]) / 2 + 30)
        cx[keys[i]] = cx[keys[i - 1]] + gap

    msgs, frags = [], []
    created = {}
    y = FIRST_MSG

    def run(steps, level):
        """Tính y; trả về danh sách fragment cấp này (để tính x sau)."""
        nonlocal y
        mine = []
        for s in steps:
            if s[0] in ("alt", "opt", "loop"):
                op, operands = s
                frag = dict(op=op, y0=y - 14, operands=[], children=[], operands_raw=operands)
                y += FRAG_HEAD
                for j, (guard, sub) in enumerate(operands):
                    if j == 0:
                        o = dict(guard=guard, y0=frag["y0"])
                    else:
                        o = dict(guard=guard, y0=y - 14)
                        y += OPER_HEAD
                    frag["children"] += run(sub, level + 1)
                    frag["operands"].append(o)
                frag["y1"] = y - 10
                for j, o in enumerate(frag["operands"]):
                    o["y1"] = frag["operands"][j + 1]["y0"] if j + 1 < len(frag["operands"]) else frag["y1"]
                frags.append(frag)
                mine.append(frag)
                y = frag["y1"] + 26
            else:
                kind, f, t, text = s
                msgs.append(dict(kind=kind, f=f, t=t, text=text, y=y))
                if kind == "create":
                    created[t] = y
                y += STEP + (SELF_H if kind == "self" else 0)
        return mine

    def place_x(frag):
        for ch in frag["children"]:
            place_x(ch)
        inv = involved(frag["operands_raw"])
        lo = min(cx[k] for k in inv)
        hi = max(cx[k] for k in inv)
        x0 = lo - ACT_W / 2 - 14 - guard_w(frag["operands_raw"])
        x1 = hi + ACT_W / 2 + 40
        for m in _iter_msgs([("alt", frag["operands_raw"])]):
            if m[0] == "self":
                x1 = max(x1, cx[m[1]] + 50 + len(m[3]) * CHAR)
        for ch in frag["children"]:
            x0 = min(x0, ch["x0"] - 14)
            x1 = max(x1, ch["x1"] + 14)
        frag["x0"], frag["x1"] = max(x0, 8), x1

    for top_frag in run(sd["steps"], 0):
        place_x(top_frag)
    bottom = y + 20

    # activation: từ thông điệp đầu tiên tới cuối cùng liên quan tới lifeline
    acts = {}
    for k in keys:
        ys = [m["y"] for m in msgs if k in (m["f"], m["t"])]
        ys += [m["y"] + SELF_H for m in msgs if m["kind"] == "self" and m["f"] == k]
        if not ys:
            continue
        y0, y1 = min(ys) - 8, max(ys) + 10
        if k in created:
            # đối tượng được tạo: activation bắt đầu ngay dưới hộp tên (ít nhất 16px)
            y0 = created[k] + HEAD / 2 + 4
            y1 = max(y1, y0 + 16)
        acts[k] = (y0, y1)

    return dict(keys=keys, labels=labels, widths=widths, cx=cx, msgs=msgs, frags=frags,
                acts=acts, created=created, bottom=bottom)


# ------------------------------------------------------------------ model + diagram cho sequence
def models(ctx):
    """Ghi model: actor/use case + Frame (interaction) cho mỗi sequence diagram."""
    w, A, nid = ctx.w, ctx.A, ctx.nid
    _uc_models(ctx)
    for sd in SEQUENCES:
        sd["L"] = lay = _layout(sd)
        sd["frame_id"] = nid("FR")
        sd["ll_ids"] = {}
        sd["act_ids"] = {}
        w(2, f"<Frame {A(Id=sd['frame_id'], Name=sd['name'], Type='sd')}>")
        w(3, "<ModelChildren>")
        for key, cls, inst, stereo in sd["lifelines"]:
            mid = nid("LL")
            sd["ll_ids"][key] = mid
            if cls is None:
                w(4, f"<InteractionActor {A(Id=mid, Name=inst)}>")
            else:
                w(4, f"<InteractionLifeLine {A(Id=mid, Name=inst)}>")
                w(5, f"<BaseClassifier><Class {A(Idref=ctx.classes[cls]['id'], Name=cls)}/></BaseClassifier>")
                if stereo:
                    w(5, f"<Stereotypes><Stereotype {A(Idref=ctx.stereo_id(stereo, 'InteractionLifeLine'), Name=stereo)}/></Stereotypes>")
            if key in lay["acts"]:
                aid = nid("AC")
                sd["act_ids"][key] = aid
                w(5, "<ModelChildren>")
                w(6, f"<Activation {A(Id=aid, Name='Activation')}/>")
                w(5, "</ModelChildren>")
            w(4, "</InteractionActor>" if cls is None else "</InteractionLifeLine>")
        for fr in lay["frags"]:
            fr["id"] = nid("CF")
            w(4, f"<CombinedFragment {A(Id=fr['id'], Name='', OperatorKind=fr['op'])}>")
            w(5, "<ModelChildren>")
            for o in fr["operands"]:
                o["id"] = nid("OP")
                w(6, f"<InteractionOperand {A(Id=o['id'], Name='')}>")
                w(7, f"<Guard><InteractionConstraint {A(Id=nid('GC'), Name='InteractionConstraint', Constraint=o['guard'])}/></Guard>")
                w(6, "</InteractionOperand>")
            w(5, "</ModelChildren>")
            w(4, "</CombinedFragment>")
        w(3, "</ModelChildren>")
        w(2, "</Frame>")


def relationships(ctx):
    """Ghi các quan hệ (đặt bên trong ModelRelationshipContainer 'relationships')."""
    w, A, nid = ctx.w, ctx.A, ctx.nid
    w(4, f"<ModelRelationshipContainer {A(Id=nid('RC'), Name='UseCaseLinks')}>")
    w(5, "<ModelChildren>")
    _uc_relationships(ctx)
    w(5, "</ModelChildren>")
    w(4, "</ModelRelationshipContainer>")

    w(4, f"<ModelRelationshipContainer {A(Id=nid('RC'), Name='Message')}>")
    w(5, "<ModelChildren>")
    for sd in SEQUENCES:
        lay = sd["L"]
        top = {}          # đánh số: thông điệp con theo lifeline đang xử lý
        counters = {}
        top_counter = [0]
        actor_keys = {l[0] for l in sd["lifelines"] if l[1] is None}
        for m in lay["msgs"]:
            m["id"] = nid("M")
            if m["kind"] == "return":
                m["num"] = ""
            else:
                f = m["f"]
                parent = top.get(f)
                if f in actor_keys or parent is None:
                    top_counter[0] += 1
                    m["num"] = str(top_counter[0])
                else:
                    counters[parent] = counters.get(parent, 0) + 1
                    m["num"] = f"{parent}.{counters[parent]}"
                if m["kind"] in ("call", "create"):
                    top[m["t"]] = m["num"]
            action = {"call": "ActionTypeCall", "self": "ActionTypeCall",
                      "return": "ActionTypeReturn", "create": "ActionTypeCreate"}[m["kind"]]
            w(6, f"<Message {A(Id=m['id'], Name=m['text'], SequenceNumber=m['num'] or None)}>")
            w(7, f"<ActionType><{action} {A(Id=nid('AT'), Name='')}/></ActionType>")
            w(7, f"<FromEnd><MessageEnd {A(Id=nid('ME'), EndModelElement=sd['ll_ids'][m['f']])}/></FromEnd>")
            w(7, f"<ToEnd><MessageEnd {A(Id=nid('ME'), EndModelElement=sd['ll_ids'][m['t']])}/></ToEnd>")
            w(6, "</Message>")
    w(5, "</ModelChildren>")
    w(4, "</ModelRelationshipContainer>")


def _rgb(t):
    return f"rgb({t[0]}, {t[1]}, {t[2]})"


STEREO_COLOR = {
    "boundary": (224, 242, 254),
    "control": (220, 252, 231),
    "entity": (255, 237, 213),
    None: (241, 245, 249),
}


def diagrams(ctx):
    w, A, nid = ctx.w, ctx.A, ctx.nid

    # -------------------------------------------------------------- use case diagram
    shape = {}
    w(2, f"<UseCaseDiagram {A(Id=nid('D'), Name=UC_LAYOUT['name'], ConnectorStyle='1')}>")
    w(3, "<Shapes>")
    sx, sy, sw, sh = UC_LAYOUT["system"]
    w(3, f"<System {A(Id=nid('S'), Model=UC_LAYOUT['system_id'], MetaModelElement=UC_LAYOUT['system_id'], Name=UC_LAYOUT['system_name'], X=sx, Y=sy, Width=sw, Height=sh, RequestResetCaption='true', Background='rgb(255, 255, 255)')}>")
    w(4, '<FillColor Color="rgb(255, 255, 255)" Style="1" Transparency="0" Type="1"/>')
    w(4, "<DiagramElementChildren>")
    for k, uc in USE_CASES.items():
        x, y = UC_LAYOUT["pos"][k]
        shape[k] = sid = nid("S")
        ww, hh = UC_LAYOUT.get("size", {}).get(k, (170, 60))
        UC_LAYOUT.setdefault("rect", {})[k] = (x, y, ww, hh)
        w(5, f"<UseCase {A(Id=sid, Model=uc['id'], MetaModelElement=uc['id'], Name=uc['name'], X=x, Y=y, Width=ww, Height=hh, RequestResetCaption='true', Background='rgb(255, 255, 255)')}>")
        w(6, '<FillColor Color="rgb(255, 255, 255)" Style="1" Transparency="0" Type="1"/>')
        w(5, "</UseCase>")
    w(4, "</DiagramElementChildren>")
    w(3, "</System>")
    for k, a in ACTORS.items():
        x, y = UC_LAYOUT["pos"][k]
        shape[k] = sid = nid("S")
        UC_LAYOUT["rect"][k] = (x, y, 30, 60)
        w(3, f"<Actor {A(Id=sid, Model=a['id'], MetaModelElement=a['id'], Name=a['name'], X=x, Y=y, Width=30, Height=60, RequestResetCaption='true', Background='rgb(224, 231, 255)')}>")
        w(4, '<FillColor Color="rgb(224, 231, 255)" Style="1" Transparency="0" Type="1"/>')
        w(3, "</Actor>")
    w(3, "</Shapes>")
    w(3, "<Connectors>")
    tags = {"assoc": "Association", "gen": "Generalization", "include": "Include", "extend": "Extend"}
    for link, lid in zip(UC_LINKS, link_ids):
        kind, f, t = link[:3]
        rf, rt = UC_LAYOUT["rect"][f], UC_LAYOUT["rect"][t]
        pf = (rf[0] + rf[2] / 2, rf[1] + rf[3] / 2)
        pt = (rt[0] + rt[2] / 2, rt[1] + rt[3] / 2)
        a, b = shape[f], shape[t]
        if kind in ("gen", "extend"):
            a, b, pf, pt = b, a, pt, pf
        w(4, f"<{tags[kind]} {A(Id=nid('CN'), Model=lid, MetaModelElement=lid, From=a, To=b, RequestResetCaption='true')}>")
        w(5, f'<Points><Point X="{pf[0]:.1f}" Y="{pf[1]:.1f}"/><Point X="{pt[0]:.1f}" Y="{pt[1]:.1f}"/></Points>')
        w(4, f"</{tags[kind]}>")
    w(3, "</Connectors>")
    w(2, "</UseCaseDiagram>")

    # -------------------------------------------------------------- sequence diagrams
    for sd in SEQUENCES:
        lay = sd["L"]
        ll_shape, act_shape = {}, {}
        w(2, f"<InteractionDiagram {A(Id=nid('D'), Name=sd['name'])}>")
        w(3, "<Shapes>")
        # fragments trước (nằm dưới lifeline)
        zo = 0
        for fr in sorted(lay["frags"], key=lambda f: f["x0"]):
            fid = nid("S")
            zo += 1
            w(3, f"<CombinedFragment {A(Id=fid, Model=fr['id'], MetaModelElement=fr['id'], X=int(fr['x0']), Y=int(fr['y0']), Width=int(fr['x1'] - fr['x0']), Height=int(fr['y1'] - fr['y0']), RequestResetCaption='true', ZOrder=zo)}>")
            w(4, "<DiagramElementChildren>")
            for o in fr["operands"]:
                w(5, f"<InteractionOperand {A(Id=nid('S'), Model=o['id'], MetaModelElement=o['id'], X=int(fr['x0']), Y=int(o['y0']), Width=int(fr['x1'] - fr['x0']), Height=int(o['y1'] - o['y0']), RequestResetCaption='true')}/>")
            w(4, "</DiagramElementChildren>")
            w(3, "</CombinedFragment>")
        for key, cls, inst, stereo in sd["lifelines"]:
            ll_shape[key] = sid = nid("S")
            cxk, wk = lay["cx"][key], lay["widths"][key]
            y0 = TOP if key not in lay["created"] else lay["created"][key] - HEAD / 2
            mid = sd["ll_ids"][key]
            color = STEREO_COLOR.get(stereo, STEREO_COLOR[None])
            if cls is None:
                tag, x, width = "InteractionActor", cxk - 20, 40
                color = (224, 231, 255)
            else:
                tag, x, width = "InteractionLifeLine", cxk - wk / 2, wk
            extra = {} if cls is None else {"OverrideAppearanceWithStereotypeIcon": "false"}
            w(3, f"<{tag} {A(Id=sid, Model=mid, MetaModelElement=mid, X=int(x), Y=int(y0), Width=int(width), Height=int(lay['bottom'] - y0), RequestResetCaption='true', Background=_rgb(color), ZOrder=100 + len(ll_shape), **extra)}>")
            w(4, f'<FillColor Color="{_rgb(color)}" Style="1" Transparency="0" Type="1"/>')
            if key in lay["acts"]:
                a0, a1 = lay["acts"][key]
                act_shape[key] = aid = nid("S")
                am = sd["act_ids"][key]
                w(4, "<DiagramElementChildren>")
                w(5, f"<Activation {A(Id=aid, Model=am, MetaModelElement=am, X=int(cxk - ACT_W / 2), Y=int(a0), Width=ACT_W, Height=int(a1 - a0), Background=_rgb(color))}>")
                w(6, f'<FillColor Color="{_rgb(color)}" Style="1" Transparency="0" Type="1"/>')
                w(5, "</Activation>")
                w(4, "</DiagramElementChildren>")
            w(3, f"</{tag}>")
        w(3, "</Shapes>")

        w(3, "<Connectors>")
        for m in lay["msgs"]:
            f, t, y = m["f"], m["t"], m["y"]
            src = act_shape.get(f, ll_shape[f])
            if m["kind"] == "create":
                dst = ll_shape[t]
            else:
                dst = act_shape.get(t, ll_shape[t])
            xf, xt = lay["cx"][f], lay["cx"][t]
            if m["kind"] == "self":
                x = xf + ACT_W / 2
                pts = [(x, y), (x + 30, y), (x + 30, y + SELF_H), (x, y + SELF_H)]
            else:
                d = 1 if xt > xf else -1
                p0 = xf + d * ACT_W / 2
                if m["kind"] == "create":
                    p1 = xt - d * lay["widths"][t] / 2
                else:
                    p1 = xt - d * ACT_W / 2
                pts = [(p0, y), (p1, y)]
            w(4, f"<Message {A(Id=nid('CN'), Model=m['id'], MetaModelElement=m['id'], From=src, To=dst, RequestResetCaption='true')}>")
            w(5, "<Points>" + "".join(f'<Point X="{px:.1f}" Y="{py:.1f}"/>' for px, py in pts) + "</Points>")
            w(4, "</Message>")
        w(3, "</Connectors>")
        w(2, "</InteractionDiagram>")
