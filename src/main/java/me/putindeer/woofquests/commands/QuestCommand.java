package me.putindeer.woofquests.commands;

import me.putindeer.woofquests.Main;
import me.putindeer.woofquests.core.QuestManager;
import me.putindeer.woofquests.core.QuestRequirement;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class QuestCommand implements CommandExecutor {

    private final Main plugin;
    private final QuestManager questManager;

    public QuestCommand(Main plugin) {
        this.plugin = plugin;
        this.questManager = plugin.questManager;
        Objects.requireNonNull(plugin.getCommand("quest")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.utils.message(sender, "<red>Este comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        if (args.length == 0) {
            showProgress(player, player);
        } else {
            String name = args[0];
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            if (offlinePlayer.hasPlayedBefore()) {
                showProgress(player, offlinePlayer);
            }
        }

        return true;
    }

    private void showProgress(Player player, OfflinePlayer target) {
        if (!questManager.isEventStarted()) {
            plugin.utils.message(player, "<red>El evento aún no ha comenzado.");
            return;
        }

        if (!questManager.isPlayerAlive(target.getUniqueId())) {
            String message = player == target.getPlayer() ? "¡Estás muerto!" : "¡Este jugador está muerto!";
            plugin.utils.message(player, "<red>" + message + " No puedes ver el progreso.");
            return;
        }

        int currentDay = questManager.getCurrentDay();
        QuestRequirement[] requirements = QuestRequirement.getRequirementsForDay(currentDay);

        if (requirements.length == 0) {
            plugin.utils.message(player, "<red>No hay misiones para este día.");
            return;
        }

        plugin.utils.message(player, "<gold><bold>════════════════════════════",
                "<yellow><bold>DÍA " + currentDay + " - PROGRESO DE MISIONES");
        if (player != target.getPlayer()) {
            plugin.utils.message(player, "<gray>(de " + target.getName() + ")");
        }
        plugin.utils.message(player, "<gold><bold>════════════════════════════", "");

        for (QuestRequirement req : requirements) {
            int progress = questManager.getProgress(target.getUniqueId(), req);
            int required = req.getRequired();
            boolean completed = questManager.isCompleted(target.getUniqueId(), req);

            String status = completed ? "<green><bold>✓" : "<yellow>○";

            plugin.utils.message(player,
                    status + " <white>" + req.getDisplayName() +
                            " <gray>(" + progress + "/" + required + ")"
            );
        }

        plugin.utils.message(player, "");
        plugin.utils.message(player, "<gold><bold>════════════════════════════");
    }
}