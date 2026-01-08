package me.putindeer.woofquests.listeners;

import io.papermc.paper.event.block.VaultChangeStateEvent;
import me.putindeer.woofquests.core.QuestManager;
import me.putindeer.woofquests.core.QuestRequirement;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class Day5Listener implements Listener {

    private final QuestManager questManager;

    public Day5Listener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onVaultChangeState(VaultChangeStateEvent event) {
        if (event.getNewState() != Vault.State.UNLOCKING) return;

        Block block = event.getBlock();
        Player opener = event.getPlayer();

        if (opener == null) return;

        boolean ominous = block.getBlockData() instanceof Vault data && data.isOminous();

        if (ominous) {
            questManager.addProgress(opener.getUniqueId(), QuestRequirement.OMINOUS_VAULTS);
        } else {
            questManager.addProgress(opener.getUniqueId(), QuestRequirement.VAULTS);
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

