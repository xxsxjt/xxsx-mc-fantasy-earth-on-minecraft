package com.xxsx.earthonlinemagic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class MagicJourney {
    public static final String DATA_KEY = "earth_online_arcana.magic_journey_mask";

    private MagicJourney() {
    }

    public static int reconcile(Player player) {
        int mask = storedMask(player);
        if (ArcanaPower.getMagicResearchLevel(player) > 0) {
            mask = apply(mask, Milestone.INITIATION);
        }
        boolean hasPractice = false;
        for (ArcaneFocus focus : ArcaneFocus.values()) {
            if (ArcanaPower.getFocusXp(player, focus) > 0 || ArcanaPower.getFocusLevel(player, focus) > 1) {
                hasPractice = true;
                break;
            }
        }
        if (hasPractice) {
            mask = apply(mask, Milestone.ATTUNEMENT);
        }
        store(player, mask);
        return mask;
    }

    public static boolean complete(ServerPlayer player, Milestone milestone) {
        int stored = storedMask(player);
        if (apply(stored, milestone) == stored) {
            return false;
        }
        int after = apply(reconcile(player), milestone);
        store(player, after);
        player.sendSystemMessage(Component.translatable(
                "message.earth_online_magic.journey.milestone",
                Component.translatable(milestone.translationKey()), count(after), total())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        if (isComplete(after)) {
            player.giveExperiencePoints(30);
            player.sendSystemMessage(Component.translatable(
                    "message.earth_online_magic.journey.complete")
                    .withStyle(ChatFormatting.GOLD));
        }
        return true;
    }

    public static int total() {
        return Milestone.values().length;
    }

    public static int count(int mask) {
        return JourneyProgress.count(mask, total());
    }

    public static boolean isComplete(int mask) {
        return JourneyProgress.isComplete(mask, total());
    }

    static int apply(int mask, Milestone milestone) {
        return JourneyProgress.apply(mask, milestone.id(), total());
    }

    public static Milestone nextMilestone(int mask) {
        int id = JourneyProgress.nextId(mask, total());
        return id < 0 ? null : Milestone.values()[id];
    }

    private static int storedMask(Player player) {
        return JourneyProgress.sanitize(player.getPersistentData().getIntOr(DATA_KEY, 0), total());
    }

    private static void store(Player player, int mask) {
        player.getPersistentData().putInt(DATA_KEY, JourneyProgress.sanitize(mask, total()));
    }

    public enum Milestone {
        INITIATION(0, "initiation"),
        ATTUNEMENT(1, "attunement"),
        FACILITY_OUTPUT(2, "facility_output"),
        ACTIVE_SPELL(3, "active_spell"),
        MANA_RECOVERY(4, "mana_recovery"),
        FAMILIAR_CONTRACT(5, "familiar_contract");

        private final int id;
        private final String path;

        Milestone(int id, String path) {
            this.id = id;
            this.path = path;
        }

        int id() {
            return id;
        }

        public String translationKey() {
            return "journey.earth_online_magic.milestone." + path;
        }

        public String nextKey() {
            return "journey.earth_online_magic.next." + path;
        }
    }
}
