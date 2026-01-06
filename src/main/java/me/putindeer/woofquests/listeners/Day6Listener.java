package me.putindeer.woofquests.listeners;

import me.putindeer.woofquests.core.QuestManager;
import me.putindeer.woofquests.core.QuestRequirement;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.*;

public class Day6Listener implements Listener {

    private final QuestManager questManager;
    private final Map<UUID, Set<UUID>> witherDamagers = new HashMap<>();

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
    public void onWitherDeath(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.WITHER) return;

        UUID witherId = event.getEntity().getUniqueId();
        Set<UUID> damagers = witherDamagers.remove(witherId);

        if (damagers == null) return;

        damagers.forEach(uuid -> questManager.addProgress(uuid, QuestRequirement.WITHERS));
    }
}

