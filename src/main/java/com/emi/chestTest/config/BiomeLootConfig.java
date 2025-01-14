package com.emi.chestTest.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BiomeLootConfig {
    private static final Map<String, List<String>> BIOME_TO_LOOT_TABLE = new HashMap<>();
    private static final Path configPath = Path.of("config/chestloot.json");

    public static void loadConfig() {
        try (FileReader reader = new FileReader(configPath.toFile())) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            BIOME_TO_LOOT_TABLE.clear();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                List<String> lootTables = new ArrayList<>();
                entry.getValue().getAsJsonArray().forEach(element -> lootTables.add(element.getAsString()));
                BIOME_TO_LOOT_TABLE.put(entry.getKey(), lootTables);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, List<String>> getBiomeToLootTable() {
        return BIOME_TO_LOOT_TABLE;
    }
}