package me.putindeer.woofquests.listeners;

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import io.papermc.paper.registry.keys.SoundEventKeys;
import me.putindeer.woofquests.Main;
import me.putindeer.woofquests.core.MannequinData;
import me.putindeer.woofquests.core.QuestManager;
import net.kyori.adventure.sound.Sound;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.*;

public class GeneralListener implements Listener {

    private final Main plugin;
    private final QuestManager questManager;

    public GeneralListener(Main plugin) {
        this.plugin = plugin;
        this.questManager = plugin.questManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startUpdateTask();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getOnlinePlayers().forEach(this::updateTabList);
        Player player = event.getPlayer();

        MannequinData data = plugin.mannequin.getMannequinData(player.getUniqueId());
        if (data != null && !data.getMannequin().isDead()) {
            Mannequin mannequin = data.getMannequin();
            player.setHealth(mannequin.getHealth());
            player.setAbsorptionAmount(mannequin.getAbsorptionAmount());
            player.teleport(mannequin.getLocation());
            plugin.mannequin.removeMannequin(player.getUniqueId());
        }

        if (!questManager.isEventStarted()) return;
        if (questManager.isPlayerWaitingDeath(player.getUniqueId())) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 9));
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 9));
            return;
        }

        if (!questManager.isPlayerAlive(player.getUniqueId())) {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    private final Map<UUID, Integer> recentlyDisconnected = new HashMap<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getOnlinePlayers().forEach(this::updateTabList);
        Player player = event.getPlayer();

        if (!questManager.isEventStarted()) return;
        if (!questManager.isPlayerAlive(player.getUniqueId())) return;
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        recentlyDisconnected.put(player.getUniqueId(), Bukkit.getCurrentTick());
        plugin.mannequin.createMannequin(player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> recentlyDisconnected.remove(player.getUniqueId()), 5L);
    }

    @EventHandler
    public void onMannequin(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Mannequin)) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (Boolean.TRUE.equals(player.getWorld().getGameRuleValue(GameRules.PVP))) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onUntarget(EntityTargetEvent event) {
        if (!questManager.isEventStarted()) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (event.getReason() != EntityTargetEvent.TargetReason.FORGOT_TARGET) return;

        long currentTick = Bukkit.getCurrentTick();

        UUID uuid = null;
        for (Map.Entry<UUID, Integer> entry : recentlyDisconnected.entrySet()) {
            if (currentTick - entry.getValue() <= 3) {
                uuid = entry.getKey();
                break;
            }
        }

        if (uuid == null) return;


        MannequinData data = plugin.mannequin.getMannequinData(uuid);
        if (data == null || data.getMannequin().isDead()) return;

        if (entity.getLocation().distance(data.getMannequin().getLocation()) < 20) {
            event.setTarget(data.getMannequin());
        }
    }

    private void updateTabList(Player player) {
        String tps = new DecimalFormat("##").format(plugin.getServer().getTPS()[0]);

        String completedDay = plugin.questManager.hasCompletedCurrentDayMission(player.getUniqueId()) ? "<green>✓ Misiones del día completadas"
                : plugin.questManager.getDayProgressStringDetailed(player);
        String header = String.format(
                """
                <gradient:#ffffff:#ffff00><st>                                                      </st></gradient>
                <yellow><bold>El Pueblo</bold></yellow>
                %s
                <gray>Jugadores conectados: <white>%d</white>""", completedDay, Bukkit.getOnlinePlayers().size());

        String footer = String.format(
                        """
                        <gray>Ping: <white>%d <dark_gray>| <gray>TPS: <white>%s
 
                        <gray>Servidor patrocinado por <yellow><bold>HolyHosting</bold>
                        <white>Adquiere un servidor en <yellow>holy.gg
                        <gradient:#ffffff:#ffff00><st>                                                      </st></gradient>""",
                player.getPing(), tps);

        player.sendPlayerListHeaderAndFooter(plugin.utils.chat(header), plugin.utils.chat(footer));
    }

    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(GeneralListener.this::updateTabList);
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L);
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
                || type == Material.PLAYER_HEAD
                || type == Material.SKELETON_SKULL
                || type == Material.WITHER_SKELETON_SKULL
                || type == Material.ZOMBIE_HEAD
                || type == Material.CREEPER_HEAD
                || type == Material.DRAGON_HEAD
                || type == Material.PIGLIN_HEAD;
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
                    "<red>☠ <bold>" + player.getName() + " ha muerto!");
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

    @EventHandler
    public void onMannequinDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Mannequin mannequin)) return;

        UUID uuid = plugin.mannequin.getPlayerUUIDFromMannequin(mannequin);
        if (uuid == null) return;
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        MannequinData data = plugin.mannequin.mannequinDataMap.get(uuid);
        List<ItemStack> savedItems = data.getSavedItems();
        savedItems.add(plugin.utils.ib(Material.PLAYER_HEAD).profile(player).build());
        plugin.mannequin.dropItems(data.getSavedItems(), mannequin.getLocation());
        data.despawnTask.cancel();

        if (questManager.isPlayerAlive(uuid) || questManager.isPlayerWaitingDeath(uuid)) {
            questManager.killPlayer(uuid);

            int allTimePlayers = questManager.getAlivePlayers().size() + questManager.getDeadPlayers().size();

            plugin.utils.broadcast(Sound.sound(SoundEventKeys.ENTITY_IRON_GOLEM_DEATH, Sound.Source.PLAYER, 10, 0.1f),
                    "<red>☠ <bold>" + player.getName() + " ha muerto! </bold><gray>[Desconectado]");
            Bukkit.getOnlinePlayers().forEach(soundPlayer -> soundPlayer.playSound(Sound.sound(SoundEventKeys.ENTITY_BREEZE_DEATH, Sound.Source.PLAYER, 10, 0.1f)));
            if (questManager.getCurrentDay() != 1) {
                plugin.utils.broadcast("<gray>Jugadores restantes: <yellow>" + questManager.getAlivePlayers().size() + "/" + allTimePlayers);
            }
        }
    }
}