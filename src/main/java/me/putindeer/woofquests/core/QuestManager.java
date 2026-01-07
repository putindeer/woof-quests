package me.putindeer.woofquests.core;

import io.papermc.paper.registry.keys.SoundEventKeys;
import lombok.Getter;
import me.putindeer.woofquests.Main;
import me.putindeer.woofquests.listeners.*;
import net.kyori.adventure.sound.Sound;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuestManager {
    private final Main plugin;
    private final File playerDataFolder;

    @Getter private int currentDay;
    @Getter private boolean eventStarted;
    @Getter private final Set<UUID> alivePlayers;
    @Getter private final Set<UUID> deadPlayers;
    @Getter private final Set<UUID> waitingDeathPlayers;
    private final Map<UUID, PlayerQuestData> questDataCache;
    private Listener currentDayListener;

    public QuestManager(Main plugin) {
        this.plugin = plugin;
        this.alivePlayers = new HashSet<>();
        this.deadPlayers = new HashSet<>();
        this.waitingDeathPlayers = new HashSet<>();
        this.questDataCache = new HashMap<>();

        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!playerDataFolder.exists()) {
            if (playerDataFolder.mkdirs()) {
                plugin.utils.log("Directorio de datos de jugadores creado correctamente.");
            } else {
                plugin.utils.severe("¡No se pudo crear el directorio de datos de jugadores!");
            }
        }

        loadData();
    }

    private void loadData() {
        FileConfiguration config = plugin.getConfig();

        this.currentDay = config.getInt("current-day", 1);
        this.eventStarted = config.getBoolean("event-started", false);

        List<String> aliveList = config.getStringList("alive-players");
        aliveList.forEach(uuidStr -> {
            UUID uuid = UUID.fromString(uuidStr);
            alivePlayers.add(uuid);
            loadPlayerData(uuid);
        });

        List<String> deadList = config.getStringList("dead-players");
        deadList.forEach(uuidStr -> deadPlayers.add(UUID.fromString(uuidStr)));
        List<String> waitingDeathList = config.getStringList("waiting-death-players");
        deadList.forEach(uuidStr -> waitingDeathPlayers.add(UUID.fromString(uuidStr)));
        loadDayListener(currentDay);
    }

    private PlayerQuestData loadPlayerData(UUID uuid) {
        if (questDataCache.containsKey(uuid)) {
            return questDataCache.get(uuid);
        }

        File playerFile = new File(playerDataFolder, uuid.toString() + ".yml");

        if (!playerFile.exists()) {
            PlayerQuestData newData = new PlayerQuestData(uuid);
            questDataCache.put(uuid, newData);
            return newData;
        }

        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        PlayerQuestData data = new PlayerQuestData(uuid);

        ConfigurationSection progressSection = playerConfig.getConfigurationSection("progress");
        if (progressSection != null) {
            progressSection.getKeys(false).forEach(key -> {
                QuestRequirement requirement = QuestRequirement.fromKey(key);
                if (requirement != null) {
                    int value = playerConfig.getInt("progress." + key);
                    data.getProgress().put(requirement, value);
                }
            });
        }

        questDataCache.put(uuid, data);
        return data;
    }

    private void savePlayerData(UUID uuid) {
        PlayerQuestData data = questDataCache.get(uuid);
        if (data == null) return;

        File playerFile = new File(playerDataFolder, uuid.toString() + ".yml");
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

        data.getProgress().forEach((requirement, value) -> playerConfig.set("progress." + requirement.getKey(), value));

        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            plugin.utils.severe("Error al guardar datos del jugador " + uuid + ": " + e.getMessage());
            plugin.utils.severe(e.getStackTrace());
        }
    }

    public void saveData() {
        FileConfiguration config = plugin.getConfig();

        config.set("current-day", currentDay);
        config.set("event-started", eventStarted);

        List<String> aliveList = new ArrayList<>();
        alivePlayers.forEach(uuid -> aliveList.add(uuid.toString()));
        config.set("alive-players", aliveList);

        List<String> deadList = new ArrayList<>();
        deadPlayers.forEach(uuid -> deadList.add(uuid.toString()));
        config.set("dead-players", deadList);

        List<String> waitingDeathList = new ArrayList<>();
        waitingDeathPlayers.forEach(uuid -> waitingDeathList.add(uuid.toString()));
        config.set("waiting-death-players", waitingDeathList);

        plugin.saveConfig();

        alivePlayers.forEach(this::savePlayerData);
    }

    public void startEvent() {
        if (eventStarted) {
            plugin.utils.warning("El evento ya está iniciado.");
            return;
        }

        eventStarted = true;
        currentDay = 1;

        plugin.utils.broadcast(Sound.sound(SoundEventKeys.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1, 2),
                "<green><bold>EVENTO INICIADO",
                "<yellow><bold>=== DÍA " + currentDay + " ===");
        QuestRequirement[] requirements = QuestRequirement.getRequirementsForDay(currentDay);
        for (QuestRequirement req : requirements) {
            plugin.utils.broadcast("○ <white>" + req.getDisplayName() + " <gray>(" + req.getRequired() + ")");
        }
        Bukkit.getOnlinePlayers().forEach(this::addAlivePlayer);

        loadDayListener(currentDay);
        saveData();

        int i = 1;
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.utils.delay(i * 20, () -> teleportToRandomBorder(player));
            i++;
        }
    }

    public void changeDay() {
        Set<UUID> failedPlayers = getPlayersWhoDidNotCompleteMission();

        alivePlayers.forEach(player -> Bukkit.getOfflinePlayer(player).setWhitelisted(true));

        failedPlayers.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 9));
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 9));
                plugin.utils.message(player, "<red><bold>¡No completaste la misión del Día " + currentDay + "!");
            } else {
                alivePlayers.remove(uuid);
                deadPlayers.add(uuid);
                waitingDeathPlayers.add(uuid);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                offlinePlayer.setWhitelisted(false);
                plugin.utils.log("Jugador " + offlinePlayer.getName() + " eliminado por no completar misión (offline).");
            }
        });

        alivePlayers.forEach(uuid -> {
            PlayerQuestData data = questDataCache.get(uuid);
            if (data != null) {
                data.clearProgress();
                savePlayerData(uuid);
            }
        });

        currentDay++;

        if (currentDayListener != null) {
            HandlerList.unregisterAll(currentDayListener);
        }

        loadDayListener(currentDay);
        saveData();

        plugin.utils.broadcast("<yellow><bold>=== DÍA " + currentDay + " ===");
        QuestRequirement[] requirements = QuestRequirement.getRequirementsForDay(currentDay);
        for (QuestRequirement req : requirements) {
            plugin.utils.broadcast("○ <white>" + req.getDisplayName() + " <gray>(" + req.getRequired() + ")");
        }
    }

    private void loadDayListener(int day) {
        if (currentDayListener != null) {
            HandlerList.unregisterAll(currentDayListener);
        }

        currentDayListener = switch (day) {
            case 1 -> new Day1Listener(this, plugin);
            case 2 -> new Day2Listener(this);
            case 3 -> new Day3Listener(this);
            case 4 -> new Day4Listener(this);
            case 5 -> new Day5Listener(this);
            case 6 -> new Day6Listener(this);
            default -> {
                plugin.utils.warning("No hay listener para el día " + day);
                yield null;
            }
        };

        if (currentDayListener != null) {
            Bukkit.getPluginManager().registerEvents(currentDayListener, plugin);
        }
    }

    public Set<UUID> getPlayersWhoDidNotCompleteMission() {
        Set<UUID> failed = new HashSet<>();

        alivePlayers.forEach(uuid -> {
            if (!hasCompletedCurrentDayMission(uuid)) {
                failed.add(uuid);
            }
        });

        return failed;
    }

    public boolean hasCompletedCurrentDayMission(UUID uuid) {
        PlayerQuestData data = questDataCache.get(uuid);
        if (data == null) {
            return false;
        }

        return data.isDayCompleted(currentDay);
    }

    public boolean isPlayerAlive(UUID uuid) {
        return alivePlayers.contains(uuid);
    }

    public boolean isPlayerNotAliveButNotDead(UUID uuid) {
        return !isPlayerAlive(uuid) && !deadPlayers.contains(uuid);
    }

    public boolean isPlayerWaitingDeath(UUID uuid) {
        return waitingDeathPlayers.contains(uuid);
    }

    public void teleportToRandomBorder(Player player) {
        World world = plugin.getServer().getRespawnWorld();
        int border = (int) world.getWorldBorder().getSize() / 2 - 2;

        Random random = new Random();
        int side = random.nextInt(4);

        int x = 0;
        int z = 0;

        switch (side) {
            case 0 -> {
                x = random(-border, border);
                z = -border;
            }
            case 1 -> {
                x = random(-border, border);
                z = border;
            }
            case 2 -> {
                x = -border;
                z = random(-border, border);
            }
            case 3 -> {
                x = border;
                z = random(-border, border);
            }
        }

        int y = world.getHighestBlockYAt(x, z) + 1;

        Location location = new Location(world, x + 0.5, y, z + 0.5);

        player.playSound(Sound.sound(SoundEventKeys.ENTITY_ENDERMAN_TELEPORT, Sound.Source.MASTER, 1, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 600, 0, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 0, false, false, true));
        player.getInventory().addItem(new ItemStack(Material.BIRCH_BOAT));
        player.teleportAsync(location);
    }

    private int random(int min, int max) {
        return new Random().nextInt(min, max + 1);
    }


    public void addAlivePlayer(OfflinePlayer player) {
        alivePlayers.add(player.getUniqueId());
        deadPlayers.remove(player.getUniqueId());
        loadPlayerData(player.getUniqueId());
    }

    public void killPlayer(UUID uuid) {
        alivePlayers.remove(uuid);
        deadPlayers.add(uuid);
        savePlayerData(uuid);
    }

    public void addProgress(UUID uuid, QuestRequirement requirement) {
        if (!isCompleted(uuid, requirement)) {
            int progress = getProgress(uuid, requirement);
            updateProgress(uuid, requirement, 1);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                checkDayCompletion(player, currentDay);
            }
        }
    }

    public void updateProgress(UUID uuid, QuestRequirement requirement, int amount) {
        PlayerQuestData data = loadPlayerData(uuid);
        data.updateProgress(requirement, amount);

        int newValue = data.getProgress(requirement);

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            String progressText = newValue + "/" + requirement.getRequired();
            plugin.utils.message(player, "<green>Progreso: <yellow>" + requirement.getDisplayName() + " <gray>(" + progressText + ")");

            if (data.isCompleted(requirement)) {
                plugin.utils.message(player, Sound.sound(SoundEventKeys.BLOCK_NOTE_BLOCK_PLING, Sound.Source.PLAYER, 1, 2),
                        "<green>Completaste: <gold>" + requirement.getDisplayName());
            }
        }

        savePlayerData(uuid);
    }

    public int getProgress(UUID uuid, QuestRequirement requirement) {
        PlayerQuestData data = questDataCache.get(uuid);
        if (data == null) {
            return 0;
        }
        return data.getProgress(requirement);
    }

    public boolean isCompleted(UUID uuid, QuestRequirement requirement) {
        PlayerQuestData data = questDataCache.get(uuid);
        if (data == null) {
            return false;
        }
        return data.isCompleted(requirement);
    }

    private void checkDayCompletion(Player player, int day) {
        QuestRequirement[] requirements = QuestRequirement.getRequirementsForDay(day);

        boolean allCompleted = true;
        for (QuestRequirement req : requirements) {
            if (!isCompleted(player.getUniqueId(), req)) {
                allCompleted = false;
                break;
            }
        }

        if (allCompleted) {
            plugin.utils.message(player, "<gold><bold>════════════════════════════",
                    "<green><bold>✓ ¡COMPLETASTE EL DÍA " + day + "!",
                    "<gold><bold>════════════════════════════");
        }
    }

    public String getDayProgressStringDetailed(Player player) {
        int currentDay = getCurrentDay();
        QuestRequirement[] requirements = QuestRequirement.getRequirementsForDay(currentDay);

        if (requirements.length == 0) return "<gray>Sin misiones para este día";

        int totalProgress = 0;
        int totalRequired = 0;
        int completed = 0;

        for (QuestRequirement req : requirements) {
            totalProgress += getProgress(player.getUniqueId(), req);
            totalRequired += req.getRequired();
            if (isCompleted(player.getUniqueId(), req)) completed++;
        }

        double percentage = totalRequired > 0 ? (double) totalProgress / totalRequired : 0.0;

        String color = getSmoothGradientColor(percentage);

        return color + "⏱ Misiones del día por completar: " + completed + "/" + requirements.length;
    }

    private String getSmoothGradientColor(double percentage) {
        int red = (int) (255 * (1 - percentage));
        int green = (int) (255 * percentage);
        int blue = 0;

        return String.format("<#%02X%02X%02X>", red, green, blue);
    }
}