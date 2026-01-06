package me.putindeer.woofquests.listeners;

import io.papermc.paper.event.block.VaultChangeStateEvent;
import me.putindeer.woofquests.core.QuestManager;
import me.putindeer.woofquests.core.QuestRequirement;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
}

