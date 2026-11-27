"""H06 protected-login candidate contract; direct execution is quarantined.

The previous direct ConPTY helper had no outer process-tree watchdog and
could write an existing login file. Do not launch mysql_config_editor here.
Future parent must establish a fresh native protected directory/file absence,
pin the toolchain, supervise the complete process tree, validate actual exit
and file identities/ACL, prove contained login and reconcile failure custody.
A caller-supplied successful result cannot establish any of those facts.
"""
import re
import sys

MAX_PRINT = 8192
SECTIONS = {"c9-backup": "c9_backup", "c9-restore": "c9_restore"}

def require(condition):
    if not condition:
        raise ValueError("C9W_PROTECTED_LOGIN_RESULT_REFUSED")

def validate_editor_result(section, user, set_exit, print_exit, output):
    """Pure strict masked-print/exit oracle; never emits raw output or secrets."""
    require(type(section) is str and section in SECTIONS)
    require(type(user) is str and user == SECTIONS[section])
    require(type(set_exit) is int and set_exit == 0)
    require(type(print_exit) is int and print_exit == 0)
    require(type(output) is bytes and 0 < len(output) <= MAX_PRINT)
    try:
        value = output.decode("ascii").replace("\r\n", "\n")
    except UnicodeError:
        raise ValueError("C9W_PROTECTED_LOGIN_RESULT_REFUSED") from None
    require("\r" not in value and "\x00" not in value)
    lines = value.split("\n")
    # Permit a single final newline; reject other blank lines and extra sections.
    if lines[-1] == "":
        lines.pop()
    require(len(lines) == 4 and lines[0] == "[" + section + "]")
    fields = {}
    for line in lines[1:]:
        match = re.fullmatch(r"[ \t]*(user|password|host)[ \t]*=[ \t]*(.*?)[ \t]*", line)
        require(match is not None)
        key, field = match.groups()
        require(key not in fields)
        if field.startswith('"') or field.endswith('"'):
            require(len(field) >= 2 and field.startswith('"') and field.endswith('"'))
            field = field[1:-1]
        fields[key] = field
    require(set(fields) == {"user", "password", "host"})
    require(fields["user"] == user and fields["host"] == "127.0.0.1")
    require(re.fullmatch(r"\*{1,128}", fields["password"]) is not None)

if __name__ == "__main__":
    # Refusal before environment/credential/file/tool access. The old caller
    # must remain unusable until the native parent integration is reviewed.
    print("C9W_PROTECTED_LOGIN_REFUSED_H06_INCOMPLETE", file=sys.stderr)
    sys.exit(1)
