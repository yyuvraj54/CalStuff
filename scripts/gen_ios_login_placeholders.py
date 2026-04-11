#!/usr/bin/env python3
"""Write minimal PNGs for iOS Assets (stdlib only)."""
from __future__ import annotations

import math
import struct
import zlib


def _chunk(tag: bytes, data: bytes) -> bytes:
    return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)


def write_png_rgb(path: str, width: int, height: int, pixel_rgb: bytes) -> None:
    if len(pixel_rgb) != width * height * 3:
        raise ValueError("pixel_rgb size mismatch")
    raw = bytearray()
    for y in range(height):
        row = bytearray()
        row.append(0)  # filter: None
        for x in range(width):
            i = (y * width + x) * 3
            row.extend(pixel_rgb[i : i + 3])
        raw.extend(row)
    compressed = zlib.compress(bytes(raw), level=9)
    signature = b"\x89PNG\r\n\x1a\n"
    ihdr = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)
    body = signature + _chunk(b"IHDR", ihdr) + _chunk(b"IDAT", compressed) + _chunk(b"IEND", b"")
    with open(path, "wb") as f:
        f.write(body)


def main() -> None:
    import os
    import sys

    # Default: sibling Desktop ios app Assets imagesets
    default = os.path.normpath(
        os.path.join(os.path.dirname(__file__), "..", "..", "ios", "CalStuff", "CalStuff", "Assets.xcassets")
    )
    base = sys.argv[1] if len(sys.argv) > 1 else default
    os.makedirs(os.path.join(base, "login_bg.imageset"), exist_ok=True)
    os.makedirs(os.path.join(base, "google_btn_icon.imageset"), exist_ok=True)
    w, h = 800, 520
    # Soft blue → lavender vertical-ish gradient (placeholder hero)
    pix = bytearray(w * h * 3)
    for y in range(h):
        t = y / max(h - 1, 1)
        r = int(227 + (243 - 227) * t)
        g = int(242 + (229 - 242) * t)
        b = int(253 + (245 - 253) * t)
        for x in range(w):
            i = (y * w + x) * 3
            pix[i] = r
            pix[i + 1] = g
            pix[i + 2] = b
    write_png_rgb(os.path.join(base, "login_bg.imageset", "login_bg.png"), w, h, bytes(pix))

    # Simple "G" on brand-red circle for Google button placeholder (24px logical → 72px @3x)
    size = 72
    pix2 = bytearray(size * size * 3)
    cx, cy = size // 2, size // 2
    r0 = size // 2 - 2
    for y in range(size):
        for x in range(size):
            i = (y * size + x) * 3
            dx, dy = x - cx, y - cy
            if dx * dx + dy * dy <= r0 * r0:
                pix2[i] = 255
                pix2[i + 1] = 107
                pix2[i + 2] = 107
            else:
                pix2[i] = 255
                pix2[i + 1] = 107
                pix2[i + 2] = 107
    # crude white arc for "G" hint
    for angle in range(0, 270, 3):
        rad = math.radians(angle)
        gx = int(cx + r0 * 0.45 * math.cos(rad))
        gy = int(cy + r0 * 0.45 * math.sin(rad))
        if 0 <= gx < size and 0 <= gy < size:
            j = (gy * size + gx) * 3
            pix2[j] = 255
            pix2[j + 1] = 255
            pix2[j + 2] = 255
    write_png_rgb(os.path.join(base, "google_btn_icon.imageset", "google_btn_icon.png"), size, size, bytes(pix2))
    print("Wrote imagesets under:", base)


if __name__ == "__main__":
    main()
