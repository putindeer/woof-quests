package me.putindeer.woofquests.listeners;

import me.putindeer.woofquests.core.QuestManager;
import me.putindeer.woofquests.core.QuestRequirement;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class Day3Listener implements Listener {

    private final QuestManager questManager;

    public Day3Listener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onEnterEnd(PlayerPortalEvent event) {
        if (event.getTo().getWorld().getEnvironment() != World.Environment.THE_END) return;

        Player player = event.getPlayer();
        questManager.addProgress(player.getUniqueId(), QuestRequirement.ENTERED_END);
    }
}

