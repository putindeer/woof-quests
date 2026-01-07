package me.putindeer.woofquests.listeners;

import me.putindeer.woofquests.core.QuestManager;
import me.putindeer.woofquests.core.QuestRequirement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class Day4Listener implements Listener {
    private final QuestManager questManager;

    public Day4Listener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;

        switch (event.getEntityType()) {
            case EVOKER -> questManager.addProgress(player.getUniqueId(), QuestRequirement.EVOKERS);
            case VINDICATOR -> questManager.addProgress(player.getUniqueId(), QuestRequirement.VINDICATORS);
            case PILLAGER -> questManager.addProgress(player.getUniqueId(), QuestRequirement.RAVAGERS);
        }
    }
}
