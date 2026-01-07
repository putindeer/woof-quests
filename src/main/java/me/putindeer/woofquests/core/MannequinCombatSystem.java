package me.putindeer.woofquests.core;

import io.papermc.paper.datacomponent.item.ResolvableProfile;
import me.putindeer.woofquests.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class MannequinCombatSystem implements Listener {
    private final Main plugin;
    public final Map<UUID, MannequinData> mannequinDataMap;

    public MannequinCombatSystem(Main plugin) {
        this.plugin = plugin;
        this.mannequinDataMap = new HashMap<>();
    }

    public void createMannequin(Player player) {
        Location location = player.getLocation();

        Mannequin mannequin = location.getWorld().spawn(location, Mannequin.class, CreatureSpawnEvent.SpawnReason.COMMAND);

        mannequin.setProfile(ResolvableProfile.resolvableProfile(player.getPlayerProfile()));
        mannequin.setCustomNameVisible(true);
        mannequin.setAbsorptionAmount(player.getAbsorptionAmount());
        mannequin.setMainHand(player.getMainHand());
        mannequin.setCanPickupItems(false);
        mannequin.setHealth(player.getHealth());
        mannequin.customName(plugin.utils.chat("<red>" + player.getName()));
        mannequin.setDescription(plugin.utils.chat("<gray>[Desconectado]"));

        List<ItemStack> savedItems = new ArrayList<>(Arrays.stream(player.getInventory().getContents().clone()).toList());

        MannequinData data = new MannequinData(player.getUniqueId(), mannequin, savedItems);
        mannequinDataMap.put(player.getUniqueId(), data);

        BukkitTask despawnTask = Bukkit.getScheduler().runTaskLater(plugin, () -> removeMannequin(mannequin), 20 * 30);

        data.setDespawnTask(despawnTask);
    }

    public UUID getPlayerUUIDFromMannequin(Mannequin mannequin) {
        for (Map.Entry<UUID, MannequinData> entry : mannequinDataMap.entrySet()) {
            if (entry.getValue().getMannequin().equals(mannequin)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void dropItems(List<ItemStack> items, Location location) {
        items.forEach(item -> {
            if (item != null) {
                location.getWorld().dropItemNaturally(location, item);
            }
        });
    }

    public void removeMannequin(UUID playerUUID) {
        if (!mannequinDataMap.containsKey(playerUUID)) {
            return;
        }

        MannequinData data = mannequinDataMap.get(playerUUID);

        if (data.getDespawnTask() != null) {
            data.getDespawnTask().cancel();
        }

        Mannequin mannequin = data.getMannequin();
        if (mannequin != null) {
            data.getMannequin().remove();
        }
        mannequinDataMap.remove(playerUUID);
    }

    public void removeMannequin(Mannequin mannequin) {
        UUID uuid = getPlayerUUIDFromMannequin(mannequin);
        mannequin.remove();
        if (uuid != null) {
            removeMannequin(uuid);
        }
    }

    public boolean hasMannequin(UUID playerUUID) {
        return mannequinDataMap.containsKey(playerUUID);
    }

    public MannequinData getMannequinData(UUID playerUUID) {
        return mannequinDataMap.get(playerUUID);
    }

    public void cleanupAll() {
        new HashSet<>(mannequinDataMap.keySet()).forEach(this::removeMannequin);
        Bukkit.getWorlds().forEach(world -> world.getEntitiesByClass(Mannequin.class).forEach(Entity::remove));
    }
}