#!/usr/bin/env python3
"""Check publishable source without printing potential credential values."""
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SKIP = {'.git', '.idea', '.vscode', 'node_modules', 'target', 'dist', 'build', '__pycache__'}
BINARY = {'.png', '.jpg', '.jpeg', '.webp', '.glb', '.ico', '.woff', '.woff2'}
FORBIDDEN = {'.pem', '.key', '.p12', '.pfx', '.jks', '.keystore', '.dump', '.zip', '.sqlite', '.log'}
PATTERNS = {
    'private key': re.compile(r'-----BEGIN (?:RSA |EC |OPENSSH |DSA |ENCRYPTED )?PRIVATE KEY-----'),
    'Google API key': re.compile(r'AIza[0-9A-Za-z_-]{35}'),
    'GitHub token': re.compile(r'(?:gh[pousr]_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{30,})'),
    'AWS access key': re.compile(r'(?:AKIA|ASIA)[A-Z0-9]{16}'),
    'JWT literal': re.compile(r'eyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}'),
    'hardcoded secret literal': re.compile(r'''(?im)\b(?:[\w]*(?:password|secretKey|apiKey|secret_key|api_key))[ \t]*=[ \t]*["']([^"'\r\n]{8,})["']'''),
    'credential in URL': re.compile(r'\b(?:https?|postgres(?:ql)?|redis)://[^\s/:]+:[^\s/@]+@'),
    'embedded credential': re.compile(r'''(?im)^[ \t]*(?:[A-Z_]*(?:PASSWORD|SECRET_KEY|API_KEY)|password|secret-key|api-key)[ \t]*[:=][ \t]*([^\r\n]*)'''),
}


def scan(paths):
    failures = []
    for path in paths:
        rel = path.relative_to(ROOT)
        if path.is_symlink():
            failures.append((str(rel), 'symlink requires manual inspection'))
            continue
        if path.name.startswith('.env') and path.name != '.env.example':
            failures.append((str(rel), 'local environment file'))
            continue
        if path.suffix in FORBIDDEN:
            failures.append((str(rel), 'private/runtime artifact'))
            continue
        if path.suffix in BINARY:
            continue
        try:
            content = path.read_text(encoding='utf-8')
        except (UnicodeError, OSError):
            failures.append((str(rel), 'unrecognized binary/unreadable file'))
            continue
        for label, pattern in PATTERNS.items():
            for match in pattern.finditer(content):
                if label == 'embedded credential':
                    if path.suffix not in {'.yml', '.yaml', '.properties', '.example', '.env'}:
                        continue
                    value = match.group(1).strip().strip('"\'').strip()
                    if not value or value.startswith(('${', '#', '//')):
                        continue
                line = content.count('\n', 0, match.start()) + 1
                failures.append((f'{rel}:{line}', label))
    return failures


def main():
    result = subprocess.run(['git', '-C', str(ROOT), 'ls-files', '-z'], capture_output=True)
    if result.returncode == 0:
        paths = [ROOT / p for p in result.stdout.decode().split('\0') if p]
        paths = [p for p in paths if p.exists()]
        mode = 'tracked files'
    else:
        paths = [p for p in ROOT.rglob('*') if p.is_file() and not (set(p.relative_to(ROOT).parts) & SKIP)]
        mode = 'source files (no Git history present)'
    failures = scan(paths)
    if failures:
        for location, label in failures:
            print(f'{location}: {label}')
        print(f'FAIL: {len(failures)} findings; credential values withheld.')
        return 1
    print(f'PASS: {len(paths)} {mode}; no supported secret patterns detected.')
    return 0


if __name__ == '__main__':
    sys.exit(main())
