package com.clanmaster.model;

/**
 * Represents clan member ranks.
 */
public enum ClanRank {
    LEADER,
    OFFICER,
    MEMBER;

    /**
     * Returns whether this rank has management privileges over another rank.
     *
     * @param other other rank
     * @return true if can manage
     */
    public boolean canManage(ClanRank other) {
        if (this == LEADER) {
            return true;
        }
        return this == OFFICER && other == MEMBER;
    }
}
