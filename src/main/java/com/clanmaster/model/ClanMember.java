package com.clanmaster.model;

import java.util.UUID;

/**
 * Represents a member inside a clan with their rank.
 */
public class ClanMember {

    private final UUID uuid;
    private ClanRank rank;

    /**
     * Creates a clan member with rank.
     *
     * @param uuid member unique id
     * @param rank member rank
     */
    public ClanMember(UUID uuid, ClanRank rank) {
        this.uuid = uuid;
        this.rank = rank;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ClanRank getRank() {
        return rank;
    }

    public void setRank(ClanRank rank) {
        this.rank = rank;
    }
}
