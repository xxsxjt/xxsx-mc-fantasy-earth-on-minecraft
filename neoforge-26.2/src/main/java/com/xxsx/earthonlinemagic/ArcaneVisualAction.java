package com.xxsx.earthonlinemagic;

public enum ArcaneVisualAction {
    ATTUNEMENT(0, 30),
    MANA_RECOVERY(1, 22),
    AETHER_PULSE(2, 18),
    BODY_WARD(3, 24),
    BREATH_WARD(4, 24);

    private final int id;
    private final int durationTicks;

    ArcaneVisualAction(int id, int durationTicks) {
        this.id = id;
        this.durationTicks = durationTicks;
    }

    public int id() {
        return id;
    }

    public int durationTicks() {
        return durationTicks;
    }

    public static ArcaneVisualAction forFocus(ArcaneFocus focus) {
        return switch (focus) {
            case ATTUNEMENT -> AETHER_PULSE;
            case BODY_WARD -> BODY_WARD;
            case BREATH_WARD -> BREATH_WARD;
        };
    }

    public static ArcaneVisualAction byId(int id) {
        for (ArcaneVisualAction action : values()) {
            if (action.id == id) {
                return action;
            }
        }
        return ATTUNEMENT;
    }
}
