package me.putindeer.woofquests.listeners;

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import io.papermc.paper.registry.keys.SoundEventKeys;
import me.putindeer.woofquests.Main;
import me.putindeer.woofquests.core.QuestManager;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class GeneralListener implements Listener {

    private final Main plugin;
    private final QuestManager questManager;

    public GeneralListener(Main plugin) {
        this.plugin = plugin;
        this.questManager = plugin.questManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!questManager.isEventStarted()) return;
        if (questManager.getCurrentDay() == 1) return;

        if (!questManager.isPlayerAlive(player.getUniqueId()) && player.getGameMode() != GameMode.SPECTATOR) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 9));
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 9));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!questManager.isEventStarted()) return;

        if (!questManager.isPlayerAlive(player.getUniqueId())) return;

        for (Map.Entry<EquipmentSlot, EntityEquipmentChangedEvent.EquipmentChange> entry : event.getEquipmentChanges().entrySet()) {
            EquipmentSlot slot = entry.getKey();
            if (!slot.isArmor()) continue;
            ItemStack newItem = entry.getValue().newItem();
            if (newItem.getType() == Material.AIR) continue;

            Material type = newItem.getType();
            if (isAllowedArmor(type)) continue;

            player.getInventory().setItem(slot, new ItemStack(Material.AIR));
            plugin.utils.message(player, "<red>¡No puedes usar armadura! Solo calabazas, elytras y cabezas de jugador.");
            break;
        }
    }

    private boolean isAllowedArmor(Material type) {
        return type == Material.CARVED_PUMPKIN
                || type == Material.ELYTRA
                || type == Material.PLAYER_HEAD;
    }

    @EventHandler
    public void onTotem(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        if (!questManager.isEventStarted()) return;

        ItemStack head = plugin.utils.ib(Material.PLAYER_HEAD).profile(player).build();
        event.getDrops().add(head);

        if (questManager.isPlayerAlive(player.getUniqueId()) || questManager.isPlayerWaitingDeath(player.getUniqueId())) {
            questManager.killPlayer(player.getUniqueId());

            int allTimePlayers = questManager.getAlivePlayers().size() + questManager.getDeadPlayers().size();

            plugin.utils.broadcast(Sound.sound(SoundEventKeys.ENTITY_IRON_GOLEM_DEATH, Sound.Source.PLAYER, 10, 0.1f),
                    "<red><bold>☠ " + player.getName() + " ha muerto!");
            Bukkit.getOnlinePlayers().forEach(soundPlayer -> soundPlayer.playSound(Sound.sound(SoundEventKeys.ENTITY_BREEZE_DEATH, Sound.Source.PLAYER, 10, 0.1f)));
            if (questManager.getCurrentDay() != 1) {
                plugin.utils.broadcast("<gray>Jugadores restantes: <yellow>" + questManager.getAlivePlayers().size() + "/" + allTimePlayers);
            }

            player.setWhitelisted(false);
            plugin.utils.delay(400, () -> player.kick(plugin.utils.chat("<green>¡Gracias por jugar!")));
        }
    }

    @EventHandler
    public void onPlayerMilk(EntityPotionEffectEvent event) {
        if (!event.getCause().equals(EntityPotionEffectEvent.Cause.MILK)) return;
        if (event.getEntity() instanceof Player player) {
            if (player.getActivePotionEffects().stream().anyMatch(effect -> effect.getAmplifier() == 9)) {
                event.setCancelled(true);
            }
        }
    }
}