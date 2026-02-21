package com.clanmaster.data.json;

import com.clanmaster.data.ClanStorage;
import com.clanmaster.model.Clan;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * JSON file based storage using Gson.
 */
public class JsonClanStorage implements ClanStorage {

    private final File file;
    private final Logger logger;
    private final Gson gson;
    private final Type mapType = new TypeToken<Map<String, Clan>>() { }.getType();

    /**
     * Creates the storage pointing to a file.
     */
    public JsonClanStorage(File file, Logger logger) {
        this.file = file;
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public Map<String, Clan> loadAll() {
        if (!file.exists()) {
            return Collections.emptyMap();
        }
        try (FileReader reader = new FileReader(file)) {
            Map<String, Clan> data = gson.fromJson(reader, mapType);
            return data == null ? Collections.emptyMap() : data;
        } catch (Exception ex) {
            logger.warning("Failed to load clans.json: " + ex.getMessage());
            return Collections.emptyMap();
        }
    }

    @Override
    public void saveAll(Map<String, Clan> clans) {
        try {
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(clans, mapType, writer);
            }
        } catch (Exception ex) {
            logger.warning("Failed to save clans.json: " + ex.getMessage());
        }
    }

    @Override
    public void saveClan(Clan clan) {
        // Save all to keep it simple and consistent.
    }

    @Override
    public void deleteClan(String name) {
        // handled by saveAll on next flush
    }

    @Override
    public Optional<Clan> findByPlayer(UUID playerId, Map<String, Clan> clans) {
        return clans.values().stream()
                .filter(clan -> clan.getMembers().containsKey(playerId))
                .findFirst();
    }
}
