package com.clanmaster.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/** Utility to serialize/deserialize locations to strings. */
public final class LocationUtil {
    private LocationUtil() {}

    public static String toString(Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch();
    }

    public static Location fromString(String s) {
        if (s == null || s.isEmpty()) return null;
        String[] p = s.split(":");
        if (p.length < 6) return null;
        World w = Bukkit.getWorld(p[0]);
        if (w == null) return null;
        return new Location(w, Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3]), Float.parseFloat(p[4]), Float.parseFloat(p[5]));
    }
}
