package com.clanmaster.data.sql;

import com.clanmaster.data.ClanStorage;
import com.clanmaster.model.Clan;
import com.clanmaster.model.ClanMember;
import com.clanmaster.model.ClanRank;
import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * SQL storage that works for both SQLite and MySQL depending on JDBC URL in config.
 */
public class SqlClanStorage implements ClanStorage {

    private final Logger logger;
    private final HikariDataSource dataSource;
    private final Gson gson = new Gson();

    /**
     * Creates SQL storage using parameters from config.
     */
    public SqlClanStorage(FileConfiguration config, Logger logger) {
        this.logger = logger;
        HikariConfig hikari = new HikariConfig();
        String type = config.getString("storage.type", "SQLITE").toUpperCase();
        if ("MYSQL".equals(type)) {
            hikari.setJdbcUrl(config.getString("storage.mysql.url"));
            hikari.setUsername(config.getString("storage.mysql.user"));
            hikari.setPassword(config.getString("storage.mysql.password"));
        } else {
            String path = config.getString("storage.sqlite.file", "clans.db");
            hikari.setJdbcUrl("jdbc:sqlite:" + path);
        }
        hikari.setMaximumPoolSize(5);
        hikari.setPoolName("ClanMasterPool");
        this.dataSource = new HikariDataSource(hikari);
        createTables();
    }

    @Override
    public Map<String, Clan> loadAll() {
        Map<String, Clan> clans = new HashMap<>();
        String sql = "SELECT name, level, xp, coins FROM clans";
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Clan clan = new Clan(rs.getString("name"));
                clan.setLevel(rs.getInt("level"));
                clan.setXp(rs.getDouble("xp"));
                clan.setCoins(rs.getDouble("coins"));
                clans.put(clan.getName(), clan);
            }
        } catch (Exception ex) {
            logger.warning("Failed to load clans: " + ex.getMessage());
            return Collections.emptyMap();
        }
        loadMembers(clans);
        return clans;
    }

    private void loadMembers(Map<String, Clan> clans) {
        String sql = "SELECT clan_name, uuid, rank FROM clan_members";
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String clanName = rs.getString("clan_name");
                Clan clan = clans.get(clanName);
                if (clan == null) {
                    continue;
                }
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                ClanRank rank = ClanRank.valueOf(rs.getString("rank"));
                clan.getMembers().put(uuid, new ClanMember(uuid, rank));
            }
        } catch (Exception ex) {
            logger.warning("Failed to load clan members: " + ex.getMessage());
        }
    }

    @Override
    public void saveAll(Map<String, Clan> clans) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM clans");
                st.executeUpdate("DELETE FROM clan_members");
            }
            try (PreparedStatement psClan = conn.prepareStatement("INSERT INTO clans(name, level, xp, coins) VALUES(?,?,?,?)");
                 PreparedStatement psMember = conn.prepareStatement("INSERT INTO clan_members(clan_name, uuid, rank) VALUES(?,?,?)")) {
                for (Clan clan : clans.values()) {
                    psClan.setString(1, clan.getName());
                    psClan.setInt(2, clan.getLevel());
                    psClan.setDouble(3, clan.getXp());
                    psClan.setDouble(4, clan.getCoins());
                    psClan.addBatch();
                    for (ClanMember member : clan.getMembers().values()) {
                        psMember.setString(1, clan.getName());
                        psMember.setString(2, member.getUuid().toString());
                        psMember.setString(3, member.getRank().name());
                        psMember.addBatch();
                    }
                }
                psClan.executeBatch();
                psMember.executeBatch();
            }
            conn.commit();
        } catch (Exception ex) {
            logger.warning("Failed to save clans: " + ex.getMessage());
        }
    }

    @Override
    public void saveClan(Clan clan) {
        Map<String, Clan> map = new HashMap<>();
        map.put(clan.getName(), clan);
        saveAll(map);
    }

    @Override
    public void deleteClan(String name) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM clans WHERE name=?")) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (Exception ex) {
            logger.warning("Failed to delete clan: " + ex.getMessage());
        }
    }

    @Override
    public Optional<Clan> findByPlayer(UUID playerId, Map<String, Clan> clans) {
        return clans.values().stream()
                .filter(clan -> clan.getMembers().containsKey(playerId))
                .findFirst();
    }

    private void createTables() {
        String clansTable = "CREATE TABLE IF NOT EXISTS clans (name VARCHAR(32) PRIMARY KEY, level INT, xp DOUBLE, coins DOUBLE)";
        String membersTable = "CREATE TABLE IF NOT EXISTS clan_members (clan_name VARCHAR(32), uuid VARCHAR(36), rank VARCHAR(16), PRIMARY KEY(clan_name, uuid))";
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate(clansTable);
            st.executeUpdate(membersTable);
        } catch (Exception ex) {
            logger.warning("Failed to create tables: " + ex.getMessage());
        }
    }
}
