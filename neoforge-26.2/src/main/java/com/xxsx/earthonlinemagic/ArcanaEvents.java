package com.xxsx.earthonlinemagic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class ArcanaEvents {
    private static final String ARCANA_PREFIX = "earth_online_arcana.";

    private ArcanaEvents() {
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag source = event.getOriginal().getPersistentData();
        CompoundTag target = event.getEntity().getPersistentData();
        for (String key : source.keySet()) {
            if (!key.startsWith(ARCANA_PREFIX)) {
                continue;
            }
            Tag value = source.get(key);
            if (value != null) {
                target.put(key, value.copy());
            }
        }
    }
}
