package com.xxsx.earthonlinemagic.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MagicHandbookScreen extends Screen {
    private static final int PAPER = 0xFFD8D8D0;
    private static final int EDGE = 0xFF5B526B;
    private static final int INK = 0xFF1D2630;
    private static final int MUTED = 0xFF586574;
    private static final int PURPLE = 0xFF7B3AA5;
    private static final int BLUE = 0xFF255A78;
    private static final int GOLD = 0xFF956215;
    private static final int LINE_HEIGHT = 12;

    private final List<Page> pages = createPages();
    private final List<Button> tabButtons = new ArrayList<>();
    private int page;
    private int scroll;
    private Button prevButton;
    private Button nextButton;

    public MagicHandbookScreen() {
        super(Component.translatable("screen.earth_online_magic.handbook.title"));
    }

    @Override
    protected void init() {
        tabButtons.clear();
        int left = bookLeft();
        int top = bookTop();
        for (int i = 0; i < pages.size(); i++) {
            final int index = i;
            tabButtons.add(addRenderableWidget(Button.builder(Component.literal(pages.get(i).shortTitle), b -> setPage(index))
                    .bounds(left + 10, top + 42 + i * 18, 58, 16).build()));
        }
        int bottom = top + bookHeight() - 28;
        prevButton = addRenderableWidget(Button.builder(Component.translatable("screen.earth_online_magic.handbook.prev"), b -> setPage(page - 1))
                .bounds(left + bookWidth() - 182, bottom, 76, 20).build());
        nextButton = addRenderableWidget(Button.builder(Component.translatable("screen.earth_online_magic.handbook.next"), b -> setPage(page + 1))
                .bounds(left + bookWidth() - 100, bottom, 76, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.earth_online_magic.handbook.close"), b -> onClose())
                .bounds(left + 12, bottom, 54, 20).build());
        updateButtonState();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, this.width, this.height, 0xB0000000);
        int left = bookLeft();
        int top = bookTop();
        int bw = bookWidth();
        int bh = bookHeight();
        g.fill(left - 3, top + 4, left + bw + 3, top + bh + 2, 0xA00D0C13);
        g.fill(left, top, left + bw, top + bh, 0xFF282333);
        g.fill(left + 4, top + 4, left + bw - 4, top + bh - 4, PAPER);
        g.fill(left + 5, top + 5, contentLeft() - 10, top + bh - 5, 0xFF373043);
        g.outline(left, top, bw, bh, EDGE);
        drawBrassCorners(g, left, top, bw, bh);
        drawRingBinding(g, top, bh);
        drawHeaderCircuit(g, left, top, bw);
        String bookTitle = tr("screen.earth_online_magic.handbook.title");
        g.text(font, bookTitle, left + (bw - font.width(bookTitle)) / 2, top + 8, 0xFFE9E5F0, false);
        g.text(font, tr("screen.earth_online_magic.handbook.subtitle"), left + 14, top + 24, MUTED, false);
        g.text(font, tr("screen.earth_online_magic.handbook.page", page + 1, pages.size()), left + bw - 70, top + 24, MUTED, false);
        super.extractRenderState(g, mouseX, mouseY, delta);

        Page current = pages.get(page);
        int contentX = contentLeft();
        int contentY = top + 46;
        int contentW = contentWidth();
        int contentH = Math.max(80, bh - 84);
        g.fill(contentX - 6, contentY - 6, contentX + contentW - 2, contentY + 14, 0x30255A78);
        g.fill(contentX - 6, contentY - 6, contentX - 2, contentY + 14, current.color);
        g.text(font, current.title, contentX, contentY - 3, current.color, false);
        g.fill(contentX, contentY + 12, contentX + Math.min(contentW, 146), contentY + 14, 0xFF8FA7B5);
        g.fill(contentX + Math.min(contentW, 146), contentY + 12,
                contentX + Math.min(contentW, 152), contentY + 14, current.color);
        List<Line> wrapped = wrap(current);
        int visible = Math.max(5, (contentH - 24) / LINE_HEIGHT);
        int maxScroll = Math.max(0, wrapped.size() - visible);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        drawScrollMarker(g, contentX + contentW + 5, contentY + 18, contentH - 26, maxScroll, current.color);
        int y = contentY + 18;
        for (int i = scroll; i < Math.min(wrapped.size(), scroll + visible); i++) {
            Line line = wrapped.get(i);
            if (line.blank) {
                y += 4;
                continue;
            }
            g.fill(contentX + line.indent - 5, y + 4, contentX + line.indent - 2, y + 7, current.color);
            g.text(font, line.text, contentX + line.indent, y, line.color, false);
            y += LINE_HEIGHT;
        }
    }

    private void drawBrassCorners(GuiGraphicsExtractor g, int left, int top, int width, int height) {
        int brass = 0xFF9C8350;
        g.fill(left + 2, top + 2, left + 16, top + 4, brass);
        g.fill(left + 2, top + 2, left + 4, top + 16, brass);
        g.fill(left + width - 16, top + 2, left + width - 2, top + 4, brass);
        g.fill(left + width - 4, top + 2, left + width - 2, top + 16, brass);
        g.fill(left + 2, top + height - 4, left + 16, top + height - 2, brass);
        g.fill(left + 2, top + height - 16, left + 4, top + height - 2, brass);
        g.fill(left + width - 16, top + height - 4, left + width - 2, top + height - 2, brass);
        g.fill(left + width - 4, top + height - 16, left + width - 2, top + height - 2, brass);
    }

    private void drawRingBinding(GuiGraphicsExtractor g, int top, int height) {
        int x = contentLeft() - 15;
        g.fill(x, top + 31, x + 2, top + height - 34, 0xFF596574);
        for (int y = top + 49; y < top + height - 40; y += 34) {
            g.fill(x - 5, y, x + 7, y + 3, 0xFF9C8350);
            g.fill(x - 2, y - 2, x + 4, y + 5, 0xFF25212E);
        }
    }

    private void drawHeaderCircuit(GuiGraphicsExtractor g, int left, int top, int width) {
        int x = left + width - 105;
        int y = top + 9;
        g.fill(x, y + 4, x + 55, y + 5, 0xFF5E7E8E);
        for (int i = 0; i < 4; i++) {
            int nx = x + i * 18;
            g.fill(nx - 2, y + 2, nx + 3, y + 7, 0xFF282333);
            g.fill(nx, y + 4, nx + 1, y + 5, i <= page ? 0xFF8FC6D2 : 0xFF68707B);
        }
    }

    private void drawScrollMarker(GuiGraphicsExtractor g, int x, int y, int height, int maxScroll, int color) {
        if (maxScroll <= 0 || height < 12) {
            return;
        }
        g.fill(x, y, x + 2, y + height, 0x554D5964);
        int markerHeight = Math.max(8, height / (maxScroll + 2));
        int markerY = y + (height - markerHeight) * scroll / maxScroll;
        g.fill(x - 1, markerY, x + 3, markerY + markerHeight, color);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= contentLeft() - 8 && mouseX <= contentLeft() + contentWidth() + 16) {
            scroll = Math.max(0, scroll - (int) Math.signum(scrollY) * 3);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(null);
        } else {
            Minecraft.getInstance().gui.setScreen(null);
        }
    }

    private void setPage(int next) {
        if (next < 0 || next >= pages.size()) {
            return;
        }
        page = next;
        scroll = 0;
        updateButtonState();
    }

    private void updateButtonState() {
        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).active = i != page;
        }
        if (prevButton != null) {
            prevButton.active = page > 0;
        }
        if (nextButton != null) {
            nextButton.active = page < pages.size() - 1;
        }
    }

    private List<Line> wrap(Page current) {
        List<Line> result = new ArrayList<>();
        for (Entry entry : current.entries) {
            if (entry.text.isBlank()) {
                result.add(new Line(FormattedCharSequence.EMPTY, 0, INK, true));
                continue;
            }
            for (FormattedCharSequence seq : font.split(FormattedText.of(entry.text), Math.max(30, contentWidth() - entry.indent))) {
                result.add(new Line(seq, entry.indent, entry.color, false));
            }
        }
        return result;
    }

    private int bookWidth() {
        return Math.min(520, Math.max(320, this.width - 18));
    }

    private int bookHeight() {
        return Math.min(300, Math.max(224, this.height - 18));
    }

    private int bookLeft() {
        return (this.width - bookWidth()) / 2;
    }

    private int bookTop() {
        return (this.height - bookHeight()) / 2;
    }

    private int contentLeft() {
        return bookLeft() + 86;
    }

    private int contentWidth() {
        return bookLeft() + bookWidth() - contentLeft() - 20;
    }

    private static List<Page> createPages() {
        boolean earthLoaded = ModList.get().isLoaded("earth_on_minecraft");
        boolean humanLoaded = ModList.get().isLoaded("earth_human");
        boolean xuanhuanLoaded = ModList.get().isLoaded("earth_online_xuanhuan");
        if (!Minecraft.getInstance().getLanguageManager().getSelected().toLowerCase(Locale.ROOT).startsWith("zh")) {
            return List.of(
                    page("Start", "1. Magic loop", PURPLE,
                            "Craft the Field Arcane Notebook from dirt, planks, or stone. Right-click it to open this handbook.",
                            "Craft starter arcane dust from redstone, glowstone and amethyst. Use it for rune ink, then build the Alchemy Table and Rune Carving Table.",
                            "Make mana salt at the alchemy table and a dormant core at the rune table. The Ritual Pedestal now uses amethyst, so it can activate the first aether crystals without a circular recipe."),
                    page("Stations", "2. Alchemy, runes and rituals", BLUE,
                            "Alchemy Table: 2 dust + glowstone -> 2 mana salt; crystal + glass -> 2 aether glass; glowstone + amethyst -> 2 dust.",
                            "Rune Carving Table: conductor + rune ink -> 2 copper plates; plate + ink/glass -> ward notes; chalk + ink -> dormant core.",
                            "Ritual Pedestal: dormant core + mana salt -> 2 crystals; aether glass + 2 salt -> dormant core; substrate + salt -> 2 crystals.",
                            "Full ritual circle: pedestal center, crystal corners and ritual marks on the four sides. It lowers aether requirement and works faster."),
                    page("Use", "3. What can I use?", GOLD,
                            "Crystallized mana salt and aether crystals are consumable mana recovery items.",
                            "Initiation and adaptation notes train the magic route. They stack with the qi route by adding to shared mana."),
                    page("Actions", "4. Actions and Earth Human", PURPLE,
                            "Press the configurable arcane-panel key (M by default) to attune anywhere. Free attunement runs at 72% efficiency and disturbs local aether.",
                            "The Arcane Focus Mat is optional: sitting provides full efficiency, extra aether gathering, continuous cycles and stronger Earth Human recovery.",
                            "Combat actions such as arcane body ward grant short protection while lightly supporting torso and limbs."),
                    page("Links", "5. Optional integrations", PURPLE,
                            earthLoaded
                                    ? "Earth on Minecraft connected: geology catalysts, mana conductors and crystal substrates work directly in arcane facilities."
                                    : "Earth on Minecraft not installed: vanilla fallback materials keep the magic route playable.",
                            humanLoaded
                                    ? "Earth Human connected: wards and attunement affect fatigue, breathing and body-part recovery."
                                    : "Earth Human not installed: magic progression remains available without realistic-human mechanics.",
                            xuanhuanLoaded
                                    ? "Xuanhuan Earth on Minecraft connected: magic and qi contributions are added into one shared mana pool."
                                    : "Xuanhuan Earth on Minecraft not installed: this magic route remains fully standalone."));
        }
        return List.of(
                page("入门", "1. 魔幻流程", PURPLE,
                        "用一块泥土、任意木板或任意石头合成魔幻田野手册。右键打开手册，同时在聊天里显示本地以太场。",
                        "先用红石、荧石粉和紫水晶碎片制基础魔尘，再制符文墨水，做出炼金台与符文刻台。",
                        "炼金台产出魔盐，符文刻台产出休眠核心；仪式基座改用紫水晶制作，因此无需先有聚魔水晶，就能用核心 + 魔盐获得第一批水晶。"),
                page("设施", "2. 三条奥术路线", BLUE,
                        "炼金台：2 魔尘 + 荧石粉 -> 2 晶化魔盐；聚魔水晶 + 玻璃 -> 2 以太玻璃；荧石粉 + 紫水晶 -> 2 魔尘。",
                        "符文刻台：导能材料 + 符文墨水 -> 2 符文铜片；铜片 + 墨水/以太玻璃 -> 两类札记；仪式粉笔 + 墨水 -> 休眠核心。",
                        "仪式基座：休眠核心 + 魔盐 -> 2 聚魔水晶；以太玻璃 + 2 魔盐 -> 休眠核心；晶体基底 + 魔盐 -> 2 聚魔水晶。",
                        "正式仪式圈：中心仪式基座，四角聚魔水晶簇或紫水晶簇，四边书架/荧石/红石块/炼金台/符文刻台。成型后门槛降低并加速。"),
                page("法力", "3. 法力、以太场和消耗品", GOLD,
                        "奥术启蒙笔记会增加魔力路线法力上限；奥术护身和水肺札记提供身体适应方向。",
                        "晶化魔盐和聚魔水晶可以直接右键回法；它们会轻微扰动本地区块以太场。",
                        "如果奥术设施不运行，先看界面状态：可能是没材料、输出满、红石模式不允许，或本地以太场低于配方要求。"),
                page("动作", "4. 研习、恢复和战斗动作", PURPLE,
                        "研习动作：奥术启蒙、奥术护身、水肺调律会提高魔力路线和共享法力上限。",
                        "按可配置的奥术面板键（默认 M）可在任何位置执行调律；自由调律效率为 72%，仍会读取并扰动本区块以太场。",
                        "奥术冥想垫是可选增益：坐下后获得完整效率、额外聚魔、持续调律和更强的地球人恢复；只有坐着时 Shift 才用于离开。",
                        "战斗动作：奥术护身札记会给短时间抗性和吸收，水肺调律偏呼吸与躯干恢复。"),
                page("边界", "5. 与玄幻和本体的关系", PURPLE,
                        earthLoaded
                                ? "已连接《我的地球》：地质催化物、导能材料和晶体基底可以直接放入奥术设施。"
                                : "未安装《我的地球》：当前使用原版材料回退路线，魔幻内容仍可独立游玩。",
                        humanLoaded
                                ? "已连接《地球人》：调律与护身会联动疲劳、呼吸和全身部位恢复。"
                                : "未安装《地球人》：魔法属性仍生效，但不会出现真实人体状态联动。",
                        xuanhuanLoaded
                                ? "已连接《玄幻地球 on Minecraft》：魔力与灵力贡献相加到同一条法力值。"
                                : "未安装《玄幻地球 on Minecraft》：魔幻路线保持完整独立。"));
    }

    private static Page page(String shortTitle, String title, int color, String... lines) {
        List<Entry> entries = new ArrayList<>();
        for (String line : lines) {
            entries.add(new Entry(line, line.isBlank() ? 0 : 10, INK));
        }
        return new Page(shortTitle, title, color, List.copyOf(entries));
    }

    private static String tr(String key, Object... args) {
        String raw = Language.getInstance().getOrDefault(key);
        return args.length == 0 ? raw : String.format(Locale.ROOT, raw, args);
    }

    private record Page(String shortTitle, String title, int color, List<Entry> entries) {
    }

    private record Entry(String text, int indent, int color) {
    }

    private record Line(FormattedCharSequence text, int indent, int color, boolean blank) {
    }
}
