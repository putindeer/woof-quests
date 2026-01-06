package me.putindeer.woofquests.listeners;

import me.putindeer.woofquests.core.QuestManager;
import me.putindeer.woofquests.core.QuestRequirement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class Day2Listener implements Listener {
    private final QuestManager questManager;

    public Day2Listener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;

        switch (event.getEntityType()) {
            case WITHER_SKELETON -> questManager.addProgress(player.getUniqueId(), QuestRequirement.WITHER_SKELETONS);
            case BLAZE -> questManager.addProgress(player.getUniqueId(), QuestRequirement.BLAZES);
        }
    }
}

