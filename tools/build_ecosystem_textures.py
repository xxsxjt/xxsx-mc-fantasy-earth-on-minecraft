from __future__ import annotations

import random
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
MASTER = ROOT / "tmp" / "imagegen" / "ecosystem-0.7"
ASSETS = ROOT / "neoforge-26.2" / "src" / "main" / "resources" / "assets" / "earth_online_magic"


def hex_color(value: str) -> tuple[int, int, int]:
    value = value.removeprefix("#")
    return tuple(int(value[index:index + 2], 16) for index in (0, 2, 4))


def shade(color: tuple[int, int, int], delta: int) -> tuple[int, int, int, int]:
    return tuple(max(0, min(255, channel + delta)) for channel in color) + (255,)


def palette_from(path: Path, crop: tuple[int, int, int, int], fallback: list[str]) -> list[tuple[int, int, int]]:
    if not path.exists():
        return [hex_color(value) for value in fallback]
    image = Image.open(path).convert("RGB").crop(crop)
    quantized = image.quantize(colors=12, method=Image.Quantize.MEDIANCUT)
    colors = quantized.getpalette()[:36]
    ranked = sorted(quantized.getcolors() or [], reverse=True)
    result: list[tuple[int, int, int]] = []
    for _, index in ranked:
        color = tuple(colors[index * 3:index * 3 + 3])
        brightness = sum(color) / 3
        if brightness < 23 or brightness > 244:
            continue
        if all(sum(abs(a - b) for a, b in zip(color, old)) > 42 for old in result):
            result.append(color)
        if len(result) == 7:
            break
    return result if len(result) >= 4 else [hex_color(value) for value in fallback]


def material_texture(size: tuple[int, int], palette: list[tuple[int, int, int]], seed: int,
                     accent: tuple[int, int, int], secondary: tuple[int, int, int] | None = None,
                     plate: bool = False) -> Image.Image:
    random.seed(seed)
    width, height = size
    image = Image.new("RGBA", size, shade(palette[0], -8))
    draw = ImageDraw.Draw(image)
    for y in range(0, height, 2):
        for x in range(0, width, 2):
            base = palette[(x // 2 * 5 + y // 2 * 3 + random.randrange(len(palette))) % len(palette)]
            delta = random.choice((-20, -11, -5, 0, 5, 10))
            draw.rectangle((x, y, min(width - 1, x + 1), min(height - 1, y + 1)), fill=shade(base, delta))
    if plate:
        for y in range(6, height, 12):
            draw.line((0, y, width - 1, y), fill=shade(palette[0], -38), width=1)
        for x in range(8, width, 17):
            draw.line((x, 0, x, height - 1), fill=shade(palette[-1], -20), width=1)
    for offset in range(-height, width, 18):
        points = []
        for y in range(height):
            x = offset + y // 2 + int(2 * ((y // 5) % 2))
            if 0 <= x < width:
                points.append((x, y))
        if len(points) > 3:
            draw.line(points, fill=shade(accent, 12), width=1)
    if secondary:
        for x in range(5, width, 19):
            draw.rectangle((x, 3, min(width - 1, x + 2), min(height - 1, height - 4)),
                           outline=shade(secondary, -5), width=1)
    return image


def save_entity(name: str, size: tuple[int, int], master: str, crop: tuple[int, int, int, int],
                fallback: list[str], seed: int, accent: str, secondary: str | None = None,
                plate: bool = False) -> None:
    palette = palette_from(MASTER / master, crop, fallback)
    image = material_texture(size, palette, seed, hex_color(accent),
                             hex_color(secondary) if secondary else None, plate)
    out = ASSETS / "textures" / "entity" / f"{name}.png"
    out.parent.mkdir(parents=True, exist_ok=True)
    image.save(out)


def egg_icon(path: Path, base: str, spots: str, rune: str) -> None:
    image = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    b = hex_color(base)
    s = hex_color(spots)
    r = hex_color(rune)
    for y in range(3, 29):
        half = int(10 * (1.0 - abs(y - 17) / 18.0)) + 2
        for x in range(16 - half, 17 + half):
            edge = min(x - (16 - half), (16 + half) - x, y - 3, 28 - y)
            draw.point((x, y), fill=shade(b, -18 if edge < 1 else (10 if (x + y) % 5 == 0 else 0)))
    for x, y in ((11, 10), (20, 13), (13, 20), (21, 23), (16, 7)):
        draw.rectangle((x, y, x + 2, y + 2), fill=shade(s, 4))
    draw.line((12, 17, 16, 12, 20, 17, 16, 22, 12, 17), fill=shade(r, 20), width=1)
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def contract_icon(path: Path) -> None:
    image = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    paper = hex_color("d7d0bc")
    brass = hex_color("bd8f3c")
    violet = hex_color("8754d6")
    cyan = hex_color("48cfe0")
    draw.rounded_rectangle((4, 3, 27, 28), radius=2, fill=shade(paper, 0), outline=shade(brass, -30), width=2)
    draw.rectangle((7, 6, 24, 25), outline=shade(violet, -5), width=1)
    draw.ellipse((10, 9, 21, 20), outline=shade(brass, 5), width=2)
    draw.line((11, 15, 16, 10, 21, 15, 16, 22, 11, 15), fill=shade(violet, 16), width=1)
    draw.line((9, 23, 23, 23), fill=shade(cyan, 8), width=1)
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def main() -> None:
    save_entity("aether_fox", (96, 96), "aether_fox.png", (80, 50, 1450, 760),
                ["151d39", "26345e", "68749b", "8b54d2", "c6cee5"], 801, "8a58e8", "d8dce8")
    save_entity("rune_wolf", (104, 96), "rune_wolf.png", (70, 40, 1450, 700),
                ["18191c", "32343a", "65512b", "a38445", "18b9cf"], 802, "22d6e7", "9d7938", True)
    save_entity("crystal_armored_spider", (96, 64), "crystal_spider.png", (50, 80, 1500, 720),
                ["171327", "302657", "673d8d", "a55ed0", "2fc9d4"], 803, "a765d8", "35d6dc", True)
    save_entity("mana_wisp", (64, 64), "mana_wisp.png", (80, 70, 1470, 760),
                ["202050", "50318b", "8e5add", "48bdd7", "d4eff5"], 804, "55d9ee", "c59a48")

    resident_master = MASTER / "arcane_settler.png"
    resident_specs = [
        ("arcane_settler_witch", (40, 40, 540, 700), ["222019", "4a5130", "7c6447", "d7ceba", "7950a3"], 811, "8f5ccc", "9c7c3f"),
        ("arcane_settler_goblin", (500, 100, 1030, 710), ["303124", "53623e", "744b35", "b3916b", "9a793b"], 812, "91a94f", "c69b44"),
        ("arcane_settler_researcher", (980, 80, 1510, 720), ["202a45", "3e5680", "d5c7a4", "704733", "4da0b8"], 813, "50c7df", "c59a48"),
    ]
    for name, crop, fallback, seed, accent, secondary in resident_specs:
        palette = palette_from(resident_master, crop, fallback)
        image = material_texture((64, 64), palette, seed, hex_color(accent), hex_color(secondary), True)
        out = ASSETS / "textures" / "entity" / f"{name}.png"
        out.parent.mkdir(parents=True, exist_ok=True)
        image.save(out)

    item_dir = ASSETS / "textures" / "item"
    contract_icon(item_dir / "familiar_contract.png")
    egg_icon(item_dir / "aether_fox_spawn_egg.png", "26345e", "c6cee5", "8a58e8")
    egg_icon(item_dir / "rune_wolf_spawn_egg.png", "27282c", "9d7938", "22d6e7")
    egg_icon(item_dir / "crystal_armored_spider_spawn_egg.png", "201835", "8d50b8", "35d6dc")
    egg_icon(item_dir / "mana_wisp_spawn_egg.png", "50318b", "48bdd7", "d4eff5")
    egg_icon(item_dir / "arcane_settler_spawn_egg.png", "303124", "3e5680", "c59a48")


if __name__ == "__main__":
    main()
