package me.putindeer.woofquests.listeners;

import me.putindeer.woofquests.core.QuestManager;
import me.putindeer.woofquests.core.QuestRequirement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

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
            case RAVAGER -> questManager.addProgress(player.getUniqueId(), QuestRequirement.RAVAGERS);
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
