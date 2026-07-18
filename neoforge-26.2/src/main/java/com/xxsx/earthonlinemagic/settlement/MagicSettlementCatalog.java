package com.xxsx.earthonlinemagic.settlement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xxsx.earthonlinemagic.EarthOnlineMagic;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MagicSettlementCatalog implements ResourceManagerReloadListener {
    private static final FileToIdConverter LISTER = FileToIdConverter.json("settlement");
    private static volatile Map<Identifier, SettlementDefinition> definitions = Map.of();

    public static void register(AddServerReloadListenersEvent event) {
        event.addListener(EarthOnlineMagic.id("settlement_catalog"), new MagicSettlementCatalog());
    }

    public static Map<Identifier, SettlementDefinition> definitions() {
        return definitions;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Map<Identifier, SettlementDefinition> loaded = new LinkedHashMap<>();
        for (Map.Entry<Identifier, Resource> entry
                : LISTER.listMatchingResourcesFromNamespace(resourceManager, EarthOnlineMagic.MODID).entrySet()) {
            Identifier id = LISTER.fileToId(entry.getKey());
            try (Reader reader = entry.getValue().openAsReader()) {
                SettlementDefinition definition = parse(id, JsonParser.parseReader(reader).getAsJsonObject());
                loaded.put(id, definition);
            } catch (Exception exception) {
                EarthOnlineMagic.LOGGER.error("Invalid magic settlement definition {}", id, exception);
            }
        }
        definitions = Map.copyOf(loaded);
        EarthOnlineMagic.LOGGER.info("Loaded {} magic settlement definitions", definitions.size());
    }

    private static SettlementDefinition parse(Identifier id, JsonObject json) {
        String displayName = requireString(json, "display_name");
        String culture = requireString(json, "culture");
        String generationStatus = requireString(json, "generation_status");
        int weight = positiveInt(json, "weight");
        int minDistanceChunks = positiveInt(json, "min_distance_chunks");
        List<String> biomes = requireList(json, "biomes");
        List<String> buildings = requireList(json, "buildings");
        List<String> residents = requireList(json, "residents");
        List<String> outputs = requireList(json, "outputs");
        if (!generationStatus.equals("catalog_only") && !generationStatus.equals("prototype")
                && !generationStatus.equals("enabled")) {
            throw new IllegalArgumentException("generation_status must be catalog_only, prototype, or enabled");
        }
        return new SettlementDefinition(id, displayName, culture, generationStatus, weight,
                minDistanceChunks, biomes, buildings, residents, outputs);
    }

    private static String requireString(JsonObject json, String key) {
        if (!json.has(key) || !json.get(key).isJsonPrimitive()) {
            throw new IllegalArgumentException("Missing string field: " + key);
        }
        String value = json.get(key).getAsString().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Empty string field: " + key);
        }
        return value;
    }

    private static int positiveInt(JsonObject json, String key) {
        if (!json.has(key)) {
            throw new IllegalArgumentException("Missing integer field: " + key);
        }
        int value = json.get(key).getAsInt();
        if (value < 1) {
            throw new IllegalArgumentException(key + " must be positive");
        }
        return value;
    }

    private static List<String> requireList(JsonObject json, String key) {
        if (!json.has(key) || !json.get(key).isJsonArray()) {
            throw new IllegalArgumentException("Missing array field: " + key);
        }
        JsonArray array = json.getAsJsonArray(key);
        List<String> values = new ArrayList<>(array.size());
        array.forEach(element -> {
            String value = element.getAsString().trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        });
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Empty array field: " + key);
        }
        return List.copyOf(values);
    }

    public record SettlementDefinition(
            Identifier id,
            String displayName,
            String culture,
            String generationStatus,
            int weight,
            int minDistanceChunks,
            List<String> biomes,
            List<String> buildings,
            List<String> residents,
            List<String> outputs) {
    }
}
