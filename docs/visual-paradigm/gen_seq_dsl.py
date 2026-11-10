"""DSL mô tả bước trong sequence diagram (dùng ở seq_spec.py)."""


def C(f, t, text):
    return ("call", f, t, text)


def R(f, t, text):
    return ("return", f, t, text)


def N(f, t, text):
    return ("create", f, t, text)


def S(l, text):
    return ("self", l, l, text)


def ALT(*operands):
    return ("alt", list(operands))


def OPT(guard, *steps):
    return ("opt", [(guard, list(steps))])


def LOOP(guard, *steps):
    return ("loop", [(guard, list(steps))])
