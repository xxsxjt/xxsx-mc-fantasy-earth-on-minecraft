package com.xxsx.earthonlinemagic;

import net.minecraft.world.entity.player.Player;

public enum ArcaneFocus {
    ATTUNEMENT(0, "attunement"),
    BODY_WARD(1, "body_ward"),
    BREATH_WARD(2, "breath_ward");

    private final int id;
    private final String path;

    ArcaneFocus(int id, String path) {
        this.id = id;
        this.path = path;
    }

    public int id() {
        return id;
    }

    public String path() {
        return path;
    }

    public String titleKey() {
        return "screen.earth_online_magic.attunement.focus." + path;
    }

    public String descriptionKey() {
        return titleKey() + ".desc";
    }

    public boolean isUnlocked(Player player) {
        return switch (this) {
            case ATTUNEMENT -> ArcanaPower.getMagicResearchLevel(player) > 0;
            case BODY_WARD -> ArcanaPower.getArcaneBodyWardLevel(player) > 0;
            case BREATH_WARD -> ArcanaPower.getArcaneBreathWardLevel(player) > 0;
        };
    }

    public static ArcaneFocus byId(int id) {
        for (ArcaneFocus focus : values()) {
            if (focus.id == id) {
                return focus;
            }
        }
        return ATTUNEMENT;
    }
}
