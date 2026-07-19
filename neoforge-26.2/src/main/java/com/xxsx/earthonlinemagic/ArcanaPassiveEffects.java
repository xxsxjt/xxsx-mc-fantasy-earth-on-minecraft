package com.xxsx.earthonlinemagic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Applies progression benefits when the standalone magic module is active. */
public final class ArcanaPassiveEffects {
    private static final String XUANHUAN_MODID = "earth_online_xuanhuan";
    private static final boolean XUANHUAN_PRESENT = ModList.get().isLoaded(XUANHUAN_MODID);
    private static final String LAST_FOOD_LEVEL = "earth_online_arcana.passive_last_food_level";
    private static final String LAST_SATURATION = "earth_online_arcana.passive_last_saturation";
    private static final String FOOD_RESTORE_CREDIT = "earth_online_arcana.passive_food_restore_credit";
    private static final String BREATH_CREDIT = "earth_online_arcana.passive_breath_credit";

    private ArcanaPassiveEffects() {
    }

    private static boolean ownsSharedEffects() {
        return !XUANHUAN_PRESENT;
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!ownsSharedEffects() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        applyMetabolicReduction(player);
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!ownsSharedEffects() || !(event.getEntity() instanceof ServerPlayer player)
                || (event.getSource().getEntity() == null && event.getSource().getDirectEntity() == null)) {
            return;
        }
        double reduction = ArcanaPower.getCombatDamageReduction(player);
        if (reduction <= 0.0D || event.getAmount() <= 0.0F) {
            return;
        }
        event.setAmount((float) (event.getAmount() * (1.0D - reduction)));
    }

    public static void onFall(LivingFallEvent event) {
        if (!ownsSharedEffects() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        double reduction = ArcanaBalance.fallDamageReduction(ArcanaPower.getEnduranceBonus(player));
        if (reduction > 0.0D) {
            event.setDamageMultiplier((float) (event.getDamageMultiplier() * (1.0D - reduction)));
        }
    }

    public static void onBreathe(LivingBreatheEvent event) {
        if (!ownsSharedEffects() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        if (event.canBreathe()) {
            if (data.getDoubleOr(BREATH_CREDIT, 0.0D) != 0.0D) {
                data.putDouble(BREATH_CREDIT, 0.0D);
            }
            return;
        }

        double multiplier = ArcanaPower.getBreathMultiplier(player);
        int baseConsume = event.getConsumeAirAmount();
        if (multiplier <= 1.0001D || baseConsume <= 0) {
            return;
        }

        double credit = data.getDoubleOr(BREATH_CREDIT, 0.0D);
        credit += baseConsume / multiplier;
        int consume = Math.max(0, (int) Math.floor(credit));
        data.putDouble(BREATH_CREDIT, credit - consume);
        event.setConsumeAirAmount(consume);
    }

    private static void applyMetabolicReduction(ServerPlayer player) {
        FoodData food = player.getFoodData();
        CompoundTag data = player.getPersistentData();
        int currentFood = food.getFoodLevel();
        float currentSaturation = food.getSaturationLevel();
        int previousFood = data.getIntOr(LAST_FOOD_LEVEL, -1);
        double previousSaturation = data.getDoubleOr(LAST_SATURATION, -1.0D);
        if (previousFood < 0 || previousFood > 20 || !Double.isFinite(previousSaturation)
                || previousSaturation < 0.0D || previousSaturation > 20.0D) {
            rememberFoodState(data, currentFood, currentSaturation);
            return;
        }
        if (currentFood == previousFood && Math.abs(currentSaturation - previousSaturation) < 0.0001D) {
            return;
        }

        double reduction = ArcanaPower.getExhaustionReduction(player);
        double credit = Math.max(0.0D, data.getDoubleOr(FOOD_RESTORE_CREDIT, 0.0D));
        if (reduction > 0.0D && currentFood < previousFood) {
            credit += (previousFood - currentFood) * reduction;
            int restoredFood = (int) Math.floor(credit);
            if (restoredFood > 0) {
                food.setFoodLevel(Math.min(20, currentFood + restoredFood));
                credit -= restoredFood;
                currentFood = food.getFoodLevel();
            }
        }
        if (reduction > 0.0D && currentSaturation + 0.0001D < previousSaturation) {
            double restoredSaturation = (previousSaturation - currentSaturation) * reduction;
            food.setSaturation((float) Math.min(currentFood, currentSaturation + restoredSaturation));
            currentSaturation = food.getSaturationLevel();
        }

        data.putDouble(FOOD_RESTORE_CREDIT, Math.min(0.9999D, credit));
        rememberFoodState(data, currentFood, currentSaturation);
    }

    private static void rememberFoodState(CompoundTag data, int foodLevel, float saturation) {
        data.putInt(LAST_FOOD_LEVEL, foodLevel);
        data.putDouble(LAST_SATURATION, saturation);
    }
}
