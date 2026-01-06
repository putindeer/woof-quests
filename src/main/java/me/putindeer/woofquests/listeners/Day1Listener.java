package me.putindeer.woofquests.listeners;

import me.putindeer.woofquests.Main;
import me.putindeer.woofquests.core.QuestManager;
import me.putindeer.woofquests.core.QuestRequirement;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class Day1Listener implements Listener {
    private final QuestManager questManager;
    private final Main plugin;

    public Day1Listener(QuestManager questManager, Main plugin) {
        this.questManager = questManager;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (questManager.isEventStarted() && questManager.isPlayerNotAliveButNotDead(player.getUniqueId())) {
            questManager.addAlivePlayer(player);
            plugin.utils.delay(20, () -> questManager.teleportToRandomBorder(player));
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!questManager.isPlayerAlive(player.getUniqueId())) {
            return;
        }

        QuestRequirement requirement = getQuestRequirement(event);

        if (requirement != null) {
            questManager.addProgress(player.getUniqueId(), requirement);
        }
    }

    private @Nullable QuestRequirement getQuestRequirement(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        Material type = result.getType();

        return switch (type) {
            case DIAMOND_SWORD -> QuestRequirement.DIAMOND_SWORD;
            case DIAMOND_AXE -> QuestRequirement.DIAMOND_AXE;
            case DIAMOND_PICKAXE -> QuestRequirement.DIAMOND_PICKAXE;
            case DIAMOND_SHOVEL -> QuestRequirement.DIAMOND_SHOVEL;
            case DIAMOND_SPEAR -> QuestRequirement.DIAMOND_SPEAR;
            default -> null;
        };
    }
}