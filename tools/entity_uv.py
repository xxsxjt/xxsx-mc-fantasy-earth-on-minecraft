from __future__ import annotations

import random
from pathlib import Path

from PIL import Image, ImageDraw


def color(value: str) -> tuple[int, int, int]:
    value = value.removeprefix("#")
    return tuple(int(value[index:index + 2], 16) for index in (0, 2, 4))


def shade(value: tuple[int, int, int], delta: int) -> tuple[int, int, int, int]:
    return tuple(max(0, min(255, channel + delta)) for channel in value) + (255,)


class Atlas:
    FACE_LIGHT = {"up": 18, "down": -26, "west": -7, "north": 5, "east": 9, "south": -12}

    def __init__(self, size: tuple[int, int], output_dir: Path) -> None:
        self.image = Image.new("RGBA", size, (0, 0, 0, 0))
        self.draw = ImageDraw.Draw(self.image)
        self.output_dir = output_dir

    def surface(self, rect: tuple[int, int, int, int], base: tuple[int, int, int], face: str,
                style: str, accent: tuple[int, int, int] | None, seed: int) -> None:
        x0, y0, x1, y1 = rect
        if x1 <= x0 or y1 <= y0:
            return
        rng = random.Random(seed)
        self.draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=shade(base, self.FACE_LIGHT[face]))
        if x1 - x0 > 2 and y1 - y0 > 2:
            self.draw.line((x0, y0, x1 - 1, y0), fill=shade(base, 25))
            self.draw.line((x0, y0, x0, y1 - 1), fill=shade(base, 14))
            self.draw.line((x0, y1 - 1, x1 - 1, y1 - 1), fill=shade(base, -28))
            self.draw.line((x1 - 1, y0, x1 - 1, y1 - 1), fill=shade(base, -18))
        width = x1 - x0
        height = y1 - y0
        if style == "fur":
            for y in range(y0 + 2, y1 - 1, 3):
                for x in range(x0 + 1 + ((y - y0) & 1), x1 - 1, 4):
                    self.draw.point((x, y), fill=shade(base, rng.choice((-15, 13, 20))))
        elif style == "cloth":
            for y in range(y0 + 3, y1 - 1, 4):
                self.draw.line((x0 + 1, y, x1 - 2, y), fill=shade(base, -8))
            for x in range(x0 + 2, x1 - 1, 5):
                for y in range(y0 + 1, y1 - 1, 4):
                    self.draw.point((x, y), fill=shade(base, 12))
        elif style == "stone":
            for _ in range(max(1, width * height // 18)):
                x = rng.randrange(x0, x1)
                y = rng.randrange(y0, y1)
                self.draw.point((x, y), fill=shade(base, rng.choice((-24, -13, 16))))
        elif style == "metal":
            if height >= 5:
                y = y0 + height // 2
                self.draw.line((x0, y, x1 - 1, y), fill=shade(base, -24))
                self.draw.line((x0, y - 1, x1 - 1, y - 1), fill=shade(base, 15))
            for x, y in ((x0 + 1, y0 + 1), (x1 - 2, y0 + 1), (x0 + 1, y1 - 2), (x1 - 2, y1 - 2)):
                if x0 <= x < x1 and y0 <= y < y1:
                    self.draw.point((x, y), fill=shade(base, 28))
        elif style == "crystal":
            crystal = accent or shade(base, 35)[:3]
            for offset in range(-height, width, 4):
                self.draw.line((x0 + offset, y1 - 1, x0 + offset + height, y0), fill=shade(crystal, 22))

    def box(self, uv: tuple[int, int], dims: tuple[float, float, float], base: str,
            style: str = "flat", accent: str | None = None, seed: int = 0) -> dict[str, tuple[int, int, int, int]]:
        u, v = uv
        width, height, depth = (max(1, int(round(value))) for value in dims)
        faces = {
            "down": (u + depth, v, u + depth + width, v + depth),
            "up": (u + depth + width, v, u + depth + width * 2, v + depth),
            "west": (u, v + depth, u + depth, v + depth + height),
            "north": (u + depth, v + depth, u + depth + width, v + depth + height),
            "east": (u + depth + width, v + depth, u + depth + width + depth, v + depth + height),
            "south": (u + depth + width + depth, v + depth,
                      u + depth + width + depth + width, v + depth + height),
        }
        base_color = color(base)
        accent_color = color(accent) if accent else None
        for index, (face, rect) in enumerate(faces.items()):
            self.surface(rect, base_color, face, style, accent_color, seed * 11 + index)
        return faces

    def save(self, name: str) -> None:
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.image.save(self.output_dir / f"{name}.png")


def point(draw: ImageDraw.ImageDraw, rect: tuple[int, int, int, int], x: int, y: int,
          fill: tuple[int, int, int, int]) -> None:
    x0, y0, x1, y1 = rect
    if 0 <= x < x1 - x0 and 0 <= y < y1 - y0:
        draw.point((x0 + x, y0 + y), fill=fill)


def line(draw: ImageDraw.ImageDraw, rect: tuple[int, int, int, int], points: list[tuple[int, int]],
         fill: tuple[int, int, int, int]) -> None:
    x0, y0, _, _ = rect
    draw.line([(x0 + x, y0 + y) for x, y in points], fill=fill, width=1)


def side_rune(atlas: Atlas, rect: tuple[int, int, int, int], accent: str) -> None:
    width = rect[2] - rect[0]
    height = rect[3] - rect[1]
    if width < 6 or height < 5:
        return
    line(atlas.draw, rect,
         [(1, height // 2), (3, 1), (width // 2, 2), (width // 2 + 1, height - 2),
          (width - 2, height // 2), (width - 4, 1)], shade(color(accent), 18))


def wolf_face(atlas: Atlas, rect: tuple[int, int, int, int], accent: str) -> None:
    bright = shade(color(accent), 25)
    dark = shade(color("17191d"), 0)
    for x in (1, 5):
        point(atlas.draw, rect, x, 2, dark)
        point(atlas.draw, rect, x + 1, 2, bright)
    line(atlas.draw, rect, [(3, 0), (3, 1), (2, 2), (3, 3), (4, 2), (3, 1)], bright)


def muzzle(atlas: Atlas, rect: tuple[int, int, int, int]) -> None:
    x0, y0, x1, y1 = rect
    cx = (x0 + x1) // 2
    atlas.draw.rectangle((max(x0, cx - 1), y0, min(x1 - 1, cx + 1), min(y1 - 1, y0 + 1)),
                         fill=shade(color("111318"), 0))


def human_face(atlas: Atlas, rect: tuple[int, int, int, int], hair: str, eye: str,
               beard: bool, goblin: bool = False) -> None:
    dark_hair = shade(color(hair), -12)
    for x in range(1, 7):
        point(atlas.draw, rect, x, 0, dark_hair)
    for x in (1, 2, 5, 6):
        point(atlas.draw, rect, x, 1, dark_hair)
    for x in (2, 5):
        point(atlas.draw, rect, x, 3, shade(color("f4f0db"), 0))
        point(atlas.draw, rect, x, 4, shade(color(eye), 12))
    if goblin:
        point(atlas.draw, rect, 1, 5, shade(color("406030"), -8))
        point(atlas.draw, rect, 6, 5, shade(color("406030"), -8))
    if beard:
        for x, y in ((1, 6), (2, 6), (3, 7), (4, 7), (5, 6), (6, 6)):
            point(atlas.draw, rect, x, y, dark_hair)


def robe_front(atlas: Atlas, rect: tuple[int, int, int, int], trim: str) -> None:
    width = rect[2] - rect[0]
    height = rect[3] - rect[1]
    cx = width // 2
    trim_color = shade(color(trim), 12)
    line(atlas.draw, rect, [(cx - 2, 0), (cx, 3), (cx + 2, 0)], trim_color)
    if height > 7:
        line(atlas.draw, rect, [(0, 7), (width - 1, 7)], shade(color("8f6e37"), 0))
        point(atlas.draw, rect, cx, 7, shade(color("d3aa4f"), 8))


def pack_back(atlas: Atlas, rect: tuple[int, int, int, int], accent: str) -> None:
    width = rect[2] - rect[0]
    height = rect[3] - rect[1]
    for x in range(2, width - 1, 3):
        line(atlas.draw, rect, [(x, 1), (x, height - 2)], shade(color("2e271e"), -5))
    if width >= 7 and height >= 7:
        cx, cy = width // 2, height // 2
        line(atlas.draw, rect, [(cx - 2, cy), (cx, cy - 2), (cx + 2, cy), (cx, cy + 2), (cx - 2, cy)],
             shade(color(accent), 14))
