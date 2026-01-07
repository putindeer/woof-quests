package me.putindeer.woofquests.core;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;

@Getter
public class MannequinData {
    public final UUID playerUUID;
    public final Mannequin mannequin;
    public final List<ItemStack> savedItems;
    @Setter
    public BukkitTask despawnTask;
    public Location initialLocation;

    public MannequinData(UUID playerUUID, Mannequin mannequin, List<ItemStack> savedItems, Location location) {
        this.playerUUID = playerUUID;
        this.mannequin = mannequin;
        this.savedItems = savedItems;
        this.initialLocation = location;
    }
}
