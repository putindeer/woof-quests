package me.putindeer.woofquests.listeners;

import me.putindeer.woofquests.core.QuestManager;
import me.putindeer.woofquests.core.QuestRequirement;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Day6Listener implements Listener {
    private final QuestManager questManager;
    private final Map<UUID, Set<UUID>> witherDamagers = new HashMap<>();
    private final Map<UUID, Set<UUID>> elderGuardianDamagers = new HashMap<>();

    public Day6Listener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onWitherDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Wither wither)) return;
        if (!(event.getDamager() instanceof Player player)) return;

        witherDamagers.computeIfAbsent(wither.getUniqueId(), k -> new HashSet<>()).add(player.getUniqueId());
    }

    @EventHandler
    public void onElderGuardianDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ElderGuardian elderGuardian)) return;
        if (!(event.getDamager() instanceof Player player)) return;

        elderGuardianDamagers.computeIfAbsent(elderGuardian.getUniqueId(), k -> new HashSet<>()).add(player.getUniqueId());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        switch (event.getEntityType()) {
            case GUARDIAN -> {
                if (!(event.getEntity().getKiller() instanceof Player player)) return;
                questManager.addProgress(player.getUniqueId(), QuestRequirement.GUARDIANS);
            }
            case WITHER -> {
                UUID witherId = event.getEntity().getUniqueId();
                Set<UUID> damagers = witherDamagers.remove(witherId);

                if (damagers == null) return;

                damagers.forEach(uuid -> questManager.addProgress(uuid, QuestRequirement.WITHERS));
            }
            case ELDER_GUARDIAN -> {
                UUID elderGuardianId = event.getEntity().getUniqueId();
                Set<UUID> damagers = elderGuardianDamagers.remove(elderGuardianId);

                if (damagers == null) return;

                damagers.forEach(uuid -> questManager.addProgress(uuid, QuestRequirement.ELDER_GUARDIANS));
            }
        }
    }

    @EventHandler
    public void onWitherExplode(EntityExplodeEvent event) {
        EntityType type = event.getEntityType();
        if (type != EntityType.WITHER && type != EntityType.WITHER_SKULL) return;

        Location center = event.getLocation();

        World world = center.getWorld();
        if (world.getEnvironment() != World.Environment.NETHER) return;

        if (center.getY() <= 90) return;

        int radius = 2;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = world.getBlockAt(
                            center.getBlockX() + x,
                            center.getBlockY() + y,
                            center.getBlockZ() + z
                    );

                    if (block.getType() != Material.BEDROCK) continue;

                    block.setType(Material.AIR, false);
                }
            }
        }
    }

    private final Set<PotionEffectType> BLOCKED_EFFECTS = Set.of(PotionEffectType.INVISIBILITY, PotionEffectType.RESISTANCE);

    @EventHandler
    public void onPotionGain(EntityPotionEffectEvent event) {
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED) return;
        if (event.getNewEffect() == null) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!questManager.isPlayerAlive(player.getUniqueId())) return;

        if (BLOCKED_EFFECTS.contains(event.getNewEffect().getType())) {
            event.setCancelled(true);
        }
    }
}

