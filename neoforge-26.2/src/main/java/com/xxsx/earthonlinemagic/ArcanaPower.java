package com.xxsx.earthonlinemagic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Locale;

public final class ArcanaPower {
    public static final String CURRENT_MANA = "earth_online_arcana.current_mana";
    public static final String BASE_MANA = "earth_online_arcana.base_mana";
    public static final String XUANHUAN_MANA_BONUS = "earth_online_arcana.xuanhuan_mana_bonus";
    public static final String MAGIC_MANA_BONUS = "earth_online_arcana.magic_mana_bonus";
    public static final String EQUIPMENT_MANA_BONUS = "earth_online_arcana.equipment_mana_bonus";
    public static final String TEMPORARY_MANA_BONUS = "earth_online_arcana.temporary_mana_bonus";
    public static final String QI_ABSORPTION_RATE = "earth_online_arcana.qi_absorption_rate";
    public static final String MAGIC_ATTUNEMENT_RATE = "earth_online_arcana.magic_attunement_rate";
    public static final String CULTIVATION_LEVEL = "earth_online_arcana.cultivation_level";
    public static final String MAGIC_RESEARCH_LEVEL = "earth_online_arcana.magic_research_level";
    public static final String FASTING_FOOD_BONUS = "earth_online_arcana.fasting_food_bonus";
    public static final String BREATH_CAPACITY_BONUS = "earth_online_arcana.breath_capacity_bonus";
    public static final String ENDURANCE_BONUS = "earth_online_arcana.endurance_bonus";
    public static final String BODY_TEMPERING_BONUS = "earth_online_arcana.body_tempering_bonus";
    public static final String ARCANE_BODY_WARD_LEVEL = "earth_online_arcana.arcane_body_ward_level";
    public static final String ARCANE_BREATH_WARD_LEVEL = "earth_online_arcana.arcane_breath_ward_level";
    public static final String MAGIC_RESEARCH_XP = "earth_online_arcana.magic_research_xp";
    public static final String ARCANE_BODY_WARD_XP = "earth_online_arcana.arcane_body_ward_xp";
    public static final String ARCANE_BREATH_WARD_XP = "earth_online_arcana.arcane_breath_ward_xp";
    public static final String MAGIC_FOCUS_COOLDOWN_UNTIL = "earth_online_arcana.magic_focus_cooldown_until";
    public static final String MAGIC_SKILL_COOLDOWN_UNTIL = "earth_online_arcana.magic_skill_cooldown_until";
    public static final String ARCANE_FOCUS = "earth_online_arcana.arcane_focus";

    private static final double DEFAULT_BASE_MANA = 20.0D;
    private static final double MAX_REASONABLE_MANA = 1_000_000.0D;
    public static final int MAGIC_FOCUS_COOLDOWN_TICKS = 20 * 16;
    public static final int MAGIC_SKILL_COOLDOWN_TICKS = 20 * 12;
    public static final int MAX_ARCANE_LEVEL = 10;

    private ArcanaPower() {
    }

    public static double getBaseMana(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(BASE_MANA, DEFAULT_BASE_MANA));
    }

    public static double getXuanhuanBonus(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(XUANHUAN_MANA_BONUS, 0.0D));
    }

    public static double getMagicBonus(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(MAGIC_MANA_BONUS, 0.0D));
    }

    public static double getMaxMana(Player player) {
        CompoundTag tag = data(player);
        double max = getBaseMana(player)
                + getXuanhuanBonus(player)
                + getMagicBonus(player)
                + Math.max(0.0D, tag.getDoubleOr(EQUIPMENT_MANA_BONUS, 0.0D))
                + Math.max(0.0D, tag.getDoubleOr(TEMPORARY_MANA_BONUS, 0.0D));
        return clamp(max, 0.0D, MAX_REASONABLE_MANA);
    }

    public static double getCurrentMana(Player player) {
        double max = getMaxMana(player);
        return clamp(data(player).getDoubleOr(CURRENT_MANA, max), 0.0D, max);
    }

    public static void setCurrentMana(Player player, double value) {
        data(player).putDouble(CURRENT_MANA, clamp(value, 0.0D, getMaxMana(player)));
    }

    public static boolean trySpendMana(Player player, double amount) {
        double cost = Math.max(0.0D, amount);
        double current = getCurrentMana(player);
        if (current + 0.0001D < cost) {
            return false;
        }
        setCurrentMana(player, current - cost);
        return true;
    }

    public static int getMagicResearchLevel(Player player) {
        return Math.max(0, data(player).getIntOr(MAGIC_RESEARCH_LEVEL, 0));
    }

    public static double getMagicAttunementRate(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(MAGIC_ATTUNEMENT_RATE, 0.0D));
    }

    public static int getArcaneBodyWardLevel(Player player) {
        return Math.max(0, data(player).getIntOr(ARCANE_BODY_WARD_LEVEL, 0));
    }

    public static int getArcaneBreathWardLevel(Player player) {
        return Math.max(0, data(player).getIntOr(ARCANE_BREATH_WARD_LEVEL, 0));
    }

    public static int getFocusLevel(Player player, ArcaneFocus focus) {
        return switch (focus) {
            case ATTUNEMENT -> getMagicResearchLevel(player);
            case BODY_WARD -> getArcaneBodyWardLevel(player);
            case BREATH_WARD -> getArcaneBreathWardLevel(player);
        };
    }

    public static int getFocusXp(Player player, ArcaneFocus focus) {
        return Math.max(0, data(player).getIntOr(xpKey(focus), 0));
    }

    public static int getFocusXpNeeded(Player player, ArcaneFocus focus) {
        return xpNeededForLevel(getFocusLevel(player, focus));
    }

    public static ProgressResult addArcaneExperience(Player player, ArcaneFocus focus, int amount) {
        int previousLevel = getFocusLevel(player, focus);
        if (previousLevel <= 0 || amount <= 0 || previousLevel >= MAX_ARCANE_LEVEL) {
            return new ProgressResult(previousLevel, previousLevel, getFocusXp(player, focus),
                    getFocusXpNeeded(player, focus), 0, false, previousLevel >= MAX_ARCANE_LEVEL);
        }

        CompoundTag tag = data(player);
        int level = previousLevel;
        int xp = getFocusXp(player, focus) + amount;
        double manaBefore = getCurrentMana(player);
        while (level < MAX_ARCANE_LEVEL) {
            int needed = xpNeededForLevel(level);
            if (xp < needed) {
                break;
            }
            xp -= needed;
            level++;
            applyLevelReward(tag, focus);
        }
        if (level >= MAX_ARCANE_LEVEL) {
            xp = 0;
        }
        tag.putInt(levelKey(focus), level);
        tag.putInt(xpKey(focus), xp);
        if (level > previousLevel) {
            setCurrentMana(player, manaBefore + 4.0D * (level - previousLevel));
        }
        return new ProgressResult(previousLevel, level, xp, xpNeededForLevel(level), amount,
                level > previousLevel, level >= MAX_ARCANE_LEVEL);
    }

    public static double getBreathCapacityBonus(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(BREATH_CAPACITY_BONUS, 0.0D));
    }

    public static double getEnduranceBonus(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(ENDURANCE_BONUS, 0.0D));
    }

    public static double getBodyTemperingBonus(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(BODY_TEMPERING_BONUS, 0.0D));
    }

    public static double getExhaustionReduction(Player player) {
        return ArcanaBalance.exhaustionReduction(getEnduranceBonus(player));
    }

    public static double getCombatDamageReduction(Player player) {
        return ArcanaBalance.combatDamageReduction(getBodyTemperingBonus(player));
    }

    public static double getBreathMultiplier(Player player) {
        return ArcanaBalance.breathMultiplier(getBreathCapacityBonus(player));
    }

    public static ArcaneFocus getArcaneFocus(Player player) {
        ArcaneFocus focus = ArcaneFocus.byId(data(player).getIntOr(ARCANE_FOCUS, 0));
        return focus.isUnlocked(player) ? focus : ArcaneFocus.ATTUNEMENT;
    }

    public static boolean setArcaneFocus(Player player, ArcaneFocus focus) {
        if (!focus.isUnlocked(player) || getArcaneFocus(player) == focus) {
            return false;
        }
        data(player).putInt(ARCANE_FOCUS, focus.id());
        return true;
    }

    public static int getArcaneFocusMask(Player player) {
        int mask = 0;
        for (ArcaneFocus focus : ArcaneFocus.values()) {
            if (focus.isUnlocked(player)) {
                mask |= 1 << focus.id();
            }
        }
        return mask;
    }

    public static boolean learnArcaneInitiation(Player player) {
        CompoundTag tag = data(player);
        if (getMagicResearchLevel(player) > 0) {
            return false;
        }
        tag.putInt(MAGIC_RESEARCH_LEVEL, 1);
        tag.putInt(MAGIC_RESEARCH_XP, 0);
        add(tag, MAGIC_MANA_BONUS, 30.0D);
        add(tag, MAGIC_ATTUNEMENT_RATE, 0.08D);
        add(tag, ENDURANCE_BONUS, 0.03D);
        setCurrentMana(player, getMaxMana(player));
        return true;
    }

    public static boolean learnArcaneBodyWard(Player player) {
        CompoundTag tag = data(player);
        if (getMagicResearchLevel(player) <= 0 || getArcaneBodyWardLevel(player) > 0) {
            return false;
        }
        tag.putInt(ARCANE_BODY_WARD_LEVEL, 1);
        tag.putInt(ARCANE_BODY_WARD_XP, 0);
        add(tag, ENDURANCE_BONUS, 0.14D);
        add(tag, BODY_TEMPERING_BONUS, 0.18D);
        add(tag, MAGIC_MANA_BONUS, 12.0D);
        setCurrentMana(player, getMaxMana(player));
        return true;
    }

    public static boolean learnArcaneBreathWard(Player player) {
        CompoundTag tag = data(player);
        if (getMagicResearchLevel(player) <= 0 || getArcaneBreathWardLevel(player) > 0) {
            return false;
        }
        tag.putInt(ARCANE_BREATH_WARD_LEVEL, 1);
        tag.putInt(ARCANE_BREATH_WARD_XP, 0);
        add(tag, BREATH_CAPACITY_BONUS, 160.0D);
        add(tag, ENDURANCE_BONUS, 0.06D);
        add(tag, MAGIC_MANA_BONUS, 10.0D);
        setCurrentMana(player, getMaxMana(player));
        return true;
    }

    public static double focusAmbientMagic(Player player, int aetherField) {
        double rate = getMagicAttunementRate(player);
        if (rate <= 0.0D) {
            return 0.0D;
        }
        double before = getCurrentMana(player);
        double amount = Math.max(1.0D, aetherField * rate);
        setCurrentMana(player, before + amount);
        return getCurrentMana(player) - before;
    }

    public static long getMagicFocusCooldownTicks(Player player, Level level) {
        long until = data(player).getLongOr(MAGIC_FOCUS_COOLDOWN_UNTIL, 0L);
        return Math.max(0L, until - level.getGameTime());
    }

    public static void startMagicFocusCooldown(Player player, Level level) {
        data(player).putLong(MAGIC_FOCUS_COOLDOWN_UNTIL, level.getGameTime() + MAGIC_FOCUS_COOLDOWN_TICKS);
    }

    public static long getSkillCooldownTicks(Player player, Level level) {
        long until = data(player).getLongOr(MAGIC_SKILL_COOLDOWN_UNTIL, 0L);
        return Math.max(0L, until - level.getGameTime());
    }

    public static void startSkillCooldown(Player player, Level level) {
        data(player).putLong(MAGIC_SKILL_COOLDOWN_UNTIL,
                level.getGameTime() + MAGIC_SKILL_COOLDOWN_TICKS);
    }

    public static String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.05D) {
            return Integer.toString((int) Math.rint(value));
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static CompoundTag data(Player player) {
        return player.getPersistentData();
    }

    private static void add(CompoundTag tag, String key, double amount) {
        tag.putDouble(key, Math.max(0.0D, tag.getDoubleOr(key, 0.0D) + amount));
    }

    private static int xpNeededForLevel(int level) {
        return ArcanaBalance.xpNeededForLevel(level, MAX_ARCANE_LEVEL);
    }

    private static String levelKey(ArcaneFocus focus) {
        return switch (focus) {
            case ATTUNEMENT -> MAGIC_RESEARCH_LEVEL;
            case BODY_WARD -> ARCANE_BODY_WARD_LEVEL;
            case BREATH_WARD -> ARCANE_BREATH_WARD_LEVEL;
        };
    }

    private static String xpKey(ArcaneFocus focus) {
        return switch (focus) {
            case ATTUNEMENT -> MAGIC_RESEARCH_XP;
            case BODY_WARD -> ARCANE_BODY_WARD_XP;
            case BREATH_WARD -> ARCANE_BREATH_WARD_XP;
        };
    }

    private static void applyLevelReward(CompoundTag tag, ArcaneFocus focus) {
        switch (focus) {
            case ATTUNEMENT -> {
                add(tag, MAGIC_MANA_BONUS, 6.0D);
                add(tag, MAGIC_ATTUNEMENT_RATE, 0.012D);
                add(tag, ENDURANCE_BONUS, 0.01D);
            }
            case BODY_WARD -> {
                add(tag, MAGIC_MANA_BONUS, 4.0D);
                add(tag, BODY_TEMPERING_BONUS, 0.075D);
                add(tag, ENDURANCE_BONUS, 0.035D);
            }
            case BREATH_WARD -> {
                add(tag, MAGIC_MANA_BONUS, 4.0D);
                add(tag, BREATH_CAPACITY_BONUS, 28.0D);
                add(tag, ENDURANCE_BONUS, 0.018D);
            }
        }
    }

    private static double clamp(double value, double min, double max) {
        return ArcanaBalance.clamp(value, min, max);
    }

    public record ProgressResult(int previousLevel, int level, int xp, int xpNeeded, int gainedXp,
                                 boolean leveledUp, boolean atCap) {
    }
}
