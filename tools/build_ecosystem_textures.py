from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

from entity_uv import Atlas, color, human_face, line, muzzle, pack_back, point, robe_front, shade, side_rune, wolf_face


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "neoforge-26.2" / "src" / "main" / "resources" / "assets" / "earth_online_magic"
ENTITY_DIR = ASSETS / "textures" / "entity"


def build_aether_fox() -> None:
    atlas = Atlas((128, 128), ENTITY_DIR)
    body = atlas.box((0, 24), (8, 8, 12), "253052", "fur", "9368e7", 101)
    side_rune(atlas, body["west"], "9368e7")
    side_rune(atlas, body["east"], "9368e7")
    atlas.box((42, 25), (7, 1, 10), "7780a5", "crystal", "d7e3ff", 102)
    head = atlas.box((0, 0), (8, 7, 7), "2b355c", "fur", "9368e7", 103)
    wolf_face(atlas, head["north"], "a98af2")
    snout = atlas.box((30, 0), (4, 3, 4), "4d557d", "fur", None, 104)
    muzzle(atlas, snout["north"])
    atlas.box((48, 0), (5, 6, 1), "8d9dd0", "crystal", "e0e8ff", 105)
    atlas.box((60, 0), (5, 6, 1), "8d9dd0", "crystal", "e0e8ff", 106)
    for index, uv in enumerate(((0, 48), (12, 48), (24, 48), (36, 48))):
        atlas.box(uv, (3, 6, 3), "252f53", "fur", "9368e7", 110 + index)
    left_tail = atlas.box((0, 64), (4, 4, 7), "3b4775", "fur", "9368e7", 120)
    right_tail = atlas.box((24, 64), (4, 4, 7), "3b4775", "fur", "9368e7", 121)
    left_mid = atlas.box((48, 64), (5, 5, 7), "536092", "fur", "a77aec", 122)
    right_mid = atlas.box((74, 64), (5, 5, 7), "536092", "fur", "a77aec", 123)
    left_tip = atlas.box((0, 84), (6, 6, 6), "7580b1", "fur", "c09af4", 124)
    right_tip = atlas.box((26, 84), (6, 6, 6), "7580b1", "fur", "c09af4", 125)
    for face in (left_tail["east"], right_tail["west"], left_mid["east"], right_mid["west"],
                 left_tip["east"], right_tip["west"]):
        side_rune(atlas, face, "a989f0")
    atlas.save("aether_fox")


def build_rune_wolf() -> None:
    atlas = Atlas((128, 128), ENTITY_DIR)
    body = atlas.box((0, 24), (8, 8, 14), "292b2e", "metal", "25d7e8", 201)
    side_rune(atlas, body["west"], "25d7e8")
    side_rune(atlas, body["east"], "25d7e8")
    armor = atlas.box((48, 20), (10, 5, 12), "242629", "metal", "b38d45", 202)
    side_rune(atlas, armor["west"], "25d7e8")
    side_rune(atlas, armor["east"], "25d7e8")
    atlas.box((48, 40), (9, 5, 5), "3b3529", "metal", "b38d45", 203)
    back_rune = atlas.box((92, 20), (4, 1, 4), "173a42", "crystal", "39e9f4", 204)
    side_rune(atlas, back_rune["up"], "39e9f4")
    head = atlas.box((0, 0), (7, 7, 7), "303238", "metal", "25d7e8", 205)
    wolf_face(atlas, head["north"], "25d7e8")
    snout = atlas.box((30, 0), (4, 3, 5), "4b4a48", "metal", "b38d45", 206)
    muzzle(atlas, snout["north"])
    atlas.box((48, 0), (2, 5, 2), "2d2f33", "metal", "25d7e8", 207)
    atlas.box((58, 0), (2, 5, 2), "2d2f33", "metal", "25d7e8", 208)
    lens = atlas.box((70, 0), (3, 3, 1), "147e8b", "crystal", "66f5ff", 209)
    side_rune(atlas, lens["north"], "e2b654")
    for index in range(4):
        atlas.box((index * 12, 52), (2, 6, 2), "2b2d30", "metal", "25d7e8", 220 + index)
        paw = atlas.box((index * 16, 68), (3, 2, 4), "3a3b3c", "metal", "b38d45", 230 + index)
        line(atlas.draw, paw["north"], [(1, 0), (1, 1)], shade(color("121416"), 0))
    tail = atlas.box((0, 84), (4, 4, 7), "292b2f", "metal", "25d7e8", 240)
    side_rune(atlas, tail["east"], "25d7e8")
    tip = atlas.box((28, 84), (4, 4, 7), "36383b", "metal", "b38d45", 241)
    side_rune(atlas, tip["east"], "25d7e8")
    atlas.save("rune_wolf")


def build_crystal_spider() -> None:
    atlas = Atlas((96, 80), ENTITY_DIR)
    head = atlas.box((0, 0), (8, 6, 6), "211b37", "stone", "a968d8", 301)
    for x in (1, 3, 5, 6):
        point(atlas.draw, head["north"], x, 3, shade(color("42e5ef"), 20))
    atlas.box((28, 0), (6, 3, 3), "332653", "stone", "42e5ef", 302)
    abdomen = atlas.box((0, 16), (12, 8, 11), "2b2048", "stone", "a968d8", 303)
    side_rune(atlas, abdomen["west"], "42e5ef")
    side_rune(atlas, abdomen["east"], "42e5ef")
    armor = atlas.box((48, 16), (13, 5, 10), "56327a", "crystal", "bd79eb", 304)
    side_rune(atlas, armor["west"], "42e5ef")
    for uv, dims, seed in (((0, 40), (3, 7, 3), 305), ((14, 40), (2, 5, 2), 306), ((26, 40), (2, 4, 2), 307)):
        atlas.box(uv, dims, "7840a5", "crystal", "d093f4", seed)
    for row in range(4):
        inner = atlas.box((row * 20, 52), (6, 2, 2), "38274f", "crystal", "45dce7", 310 + row)
        outer = atlas.box((row * 20, 60), (7, 2, 2), "493161", "crystal", "64e9ef", 320 + row)
        side_rune(atlas, inner["up"], "45dce7")
        side_rune(atlas, outer["up"], "64e9ef")
    atlas.save("crystal_armored_spider")


def build_mana_wisp() -> None:
    atlas = Atlas((64, 64), ENTITY_DIR)
    core = atlas.box((0, 0), (6, 6, 6), "7050bd", "crystal", "b77af0", 401)
    for face in ("north", "south", "west", "east"):
        side_rune(atlas, core[face], "62e8f2")
    atlas.box((24, 0), (7, 7, 7), "3c2d75", "crystal", "8ddff2", 402)
    ring = atlas.box((0, 16), (10, 2, 10), "9a793a", "metal", "e0b958", 403)
    side_rune(atlas, ring["up"], "5ce5ef")
    atlas.box((0, 30), (7, 8, 1), "57cfe4", "crystal", "d7fbff", 404)
    atlas.box((16, 30), (7, 8, 1), "57cfe4", "crystal", "d7fbff", 405)
    flame = atlas.box((34, 30), (4, 8, 4), "6d44b5", "crystal", "55e7f1", 406)
    side_rune(atlas, flame["north"], "55e7f1")
    atlas.save("mana_wisp")


def build_settler(name: str, skin: str, hair: str, coat: str, trim: str, pack: str,
                  eye: str, role: int, seed: int) -> None:
    atlas = Atlas((128, 128), ENTITY_DIR)
    head = atlas.box((0, 0), (8, 8, 8), skin, "flat", None, seed)
    human_face(atlas, head["north"], hair, eye, beard=role == 2, goblin=role == 1)
    atlas.box((32, 0), (9, 3, 9), hair, "fur", trim, seed + 1)
    atlas.box((70, 0), (10, 2, 10), coat, "cloth", trim, seed + 2)
    atlas.box((70, 14), (6, 7, 6), coat, "cloth", trim, seed + 3)
    atlas.box((104, 0), (3, 3, 1), skin, "flat", trim, seed + 4)
    atlas.box((112, 0), (3, 3, 1), skin, "flat", trim, seed + 5)
    atlas.box((104, 8), (3, 3, 1), "366f80", "crystal", "60e9f2", seed + 6)
    atlas.box((0, 20), (8, 12, 4), coat, "cloth", trim, seed + 7)
    coat_faces = atlas.box((26, 20), (9, 13, 5), coat, "cloth", trim, seed + 8)
    robe_front(atlas, coat_faces["north"], trim)
    atlas.box((56, 20), (10, 8, 6), coat, "cloth", trim, seed + 9)
    for uv in ((0, 42), (16, 42)):
        atlas.box(uv, (4, 12, 4), coat, "cloth", trim, seed + 10 + uv[0])
    for uv in ((32, 42), (52, 42)):
        atlas.box(uv, (5, 9, 5), coat, "cloth", trim, seed + 11 + uv[0])
    for uv in ((0, 60), (16, 60)):
        atlas.box(uv, (4, 12, 4), coat, "cloth", trim, seed + 12 + uv[0])
    for uv in ((32, 60), (52, 60)):
        atlas.box(uv, (5, 5, 5), "3a2b24", "cloth", trim, seed + 13 + uv[0])
    witch = atlas.box((0, 80), (9, 12, 4), pack, "metal", trim, seed + 14)
    pack_back(atlas, witch["south"], trim)
    atlas.box((28, 80), (3, 5, 3), "6b5038", "crystal", "a96ae0", seed + 15)
    goblin = atlas.box((42, 80), (10, 11, 5), pack, "metal", trim, seed + 16)
    pack_back(atlas, goblin["south"], trim)
    atlas.box((74, 80), (8, 4, 4), "59422d", "cloth", trim, seed + 17)
    researcher = atlas.box((0, 102), (9, 13, 4), pack, "metal", trim, seed + 18)
    pack_back(atlas, researcher["south"], trim)
    atlas.box((28, 102), (3, 14, 3), "6d5030", "metal", "d7b45b", seed + 19)
    atlas.save(name)


def egg_icon(path: Path, base: str, spots: str, rune: str) -> None:
    image = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    b = color(base)
    s = color(spots)
    r = color(rune)
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
    paper = color("d7d0bc")
    brass = color("bd8f3c")
    violet = color("8754d6")
    cyan = color("48cfe0")
    draw.rounded_rectangle((4, 3, 27, 28), radius=2, fill=shade(paper, 0), outline=shade(brass, -30), width=2)
    draw.rectangle((7, 6, 24, 25), outline=shade(violet, -5), width=1)
    draw.ellipse((10, 9, 21, 20), outline=shade(brass, 5), width=2)
    draw.line((11, 15, 16, 10, 21, 15, 16, 22, 11, 15), fill=shade(violet, 16), width=1)
    draw.line((9, 23, 23, 23), fill=shade(cyan, 8), width=1)
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def main() -> None:
    build_aether_fox()
    build_rune_wolf()
    build_crystal_spider()
    build_mana_wisp()
    build_settler("arcane_settler_witch", "b98d6d", "3a3028", "565737", "9b67c7", "4d4930", "7050a2", 0, 501)
    build_settler("arcane_settler_goblin", "718b52", "493728", "6a5137", "b08b3f", "4b3d2e", "d0a346", 1, 531)
    build_settler("arcane_settler_researcher", "bc8867", "573728", "344d74", "56cfe1", "394456", "347d8d", 2, 561)

    item_dir = ASSETS / "textures" / "item"
    contract_icon(item_dir / "familiar_contract.png")
    egg_icon(item_dir / "aether_fox_spawn_egg.png", "26345e", "c6cee5", "8a58e8")
    egg_icon(item_dir / "rune_wolf_spawn_egg.png", "27282c", "9d7938", "22d6e7")
    egg_icon(item_dir / "crystal_armored_spider_spawn_egg.png", "201835", "8d50b8", "35d6dc")
    egg_icon(item_dir / "mana_wisp_spawn_egg.png", "50318b", "48bdd7", "d4eff5")
    egg_icon(item_dir / "arcane_settler_spawn_egg.png", "303124", "3e5680", "c59a48")


if __name__ == "__main__":
    main()
