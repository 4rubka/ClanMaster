package com.clanmaster.data;

import com.clanmaster.model.Clan;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstraction for persistence layer.
 */
public interface ClanStorage {

    /**
     * Loads all clans into memory.
     *
     * @return map by clan name
     */
    Map<String, Clan> loadAll();

    /**
     * Persists all clans to the backend.
     *
     * @param clans map to persist
     */
    void saveAll(Map<String, Clan> clans);

    /**
     * Persists a single clan.
     */
    void saveClan(Clan clan);

    /**
     * Deletes clan by name.
     */
    void deleteClan(String name);

    /**
     * Resolves clan by player id.
     */
    Optional<Clan> findByPlayer(UUID playerId, Map<String, Clan> clans);
}
